package br.com.gabryel.maplewood.service.scheduler;

import br.com.gabryel.maplewood.model.db.Student;
import br.com.gabryel.maplewood.model.db.StudentCourseHistory;
import br.com.gabryel.maplewood.model.db.enums.CourseHistoryStatus;
import br.com.gabryel.maplewood.repository.StudentCourseHistoryRepository;
import br.com.gabryel.maplewood.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
public class StudentDataService {
    private final StudentRepository studentRepository;
    private final StudentCourseHistoryRepository studentCourseHistoryRepository;

    public record StudentData(int id, int gradeLevel, List<CourseStatus> courses) {}
    public record CourseStatus(int studentId, int courseId, CourseHistoryStatus passed) {}

    public Map<Integer, StudentData> getStudents() {
        var activeStudents = studentRepository.findByStatus("active");
        var courseHistories = studentCourseHistoryRepository.findByStudentIn(activeStudents);

        var studentsById = activeStudents.stream().collect(toMap(Student::getId, Function.identity()));

        return courseHistories.stream()
            .collect(groupingBy(history -> history.getStudent().getId()))
            .entrySet().stream()
            .collect(toMap(Entry::getKey, courseStatus -> getStudentData(courseStatus, studentsById)));
    }

    private static StudentData getStudentData(Entry<Integer, List<StudentCourseHistory>> courseStatus, Map<Integer, Student> studentsById) {
        var studentId = courseStatus.getKey();
        var student = studentsById.get(studentId);
        var courseHistory = courseStatus.getValue().stream()
            .map(history -> new CourseStatus(history.getCourse().getId(), history.getStudent().getId(), history.getStatus()))
            .toList();

        return new StudentData(studentId, student.getGradeLevel(), courseHistory);
    }
}
