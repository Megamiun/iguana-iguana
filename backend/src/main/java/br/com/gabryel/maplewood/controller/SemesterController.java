package br.com.gabryel.maplewood.controller;

import br.com.gabryel.maplewood.api.SemestersApi;
import br.com.gabryel.maplewood.api.model.SemesterPageResponse;
import br.com.gabryel.maplewood.api.model.SemesterResponse;
import br.com.gabryel.maplewood.service.SemesterService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SemesterController implements SemestersApi {
    private final SemesterService semesterService;

    @Override
    public ResponseEntity<SemesterPageResponse> getSemesters() {
        var semesterPage = semesterService.getSemesters();
        return ResponseEntity.ok(toResponse(semesterPage));
    }

    private SemesterPageResponse toResponse(Page<SemesterResponse> page) {
        return new SemesterPageResponse()
            .content(page.getContent())
            .page(page.getNumber())
            .size(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages());
    }
}
