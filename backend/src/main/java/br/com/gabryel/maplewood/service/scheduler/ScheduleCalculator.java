package br.com.gabryel.maplewood.service.scheduler;

import br.com.gabryel.maplewood.config.TimeSchedulingConfig;
import br.com.gabryel.maplewood.model.Weekday;
import br.com.gabryel.maplewood.model.response.ScheduleResponse;
import br.com.gabryel.maplewood.model.response.ScheduleResponse.CourseScheduleResponse;
import br.com.gabryel.maplewood.service.scheduler.ClassroomDataService.ClassroomData;
import br.com.gabryel.maplewood.service.scheduler.CourseDataService.CourseData;
import br.com.gabryel.maplewood.service.scheduler.StudentDataService.StudentData;
import br.com.gabryel.maplewood.service.scheduler.TeacherDataService.TeacherData;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static br.com.gabryel.maplewood.model.db.enums.CourseType.CORE;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Comparator.comparing;
import static java.util.Map.entry;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.flatMapping;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

@Slf4j
public class ScheduleCalculator {

    private record TimeSlot(Weekday weekday, int slot) { }

    private record CourseSection(CourseData course, TeacherData teacher, ClassroomData classroom, List<TimeSlot> timeSlots, List<StudentData> students, int section) { }

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
            .collect(toMap(TeacherData::id, ScheduleCalculator::getHoursMap));

        this.classroomRemainingWeeklyHours = classrooms.keySet().stream()
            .collect(toMap(identity(), key -> availableTimeSlots.size()));

        this.studentRemainingWeeklyHours = students.keySet().stream()
            .collect(toMap(identity(), key -> availableTimeSlots.size()));
    }

    public ScheduleResponse generateSchedule() {
        var schedule = generateSchedule(0).stream().map(section -> new CourseScheduleResponse(
            section.course.name(),
            section.section,
            section.teacher.name(),
            section.classroom.name(),
            // TODO Merge consecutive slots
            section.timeSlots().stream().map(slot -> new ScheduleResponse.ScheduleDurationResponse(slot.weekday, slot.slot, slot.slot + 1)).toList(),
            section.classroom.capacity() - section.students.size(),
            section.students.size()
        )).toList();

        return new ScheduleResponse(schedule);
    }

    private List<CourseSection> generateSchedule(int currentIndex) {
        if (currentIndex >= courseDemands.size())
            return List.of();

        var courseDemand = courseDemands.get(currentIndex);
        var course = courseDemand.course();

        var availableClassrooms = specializationToClassrooms.get(course.specializationId()).stream()
            .filter(data -> hasEnoughWorkHours(data, course))
            .toList();

        var availableTeachers = specializationToTeachers.get(course.specializationId()).stream()
            .filter(data -> hasEnoughWorkHours(data, course))
            .toList();

        var sections = scheduleSections(course, availableTeachers, availableClassrooms, courseDemand.students(), 1);

        return Stream.concat(sections.stream(), generateSchedule(currentIndex + 1).stream()).toList();
    }

    private List<CourseSection> scheduleSections(CourseData course, List<TeacherData> availableTeachers, List<ClassroomData> availableClassrooms, List<StudentData> remainingStudents, int sectionNum) {
        if (remainingStudents.isEmpty())
            return List.of();

        var currentSection = availableTeachers.stream()
            .flatMap(teacher -> availableClassrooms.stream().map(classroom -> entry(teacher, classroom)))
            .map(entry -> getMatchingSchedule(course, entry.getKey(), entry.getValue(), remainingStudents, course.hoursPerWeek(), sectionNum))
            .filter(Optional::isPresent).map(Optional::get)
            .findFirst().orElseThrow(() -> new IllegalStateException("No arrangement found for course #" + course.id()));

        updateSchedules(course, currentSection, currentSection.students);

        var newAvailableClassrooms = availableClassrooms.stream()
            .filter(classroom -> classroom.id() != currentSection.classroom.id() || hasEnoughWorkHours(classroom, course))
            .toList();

        var newAvailableTeachers = availableTeachers.stream()
            .filter(teacher -> teacher.id() != currentSection.teacher.id() || hasEnoughWorkHours(teacher, course))
            .toList();

        var sectionStudentSet = new HashSet<>(currentSection.students);
        var newAvailableStudents = remainingStudents.stream()
            .filter(student -> !sectionStudentSet.contains(student))
            .sorted(comparing(this::getRemainingStudyHours).reversed())
            .toList();

        return Stream.concat(
            Stream.of(currentSection),
            scheduleSections(course, newAvailableTeachers, newAvailableClassrooms, newAvailableStudents, sectionNum + 1).stream()
        ).toList();
    }

    private void updateSchedules(CourseData course, CourseSection currentSection, Collection<StudentData> students) {
        // Updating schedules
        updateSchedule(classroomSchedules, currentSection.classroom.id(), currentSection, course);
        updateSchedule(teacherSchedules, currentSection.teacher.id(), currentSection, course);

        for (var student : students) {
            updateSchedule(studentSchedules, student.id(), currentSection, course);
        }

        // Update remaining hours
        var teacherRemainingHours = teacherRemainingDailyHours.get(currentSection.teacher.id());
        for (var weekdaySlots : currentSection.timeSlots.stream().collect(groupingBy(TimeSlot::weekday)).entrySet()) {
            updateHours(teacherRemainingHours, weekdaySlots.getKey(), weekdaySlots.getValue().size());
        }

        for (var student : students) {
            updateHours(studentRemainingWeeklyHours, student.id(), currentSection.timeSlots().size());
        }

        updateHours(classroomRemainingWeeklyHours, currentSection.classroom.id(), currentSection.timeSlots().size());
    }

    private <K> void updateHours(Map<K, Integer> remainingWeeklyHours, K id, int amount) {
        remainingWeeklyHours.computeIfPresent(id, (key, previous) -> previous - amount);
    }

    private void updateSchedule(Map<Integer, Map<TimeSlot, CourseData>> schedules, int id, CourseSection section, CourseData course) {
        var schedule = schedules.computeIfAbsent(id, key -> new HashMap<>());
        for (var timeSlot : section.timeSlots()) {
            schedule.put(timeSlot, course);
        }
    }

    private Optional<CourseSection> getMatchingSchedule(CourseData course, TeacherData teacher, ClassroomData classroom, List<StudentData> remainingStudents, int hoursPerWeek, int sectionNum) {
        var teacherSchedule = teacherSchedules.computeIfAbsent(teacher.id(), key -> new HashMap<>());
        var classroomSchedule = classroomSchedules.computeIfAbsent(classroom.id(), key -> new HashMap<>());
        var teacherWeekdayRemainingHours = teacherRemainingDailyHours.get(teacher.id());

        var studentFilledTimes = remainingStudents.stream().collect(toMap(
            StudentData::id,
            student -> availableTimeSlots.stream()
                .filter(slot -> studentSchedules.computeIfAbsent(student.id(), key -> new HashMap<>()).computeIfAbsent(slot, key -> null) == null)
                .collect(toSet())));

        var minimumAccepted = max(1, min(classroom.capacity(), remainingStudents.size()) - 2);

        var matchingSlots = availableTimeSlots.stream()
            .filter(timeSlot -> !teacherSchedule.containsKey(timeSlot))
            .filter(timeSlot -> !classroomSchedule.containsKey(timeSlot))
            .filter(timeSlot -> {
                long availableStudents = studentFilledTimes.values().stream()
                    .filter(slots -> slots.contains(timeSlot))
                    .count();
                return availableStudents >= minimumAccepted;
            }).toList();

        return getKCombinations(matchingSlots, hoursPerWeek, matchingSlots.size() - 1, 0, null)
            .map(Stream::toList)
            .filter(slot -> teacherHasHours(slot, teacherWeekdayRemainingHours))
            .map(slots -> {
                var students = remainingStudents.stream().filter(student -> {
                    var studentSchedule = studentSchedules.computeIfAbsent(student.id(), key -> new HashMap<>());
                    return slots.stream().noneMatch(studentSchedule::containsKey);
                }).limit(classroom.capacity()).toList();

                if (students.size() < minimumAccepted) return Optional.<CourseSection>empty();

                return Optional.of(new CourseSection(course, teacher, classroom, slots, students, sectionNum));
            })
            .filter(Optional::isPresent)
            .map(Optional::get)
            .limit(10)
            .max(comparing(section -> section.students.size()));
    }

    private static boolean teacherHasHours(List<TimeSlot> slot, Map<Weekday, Integer> remainingHours) {
        return slot.stream().collect(groupingBy(TimeSlot::weekday)).entrySet().stream()
            .allMatch(entry -> remainingHours.get(entry.getKey()) >= entry.getValue().size());
    }

    private Stream<Stream<TimeSlot>> getKCombinations(List<TimeSlot> matchingSlots, int k, int index, int consecutiveCounter, TimeSlot lastSelected) {
        if (k > index + 1)
            return Stream.empty();

        if (k == 0)
            return Stream.of(Stream.empty());

        var timeSlot = matchingSlots.get(index);
        var currentConsecutiveCounter = areConsecutive(lastSelected, timeSlot) ? consecutiveCounter + 1 : 0;

        if (currentConsecutiveCounter > timeConfig.getMaxConsecutiveClassHours())
            return getKCombinations(matchingSlots, k, index - 1, 0, lastSelected);

        if (index == 0)
            return Stream.of(Stream.of(matchingSlots.getFirst()));

        // Lazy concatenation using suppliers. Stream.concat eagerly loads
        return concatLazy(
            // Keep path
            () -> getKCombinations(matchingSlots, k - 1, index - 1, currentConsecutiveCounter, timeSlot)
                .map(rest -> Stream.concat(rest, Stream.of(timeSlot))),
            // Skip path
            () -> getKCombinations(matchingSlots, k, index - 1, currentConsecutiveCounter, lastSelected));
    }

    @SafeVarargs
    private <T> Stream<T> concatLazy(Supplier<Stream<T>> ... streams) {
        return Stream.of(streams).flatMap(Supplier::get);
    }

    private static boolean areConsecutive(TimeSlot first, TimeSlot second) {
        return first != null && second.weekday() == first.weekday() && first.slot + 1 == second.slot();
    }

    private boolean hasEnoughWorkHours(TeacherData data, CourseData course) {
        return getRemainingTeacherHours(data) >= course.hoursPerWeek();
    }

    private boolean hasEnoughWorkHours(ClassroomData data, CourseData course) {
        return getRemainingClassHours(data) >= course.hoursPerWeek();
    }

    private int getRemainingTeacherHours(TeacherData data) {
        return teacherRemainingDailyHours.get(data.id()).values().stream().mapToInt(hoursPerDay -> hoursPerDay).sum();
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

    private static Map<Weekday, Integer> getHoursMap(TeacherData teacher) {
        return Arrays.stream(Weekday.values()).collect(toMap(identity(), day -> teacher.maxDailyHours()));
    }
}
