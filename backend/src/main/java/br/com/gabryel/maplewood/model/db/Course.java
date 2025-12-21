package br.com.gabryel.maplewood.model.db;

import br.com.gabryel.maplewood.model.db.enums.CourseType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnTransformer;

import java.math.BigDecimal;

import static jakarta.persistence.FetchType.LAZY;

@Getter
@Setter
@Entity
@Table(name = "courses")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String code;
    private String name;
    private String description;
    private BigDecimal credits;
    private int hoursPerWeek;


    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "prerequisite_id")
    private Course prerequisite;
    // TODO Check if N+1 happens when loading courses

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "specialization_id")
    private Specialization specialization;

    @Enumerated(EnumType.STRING)
    @ColumnTransformer(read = "UPPER(course_type)", write = "LOWER(?)")
    private CourseType courseType;

    private int gradeLevelMin;
    private int gradeLevelMax;
    private int semesterOrder;
}
