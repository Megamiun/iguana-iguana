package br.com.gabryel.maplewood.model.db;

import br.com.gabryel.maplewood.model.Weekday;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnTransformer;

import static jakarta.persistence.FetchType.LAZY;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "course_section_time_slots")
public class CourseSectionTimeSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "course_section_id")
    private CourseSection courseSection;

    @Enumerated(EnumType.STRING)
    @ColumnTransformer(read = "UPPER(weekday)", write = "LOWER(?)")
    @Column(nullable = false)
    private Weekday weekday;

    @Column(nullable = false)
    private Integer startHour;

    @Column(nullable = false)
    private Integer endHour;
}