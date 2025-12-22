package br.com.gabryel.maplewood.service.scheduler;

import br.com.gabryel.maplewood.config.TimeSchedulingConfig;
import br.com.gabryel.maplewood.model.response.ScheduleResponse;
import br.com.gabryel.maplewood.service.scheduler.ClassroomDataService.ClassroomData;
import br.com.gabryel.maplewood.service.scheduler.CourseDataService.CourseData;
import br.com.gabryel.maplewood.service.scheduler.StudentDataService.StudentData;
import br.com.gabryel.maplewood.service.scheduler.TeacherDataService.TeacherData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static br.com.gabryel.maplewood.model.db.enums.CourseType.CORE;
import static java.util.Comparator.comparing;
import static java.util.Map.entry;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.flatMapping;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class ScheduleCalculator {

    public enum Weekday {
        MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY
    }

    private final Map<Integer, CourseData> courses;
    private final TimeSchedulingConfig timeConfig;

    private final Map<Integer, Map<Weekday, Map<Integer, CourseData>>> teacherSchedule;
    private final Map<Integer, Map<Weekday, Map<Integer, CourseData>>> classroomSchedule;
    private final Map<Integer, Map<Weekday, Map<Integer, CourseData>>> studentSchedule;


    private final Map<Integer, List<ClassroomData>> specializationToClassrooms;
    private final Map<Integer, List<TeacherData>> specializationToTeachers;

    private final Map<Integer, Map<Weekday, Integer>> teacherRemainingDailyHours;

    private final List<CourseDemand> courseDemands;

    private record CourseDemand(CourseData course, List<StudentData> students) {
    }

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
            .sorted(comparing(ClassroomData::capacity))
            .collect(groupingToMultiMap(classroom ->
                classroom.roomType().specializationIds().stream().map(specialization -> entry(specialization, classroom))
            ));

        this.teacherSchedule = setupCleanSchedule(teachers.keySet());
        this.studentSchedule = setupCleanSchedule(students.keySet());
        this.classroomSchedule = setupCleanSchedule(classrooms.keySet());

        this.courseDemands = getCoreCourseDemands(students);

        this.teacherRemainingDailyHours = teachers.values().stream()
            .collect(toMap(TeacherData::id, ScheduleCalculator::getHoursMap));
    }

    public ScheduleResponse generateSchedule() {
        return generateSchedule(0);
    }

    private ScheduleResponse generateSchedule(int currentIndex) {
        var courseDemand = courseDemands.get(currentIndex);

        if (courseDemand == null) {
            // TODO Generate response
            return new ScheduleResponse();
        }

        var course = courseDemand.course();
        var students = courseDemand.students();

        var remainingStudents = new ArrayList<>(students);

        var availableTeachers = specializationToTeachers.get(course.specializationId()).stream()
            .filter(teacher -> hasEnoughWorkHours(teacher, course))
            .toList();

        var availableClassrooms = specializationToClassrooms.get(course.specializationId());

        return generateSchedule(currentIndex + 1);
    }

    private boolean hasEnoughWorkHours(TeacherData teacher, CourseData course) {
        return teacherRemainingDailyHours.get(teacher.id()).values().stream().mapToInt(i -> i).sum() >= course.hoursPerWeek();
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
            .sorted(comparing((CourseDemand demand) -> demand.students().size()).reversed())
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

    private Map<Integer, Map<Weekday, Map<Integer, CourseData>>> setupCleanSchedule(Set<Integer> integers) {
        var timeSlots = timeConfig.getSlots();

        return integers.stream().collect(toMap(identity(), key1 ->
            Arrays.stream(Weekday.values()).collect(toMap(identity(), key2 ->
                createTimeSlotsMap(timeSlots)))
        ));
    }

    private static Map<Integer, CourseData> createTimeSlotsMap(List<Integer> timeSlots) {
        var map = new HashMap<Integer, CourseData>();
        for (var timeSlot : timeSlots) {
            map.put(timeSlot, null);
        }

        return map;
    }
}