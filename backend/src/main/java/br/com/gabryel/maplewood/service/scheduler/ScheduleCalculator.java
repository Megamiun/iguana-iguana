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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static br.com.gabryel.maplewood.model.db.enums.CourseType.CORE;
import static br.com.gabryel.maplewood.model.db.enums.CourseType.ELECTIVE;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Comparator.comparing;
import static java.util.Map.entry;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.flatMapping;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

@Slf4j

public class ScheduleCalculator {
    private record TimeSlot(Weekday weekday, int slot) {}

    private record CourseSectionInternal(
        CourseData course,
        int section,
        List<TimeSlot> timeSlots,
        TeacherData teacher,
        ClassroomData classroom,
        List<StudentData> students
    ) { }

    private record CourseDemand(CourseData course, List<StudentData> students) { }

    private final Map<Integer, CourseData> courses;
    private final TimeSchedulingConfig timeConfig;

    private final Map<Integer, Map<TimeSlot, CourseData>> teacherSchedules = new HashMap<>();
    private final Map<Integer, Map<TimeSlot, CourseData>> classroomSchedules = new HashMap<>();
    private final Map<Integer, Map<TimeSlot, CourseData>> studentSchedules = new HashMap<>();


    private final Map<Integer, List<ClassroomData>> specializationToClassrooms;
    private final Map<Integer, List<TeacherData>> specializationToTeachers;

    private final Map<Integer, Map<Weekday, Integer>> teacherRemainingDailyHours;
    private final Map<Integer, Integer> classroomRemainingWeeklyHours;
    private final Map<Integer, Integer> studentRemainingWeeklyHours;

    private final List<CourseDemand> courseDemands;
    private final List<TimeSlot> availableTimeSlots;

    public ScheduleCalculator(
        TimeSchedulingConfig timeConfig,
        Map<Integer, CourseData> courses,
        Map<Integer, StudentData> students,
        Map<Integer, TeacherData> teachers,
        Map<Integer, ClassroomData> classrooms
    ) {
        this.timeConfig = timeConfig;
        this.courses = courses;

        this.specializationToTeachers = teachers.values().stream()
            .collect(groupingBy(TeacherData::specializationId));

        this.specializationToClassrooms = classrooms.values().stream()
            .sorted(comparing(ClassroomData::capacity).reversed())
            .collect(groupingToMultiMap(classroom ->
                classroom.roomType().specializationIds().stream().map(specialization -> entry(specialization, classroom))
            ));

        var timeSlots = timeConfig.getSlots();
        this.availableTimeSlots = Arrays.stream(Weekday.values()).flatMap(weekDay ->
            timeSlots.stream().map(slot -> new TimeSlot(weekDay, slot))
        ).toList();

        this.courseDemands = getCoreCourseDemands(students);

        this.teacherRemainingDailyHours = teachers.values().stream()
            .collect(toMap(TeacherData::id, ScheduleCalculator::generateHoursMap));

        this.classroomRemainingWeeklyHours = classrooms.keySet().stream()
            .collect(toMap(identity(), key -> availableTimeSlots.size()));

        this.studentRemainingWeeklyHours = students.keySet().stream()
            .collect(toMap(identity(), key -> availableTimeSlots.size()));
    }

    public List<CourseSectionDto> generateSchedule() {
        var coreSections = generateCoreSchedule(0);
        var electiveSections = generateElectiveSchedule(1, getElectiveCourses());
        return Stream.concat(coreSections.stream(), electiveSections.stream()).map(this::toDto).toList();
    }

    private List<CourseSectionInternal> generateCoreSchedule(int courseDemandIndex) {
        if (courseDemandIndex >= courseDemands.size())
            return List.of();

        var courseDemand = courseDemands.get(courseDemandIndex);
        var course = courseDemand.course();

        var students = courseDemand.students().stream()
            .sorted(comparing(this::getRemainingStudyHours).reversed())
            .toList();

        var sections = scheduleSections(course, extractTeachers(course), extractClassrooms(course), students, 1);

        return Stream.concat(
            sections.stream(),
            generateCoreSchedule(courseDemandIndex + 1).stream()
        ).toList();
    }

    private List<CourseSectionInternal> generateElectiveSchedule(int sectionNum, List<CourseData> electiveCourses) {
        var sections = electiveCourses.stream().map(course -> {
            var section = scheduleEmptySection(course, extractTeachers(course), extractClassrooms(course), sectionNum);

            if (section != null)
                updateSchedules(section);

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
        var teacherSchedule = teacherSchedules.computeIfAbsent(teacher.id(), key -> new HashMap<>());
        var classroomSchedule = classroomSchedules.computeIfAbsent(classroom.id(), key -> new HashMap<>());
        var teacherWeekdayRemainingHours = teacherRemainingDailyHours.get(teacher.id());

        var matchingSlots = availableTimeSlots.stream()
            .filter(slot -> !teacherSchedule.containsKey(slot))
            .filter(slot -> !classroomSchedule.containsKey(slot))
            .toList();

        return getKCombinations(matchingSlots, course.hoursPerWeek(), matchingSlots.size() - 1, 0, null)
            .map(Stream::toList)
            .filter(slots -> teacherCanTeachAt(slots, teacherWeekdayRemainingHours))
            .map(slots -> new CourseSectionInternal(course, sectionNum, slots, teacher, classroom, List.of()));
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
            .map(entry -> getMatchingSchedule(course, entry.getKey(), entry.getValue(), students, course.hoursPerWeek(), sectionNum))
            .findFirst().orElseThrow(() -> new IllegalStateException("No arrangement found for course #" + course.id()));

        updateSchedules(section);

        var newAvailableClassrooms = classrooms.stream()
            .filter(classroom -> classroom.id() != section.classroom().id() || hasEnoughWorkHours(classroom, course))
            .sorted(comparing(this::getRemainingClassHours).reversed())
            .toList();

        var newAvailableTeachers = teachers.stream()
            .filter(teacher -> teacher.id() != section.teacher().id() || hasEnoughWorkHours(teacher, course))
            .sorted(comparing(this::getRemainingTeacherHours).reversed())
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

    private void updateSchedules(CourseSectionInternal section) {
        // Updating schedules
        updateSchedule(classroomSchedules, section.classroom().id(), section);
        updateSchedule(teacherSchedules, section.teacher().id(), section);

        for (var student : section.students()) {
            updateSchedule(studentSchedules, student.id(), section);
        }

        // Update remaining hours
        var teacherRemainingHours = teacherRemainingDailyHours.get(section.teacher().id());
        for (var weekdaySlots : section.timeSlots().stream().collect(groupingBy(TimeSlot::weekday)).entrySet()) {
            updateHours(teacherRemainingHours, weekdaySlots.getKey(), weekdaySlots.getValue().size());
        }

        for (var student : section.students()) {
            updateHours(studentRemainingWeeklyHours, student.id(), section.timeSlots().size());
        }

        updateHours(classroomRemainingWeeklyHours, section.classroom().id(), section.timeSlots().size());
    }

    private <K> void updateHours(Map<K, Integer> remainingWeeklyHours, K id, int amount) {
        remainingWeeklyHours.computeIfPresent(id, (key, previous) -> previous - amount);
    }

    private void updateSchedule(Map<Integer, Map<TimeSlot, CourseData>> schedules, int id, CourseSectionInternal section) {
        var schedule = schedules.computeIfAbsent(id, key -> new HashMap<>());
        for (var timeSlot : section.timeSlots()) {
            schedule.put(timeSlot, section.course());
        }
    }

    private CourseSectionInternal getMatchingSchedule(CourseData course, TeacherData teacher, ClassroomData classroom, List<StudentData> students, int hoursPerWeek, int sectionNum) {
        var minimumAccepted = max(1, min(classroom.capacity(), students.size()) - 2);

        var teacherWeekdayRemainingHours = teacherRemainingDailyHours.get(teacher.id());

        var studentFreeTimes = students.stream().collect(toMap(
            StudentData::id,
            student -> availableTimeSlots.stream()
                .filter(slot -> studentSchedules.computeIfAbsent(student.id(), key -> new HashMap<>()).get(slot) == null)
                .collect(toSet())));

        var matchingSlots = findAvailableSlots(teacher, classroom, studentFreeTimes, minimumAccepted);

        return getKCombinations(matchingSlots, hoursPerWeek, matchingSlots.size() - 1, 0, null)
            .map(Stream::toList)
            .filter(slots -> teacherCanTeachAt(slots, teacherWeekdayRemainingHours))
            .map(slots -> generateSection(course, sectionNum, teacher, classroom, students, studentFreeTimes, slots))
            .filter(section -> section.students().size() >= minimumAccepted)
            .limit(10)
            .max(comparing(section -> section.students().size()))
            .orElseThrow(() -> new IllegalStateException("No arrangement found for course #" + course.id()));
    }

    private List<TimeSlot> findAvailableSlots(TeacherData teacher, ClassroomData classroom, Map<Integer, Set<TimeSlot>> studentFreeTimes, int minimumAccepted) {
        var teacherSchedule = teacherSchedules.computeIfAbsent(teacher.id(), key -> new HashMap<>());
        var classroomSchedule = classroomSchedules.computeIfAbsent(classroom.id(), key -> new HashMap<>());

        return availableTimeSlots.stream()
            .filter(timeSlot -> !teacherSchedule.containsKey(timeSlot))
            .filter(timeSlot -> !classroomSchedule.containsKey(timeSlot))
            .filter(timeSlot -> hasEnoughStudents(timeSlot, studentFreeTimes, minimumAccepted))
            .toList();
    }

    private static boolean hasEnoughStudents(TimeSlot timeSlot, Map<Integer, Set<TimeSlot>> studentFreeTime, int minimumAccepted) {
        var availableStudents = studentFreeTime.values().stream()
            .filter(slots -> slots.contains(timeSlot))
            .count();
        return availableStudents >= minimumAccepted;
    }

    private static boolean teacherCanTeachAt(List<TimeSlot> slot, Map<Weekday, Integer> remainingHours) {
        var hoursByDay = slot.stream().collect(groupingBy(TimeSlot::weekday, counting()));
        return hoursByDay.entrySet().stream()
            .allMatch(entry -> remainingHours.get(entry.getKey()) >= entry.getValue());
    }

    private CourseSectionInternal generateSection(
        CourseData course,
        int sectionNum,
        TeacherData teacher,
        ClassroomData classroom,
        List<StudentData> students,
        Map<Integer, Set<TimeSlot>> studentFreeTimes,
        List<TimeSlot> slots
    ) {
        var selectedStudents = students.stream()
            .filter(student -> isStudentAvailable(student, slots, studentFreeTimes))
            .limit(classroom.capacity())
            .toList();

        return new CourseSectionInternal(course, sectionNum, slots, teacher, classroom, selectedStudents);
    }

    private static boolean isStudentAvailable(StudentData student, List<TimeSlot> slots, Map<Integer, Set<TimeSlot>> studentFreeTimes) {
        return studentFreeTimes.get(student.id()).containsAll(slots);
    }

    private Stream<Stream<TimeSlot>> getKCombinations(
        List<TimeSlot> matchingSlots,
        int size,
        int index,
        int consecutiveCounter,
        TimeSlot lastSelected
    ) {
        if (size > index + 1)
            return Stream.empty();

        if (size == 0)
            return Stream.of(Stream.empty());

        var timeSlot = matchingSlots.get(index);
        var currentConsecutiveCounter = areConsecutive(timeSlot, lastSelected) ? consecutiveCounter + 1 : 0;

        if (currentConsecutiveCounter > timeConfig.getMaxConsecutiveClassHours())
            return getKCombinations(matchingSlots, size, index - 1, 0, lastSelected);

        if (index == 0)
            return Stream.of(Stream.of(matchingSlots.getFirst()));

        // Lazy concatenation using suppliers. Stream.concat eagerly loads
        return concatLazy(
            // Keeps current item
            () -> getKCombinations(matchingSlots, size - 1, index - 1, currentConsecutiveCounter, timeSlot)
                .map(rest -> Stream.concat(Stream.of(timeSlot), rest)),
            // Skip current item
            () -> getKCombinations(matchingSlots, size, index - 1, 0, lastSelected));
    }

    @SafeVarargs
    private <T> Stream<T> concatLazy(Supplier<Stream<T>>... streams) {
        return Stream.of(streams).flatMap(Supplier::get);
    }

    private static boolean areConsecutive(TimeSlot first, TimeSlot second) {
        return first != null && second != null && second.weekday() == first.weekday() && first.slot() + 1 == second.slot();
    }

    private boolean hasEnoughWorkHours(TeacherData data, CourseData course) {
        return getRemainingTeacherHours(data) >= course.hoursPerWeek();
    }

    private boolean hasEnoughWorkHours(ClassroomData data, CourseData course) {
        return getRemainingClassHours(data) >= course.hoursPerWeek();
    }

    private int getRemainingTeacherHours(TeacherData data) {
        return teacherRemainingDailyHours.get(data.id()).values().stream().mapToInt(hours -> hours).sum();
    }

    private int getRemainingClassHours(ClassroomData data) {
        return classroomRemainingWeeklyHours.get(data.id());
    }

    private int getRemainingStudyHours(StudentData data) {
        return studentRemainingWeeklyHours.get(data.id());
    }

    private <INPUT, KEY, VALUE> Collector<INPUT, ?, Map<KEY, List<VALUE>>> groupingToMultiMap(Function<INPUT, Stream<Entry<KEY, VALUE>>> mapper) {
        return flatMapping(mapper, groupingBy(Entry::getKey, mapping(Entry::getValue, toList())));
    }

    private List<CourseData> getElectiveCourses() {
        return courses.values().stream()
            .filter(course -> course.courseType() == ELECTIVE)
            .toList();
    }

    private List<CourseDemand> getCoreCourseDemands(Map<Integer, StudentData> students) {
        var courseToStudents = students.values().stream()
            .flatMap(student -> getEligibleCoreCourses(student).stream().map(course -> entry(course, student)))
            .collect(groupingBy(Entry::getKey, mapping(Entry::getValue, toList())));

        return courseToStudents.entrySet().stream()
            .map(e -> new CourseDemand(e.getKey(), e.getValue()))
            .sorted(comparing((CourseDemand demand) -> demand.students().size() / specializationToTeachers.get(demand.course.specializationId()).size()).reversed())
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
            .toList();
    }

    private static Map<Weekday, Integer> generateHoursMap(TeacherData teacher) {
        return Arrays.stream(Weekday.values()).collect(toMap(identity(), day -> teacher.maxDailyHours()));
    }

    private CourseSectionDto toDto(CourseSectionInternal courseSection) {
        return new CourseSectionDto(
            courseSection.course(),
            courseSection.section, mergeConsecutiveSlots(courseSection.timeSlots), courseSection.teacher(),
            courseSection.classroom(),
            courseSection.students
        );
    }

    private List<TimeRange> mergeConsecutiveSlots(List<TimeSlot> timeSlots) {
        var slotsByDay = timeSlots.stream().collect(groupingBy(TimeSlot::weekday, toList()));

        return slotsByDay.entrySet().stream()
            .flatMap(entry -> mergeConsecutiveSlots(entry.getKey(), entry.getValue()).stream())
            .sorted(comparing(TimeRange::weekday).thenComparing(TimeRange::start))
            .toList();
    }

    private List<TimeRange> mergeConsecutiveSlots(Weekday weekday, List<TimeSlot> slots) {
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

    private List<TeacherData> extractTeachers(CourseData course) {
        return specializationToTeachers.get(course.specializationId()).stream()
            .filter(data -> hasEnoughWorkHours(data, course))
            .sorted(comparing(this::getRemainingTeacherHours).reversed())
            .toList();
    }

    private List<ClassroomData> extractClassrooms(CourseData course) {
        return specializationToClassrooms.get(course.specializationId()).stream()
            .filter(data -> hasEnoughWorkHours(data, course))
            .sorted(comparing(this::getRemainingClassHours).reversed())
            .toList();
    }
}
