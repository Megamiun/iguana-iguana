package br.com.gabryel.maplewood.model.db;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

import static jakarta.persistence.FetchType.LAZY;

@Data
@Entity
@Table(name = "teachers")
public class Teacher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String firstName;
    private String lastName;
    private String email;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "specialization_id", nullable = false)
    private Specialization specialization;

    private Integer maxDailyHours;
}
