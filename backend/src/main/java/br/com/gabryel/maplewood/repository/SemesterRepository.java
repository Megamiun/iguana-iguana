package br.com.gabryel.maplewood.repository;

import br.com.gabryel.maplewood.model.db.Semester;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SemesterRepository extends JpaRepository<Semester, Integer> {
    Optional<Semester> findByIsActiveTrue();
    Optional<Semester> findByYearAndOrderInYear(Integer year, Integer orderInYear);
}
