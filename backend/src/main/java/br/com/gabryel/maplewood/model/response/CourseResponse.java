package br.com.gabryel.maplewood.model.response;

import br.com.gabryel.maplewood.model.Semester;

import java.math.BigDecimal;

public record CourseResponse(
    Integer id,
    String code,
    String name,
    String description,
    BigDecimal credits,
    Integer hoursPerWeek,
    String courseType,
    Integer gradeLevelMin,
    Integer gradeLevelMax,
    Semester semester,
    String specializationName,
    PrerequisiteInfo prerequisite
) {
    public record PrerequisiteInfo(
        Integer id,
        String code,
        String name
    ) {}
}
