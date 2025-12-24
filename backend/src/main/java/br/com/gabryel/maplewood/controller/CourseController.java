package br.com.gabryel.maplewood.controller;

import br.com.gabryel.maplewood.api.CoursesApi;
import br.com.gabryel.maplewood.api.model.CoursePageResponse;
import br.com.gabryel.maplewood.api.model.CourseResponse;
import br.com.gabryel.maplewood.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CourseController implements CoursesApi {
    private final CourseService courseService;

    @Override
    public ResponseEntity<CoursePageResponse> getCourses(Integer page, Integer size) {
        var coursePage = courseService.getCourses(PageRequest.of(page, size));
        return ResponseEntity.ok(toResponse(coursePage));
    }

    private CoursePageResponse toResponse(Page<CourseResponse> page) {
        return new CoursePageResponse()
            .content(page.getContent())
            .page(page.getNumber())
            .size(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages());
    }
}