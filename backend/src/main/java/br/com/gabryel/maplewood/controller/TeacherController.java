package br.com.gabryel.maplewood.controller;

import br.com.gabryel.maplewood.api.TeachersApi;
import br.com.gabryel.maplewood.api.model.SemesterSeason;
import br.com.gabryel.maplewood.api.model.TeacherPageResponse;
import br.com.gabryel.maplewood.api.model.TeacherResponse;
import br.com.gabryel.maplewood.api.model.TeacherScheduleResponse;
import br.com.gabryel.maplewood.model.SemesterType;
import br.com.gabryel.maplewood.service.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TeacherController implements TeachersApi {
    private final TeacherService teacherService;

    @Override
    public ResponseEntity<TeacherPageResponse> getTeachers(Integer page, Integer size) {
        var teacherPage = teacherService.getTeachers(PageRequest.of(page, size));
        return ResponseEntity.ok(toResponse(teacherPage));
    }

    @Override
    public ResponseEntity<TeacherScheduleResponse> getTeacherSchedule(Integer teacherId, Integer year, SemesterSeason semesterType) {
        var semesterCore = SemesterType.valueOf(semesterType.name());
        return ResponseEntity.ok(teacherService.getTeacherSchedule(teacherId, year, semesterCore));
    }

    private TeacherPageResponse toResponse(Page<TeacherResponse> page) {
        return new TeacherPageResponse()
            .content(page.getContent())
            .page(page.getNumber())
            .size(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages());
    }
}
