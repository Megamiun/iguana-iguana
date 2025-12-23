package br.com.gabryel.maplewood.model.dto;

import br.com.gabryel.maplewood.model.SemesterType;
import br.com.gabryel.maplewood.model.db.enums.CourseType;
import lombok.Builder;

@Builder
public record CourseData(
    Integer id,
    String code,
    String name,
    SemesterType semesterType,
    CourseType courseType,
    CourseData prerequisite,
    int gradeLevelMin,
    int gradeLevelMax,
    int specializationId,
    int hoursPerWeek
) {}