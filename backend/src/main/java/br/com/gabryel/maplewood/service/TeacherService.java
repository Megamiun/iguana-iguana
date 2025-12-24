package br.com.gabryel.maplewood.service;

import br.com.gabryel.maplewood.api.model.TeacherResponse;
import br.com.gabryel.maplewood.api.model.TeacherScheduleResponse;
import br.com.gabryel.maplewood.api.model.TimeSlotResponse;
import br.com.gabryel.maplewood.api.model.Weekday;
import br.com.gabryel.maplewood.exception.ScheduleNotFoundException;
import br.com.gabryel.maplewood.exception.SemesterNotFoundException;
import br.com.gabryel.maplewood.model.SemesterType;
import br.com.gabryel.maplewood.model.db.Teacher;
import br.com.gabryel.maplewood.repository.CourseSectionRepository;
import br.com.gabryel.maplewood.repository.CourseSectionStudentRepository;
import br.com.gabryel.maplewood.repository.CourseSectionTimeSlotRepository;
import br.com.gabryel.maplewood.repository.SemesterRepository;
import br.com.gabryel.maplewood.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static br.com.gabryel.maplewood.model.SemesterType.FALL;

@Service
@RequiredArgsConstructor
public class TeacherService {
    private final TeacherRepository teacherRepository;
    private final CourseSectionRepository courseSectionRepository;
    private final CourseSectionStudentRepository courseSectionStudentRepository;
    private final CourseSectionTimeSlotRepository courseSectionTimeSlotRepository;
    private final SemesterRepository semesterRepository;

    @Transactional(readOnly = true)
    public Page<TeacherResponse> getTeachers(Pageable pageable) {
        return teacherRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public TeacherScheduleResponse getTeacherSchedule(Integer teacherId, Integer year, SemesterType semesterType) {
        var teacher = teacherRepository.findById(teacherId)
            .orElseThrow(() -> new RuntimeException("Teacher not found with id: " + teacherId));

        var semester = semesterRepository.findByYearAndOrderInYear(year, getOrderInYear(semesterType))
            .orElseThrow(() -> new SemesterNotFoundException(year, semesterType.name()));

        var sections = courseSectionRepository.findBySemesterIdAndTeacherId(semester.getId(), teacherId);

        var timeSlots = sections.stream().flatMap(section -> {
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

        return new TeacherScheduleResponse()
            .teacherId(teacher.getId())
            .teacherName(teacher.getFirstName() + " " + teacher.getLastName())
            .timeSlots(timeSlots);
    }

    private TeacherResponse toResponse(Teacher teacher) {
        return new TeacherResponse()
            .id(teacher.getId())
            .firstName(teacher.getFirstName())
            .lastName(teacher.getLastName())
            .email(teacher.getEmail())
            .specializationName(teacher.getSpecialization() != null ? teacher.getSpecialization().getName() : null)
            .maxDailyHours(teacher.getMaxDailyHours());
    }

    private static int getOrderInYear(SemesterType semesterType) {
        return semesterType == FALL ? 1 : 2;
    }
}
