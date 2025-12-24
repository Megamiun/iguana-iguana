package br.com.gabryel.maplewood.controller;

import br.com.gabryel.maplewood.api.ClassroomsApi;
import br.com.gabryel.maplewood.api.model.ClassroomPageResponse;
import br.com.gabryel.maplewood.api.model.ClassroomResponse;
import br.com.gabryel.maplewood.api.model.ClassroomScheduleResponse;
import br.com.gabryel.maplewood.api.model.SemesterSeason;
import br.com.gabryel.maplewood.model.SemesterType;
import br.com.gabryel.maplewood.service.ClassroomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ClassroomController implements ClassroomsApi {
    private final ClassroomService classroomService;

    @Override
    public ResponseEntity<ClassroomPageResponse> getClassrooms(Integer page, Integer size) {
        var classroomPage = classroomService.getClassrooms(PageRequest.of(page, size));
        return ResponseEntity.ok(toResponse(classroomPage));
    }

    @Override
    public ResponseEntity<ClassroomScheduleResponse> getClassroomSchedule(Integer id, Integer year, SemesterSeason semesterType) {
        var semesterCore = SemesterType.valueOf(semesterType.name());
        return ResponseEntity.ok(classroomService.getClassroomSchedule(id, year, semesterCore));
    }

    private ClassroomPageResponse toResponse(Page<ClassroomResponse> page) {
        return new ClassroomPageResponse()
            .content(page.getContent())
            .page(page.getNumber())
            .size(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages());
    }
}
