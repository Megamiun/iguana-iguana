package br.com.gabryel.maplewood.model.response;

public record TeacherResponse(
    Integer id,
    String firstName,
    String lastName,
    String email,
    String specializationName,
    Integer maxDailyHours
) {}
