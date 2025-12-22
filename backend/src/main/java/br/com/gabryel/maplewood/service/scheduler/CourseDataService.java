package br.com.gabryel.maplewood.service.scheduler;

import br.com.gabryel.maplewood.model.SemesterType;
import br.com.gabryel.maplewood.model.db.Course;
import br.com.gabryel.maplewood.model.db.enums.CourseType;
import br.com.gabryel.maplewood.repository.CourseRepository;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static br.com.gabryel.maplewood.model.SemesterType.FALL;
import static br.com.gabryel.maplewood.model.SemesterType.SPRING;
import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
public class CourseDataService {
    private final CourseRepository courseRepository;

    @Builder
    public record CourseData(
        Integer id,
        String name,
        SemesterType semesterType,
        CourseType courseType,
        CourseData prerequisite,
        int gradeLevelMin,
        int gradeLevelMax,
        int specializationId,
        int hoursPerWeek
    ) {}

    public Map<Integer, CourseData> getCoursesFor(SemesterType semesterType) {
        var courses = courseRepository.findAll();

        var courseDataById = new HashMap<Integer, CourseData>();
        for (var course : courses) {
            loadCourseData(course, courseDataById);
        }

        return courseDataById.entrySet().stream()
            .filter(idToCourse -> idToCourse.getValue().semesterType == semesterType)
            .collect(toMap(Entry::getKey, Entry::getValue));
    }

    private CourseData loadCourseData(Course course, Map<Integer, CourseData> cache) {
        if (course == null)
            return null;

        return cache.computeIfAbsent(course.getId(), id -> CourseData.builder()
            .id(course.getId())
            .name(course.getName())
            .semesterType(getSemesterType(course.getSemesterOrder()))
            .courseType(course.getCourseType())
            .prerequisite(loadCourseData(course.getPrerequisite(), cache))
            .gradeLevelMin(course.getGradeLevelMin())
            .gradeLevelMax(course.getGradeLevelMax())
            .specializationId(course.getSpecialization().getId())
            .hoursPerWeek(course.getHoursPerWeek())
            .build()
        );
    }

    private static SemesterType getSemesterType(int semester) {
        if (semester == 1) {
            return FALL;
        }
        return SPRING;
    }
}
