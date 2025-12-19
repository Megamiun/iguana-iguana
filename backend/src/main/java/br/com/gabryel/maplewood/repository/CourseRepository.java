package br.com.gabryel.maplewood.repository;

import br.com.gabryel.maplewood.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Integer> {

}