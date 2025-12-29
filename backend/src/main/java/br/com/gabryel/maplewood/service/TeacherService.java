package br.com.gabryel.maplewood.service;

import br.com.gabryel.maplewood.api.model.TeacherResponse;
import br.com.gabryel.maplewood.api.model.TeacherScheduleResponse;
import br.com.gabryel.maplewood.exception.SemesterNotFoundException;
import br.com.gabryel.maplewood.mapper.ResponseMapper;
import br.com.gabryel.maplewood.mapper.ScheduleMapper;
import br.com.gabryel.maplewood.model.SemesterType;
import br.com.gabryel.maplewood.repository.CourseSectionRepository;
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
    private final SemesterRepository semesterRepository;
    private final ResponseMapper responseMapper;
    private final ScheduleMapper scheduleMapper;

    @Transactional(readOnly = true)
    public Page<TeacherResponse> getTeachers(Pageable pageable) {
        return teacherRepository.findAll(pageable).map(responseMapper::toTeacherResponse);
    }

    @Transactional(readOnly = true)
    public TeacherScheduleResponse getTeacherSchedule(Integer teacherId, Integer year, SemesterType semesterType) {
        var teacher = teacherRepository.findById(teacherId)
            .orElseThrow(() -> new RuntimeException("Teacher not found with id: " + teacherId));

        var semester = semesterRepository.findByYearAndOrderInYear(year, getOrderInYear(semesterType))
            .orElseThrow(() -> new SemesterNotFoundException(year, semesterType.name()));

        var sections = courseSectionRepository.findBySemesterIdAndTeacherId(semester.getId(), teacherId);

        var timeSlots = sections.stream()
            .flatMap(scheduleMapper::toTimeSlotResponses)
            .toList();

        return new TeacherScheduleResponse()
            .teacherId(teacher.getId())
            .teacherName(responseMapper.formatFullName(teacher.getFirstName(), teacher.getLastName()))
            .timeSlots(timeSlots);
    }

    private static int getOrderInYear(SemesterType semesterType) {
        return semesterType == FALL ? 1 : 2;
    }
}
