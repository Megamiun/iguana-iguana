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
    private final List<CourseDemand> courseDemands;
    private final SlotCombinator combinator;

    public CoreScheduler(
        SchedulingContext scheduleState,
        SlotCombinator combinator,
        List<CourseDemand> courseDemands
    ) {
        this.scheduleState = scheduleState;
        this.courseDemands = courseDemands;
        this.combinator = combinator;
    }

    public List<SchedulerCourseSection> generateSchedule(int courseDemandIndex) {
        if (courseDemandIndex >= courseDemands.size())
            return List.of();

        var courseDemand = courseDemands.get(courseDemandIndex);
        var course = courseDemand.course();

        var students = courseDemand.students().stream()
            .sorted(comparing(scheduleState::getRemainingStudentHours).reversed())
            .toList();

        var teachers = scheduleState.getTeachersFor(course);
        var classrooms = scheduleState.getClassroomsFor(course);
        var sections = scheduleSections(course, teachers, classrooms, students, 1);

        return Stream.concat(
            sections.stream(),
            generateSchedule(courseDemandIndex + 1).stream()
        ).toList();
    }

    private List<SchedulerCourseSection> scheduleSections(
        CourseData course,
        List<TeacherData> teachers,
        List<ClassroomData> classrooms,
        List<StudentData> students,
        int sectionNum
    ) {
        if (students.isEmpty())
            return List.of();

        var teacherClassrooms = teachers.stream().flatMap(teacher ->
            classrooms.stream().map(classroom -> entry(teacher, classroom))
        ).toList();

        var section = teacherClassrooms.stream()
            .map(entry -> getMatchingSchedule(course, entry.getKey(), entry.getValue(), students, sectionNum))
            .findFirst().orElseThrow(() -> new IllegalStateException("No arrangement found for course #" + course));

        scheduleState.updateSchedules(section);

        var newAvailableClassrooms = classrooms.stream()
            .sorted(comparing(scheduleState::getRemainingClassroomHours).reversed())
            .toList();

        var newAvailableTeachers = teachers.stream()
            .sorted(comparing(scheduleState::getRemainingTeacherHours).reversed())
            .toList();

        var sectionStudentSet = new HashSet<>(section.students());
        var newAvailableStudents = students.stream()
            .filter(student -> !sectionStudentSet.contains(student))
            .toList();

        return Stream.concat(
            Stream.of(section),
            scheduleSections(course, newAvailableTeachers, newAvailableClassrooms, newAvailableStudents, sectionNum + 1).stream()
        ).toList();
    }

    private SchedulerCourseSection getMatchingSchedule(CourseData course, TeacherData teacher, ClassroomData classroom, List<StudentData> students, int sectionNum) {
        var minimumAccepted = max(1, min(classroom.capacity(), students.size()) - 2);

        var matchingSlots = findAvailableSlots(teacher, classroom, students, minimumAccepted);

        return combinator.getKCombinations(matchingSlots, course.hoursPerWeek(), matchingSlots.size() - 1, 0, null, null, 0, 4)
            .map(Stream::toList)
            .filter(slots -> scheduleState.teacherCanTeachAt(teacher, slots))
            .map(slots -> generateSection(course, sectionNum, teacher, classroom, students, slots))
            .filter(section -> section.students().size() >= minimumAccepted)
            .limit(5)
            .max(comparing(section -> section.students().size()))
            .orElseThrow(() -> new IllegalStateException("No arrangement found for course #" + course));
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
