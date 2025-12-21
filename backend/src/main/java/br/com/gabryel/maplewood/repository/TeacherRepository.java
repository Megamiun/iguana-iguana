package br.com.gabryel.maplewood.repository;

import br.com.gabryel.maplewood.model.db.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherRepository extends JpaRepository<Teacher, Integer> {

}
