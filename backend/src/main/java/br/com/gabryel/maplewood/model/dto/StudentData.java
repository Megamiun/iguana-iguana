package br.com.gabryel.maplewood.model.dto;

import java.util.List;

public record StudentData(
    int id,
    int gradeLevel,
    List<Integer> passedCourses
) {}