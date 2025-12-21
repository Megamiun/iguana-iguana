package br.com.gabryel.maplewood.repository;

import br.com.gabryel.maplewood.model.db.Student;
import br.com.gabryel.maplewood.model.db.StudentCourseHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentCourseHistoryRepository extends JpaRepository<StudentCourseHistory, Integer> {
    List<StudentCourseHistory> findByStudentIn(List<Student> studentId);
}
