package br.com.gabryel.maplewood.service.scheduler.algorithm;

import br.com.gabryel.maplewood.model.dto.ClassroomData;
import br.com.gabryel.maplewood.model.dto.CourseData;
import br.com.gabryel.maplewood.model.dto.StudentData;
import br.com.gabryel.maplewood.model.dto.TeacherData;
import br.com.gabryel.maplewood.service.scheduler.ScheduleCalculator.CourseDemand;
import br.com.gabryel.maplewood.service.scheduler.ScheduleCalculator.SchedulerCourseSection;
import br.com.gabryel.maplewood.service.scheduler.ScheduleCalculator.TimeSlot;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Comparator.comparing;
import static java.util.Map.entry;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

@Slf4j
public class CoreScheduler {

    private final SchedulingContext scheduleState;
    private final SlotCombinator combinator;

    public CoreScheduler(SchedulingContext scheduleState, SlotCombinator combinator) {
        this.scheduleState = scheduleState;
        this.combinator = combinator;
    }

    public void generateSchedule(List<CourseDemand> demands, boolean failOnSectionNotCreatable) {
        if (demands.isEmpty())
            return;

        var courseDemand = demands.getFirst();
        var course = courseDemand.course();

        var students = courseDemand.students().stream()
            .sorted(comparing(scheduleState::getRemainingStudentHours).reversed())
            .toList();

        var teachers = scheduleState.getTeachersFor(course);
        var classrooms = scheduleState.getClassroomsFor(course);

        scheduleSections(course, teachers, classrooms, students, failOnSectionNotCreatable);

        generateSchedule(demands.stream().skip(1).toList(), failOnSectionNotCreatable);
    }

    private void scheduleSections(
        CourseData course,
        List<TeacherData> teachers,
        List<ClassroomData> classrooms,
        List<StudentData> students,
        boolean failOnSectionNotCreatable
    ) {
        if (students.isEmpty()) return;

        var teacherClassrooms = teachers.stream().flatMap(teacher ->
            classrooms.stream().map(classroom -> entry(teacher, classroom))
        ).toList();

        var sectionNum = scheduleState.getNextSectionNum(course);

        var maybeSection = teacherClassrooms.stream()
            .map(entry -> getMatchingSchedule(course, sectionNum, entry.getKey(), entry.getValue(), students))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();

        if (maybeSection.isEmpty()) {
            if (failOnSectionNotCreatable) {
                throw new IllegalStateException("Couldn't generate Course #" + course.id() + " - Section #" + sectionNum + ". " + students.size() + " students remaining");
            }

            log.warn("Couldn't generate Course #{} - Section #{}. {} students remaining", course.id(), sectionNum, students.size());
            return;
        }

        var section = maybeSection.get();
        scheduleState.updateSchedules(section);

        var reSortedClassrooms = classrooms.stream()
            .sorted(comparing(scheduleState::getRemainingClassroomHours).reversed())
            .toList();

        var reSortedTeachers = teachers.stream()
            .sorted(comparing(scheduleState::getRemainingTeacherHours).reversed())
            .toList();

        var sectionStudentSet = new HashSet<>(section.students());
        var filteredStudents = students.stream()
            .filter(student -> !sectionStudentSet.contains(student))
            .toList();

        scheduleSections(course, reSortedTeachers, reSortedClassrooms, filteredStudents, failOnSectionNotCreatable);
    }

    private Optional<SchedulerCourseSection> getMatchingSchedule(
        CourseData course,
        int sectionNum,
        TeacherData teacher,
        ClassroomData classroom,
        List<StudentData> students
    ) {
        var minimumAccepted = max(1, min(classroom.capacity(), students.size()) - 2);  // Best effort for optional

        var matchingSlots = findAvailableSlots(teacher, classroom, students, minimumAccepted);

        return combinator.getKCombinations(matchingSlots, course.hoursPerWeek(), teacher.maxDailyHours())
            .map(Stream::toList)
            .filter(slots -> scheduleState.teacherCanTeachAt(teacher, slots))
            .map(slots -> generateSection(course, sectionNum, teacher, classroom, students, slots))
            .filter(section -> section.students().size() >= minimumAccepted)
            .limit(5)
            .max(comparing(section -> section.students().size()));
    }

    private List<TimeSlot> findAvailableSlots(TeacherData teacher, ClassroomData classroom, List<StudentData> students, int minimumAccepted) {
        var studentFreeTimes = students.stream().collect(toMap(
            StudentData::id,
            student -> scheduleState.getAvailableTimeSlots().stream()
                .filter(slot -> scheduleState.studentIsFreeAt(student, slot))
                .collect(toSet())));

        return scheduleState.getAvailableTimeSlots().stream()
            .filter(slot -> scheduleState.teacherIsFreeAt(teacher, slot))
            .filter(slot -> scheduleState.classroomIsFreeAt(classroom, slot))
            .filter(timeSlot -> hasEnoughStudents(timeSlot, studentFreeTimes, minimumAccepted))
            .toList();
    }

    private static boolean hasEnoughStudents(TimeSlot timeSlot, Map<Integer, Set<TimeSlot>> studentFreeTime, int minimumAccepted) {
        var availableStudents = studentFreeTime.values().stream()
            .filter(slots -> slots.contains(timeSlot))
            .count();
        return availableStudents >= minimumAccepted;
    }

    private SchedulerCourseSection generateSection(
        CourseData course,
        int sectionNum,
        TeacherData teacher,
        ClassroomData classroom,
        List<StudentData> students,
        List<TimeSlot> slots
    ) {
        var selectedStudents = students.stream()
            .filter(student -> scheduleState.studentIsFreeAt(student, slots))
            .limit(classroom.capacity())
            .toList();

        return new SchedulerCourseSection(course, sectionNum, slots, teacher, classroom, selectedStudents);
    }
}
