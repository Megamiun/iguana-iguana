package br.com.gabryel.maplewood.model.dto;

public record TeacherData(
    int id,
    String firstName,
    String lastName,
    int specializationId,
    int maxDailyHours
) {}