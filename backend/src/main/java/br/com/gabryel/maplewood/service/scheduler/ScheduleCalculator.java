package br.com.gabryel.maplewood.service.scheduler;

import br.com.gabryel.maplewood.config.TimeSchedulingConfig;
import br.com.gabryel.maplewood.model.Weekday;
import br.com.gabryel.maplewood.model.dto.ClassroomData;
import br.com.gabryel.maplewood.model.dto.CourseData;
import br.com.gabryel.maplewood.model.dto.CourseSectionDto;
import br.com.gabryel.maplewood.model.dto.StudentData;
import br.com.gabryel.maplewood.model.dto.TeacherData;
import br.com.gabryel.maplewood.model.dto.TimeRange;
import br.com.gabryel.maplewood.service.scheduler.algorithm.CoreScheduler;
import br.com.gabryel.maplewood.service.scheduler.algorithm.ElectiveScheduler;
import br.com.gabryel.maplewood.service.scheduler.algorithm.SchedulingContext;
import br.com.gabryel.maplewood.service.scheduler.algorithm.SlotCombinator;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static br.com.gabryel.maplewood.model.db.enums.CourseType.CORE;
import static br.com.gabryel.maplewood.model.db.enums.CourseType.ELECTIVE;
import static java.util.Comparator.comparing;
import static java.util.Map.entry;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Slf4j

public class ScheduleCalculator {
    public record CourseDemand(CourseData course, List<StudentData> students) { }

    public record TimeSlot(Weekday weekday, int slot) implements Comparable<TimeSlot> {
        private static final Comparator<TimeSlot> COMPARATOR =
            comparing((TimeSlot slot) -> slot.weekday().ordinal())
                .thenComparing(slot -> slot.slot);

        @Override
        public int compareTo(TimeSlot slot) {
            return COMPARATOR.compare(this, slot);
        }
    }

    public record SchedulerCourseSection(
        CourseData course,
        int section,
        List<TimeSlot> timeSlots,
        TeacherData teacher,
        ClassroomData classroom,
        List<StudentData> students
    ) { }

    private final Map<Integer, CourseData> courses;
    private final CoreScheduler coreScheduler;
    private final ElectiveScheduler electiveScheduler;

    public ScheduleCalculator(
        TimeSchedulingConfig timeConfig,
        Map<Integer, CourseData> courses,
        Map<Integer, StudentData> students,
        Map<Integer, TeacherData> teachers,
        Map<Integer, ClassroomData> classrooms
    ) {
        this.courses = courses;

        var context = new SchedulingContext(teachers, classrooms, timeConfig.getSlots());
        var combinator = new SlotCombinator(timeConfig);

        var courseDemands = createCourseDemands(students, getCoreCourses());

        this.coreScheduler = new CoreScheduler(context, combinator, courseDemands);
        this.electiveScheduler = new ElectiveScheduler(context, combinator);
    }

    public List<CourseSectionDto> generateSchedule() {
        var coreSections = coreScheduler.generateSchedule(0);
        var electiveSections = electiveScheduler.generateSchedule(1, getElectiveCourses());
        return Stream.concat(coreSections.stream(), electiveSections.stream()).map(this::toDto).toList();
    }

    private List<CourseData> getCoreCourses() {
        return courses.values().stream()
            .filter(course -> course.courseType() == CORE)
            .toList();
    }

    private List<CourseData> getElectiveCourses() {
        return courses.values().stream()
            .filter(course -> course.courseType() == ELECTIVE)
            .toList();
    }

    private List<TimeRange> mergeConsecutive(List<TimeSlot> timeSlots) {
        var slotsByDay = timeSlots.stream().collect(groupingBy(TimeSlot::weekday, toList()));

        return slotsByDay.entrySet().stream()
            .flatMap(entry -> mergeConsecutive(entry.getKey(), entry.getValue()).stream())
            .sorted(comparing(TimeRange::weekday).thenComparing(TimeRange::start))
            .toList();
    }

    private List<TimeRange> mergeConsecutive(Weekday weekday, List<TimeSlot> slots) {
        if (slots.isEmpty()) return List.of();

        var merged = new ArrayList<TimeRange>();
        var sortedSlots = slots.stream().sorted(comparing(TimeSlot::slot)).toList();

        for (var current : sortedSlots) {
            var currentSlot = current.slot();
            if (!merged.isEmpty() && currentSlot <= merged.getLast().end()) {
                var previous = merged.getLast();
                merged.set(merged.size() - 1, new TimeRange(weekday, previous.start(), currentSlot + 1));
            } else {
                merged.add(new TimeRange(weekday, currentSlot, currentSlot + 1));
            }
        }

        return merged;
    }

    private CourseSectionDto toDto(SchedulerCourseSection courseSection) {
        return new CourseSectionDto(
            courseSection.course(),
            courseSection.section, mergeConsecutive(courseSection.timeSlots), courseSection.teacher(),
            courseSection.classroom(),
            courseSection.students
        );
    }

    public static List<CourseDemand> createCourseDemands(Map<Integer, StudentData> students, List<CourseData> coreCourses) {
        var passedCoursesCache = students.values().stream()
            .collect(toMap(StudentData::id, student -> new HashSet<>(student.passedCourses())));

        var courseToStudents = students.values().stream()
            .flatMap(student -> {
                var passedCourses = passedCoursesCache.get(student.id());
                return coreCourses.stream()
                    .filter(course -> !passedCourses.contains(course.id()))
                    .filter(course -> course.prerequisite() == null || passedCourses.contains(course.prerequisite().id()))
                    .filter(course -> course.gradeLevelMin() <= student.gradeLevel())
                    .filter(course -> course.gradeLevelMax() >= student.gradeLevel())
                    .map(course -> entry(course, student));
            })
            .collect(groupingBy(Map.Entry::getKey, mapping(Map.Entry::getValue, toList())));

        return courseToStudents.entrySet().stream()
            .map(e -> new CourseDemand(e.getKey(), e.getValue()))
            .sorted(comparing(demand -> demand.students().stream().mapToDouble(StudentData::gradeLevel).average().orElse(0)))
            .toList();
    }
}
