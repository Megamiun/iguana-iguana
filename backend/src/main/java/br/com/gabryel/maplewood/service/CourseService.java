package br.com.gabryel.maplewood.service;

import br.com.gabryel.maplewood.api.model.CourseResponse;
import br.com.gabryel.maplewood.api.model.CourseType;
import br.com.gabryel.maplewood.api.model.PrerequisiteInfo;
import br.com.gabryel.maplewood.model.db.Course;
import br.com.gabryel.maplewood.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import static br.com.gabryel.maplewood.api.model.SemesterSeason.FALL;
import static br.com.gabryel.maplewood.api.model.SemesterSeason.SPRING;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;

    public Page<CourseResponse> getCourses(Pageable pageable) {
        return courseRepository.findAll(pageable).map(this::toResponse);
    }

    private CourseResponse toResponse(Course course) {
        var semester = course.getSemesterOrder() == 1 ? FALL : SPRING;

        return new CourseResponse()
            .id(course.getId())
            .code(course.getCode())
            .name(course.getName())
            .description(course.getDescription())
            .credits(course.getCredits())
            .hoursPerWeek(course.getHoursPerWeek())
            .courseType(CourseType.valueOf(course.getCourseType().name()))
            .gradeLevelMin(course.getGradeLevelMin())
            .gradeLevelMax(course.getGradeLevelMax())
            .semester(semester)
            .specializationName(getSpecializationName(course))
            .prerequisite(toPrerequisiteResponse(course.getPrerequisite()));
    }

    private static String getSpecializationName(Course course) {
        return course.getSpecialization() != null ? course.getSpecialization().getName() : null;
    }

    private static PrerequisiteInfo toPrerequisiteResponse(Course prerequisite) {
        if (prerequisite == null)
            return null;

        return new PrerequisiteInfo()
            .id(prerequisite.getId())
            .code(prerequisite.getCode())
            .name(prerequisite.getName());
    }
}
