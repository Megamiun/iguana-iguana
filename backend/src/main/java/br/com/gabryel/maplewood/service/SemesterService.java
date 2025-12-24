package br.com.gabryel.maplewood.service;

import br.com.gabryel.maplewood.api.model.SemesterResponse;
import br.com.gabryel.maplewood.api.model.SemesterSeason;
import br.com.gabryel.maplewood.model.db.Semester;
import br.com.gabryel.maplewood.repository.SemesterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SemesterService {
    private final SemesterRepository semesterRepository;

    public Page<SemesterResponse> getSemesters() {
        var semesters = semesterRepository.findAll().stream().map(this::toResponse).toList();
        return new PageImpl<>(semesters, PageRequest.of(0, semesters.size()), semesters.size());
    }

    private SemesterResponse toResponse(Semester semester) {
        var semesterSeason = semester.getOrderInYear() == 1 ? SemesterSeason.FALL : SemesterSeason.SPRING;
        return new SemesterResponse()
            .id(semester.getId())
            .name(semester.getName())
            .year(semester.getYear())
            .semester(semesterSeason)
            .isActive(semester.getIsActive());
    }
}