package br.com.gabryel.maplewood.repository;

import br.com.gabryel.maplewood.model.db.CourseSectionStudent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseSectionStudentRepository extends JpaRepository<CourseSectionStudent, Long> {
    List<CourseSectionStudent> findByStudentId(Integer studentId);
    List<CourseSectionStudent> findByCourseSectionId(Long courseSectionId);
}