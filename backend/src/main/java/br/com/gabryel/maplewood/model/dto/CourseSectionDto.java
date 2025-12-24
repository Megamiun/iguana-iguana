package br.com.gabryel.maplewood.model.dto;

import java.util.List;

public record CourseSectionDto(
    CourseData course,
    int section,
    List<TimeRange> timeRanges,
    TeacherData teacher,
    ClassroomData classroom,
    List<StudentData> students
) { }
