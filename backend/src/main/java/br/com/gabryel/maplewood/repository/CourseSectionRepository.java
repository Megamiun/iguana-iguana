package br.com.gabryel.maplewood.repository;

import br.com.gabryel.maplewood.model.db.CourseSection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseSectionRepository extends JpaRepository<CourseSection, Long> {

    List<CourseSection> findBySemesterId(Integer semesterId);

    boolean existsBySemesterId(Integer semesterId);

    void deleteBySemesterId(Integer semesterId);
}