package br.com.gabryel.maplewood.service.scheduler;

import br.com.gabryel.maplewood.config.TimeSchedulingConfig;
import br.com.gabryel.maplewood.model.SemesterType;
import br.com.gabryel.maplewood.model.response.ScheduleResponse;
import br.com.gabryel.maplewood.model.response.ScheduleResponse.CourseScheduleResponse;
import br.com.gabryel.maplewood.repository.SemesterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static br.com.gabryel.maplewood.model.SemesterType.FALL;
import static java.util.stream.Collectors.collectingAndThen;
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
            // TODO Merge consecutive slots
            section.timeSlots().stream().map(slot -> new ScheduleResponse.ScheduleDurationResponse(slot.weekday(), slot.slot(), slot.slot() + 1)).toList(),
            section.classroom().capacity() - section.students().size(),
            section.students().size()
        );
    }
}
