package br.com.gabryel.maplewood.service.scheduler;

import br.com.gabryel.maplewood.model.SemesterType;
import br.com.gabryel.maplewood.model.db.Course;
import br.com.gabryel.maplewood.model.db.enums.CourseType;
import br.com.gabryel.maplewood.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
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

    public record CourseData(
        Integer id,
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

        return cache.computeIfAbsent(course.getId(), id -> new CourseData(
            course.getId(),
            getSemesterType(course.getSemesterOrder()),
            course.getCourseType(),
            loadCourseData(course.getPrerequisite(), cache),
            course.getGradeLevelMin(),
            course.getGradeLevelMax(),
            course.getSpecialization().getId(),
            course.getHoursPerWeek()
        ));
    }

    private static SemesterType getSemesterType(int semester) {
        if (semester == 1) {
            return FALL;
        }
        return SPRING;
    }
}
