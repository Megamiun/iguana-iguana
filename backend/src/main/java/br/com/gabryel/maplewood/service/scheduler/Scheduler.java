package br.com.gabryel.maplewood.service.scheduler;

import br.com.gabryel.maplewood.api.model.CourseScheduleResponse;
import br.com.gabryel.maplewood.api.model.ScheduleDurationResponse;
import br.com.gabryel.maplewood.api.model.ScheduleResponse;
import br.com.gabryel.maplewood.api.model.Weekday;
import br.com.gabryel.maplewood.config.TimeSchedulingConfig;
import br.com.gabryel.maplewood.exception.ScheduleAlreadyExistsException;
import br.com.gabryel.maplewood.exception.ScheduleNotFoundException;
import br.com.gabryel.maplewood.exception.SemesterNotFoundException;
import br.com.gabryel.maplewood.model.SemesterType;
import br.com.gabryel.maplewood.model.dto.CourseSectionDto;
import br.com.gabryel.maplewood.model.dto.TimeRange;
import br.com.gabryel.maplewood.repository.SemesterRepository;
import br.com.gabryel.maplewood.service.scheduler.data.ClassroomDataService;
import br.com.gabryel.maplewood.service.scheduler.data.CourseDataService;
import br.com.gabryel.maplewood.service.scheduler.data.StudentDataService;
import br.com.gabryel.maplewood.service.scheduler.data.TeacherDataService;
import br.com.gabryel.maplewood.service.scheduler.data.ScheduleDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static br.com.gabryel.maplewood.model.SemesterType.FALL;

@Service
@RequiredArgsConstructor
public class Scheduler {
    private final SemesterRepository semesterRepository;
    private final CourseDataService courseDataService;
    private final StudentDataService studentDataService;
    private final TeacherDataService teacherDataService;
    private final ClassroomDataService classroomDataService;
    private final ScheduleDataService scheduleService;

    private final TimeSchedulingConfig timeSchedulingConfig;

    public ScheduleResponse loadSchedule(SemesterType semesterType, int year) {
        var semester = semesterRepository.findByYearAndOrderInYear(year, getOrderInYear(semesterType))
            .orElseThrow(() -> new SemesterNotFoundException(year, semesterType.name()));

        var sections = scheduleService.findSchedule(semester.getId());

        if (sections.isEmpty()) {
            throw new ScheduleNotFoundException(year, semesterType.name());
        }

        return new ScheduleResponse()
            .courses(sections.stream()
                .map(this::toResponse)
                .toList());
    }

    public void deleteSchedule(SemesterType semesterType, int year) {
        var semester = semesterRepository.findByYearAndOrderInYear(year, getOrderInYear(semesterType))
            .orElseThrow(() -> new SemesterNotFoundException(year, semesterType.name()));

        if (!scheduleService.hasSchedule(semester.getId())) {
            throw new ScheduleNotFoundException(year, semesterType.name());
        }

        scheduleService.deleteSchedule(semester.getId());
    }

    public ScheduleResponse generateSchedule(SemesterType semesterType, int year) {
        var semester = semesterRepository.findByYearAndOrderInYear(year, getOrderInYear(semesterType))
            .orElseThrow(() -> new SemesterNotFoundException(year, semesterType.name()));

        if (scheduleService.hasSchedule(semester.getId())) {
            throw new ScheduleAlreadyExistsException(semesterType, year);
        }

        // Preloading all the data, so we can simplify models and avoid N+1 issues
        var courses = courseDataService.getCoursesFor(semesterType);
        var students = studentDataService.getStudents();
        var teachers = teacherDataService.getTeachers();
        var classrooms = classroomDataService.getClassrooms();

        var calculator = new ScheduleCalculator(timeSchedulingConfig);
        var sections = calculator.generateSchedule(courses, students, teachers, classrooms);

        scheduleService.persistSchedule(semester.getId(), sections);

        var coursesResponse = sections.stream().map(this::toResponse).toList();
        return new ScheduleResponse().courses(coursesResponse);
    }

    private static int getOrderInYear(SemesterType semesterType) {
        if (semesterType == FALL) {
            return 1;
        }
        return 2;
    }

    private CourseScheduleResponse toResponse(CourseSectionDto section) {
        return new CourseScheduleResponse()
            .code(section.course().code())
            .name(section.course().name())
            .section(section.section())
            .teacher(section.teacher().firstName() + " " + section.teacher().lastName())
            .classroom(section.classroom().name())
            .schedule(section.timeRanges().stream().map(this::toResponse).toList())
            .availableSpots(section.classroom().capacity() - section.students().size())
            .filledSpots(section.students().size());
    }

    private ScheduleDurationResponse toResponse(TimeRange range) {
        return new ScheduleDurationResponse()
            .weekday(Weekday.valueOf(range.weekday().name()))
            .start(range.start())
            .end(range.end());
    }
}
