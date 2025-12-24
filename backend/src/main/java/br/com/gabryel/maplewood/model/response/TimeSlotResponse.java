package br.com.gabryel.maplewood.model.response;

import br.com.gabryel.maplewood.model.Weekday;

public record TimeSlotResponse(
    Weekday weekday,
    Integer start,
    Integer end,
    String courseCode,
    String courseName,
    Integer section,
    String classroom,
    Integer classroomId,
    String teacher,
    Integer teacherId,
    Integer filledSpots,
    Integer availableSpots
) {}
