package br.com.gabryel.maplewood.service.scheduler;

import br.com.gabryel.maplewood.config.TimeSchedulingConfig;
import br.com.gabryel.maplewood.model.SemesterType;
import br.com.gabryel.maplewood.model.dto.CourseSectionDto;
import br.com.gabryel.maplewood.model.dto.TimeRange;
import br.com.gabryel.maplewood.model.response.ScheduleResponse;
import br.com.gabryel.maplewood.model.response.ScheduleResponse.CourseScheduleResponse;
import br.com.gabryel.maplewood.model.response.ScheduleResponse.ScheduleDurationResponse;
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
    private final SchedulePersistenceService persistenceService;

    public ScheduleResponse generateSchedule(SemesterType semesterType, int year) {
        var semester = semesterRepository.findByYearAndOrderInYear(year, getOrderInYear(semesterType))
            .orElseThrow(() -> new IllegalStateException("Semester not found"));

        // Preloading all the data, so we can simplify models and avoid N+1 issues
        var courses = courseDataService.getCoursesFor(semesterType);
        var students = studentDataService.getStudents();
        var teachers = teacherDataService.getTeachers();
        var classrooms = classroomDataService.getClassrooms();

        var calculator = new ScheduleCalculator(timeSchedulingConfig, courses, students, teachers, classrooms);
        var sections = calculator.generateSchedule();

        persistenceService.persistSchedule(semester.getId(), sections);

        return sections.stream()
            .map(this::toResponse)
            .collect(collectingAndThen(toList(), ScheduleResponse::new));
    }

    private static int getOrderInYear(SemesterType semesterType) {
        if (semesterType == FALL) {
            return 1;
        }
        return 2;
    }

    private CourseScheduleResponse toResponse(CourseSectionDto section) {
        return new CourseScheduleResponse(
            section.course().code(),
            section.course().name(),
            section.section(),
            section.teacher().firstName() + " " + section.teacher().lastName(),
            section.classroom().name(),
            section.timeRanges().stream().map(this::toResponse).toList(),
            section.classroom().capacity() - section.students().size(),
            section.students().size()
        );
    }

    private ScheduleDurationResponse toResponse(TimeRange range) {
        return new ScheduleDurationResponse(range.weekday(), range.start(), range.end());
    }
}
