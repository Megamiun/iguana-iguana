package br.com.gabryel.maplewood.model.response;

import br.com.gabryel.maplewood.model.Semester;

public record SemesterResponse(
    Integer id,
    String name,
    Integer year,
    Semester semester
) {
}