package br.com.gabryel.maplewood.controller;

import br.com.gabryel.maplewood.model.response.CourseResponse;
import br.com.gabryel.maplewood.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {
    private final CourseService courseService;

    @GetMapping
    public Page<CourseResponse> getCourses(@PageableDefault(size = 20) Pageable pageable) {
        return courseService.getCourses(pageable);
    }
}