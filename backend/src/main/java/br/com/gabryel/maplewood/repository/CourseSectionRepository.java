package br.com.gabryel.maplewood.repository;

import br.com.gabryel.maplewood.model.db.CourseSection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseSectionRepository extends JpaRepository<CourseSection, Long> {

    List<CourseSection> findBySemesterId(Integer semesterId);

    List<CourseSection> findBySemesterIdAndTeacherId(Integer semesterId, Integer teacherId);

    List<CourseSection> findBySemesterIdAndClassroomId(Integer semesterId, Integer classroomId);

    boolean existsBySemesterId(Integer semesterId);

    void deleteBySemesterId(Integer semesterId);
}