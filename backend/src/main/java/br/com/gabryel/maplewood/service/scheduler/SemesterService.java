package br.com.gabryel.maplewood.service.scheduler;

import br.com.gabryel.maplewood.model.db.Semester;
import br.com.gabryel.maplewood.model.response.PageResponse;
import br.com.gabryel.maplewood.model.response.SemesterResponse;
import br.com.gabryel.maplewood.repository.SemesterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static br.com.gabryel.maplewood.model.SemesterType.FALL;
import static br.com.gabryel.maplewood.model.SemesterType.SPRING;

@Service
@RequiredArgsConstructor
public class SemesterService {
    private final SemesterRepository semesterRepository;

    public PageResponse<SemesterResponse> getSemestersPageForScheduling() {
        var semesters = getSemestersForScheduling();
        return new PageResponse<>(semesters, 0, semesters.size(), semesters.size(), semesters.isEmpty() ? 0 : 1);
    }

    private List<SemesterResponse> getSemestersForScheduling() {
        return semesterRepository.findByIsActiveTrue()
            .map(this::toResponse)
            .map(List::of)
            .orElseGet(List::of);
    }

    private SemesterResponse toResponse(Semester semester) {
        var semesterEnum = semester.getOrderInYear() == 1 ? FALL : SPRING;

        return new SemesterResponse(
            semester.getId(),
            semester.getName(),
            semester.getYear(),
            semesterEnum
        );
    }
}