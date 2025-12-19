package br.com.gabryel.maplewood.model.response;

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
    Integer semesterOrder,
    String specializationName,
    PrerequisiteInfo prerequisite
) {
    public record PrerequisiteInfo(
        Integer id,
        String code,
        String name
    ) {}
}
