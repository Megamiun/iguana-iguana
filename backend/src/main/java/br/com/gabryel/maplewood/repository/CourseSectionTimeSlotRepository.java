package br.com.gabryel.maplewood.repository;

import br.com.gabryel.maplewood.model.db.CourseSectionTimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseSectionTimeSlotRepository extends JpaRepository<CourseSectionTimeSlot, Long> {
    List<CourseSectionTimeSlot> findByCourseSectionId(Long courseSectionId);
}