package br.com.gabryel.maplewood.service.scheduler;

import br.com.gabryel.maplewood.model.SemesterType;
import br.com.gabryel.maplewood.model.db.Semester;
import br.com.gabryel.maplewood.model.response.ScheduleResponse;
import br.com.gabryel.maplewood.repository.SemesterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

import static br.com.gabryel.maplewood.model.SemesterType.FALL;

@Service
@RequiredArgsConstructor
public class Scheduler {
    private final SemesterRepository semesterRepository;
    private final CourseDataService courseDataService;
    private final StudentDataService studentDataService;
    private final TeacherDataService teacherDataService;
    private final ClassroomDataService classroomDataService;

    public ScheduleResponse generateSchedule(SemesterType semesterType, int year) {
        // Preloading all the data, so we can simplify models and avoid N+1 issues
        var currentSemester = semesterRepository.findByYearAndOrderInYear(year, getOrderInYear(semesterType))
            .orElseThrow(() -> new IllegalStateException("Semester not found"));

        var courses = courseDataService.getCoursesFor(currentSemester);
        var students = studentDataService.getStudents();
        var teachers = teacherDataService.getTeachers();
        var classrooms = classroomDataService.getClassrooms();

        return generateSchedule(currentSemester, courses, students, teachers, classrooms);
    }

    private ScheduleResponse generateSchedule(
        Semester currentSemester,
        Map<Integer, CourseDataService.CourseData> courses,
        Map<Integer, StudentDataService.StudentData> students,
        Map<Integer, TeacherDataService.TeacherData> teachers,
        Map<Integer, ClassroomDataService.ClassroomData> classrooms
    ) {
        return null;
    }

    private static int getOrderInYear(SemesterType semesterType) {
        if (semesterType == FALL) {
            return 1;
        }
        return 2;
    }
}
