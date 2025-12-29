package br.com.gabryel.maplewood.service.scheduler.data;

import br.com.gabryel.maplewood.model.SemesterType;
import br.com.gabryel.maplewood.model.db.Course;
import br.com.gabryel.maplewood.model.dto.CourseData;
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

    public Map<Integer, CourseData> getCoursesFor(SemesterType semesterType) {
        var courses = courseRepository.findAll();

        var courseDataById = new HashMap<Integer, CourseData>();
        for (var course : courses) {
            loadCourseData(course, courseDataById);
        }

        return courseDataById.entrySet().stream()
            .filter(idToCourse -> idToCourse.getValue().semesterType() == semesterType)
            .collect(toMap(Entry::getKey, Entry::getValue));
    }

    private CourseData loadCourseData(Course course, Map<Integer, CourseData> cache) {
        if (course == null)
            return null;

        return cache.computeIfAbsent(course.getId(), id -> CourseData.builder()
            .id(course.getId())
            .code(course.getCode())
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
