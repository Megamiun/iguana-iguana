package br.com.gabryel.maplewood.repository;

import br.com.gabryel.maplewood.model.db.CourseSection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseSectionRepository extends JpaRepository<CourseSection, Long> {
    List<CourseSection> findBySemesterId(Integer semesterId);
    List<CourseSection> findByTeacherId(Integer teacherId);
    List<CourseSection> findByClassroomId(Integer classroomId);
    void deleteBySemesterId(Integer semesterId);
}