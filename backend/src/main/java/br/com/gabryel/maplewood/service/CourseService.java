package br.com.gabryel.maplewood.service;

import br.com.gabryel.maplewood.api.model.CourseResponse;
import br.com.gabryel.maplewood.mapper.ResponseMapper;
import br.com.gabryel.maplewood.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;
    private final ResponseMapper responseMapper;

    public Page<CourseResponse> getCourses(Pageable pageable) {
        return courseRepository.findAll(pageable).map(responseMapper::toCourseResponse);
    }
}
