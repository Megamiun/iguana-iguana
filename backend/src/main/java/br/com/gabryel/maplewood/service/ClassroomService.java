package br.com.gabryel.maplewood.service;

import br.com.gabryel.maplewood.api.model.ClassroomResponse;
import br.com.gabryel.maplewood.api.model.ClassroomScheduleResponse;
import br.com.gabryel.maplewood.exception.SemesterNotFoundException;
import br.com.gabryel.maplewood.mapper.ResponseMapper;
import br.com.gabryel.maplewood.mapper.ScheduleMapper;
import br.com.gabryel.maplewood.model.SemesterType;
import br.com.gabryel.maplewood.repository.ClassroomRepository;
import br.com.gabryel.maplewood.repository.CourseSectionRepository;
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
    private final SemesterRepository semesterRepository;
    private final ResponseMapper responseMapper;
    private final ScheduleMapper scheduleMapper;

    @Transactional(readOnly = true)
    public Page<ClassroomResponse> getClassrooms(Pageable pageable) {
        return classroomRepository.findAll(pageable).map(responseMapper::toClassroomResponse);
    }

    @Transactional(readOnly = true)
    public ClassroomScheduleResponse getClassroomSchedule(Integer classroomId, Integer year, SemesterType semesterType) {
        var classroom = classroomRepository.findById(classroomId)
            .orElseThrow(() -> new RuntimeException("Classroom not found with id: " + classroomId));

        var semester = semesterRepository.findByYearAndOrderInYear(year, getOrderInYear(semesterType))
            .orElseThrow(() -> new SemesterNotFoundException(year, semesterType.name()));

        var sections = courseSectionRepository.findBySemesterIdAndClassroomId(semester.getId(), classroomId);

        var timeSlots = sections.stream()
            .flatMap(scheduleMapper::toTimeSlotResponses)
            .toList();

        return new ClassroomScheduleResponse()
            .classroomId(classroom.getId())
            .classroomName(classroom.getName())
            .timeSlots(timeSlots);
    }

    private static int getOrderInYear(SemesterType semesterType) {
        return semesterType == FALL ? 1 : 2;
    }
}
