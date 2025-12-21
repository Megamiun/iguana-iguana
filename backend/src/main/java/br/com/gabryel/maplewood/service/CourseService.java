package br.com.gabryel.maplewood.service;

import br.com.gabryel.maplewood.model.Course;
import br.com.gabryel.maplewood.model.response.CourseResponse;
import br.com.gabryel.maplewood.model.response.CourseResponse.PrerequisiteInfo;
import br.com.gabryel.maplewood.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;

    public Page<CourseResponse> getCourses(Pageable pageable) {
        return courseRepository.findAll(pageable).map(this::toResponse);
    }

    private CourseResponse toResponse(Course course) {
        return new CourseResponse(
            course.getId(),
            course.getCode(),
            course.getName(),
            course.getDescription(),
            course.getCredits(),
            course.getHoursPerWeek(),
            course.getCourseType(),
            course.getGradeLevelMin(),
            course.getGradeLevelMax(),
            course.getSemesterOrder(),
            course.getSpecialization() != null ? course.getSpecialization().getName() : null,
            course.getPrerequisite() != null ? new PrerequisiteInfo(
                course.getPrerequisite().getId(),
                course.getPrerequisite().getCode(),
                course.getPrerequisite().getName()
            ) : null
        );
    }
}