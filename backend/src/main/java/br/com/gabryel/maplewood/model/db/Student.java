package br.com.gabryel.maplewood.model.db;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "students")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String firstName;
    private String lastName;
    private String email;

    private int gradeLevel;
    private int enrollmentYear;
    private int expectedGraduationYear;

    // TODO: As we don't know all possible states, we will forgo using an enum here
    private String status;
}
