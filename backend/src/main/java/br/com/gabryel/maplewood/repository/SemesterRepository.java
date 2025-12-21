package br.com.gabryel.maplewood.repository;

import br.com.gabryel.maplewood.model.SemesterEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SemesterRepository extends JpaRepository<SemesterEntity, Integer> {
    Optional<SemesterEntity> findByIsActiveTrue();
}
