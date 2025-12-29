package br.com.gabryel.maplewood.service.scheduler;

import br.com.gabryel.maplewood.config.TimeSchedulingConfig;
import br.com.gabryel.maplewood.model.Weekday;
import br.com.gabryel.maplewood.model.dto.ClassroomData;
import br.com.gabryel.maplewood.model.dto.CourseData;
import br.com.gabryel.maplewood.model.dto.CourseSectionDto;
import br.com.gabryel.maplewood.model.dto.StudentData;
import br.com.gabryel.maplewood.model.dto.TeacherData;
import br.com.gabryel.maplewood.model.dto.TimeRange;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static br.com.gabryel.maplewood.model.db.enums.CourseType.CORE;
import static br.com.gabryel.maplewood.model.db.enums.CourseType.ELECTIVE;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Comparator.comparing;
import static java.util.Map.entry;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

@Slf4j

public class ScheduleCalculator {
    record TimeSlot(Weekday weekday, int slot) implements Comparable<TimeSlot> {
        private static final Comparator<TimeSlot> COMPARATOR =
            comparing((TimeSlot slot) -> slot.weekday().ordinal())
                .thenComparing(slot -> slot.slot);

        @Override
        public int compareTo(TimeSlot slot) {
            return COMPARATOR.compare(this, slot);
        }
    }

    private record CourseDemand(CourseData course, List<StudentData> students) { }

    record CourseSectionInternal(
        CourseData course,
        int section,
        List<TimeSlot> timeSlots,
        TeacherData teacher,
        ClassroomData classroom,
        List<StudentData> students
    ) { }

    private final Map<Integer, CourseData> courses;
    private final TimeSchedulingConfig timeConfig;
    private final ScheduleState scheduleState;
    private final List<CourseDemand> courseDemands;

    public ScheduleCalculator(
        TimeSchedulingConfig timeConfig,
        Map<Integer, CourseData> courses,
        Map<Integer, StudentData> students,
        Map<Integer, TeacherData> teachers,
        Map<Integer, ClassroomData> classrooms
    ) {
        this.timeConfig = timeConfig;
        this.courses = courses;

        this.scheduleState = new ScheduleState(teachers, classrooms, timeConfig.getSlots());
        this.courseDemands = getCoreCourseDemands(students);
    }

    public List<CourseSectionDto> generateSchedule() {
        var coreSections = generateCoreSchedule(0);
        var electiveSections = generateElectiveSchedule(1, getElectiveCourses());
        return Stream.concat(coreSections.stream(), electiveSections.stream()).map(this::toDto).toList();
    }

    private List<CourseSectionInternal> generateElectiveSchedule(int sectionNum, List<CourseData> electiveCourses) {
        var sections = electiveCourses.stream().map(course -> {
            var section = scheduleEmptySection(course, scheduleState.getTeachersFor(course), scheduleState.getClassroomsFor(course), sectionNum);

            if (section != null)
                scheduleState.updateSchedules(section);

            return section;
        }).filter(Objects::nonNull).toList();

        if (sections.isEmpty())
            return List.of();

        return Stream.concat(
            sections.stream(),
            generateElectiveSchedule(sectionNum + 1, electiveCourses).stream()
        ).toList();
    }

    private CourseSectionInternal scheduleEmptySection(CourseData course, List<TeacherData> teachers, List<ClassroomData> classrooms, int sectionNum) {
        var teacherClassrooms = teachers.stream().flatMap(teacher ->
            classrooms.stream().map(classroom -> entry(teacher, classroom))
        ).toList();

        return teacherClassrooms.stream()
            .flatMap(entry -> scheduleEmptySection(course, sectionNum, entry.getKey(), entry.getValue()))
            .findFirst()
            .orElse(null);
    }

    private Stream<CourseSectionInternal> scheduleEmptySection(CourseData course, int sectionNum, TeacherData teacher, ClassroomData classroom) {
        var matchingSlots = scheduleState.getAvailableTimeSlots().stream()
            .filter(slot -> scheduleState.teacherIsFreeAt(teacher.id(), slot))
            .filter(slot -> scheduleState.classroomIsFreeAt(classroom.id(), slot))
            .toList();

        return getKCombinations(matchingSlots, course.hoursPerWeek(), matchingSlots.size() - 1, 0, null, null, 0, teacher.maxDailyHours())
            .map(Stream::toList)
            .filter(slots -> scheduleState.teacherCanTeachAt(teacher.id(), slots))
            .map(slots -> new CourseSectionInternal(course, sectionNum, slots, teacher, classroom, List.of()));
    }

    private List<CourseSectionInternal> generateCoreSchedule(int courseDemandIndex) {
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
            generateCoreSchedule(courseDemandIndex + 1).stream()
        ).toList();
    }

    private List<CourseSectionInternal> scheduleSections(
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
            .findFirst().orElseThrow(() -> new IllegalStateException("No arrangement found for course #" + course.id()));

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

    private CourseSectionInternal getMatchingSchedule(CourseData course, TeacherData teacher, ClassroomData classroom, List<StudentData> students, int sectionNum) {
        var minimumAccepted = max(1, min(classroom.capacity(), students.size()) - 2);

        var matchingSlots = findAvailableSlots(teacher, classroom, students, minimumAccepted);

        return getKCombinations(matchingSlots, course.hoursPerWeek(), matchingSlots.size() - 1, 0, null, null, 0, 4)
            .map(Stream::toList)
            .filter(slots -> scheduleState.teacherCanTeachAt(teacher.id(), slots))
            .map(slots -> generateSection(course, sectionNum, teacher, classroom, students, slots))
            .filter(section -> section.students().size() >= minimumAccepted)
            .limit(5)
            .max(comparing(section -> section.students().size()))
            .orElseThrow(() -> new IllegalStateException("No arrangement found for course #" + course.id()));
    }

    private List<TimeSlot> findAvailableSlots(TeacherData teacher, ClassroomData classroom, List<StudentData> students, int minimumAccepted) {
        var studentFreeTimes = students.stream().collect(toMap(
            StudentData::id,
            student -> scheduleState.getAvailableTimeSlots().stream()
                .filter(slot -> scheduleState.studentIsFreeAt(student.id(), slot))
                .collect(toSet())));

        return scheduleState.getAvailableTimeSlots().stream()
            .filter(slot -> scheduleState.teacherIsFreeAt(teacher.id(), slot))
            .filter(slot -> scheduleState.classroomIsFreeAt(classroom.id(), slot))
            .filter(timeSlot -> hasEnoughStudents(timeSlot, studentFreeTimes, minimumAccepted))
            .toList();
    }

    private static boolean hasEnoughStudents(TimeSlot timeSlot, Map<Integer, Set<TimeSlot>> studentFreeTime, int minimumAccepted) {
        var availableStudents = studentFreeTime.values().stream()
            .filter(slots -> slots.contains(timeSlot))
            .count();
        return availableStudents >= minimumAccepted;
    }

    private CourseSectionInternal generateSection(
        CourseData course,
        int sectionNum,
        TeacherData teacher,
        ClassroomData classroom,
        List<StudentData> students,
        List<TimeSlot> slots
    ) {
        var selectedStudents = students.stream()
            .filter(student -> scheduleState.studentIsFreeAt(student.id(), slots))
            .limit(classroom.capacity())
            .toList();

        return new CourseSectionInternal(course, sectionNum, slots, teacher, classroom, selectedStudents);
    }

    private Stream<Stream<TimeSlot>> getKCombinations(
        List<TimeSlot> matchingSlots,
        int size,
        int index,
        int consecutiveCounter,
        TimeSlot lastSelected,
        Weekday lastDay,
        int inLastDay,
        int maxInDay
    ) {
        if (size > index + 1)
            return Stream.empty();

        if (size == 0)
            return Stream.of(Stream.empty());

        var timeSlot = matchingSlots.get(index);
        var currentConsecutiveCounter = areConsecutive(timeSlot, lastSelected) ? consecutiveCounter + 1 : 1;

        var currentDay = timeSlot.weekday;
        var inCurrentDay = currentDay == lastDay ? inLastDay + 1 : 1;

        if (currentConsecutiveCounter > timeConfig.getMaxConsecutiveClassHours() || inCurrentDay > maxInDay)
            return getKCombinations(matchingSlots, size, index - 1, currentConsecutiveCounter, lastSelected, currentDay, inCurrentDay, maxInDay);

        if (index == 0)
            return Stream.of(Stream.of(matchingSlots.getFirst()));

        // Lazy concatenation using suppliers. Stream.concat eagerly loads
        return concatLazy(
            // Keeps current item
            () -> getKCombinations(matchingSlots, size - 1, index - 1, currentConsecutiveCounter, timeSlot, currentDay, inCurrentDay, maxInDay)
                .map(next -> Stream.concat(Stream.of(timeSlot), next)),
            // Skip current item
            () -> getKCombinations(matchingSlots, size, index - 1, currentConsecutiveCounter, lastSelected, currentDay, inCurrentDay, maxInDay));

    }

    @SafeVarargs
    private <T> Stream<T> concatLazy(Supplier<Stream<T>>... streams) {
        return Stream.of(streams).flatMap(Supplier::get);
    }

    private static boolean areConsecutive(TimeSlot first, TimeSlot second) {
        return first != null && second != null && second.weekday() == first.weekday() && first.slot() + 1 == second.slot();
    }

    private List<CourseData> getElectiveCourses() {
        return courses.values().stream()
            .filter(course -> course.courseType() == ELECTIVE)
            .toList();
    }

    private List<CourseData> getEligibleCoreCourses(StudentData student) {
        var passedCourses = new HashSet<>(student.passedCourses());

        return courses.values().stream()
            .filter(course -> course.courseType() == CORE)
            .filter(course -> !passedCourses.contains(course.id()))
            .filter(course -> course.prerequisite() == null || passedCourses.contains(course.prerequisite().id()))
            .filter(course -> course.gradeLevelMin() <= student.gradeLevel())
            .filter(course -> course.gradeLevelMax() >= student.gradeLevel())
//            // See README for explanation
//            .filter(course -> course.gradeLevelMin() == student.gradeLevel() || course.gradeLevelMax() == student.gradeLevel())
            .toList();
    }

    private List<CourseDemand> getCoreCourseDemands(Map<Integer, StudentData> students) {
        var courseToStudents = students.values().stream()
            .flatMap(student -> getEligibleCoreCourses(student).stream().map(course -> entry(course, student)))
            .collect(groupingBy(Entry::getKey, mapping(Entry::getValue, toList())));

        return courseToStudents.entrySet().stream()
            .map(e -> new CourseDemand(e.getKey(), e.getValue()))
            .sorted(comparing((CourseDemand demand) -> demand.students().size() / scheduleState.getTeachersFor(demand.course).size()).reversed())
            .toList();
    }

    private Map<List<CourseData>, List<StudentData>> getCoreCourseGroups(Map<Integer, StudentData> students) {
        var courseToStudents = students.values().stream()
            .flatMap(student -> getStudentToEligibleCourse(student).stream())
            .collect(groupingBy(Entry::getKey, mapping(Entry::getValue, toList())));

        return courseToStudents.entrySet().stream()
            .collect(groupingBy(Entry::getValue, mapping(Entry::getKey, toList())));
    }

    private List<Entry<StudentData, CourseData>> getStudentToEligibleCourse(StudentData student) {
        return getEligibleCoreCourses(student).stream()
            .sorted(comparing(CourseData::id))
            .map(course -> entry(student, course))
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

    private CourseSectionDto toDto(CourseSectionInternal courseSection) {
        return new CourseSectionDto(
            courseSection.course(),
            courseSection.section,
            mergeConsecutive(courseSection.timeSlots),
            courseSection.teacher(),
            courseSection.classroom(),
            courseSection.students
        );
    }
}
