package br.com.gabryel.maplewood.service.scheduler.algorithm;

import br.com.gabryel.maplewood.model.dto.ClassroomData;
import br.com.gabryel.maplewood.model.dto.CourseData;
import br.com.gabryel.maplewood.model.dto.TeacherData;
import br.com.gabryel.maplewood.service.scheduler.ScheduleCalculator.SchedulerCourseSection;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.Map.entry;

public class ElectiveScheduler {
    private final SchedulingContext scheduleState;
    private final SlotCombinator generator;

    public ElectiveScheduler(SchedulingContext scheduleState, SlotCombinator generator) {
        this.scheduleState = scheduleState;
        this.generator = generator;
    }

    public void generateSchedule(List<CourseData> electiveCourses) {
        var sections = electiveCourses.stream().map(course -> {
            var section = scheduleEmptySection(course);

            if (section != null)
                scheduleState.updateSchedules(section);

            return section;
        }).filter(Objects::nonNull).toList();

        if (sections.isEmpty()) return;

        generateSchedule(electiveCourses);
    }

    private SchedulerCourseSection scheduleEmptySection(CourseData course) {
        var teachers = scheduleState.getTeachersFor(course);
        var classrooms = scheduleState.getClassroomsFor(course);

        var teacherClassrooms = teachers.stream().flatMap(teacher ->
            classrooms.stream().map(classroom -> entry(teacher, classroom))
        ).toList();

        return teacherClassrooms.stream()
            .flatMap(entry -> scheduleEmptySection(course, entry.getKey(), entry.getValue()))
            .findFirst()
            .orElse(null);
    }

    private Stream<SchedulerCourseSection> scheduleEmptySection(CourseData course, TeacherData teacher, ClassroomData classroom) {
        var matchingSlots = scheduleState.getAvailableTimeSlots().stream()
            .filter(slot -> scheduleState.teacherIsFreeAt(teacher, slot))
            .filter(slot -> scheduleState.classroomIsFreeAt(classroom, slot))
            .toList();

        var sectionNum = scheduleState.getNextSectionNum(course);

        return generator.getKCombinations(matchingSlots, course.hoursPerWeek(), teacher.maxDailyHours())
            .map(Stream::toList)
            .filter(slots -> scheduleState.teacherCanTeachAt(teacher, slots))
            .map(slots -> new SchedulerCourseSection(course, sectionNum, slots, teacher, classroom, List.of()));
    }
}
