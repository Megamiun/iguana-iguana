package br.com.gabryel.maplewood.model.db;

import jakarta.persistence.*;
import lombok.Data;

import static jakarta.persistence.FetchType.LAZY;

@Data
@Entity
@Table(name = "classrooms")
public class Classroom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "room_type_id")
    private RoomType roomType;

    private String equipment;

    private Integer capacity;
}
