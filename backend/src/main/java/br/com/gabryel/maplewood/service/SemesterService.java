package br.com.gabryel.maplewood.service;

import br.com.gabryel.maplewood.api.model.SemesterResponse;
import br.com.gabryel.maplewood.mapper.ResponseMapper;
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
    private final ResponseMapper responseMapper;

    public Page<SemesterResponse> getSemesters() {
        var semesters = semesterRepository.findAll().stream().map(responseMapper::toSemesterResponse).toList();
        return new PageImpl<>(semesters, PageRequest.of(0, semesters.size()), semesters.size());
    }
}