package br.com.gabryel.maplewood.mapper;

import br.com.gabryel.maplewood.api.model.ClassroomResponse;
import br.com.gabryel.maplewood.api.model.CourseResponse;
import br.com.gabryel.maplewood.api.model.CourseType;
import br.com.gabryel.maplewood.api.model.PrerequisiteInfo;
import br.com.gabryel.maplewood.api.model.SemesterResponse;
import br.com.gabryel.maplewood.api.model.SemesterSeason;
import br.com.gabryel.maplewood.api.model.StudentResponse;
import br.com.gabryel.maplewood.api.model.TeacherResponse;
import br.com.gabryel.maplewood.model.db.Classroom;
import br.com.gabryel.maplewood.model.db.Course;
import br.com.gabryel.maplewood.model.db.Semester;
import br.com.gabryel.maplewood.model.db.Student;
import br.com.gabryel.maplewood.model.db.Teacher;
import org.springframework.stereotype.Component;

@Component
public class ResponseMapper {
    public StudentResponse toStudentResponse(Student student) {
        return new StudentResponse()
            .id(student.getId())
            .firstName(student.getFirstName())
            .lastName(student.getLastName())
            .fullName(formatFullName(student.getFirstName(), student.getLastName()))
            .email(student.getEmail())
            .gradeLevel(student.getGradeLevel())
            .enrollmentYear(student.getEnrollmentYear())
            .expectedGraduationYear(student.getExpectedGraduationYear())
            .status(student.getStatus());
    }

    public TeacherResponse toTeacherResponse(Teacher teacher) {
        return new TeacherResponse()
            .id(teacher.getId())
            .firstName(teacher.getFirstName())
            .lastName(teacher.getLastName())
            .fullName(formatFullName(teacher.getFirstName(), teacher.getLastName()))
            .email(teacher.getEmail())
            .specializationName(teacher.getSpecialization() != null ? teacher.getSpecialization().getName() : null)
            .maxDailyHours(teacher.getMaxDailyHours());
    }

    public ClassroomResponse toClassroomResponse(Classroom classroom) {
        return new ClassroomResponse()
            .id(classroom.getId())
            .name(classroom.getName())
            .roomTypeName(classroom.getRoomType() != null ? classroom.getRoomType().getName() : null)
            .equipment(classroom.getEquipment())
            .capacity(classroom.getCapacity());
    }

    public SemesterResponse toSemesterResponse(Semester semester) {
        var semesterSeason = semester.getOrderInYear() == 1 ? SemesterSeason.FALL : SemesterSeason.SPRING;
        return new SemesterResponse()
            .id(semester.getId())
            .name(semester.getName())
            .year(semester.getYear())
            .semester(semesterSeason)
            .isActive(semester.getIsActive());
    }

    public CourseResponse toCourseResponse(Course course) {
        var semester = course.getSemesterOrder() == 1 ? SemesterSeason.FALL : SemesterSeason.SPRING;

        var response = new CourseResponse()
            .id(course.getId())
            .code(course.getCode())
            .name(course.getName())
            .description(course.getDescription())
            .credits(course.getCredits())
            .hoursPerWeek(course.getHoursPerWeek())
            .gradeLevelMin(course.getGradeLevelMin())
            .gradeLevelMax(course.getGradeLevelMax())
            .courseType(CourseType.valueOf(course.getCourseType().name()))
            .semester(semester)
            .specializationName(course.getSpecialization() != null ? course.getSpecialization().getName() : null);

        if (course.getPrerequisite() != null) {
            response.prerequisite(toPrerequisiteInfo(course.getPrerequisite()));
        }

        return response;
    }

    public PrerequisiteInfo toPrerequisiteInfo(Course prerequisite) {
        return new PrerequisiteInfo()
            .id(prerequisite.getId())
            .code(prerequisite.getCode())
            .name(prerequisite.getName());
    }

    public String formatFullName(String firstName, String lastName) {
        return firstName + " " + lastName;
    }
}
