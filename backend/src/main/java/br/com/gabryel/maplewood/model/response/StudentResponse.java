package br.com.gabryel.maplewood.model.response;

public record StudentResponse(
    Integer id,
    String firstName,
    String lastName,
    String email,
    Integer gradeLevel,
    Integer enrollmentYear,
    Integer expectedGraduationYear,
    String status
) {}
