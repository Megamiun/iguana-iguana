package br.com.gabryel.maplewood.service;

import br.com.gabryel.maplewood.api.model.StudentResponse;
import br.com.gabryel.maplewood.api.model.StudentScheduleResponse;
import br.com.gabryel.maplewood.api.model.TimeSlotResponse;
import br.com.gabryel.maplewood.api.model.Weekday;
import br.com.gabryel.maplewood.exception.ScheduleNotFoundException;
import br.com.gabryel.maplewood.exception.SemesterNotFoundException;
import br.com.gabryel.maplewood.model.SemesterType;
import br.com.gabryel.maplewood.model.db.Student;
import br.com.gabryel.maplewood.repository.CourseSectionStudentRepository;
import br.com.gabryel.maplewood.repository.CourseSectionTimeSlotRepository;
import br.com.gabryel.maplewood.repository.SemesterRepository;
import br.com.gabryel.maplewood.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static br.com.gabryel.maplewood.model.SemesterType.FALL;

@Service
@RequiredArgsConstructor
public class StudentService {
    private final StudentRepository studentRepository;
    private final CourseSectionStudentRepository courseSectionStudentRepository;
    private final CourseSectionTimeSlotRepository courseSectionTimeSlotRepository;
    private final SemesterRepository semesterRepository;

    @Transactional(readOnly = true)
    public Page<StudentResponse> getStudents(Pageable pageable) {
        return studentRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public StudentScheduleResponse getStudentSchedule(Integer studentId, Integer year, SemesterType semesterType) {
        var student = studentRepository.findById(studentId)
            .orElseThrow(() -> new RuntimeException("Student not found with id: " + studentId));

        var semester = semesterRepository.findByYearAndOrderInYear(year, getOrderInYear(semesterType))
            .orElseThrow(() -> new SemesterNotFoundException(year, semesterType.name()));

        var enrollments = courseSectionStudentRepository.findByStudentIdAndSemesterId(studentId, semester.getId());

        var timeSlots = enrollments.stream().flatMap(enrollment -> {
            var section = enrollment.getCourseSection();

            var slots = courseSectionTimeSlotRepository.findByCourseSectionId(section.getId());
            var sectionEnrollments = courseSectionStudentRepository.findByCourseSectionId(section.getId());
            var filledSpots = sectionEnrollments.size();
            var availableSpots = section.getClassroom().getCapacity() - filledSpots;

            return slots.stream().map(slot -> new TimeSlotResponse()
                .weekday(Weekday.valueOf(slot.getWeekday().name()))
                .start(slot.getStartHour())
                .end(slot.getEndHour())
                .courseCode(section.getCourse().getCode())
                .courseName(section.getCourse().getName())
                .section(section.getSectionNumber())
                .classroom(section.getClassroom().getName())
                .classroomId(section.getClassroom().getId())
                .teacher(section.getTeacher().getFirstName() + " " + section.getTeacher().getLastName())
                .teacherId(section.getTeacher().getId())
                .filledSpots(filledSpots)
                .availableSpots(availableSpots)
            );
        }).toList();

        return new StudentScheduleResponse()
            .studentId(student.getId())
            .studentName(student.getFirstName() + " " + student.getLastName())
            .timeSlots(timeSlots);
    }

    private StudentResponse toResponse(Student student) {
        return new StudentResponse()
            .id(student.getId())
            .firstName(student.getFirstName())
            .lastName(student.getLastName())
            .email(student.getEmail())
            .gradeLevel(student.getGradeLevel())
            .enrollmentYear(student.getEnrollmentYear())
            .expectedGraduationYear(student.getExpectedGraduationYear())
            .status(student.getStatus());
    }

    private static int getOrderInYear(SemesterType semesterType) {
        return semesterType == FALL ? 1 : 2;
    }
}
