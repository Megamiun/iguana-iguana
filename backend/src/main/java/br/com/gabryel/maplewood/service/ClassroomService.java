package br.com.gabryel.maplewood.service;

import br.com.gabryel.maplewood.api.model.ClassroomResponse;
import br.com.gabryel.maplewood.api.model.ClassroomScheduleResponse;
import br.com.gabryel.maplewood.api.model.TimeSlotResponse;
import br.com.gabryel.maplewood.api.model.Weekday;
import br.com.gabryel.maplewood.exception.ScheduleNotFoundException;
import br.com.gabryel.maplewood.exception.SemesterNotFoundException;
import br.com.gabryel.maplewood.model.SemesterType;
import br.com.gabryel.maplewood.model.db.Classroom;
import br.com.gabryel.maplewood.repository.ClassroomRepository;
import br.com.gabryel.maplewood.repository.CourseSectionRepository;
import br.com.gabryel.maplewood.repository.CourseSectionStudentRepository;
import br.com.gabryel.maplewood.repository.CourseSectionTimeSlotRepository;
import br.com.gabryel.maplewood.repository.SemesterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static br.com.gabryel.maplewood.model.SemesterType.FALL;

@Service
@RequiredArgsConstructor
public class ClassroomService {
    private final ClassroomRepository classroomRepository;
    private final CourseSectionRepository courseSectionRepository;
    private final CourseSectionStudentRepository courseSectionStudentRepository;
    private final CourseSectionTimeSlotRepository courseSectionTimeSlotRepository;
    private final SemesterRepository semesterRepository;

    @Transactional(readOnly = true)
    public Page<ClassroomResponse> getClassrooms(Pageable pageable) {
        return classroomRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ClassroomScheduleResponse getClassroomSchedule(Integer classroomId, Integer year, SemesterType semesterType) {
        var classroom = classroomRepository.findById(classroomId)
            .orElseThrow(() -> new RuntimeException("Classroom not found with id: " + classroomId));

        var semester = semesterRepository.findByYearAndOrderInYear(year, getOrderInYear(semesterType))
            .orElseThrow(() -> new SemesterNotFoundException(year, semesterType.name()));

        var sections = courseSectionRepository.findBySemesterIdAndClassroomId(semester.getId(), classroomId);

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

        return new ClassroomScheduleResponse()
            .classroomId(classroom.getId())
            .classroomName(classroom.getName())
            .timeSlots(timeSlots);
    }

    private ClassroomResponse toResponse(Classroom classroom) {
        return new ClassroomResponse()
            .id(classroom.getId())
            .name(classroom.getName())
            .roomTypeName(classroom.getRoomType() != null ? classroom.getRoomType().getName() : null)
            .equipment(classroom.getEquipment())
            .capacity(classroom.getCapacity());
    }

    private static int getOrderInYear(SemesterType semesterType) {
        return semesterType == FALL ? 1 : 2;
    }
}
