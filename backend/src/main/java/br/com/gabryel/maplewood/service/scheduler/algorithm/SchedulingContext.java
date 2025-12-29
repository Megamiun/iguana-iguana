package br.com.gabryel.maplewood.service.scheduler.algorithm;

import br.com.gabryel.maplewood.model.Weekday;
import br.com.gabryel.maplewood.model.dto.ClassroomData;
import br.com.gabryel.maplewood.model.dto.CourseData;
import br.com.gabryel.maplewood.model.dto.StudentData;
import br.com.gabryel.maplewood.model.dto.TeacherData;
import br.com.gabryel.maplewood.service.scheduler.ScheduleCalculator.SchedulerCourseSection;
import br.com.gabryel.maplewood.service.scheduler.ScheduleCalculator.TimeSlot;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static java.util.Comparator.comparing;
import static java.util.Map.entry;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class SchedulingContext {
    private final Map<Integer, Map<TimeSlot, CourseData>> teacherSchedules = new HashMap<>();
    private final Map<Integer, Map<TimeSlot, CourseData>> classroomSchedules = new HashMap<>();
    private final Map<Integer, Map<TimeSlot, CourseData>> studentSchedules = new HashMap<>();
    private final Map<Integer, Integer> courseIdToSectionNum = new HashMap<>();

    @Getter
    private final List<SchedulerCourseSection> sections = new ArrayList<>();

    @Getter
    private final List<TimeSlot> availableTimeSlots;

    private final Map<Integer, Map<Weekday, Integer>> teacherRemainingDailyHours;

    private final Map<Integer, List<TeacherData>> specializationToTeachers;
    private final Map<Integer, List<ClassroomData>> specializationToClassrooms;

    public SchedulingContext(
        Map<Integer, TeacherData> teachers,
        Map<Integer, ClassroomData> classrooms,
        List<Integer> workHours
    ) {
        this.teacherRemainingDailyHours = teachers.values().stream()
            .collect(toMap(TeacherData::id, SchedulingContext::generateHoursMap));
        this.availableTimeSlots = Arrays.stream(Weekday.values()).flatMap(weekDay ->
            workHours.stream().map(hour -> new TimeSlot(weekDay, hour))
        ).toList();;

        this.specializationToTeachers = teachers.values().stream()
            .collect(groupingBy(TeacherData::specializationId));

        this.specializationToClassrooms = classrooms.values().stream()
            .sorted(comparing(ClassroomData::capacity).reversed())
            .flatMap(classroom ->
                classroom.roomType().specializationIds().stream().map(specialization -> entry(specialization, classroom))
            )
            .collect(groupingBy(Entry::getKey, HashMap::new, mapping(Entry::getValue, toList())));
    }

    public void updateSchedules(SchedulerCourseSection section) {
        var timeSlots = section.timeSlots();
        var teacher = section.teacher();
        var course = section.course();

        sections.add(section);
        courseIdToSectionNum.put(course.id(), section.section());

        // Updating schedules
        updateSchedule(classroomSchedules, section.classroom().id(), course, timeSlots);
        updateSchedule(teacherSchedules, teacher.id(), course, timeSlots);

        for (var student : section.students()) {
            updateSchedule(studentSchedules, student.id(), course, timeSlots);
        }

        // Update remaining hours
        var teacherRemainingHours = teacherRemainingDailyHours.get(teacher.id());
        for (var weekdaySlots : timeSlots.stream().collect(groupingBy(TimeSlot::weekday)).entrySet()) {
            updateHours(teacherRemainingHours, weekdaySlots.getKey(), weekdaySlots.getValue().size());

            if (teacherRemainingHours.get(weekdaySlots.getKey()) == 0) {
                blockTeacherDay(teacher, weekdaySlots.getKey());
            }
        }
    }

    public Map<TimeSlot, CourseData> getTeacherSchedule(TeacherData data) {
        return teacherSchedules.computeIfAbsent(data.id(), key -> new HashMap<>());
    }

    public Map<TimeSlot, CourseData> getClassroomSchedule(ClassroomData data) {
        return classroomSchedules.computeIfAbsent(data.id(), key -> new HashMap<>());
    }

    public Map<TimeSlot, CourseData> getStudentSchedule(StudentData data) {
        return studentSchedules.computeIfAbsent(data.id(), key -> new HashMap<>());
    }

    public boolean studentIsFreeAt(StudentData data, List<TimeSlot> timeSlots) {
        var keys = getStudentSchedule(data).keySet();
        return timeSlots.stream().noneMatch(keys::contains);
    }

    public boolean teacherIsFreeAt(TeacherData data, TimeSlot timeSlot) {
        return !getTeacherSchedule(data).containsKey(timeSlot);
    }

    public boolean classroomIsFreeAt(ClassroomData data, TimeSlot timeSlot) {
        return !getClassroomSchedule(data).containsKey(timeSlot);
    }

    public boolean studentIsFreeAt(StudentData data, TimeSlot timeSlot) {
        return !getStudentSchedule(data).containsKey(timeSlot);
    }

    public int getRemainingTeacherHours(TeacherData data) {
        return teacherRemainingDailyHours.get(data.id()).values().stream().mapToInt(hours -> hours).sum();
    }

    public int getRemainingClassroomHours(ClassroomData data) {
        var spentHours = getClassroomSchedule(data).size();
        return availableTimeSlots.size() - spentHours;
    }

    public int getRemainingStudentHours(StudentData data) {
        var spentHours = getStudentSchedule(data).size();
        return availableTimeSlots.size() - spentHours;
    }

    public Integer getNextSectionNum(CourseData course) {
        return courseIdToSectionNum.getOrDefault(course.id(), 0) + 1;
    }

    public List<TeacherData> getTeachersFor(CourseData course) {
        return specializationToTeachers.get(course.specializationId()).stream()
            .filter(teacher -> getRemainingTeacherHours(teacher) >= course.hoursPerWeek())
            .sorted(comparing(this::getRemainingTeacherHours).reversed())
            .toList();
    }

    public List<ClassroomData> getClassroomsFor(CourseData course) {
        return specializationToClassrooms.get(course.specializationId()).stream()
            .filter(classroom -> getRemainingClassroomHours(classroom) >= course.hoursPerWeek())
            .sorted(comparing(this::getRemainingClassroomHours).reversed())
            .toList();
    }

    public boolean teacherCanTeachAt(TeacherData data, List<TimeSlot> slots) {
        var remainingHours = teacherRemainingDailyHours.get(data.id());
        var hoursByDay = slots.stream().collect(groupingBy(TimeSlot::weekday, counting()));
        return hoursByDay.entrySet().stream()
            .allMatch(entry -> remainingHours.get(entry.getKey()) >= entry.getValue());
    }

    private void blockTeacherDay(TeacherData teacher, Weekday day) {
        var teacherSchedule = getTeacherSchedule(teacher);

        availableTimeSlots.stream()
            .filter(slot -> slot.weekday() == day && !teacherSchedule.containsKey(slot))
            .forEach(slot -> teacherSchedule.put(slot, null));
    }

    private <K> void updateHours(Map<K, Integer> remainingWeeklyHours, K id, int amount) {
        remainingWeeklyHours.computeIfPresent(id, (key, previous) -> previous - amount);
    }

    private void updateSchedule(Map<Integer, Map<TimeSlot, CourseData>> schedules, int id, CourseData course, List<TimeSlot> timeSlots) {
        var schedule = schedules.computeIfAbsent(id, key -> new HashMap<>());
        for (var timeSlot : timeSlots) {
            schedule.put(timeSlot, course);
        }
    }
    private static Map<Weekday, Integer> generateHoursMap(TeacherData teacher) {
        return Arrays.stream(Weekday.values()).collect(toMap(identity(), day -> teacher.maxDailyHours()));
    }
}
