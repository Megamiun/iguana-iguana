package br.com.gabryel.maplewood.model.response;

import br.com.gabryel.maplewood.model.SemesterType;
import br.com.gabryel.maplewood.model.db.enums.CourseType;

import java.math.BigDecimal;

public record CourseResponse(
    Integer id,
    String code,
    String name,
    String description,
    BigDecimal credits,
    Integer hoursPerWeek,
    CourseType courseType,
    Integer gradeLevelMin,
    Integer gradeLevelMax,
    SemesterType semesterType,
    String specializationName,
    PrerequisiteInfo prerequisite
) {
    public record PrerequisiteInfo(Integer id, String code, String name) {

    }
}
