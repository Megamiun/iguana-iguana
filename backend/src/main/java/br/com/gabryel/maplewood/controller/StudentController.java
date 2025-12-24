package br.com.gabryel.maplewood.controller;

import br.com.gabryel.maplewood.api.StudentsApi;
import br.com.gabryel.maplewood.api.model.SemesterSeason;
import br.com.gabryel.maplewood.api.model.StudentPageResponse;
import br.com.gabryel.maplewood.api.model.StudentResponse;
import br.com.gabryel.maplewood.api.model.StudentScheduleResponse;
import br.com.gabryel.maplewood.model.SemesterType;
import br.com.gabryel.maplewood.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StudentController implements StudentsApi {
    private final StudentService studentService;

    @Override
    public ResponseEntity<StudentPageResponse> getStudents(Integer page, Integer size) {
        var studentPage = studentService.getStudents(PageRequest.of(page, size));
        return ResponseEntity.ok(toResponse(studentPage));
    }

    @Override
    public ResponseEntity<StudentScheduleResponse> getStudentSchedule(Integer id, Integer year, SemesterSeason semesterType) {
        var semesterCore = SemesterType.valueOf(semesterType.name());
        return ResponseEntity.ok(studentService.getStudentSchedule(id, year, semesterCore));
    }

    private StudentPageResponse toResponse(Page<StudentResponse> page) {
        return new StudentPageResponse()
            .content(page.getContent())
            .page(page.getNumber())
            .size(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages());
    }
}
