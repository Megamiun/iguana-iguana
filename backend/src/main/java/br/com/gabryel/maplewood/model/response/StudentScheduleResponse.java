package br.com.gabryel.maplewood.model.response;

import java.util.List;

public record StudentScheduleResponse(
    Integer studentId,
    String studentName,
    List<TimeSlotResponse> timeSlots
) {}
