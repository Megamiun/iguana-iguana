package br.com.gabryel.maplewood.model.response;

import java.util.List;

public record ClassroomScheduleResponse(
    Integer classroomId,
    String classroomName,
    List<TimeSlotResponse> timeSlots
) {}
