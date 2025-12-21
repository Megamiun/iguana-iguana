package br.com.gabryel.maplewood.repository;

import br.com.gabryel.maplewood.model.db.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassroomRepository extends JpaRepository<Classroom, Integer> {

}
