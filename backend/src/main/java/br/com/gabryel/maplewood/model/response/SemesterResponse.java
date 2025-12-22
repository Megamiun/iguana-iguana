package br.com.gabryel.maplewood.model.response;

import br.com.gabryel.maplewood.model.SemesterType;

public record SemesterResponse(
    Integer id,
    String name,
    Integer year,
    SemesterType semester
) {
}