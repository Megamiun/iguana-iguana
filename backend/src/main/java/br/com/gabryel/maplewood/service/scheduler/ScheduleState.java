package br.com.gabryel.maplewood.service.scheduler;

import br.com.gabryel.maplewood.model.Weekday;
import br.com.gabryel.maplewood.model.dto.ClassroomData;
import br.com.gabryel.maplewood.model.dto.CourseData;
import br.com.gabryel.maplewood.model.dto.StudentData;
import br.com.gabryel.maplewood.model.dto.TeacherData;
import br.com.gabryel.maplewood.service.scheduler.ScheduleCalculator.CourseSectionInternal;
import br.com.gabryel.maplewood.service.scheduler.ScheduleCalculator.TimeSlot;

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

class ScheduleState {
    private final Map<Integer, Map<TimeSlot, CourseData>> teacherSchedules = new HashMap<>();
    private final Map<Integer, Map<TimeSlot, CourseData>> classroomSchedules = new HashMap<>();
    private final Map<Integer, Map<TimeSlot, CourseData>> studentSchedules = new HashMap<>();
    private final Map<Integer, Map<Weekday, Integer>> teacherRemainingDailyHours;
    private final List<TimeSlot> availableTimeSlots;

    private final Map<Integer, List<TeacherData>> specializationToTeachers;
    private final Map<Integer, List<ClassroomData>> specializationToClassrooms;

    ScheduleState(
        Map<Integer, TeacherData> teachers,
        Map<Integer, ClassroomData> classrooms,
        List<Integer> workHours
    ) {
        this.teacherRemainingDailyHours = teachers.values().stream()
            .collect(toMap(TeacherData::id, ScheduleState::generateHoursMap));
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

    public void updateSchedules(CourseSectionInternal section) {
        var timeSlots = section.timeSlots();
        var course = section.course();

        var teacherId = section.teacher().id();

        // Updating schedules
        updateSchedule(classroomSchedules, section.classroom().id(), course, timeSlots);
        updateSchedule(teacherSchedules, teacherId, course, timeSlots);

        for (var student : section.students()) {
            updateSchedule(studentSchedules, student.id(), course, timeSlots);
        }

        // Update remaining hours
        var teacherRemainingHours = teacherRemainingDailyHours.get(teacherId);
        for (var weekdaySlots : timeSlots.stream().collect(groupingBy(TimeSlot::weekday)).entrySet()) {
            updateHours(teacherRemainingHours, weekdaySlots.getKey(), weekdaySlots.getValue().size());

            if (teacherRemainingHours.get(weekdaySlots.getKey()) == 0) {
                blockTeacherDay(teacherId, weekdaySlots.getKey());
            }
        }
    }

    Map<TimeSlot, CourseData> getTeacherSchedule(int id) {
        return teacherSchedules.computeIfAbsent(id, key -> new HashMap<>());
    }

    Map<TimeSlot, CourseData> getClassroomSchedule(int id) {
        return classroomSchedules.computeIfAbsent(id, key -> new HashMap<>());
    }

    Map<TimeSlot, CourseData> getStudentSchedule(int id) {
        return studentSchedules.computeIfAbsent(id, key -> new HashMap<>());
    }

    boolean studentIsFreeAt(int id, List<TimeSlot> timeSlots) {
        return containsNone(studentSchedules, id, timeSlots);
    }

    boolean teacherIsFreeAt(int id, TimeSlot timeSlot) {
        return !getTeacherSchedule(id).containsKey(timeSlot);
    }

    boolean classroomIsFreeAt(int id, TimeSlot timeSlot) {
        return !getClassroomSchedule(id).containsKey(timeSlot);
    }

    boolean studentIsFreeAt(int id, TimeSlot timeSlot) {
        return !getStudentSchedule(id).containsKey(timeSlot);
    }

    int getRemainingTeacherHours(TeacherData data) {
        return teacherRemainingDailyHours.get(data.id()).values().stream().mapToInt(hours -> hours).sum();
    }

    int getRemainingClassroomHours(ClassroomData data) {
        var spentHours = getClassroomSchedule(data.id()).size();
        return availableTimeSlots.size() - spentHours;
    }

    int getRemainingStudentHours(StudentData data) {
        var spentHours = getStudentSchedule(data.id()).size();
        return availableTimeSlots.size() - spentHours;
    }

    List<TimeSlot> getAvailableTimeSlots() {
        return availableTimeSlots;
    }

    List<TeacherData> getTeachersFor(CourseData course) {
        return specializationToTeachers.get(course.specializationId()).stream()
            .filter(teacher -> getRemainingTeacherHours(teacher) >= course.hoursPerWeek())
            .sorted(comparing(this::getRemainingTeacherHours).reversed())
            .toList();
    }

    List<ClassroomData> getClassroomsFor(CourseData course) {
        return specializationToClassrooms.get(course.specializationId()).stream()
            .filter(classroom -> getRemainingClassroomHours(classroom) >= course.hoursPerWeek())
            .sorted(comparing(this::getRemainingClassroomHours).reversed())
            .toList();
    }

    boolean teacherCanTeachAt(int teacherId, List<TimeSlot> slots) {
        var remainingHours = teacherRemainingDailyHours.get(teacherId);
        var hoursByDay = slots.stream().collect(groupingBy(TimeSlot::weekday, counting()));
        return hoursByDay.entrySet().stream()
            .allMatch(entry -> remainingHours.get(entry.getKey()) >= entry.getValue());
    }

    private void blockTeacherDay(int teacherId, Weekday day) {
        var teacherSchedule = getTeacherSchedule(teacherId);

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

    private boolean containsNone(Map<Integer, Map<TimeSlot, CourseData>> schedules, int id, List<TimeSlot> timeSlots) {
        var keys = schedules.computeIfAbsent(id, key -> new HashMap<>()).keySet();
        return timeSlots.stream().noneMatch(keys::contains);
    }

    private static Map<Weekday, Integer> generateHoursMap(TeacherData teacher) {
        return Arrays.stream(Weekday.values()).collect(toMap(identity(), day -> teacher.maxDailyHours()));
    }
}
