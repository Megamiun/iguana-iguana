package br.com.gabryel.maplewood.repository;

import br.com.gabryel.maplewood.model.db.CourseSectionStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CourseSectionStudentRepository extends JpaRepository<CourseSectionStudent, Long> {
    List<CourseSectionStudent> findByStudentId(Integer studentId);
    List<CourseSectionStudent> findByCourseSectionId(Long courseSectionId);

    @Query("SELECT css FROM CourseSectionStudent css " +
           "JOIN FETCH css.courseSection cs " +
           "WHERE css.student.id = :studentId AND cs.semester.id = :semesterId")
    List<CourseSectionStudent> findByStudentIdAndSemesterId(
        @Param("studentId") Integer studentId,
        @Param("semesterId") Integer semesterId
    );
}