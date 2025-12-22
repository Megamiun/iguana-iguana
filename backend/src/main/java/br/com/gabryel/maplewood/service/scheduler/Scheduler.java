package br.com.gabryel.maplewood.service.scheduler;

import br.com.gabryel.maplewood.config.TimeSchedulingConfig;
import br.com.gabryel.maplewood.model.SemesterType;
import br.com.gabryel.maplewood.model.Weekday;
import br.com.gabryel.maplewood.model.response.ScheduleResponse;
import br.com.gabryel.maplewood.model.response.ScheduleResponse.CourseScheduleResponse;
import br.com.gabryel.maplewood.model.response.ScheduleResponse.ScheduleDurationResponse;
import br.com.gabryel.maplewood.repository.SemesterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static br.com.gabryel.maplewood.model.SemesterType.FALL;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class Scheduler {
    private final SemesterRepository semesterRepository;
    private final CourseDataService courseDataService;
    private final StudentDataService studentDataService;
    private final TeacherDataService teacherDataService;
    private final ClassroomDataService classroomDataService;
    private final TimeSchedulingConfig timeSchedulingConfig;

    public ScheduleResponse generateSchedule(SemesterType semesterType, int year) {
        semesterRepository.findByYearAndOrderInYear(year, getOrderInYear(semesterType))
            .orElseThrow(() -> new IllegalStateException("Semester not found"));

        // Preloading all the data, so we can simplify models and avoid N+1 issues
        var courses = courseDataService.getCoursesFor(semesterType);
        var students = studentDataService.getStudents();
        var teachers = teacherDataService.getTeachers();
        var classrooms = classroomDataService.getClassrooms();

        var calculator = new ScheduleCalculator(timeSchedulingConfig, courses, students, teachers, classrooms);
        return calculator.generateSchedule().stream()
            .map(this::toResponse)
            .collect(collectingAndThen(toList(), ScheduleResponse::new));
    }

    private static int getOrderInYear(SemesterType semesterType) {
        if (semesterType == FALL) {
            return 1;
        }
        return 2;
    }

    private CourseScheduleResponse toResponse(CourseSection section) {
        return new CourseScheduleResponse(
            section.course().name(),
            section.section(),
            section.teacher().name(),
            section.classroom().name(),
            mergeConsecutiveSlots(section.timeSlots()),
            section.classroom().capacity() - section.students().size(),
            section.students().size()
        );
    }

    private List<ScheduleDurationResponse> mergeConsecutiveSlots(List<TimeSlot> timeSlots) {
        var slotsByDay = timeSlots.stream().collect(groupingBy(TimeSlot::weekday, toList()));

        return slotsByDay.entrySet().stream()
            .flatMap(entry -> mergeConsecutiveSlots(entry.getKey(), entry.getValue()).stream())
            .sorted(comparing(ScheduleDurationResponse::getWeekday).thenComparing(ScheduleDurationResponse::getStart))
            .toList();
    }

    private List<ScheduleDurationResponse> mergeConsecutiveSlots(Weekday weekday, List<TimeSlot> slots) {
        if (slots.isEmpty()) return List.of();

        var merged = new ArrayList<ScheduleDurationResponse>();
        var sortedSlots = slots.stream().sorted(comparing(TimeSlot::slot)).toList();

        for (var current : sortedSlots) {
            var currentSlot = current.slot();
            if (!merged.isEmpty() && currentSlot <= merged.getLast().getEnd()) {
                merged.getLast().setEnd(currentSlot + 1);
            } else {
                merged.add(new ScheduleDurationResponse(weekday, currentSlot, currentSlot + 1));
            }
        }

        return merged;
    }
}
