package br.com.gabryel.maplewood.model.response;

import java.util.List;

public record TeacherScheduleResponse(
    Integer teacherId,
    String teacherName,
    List<TimeSlotResponse> timeSlots
) {}
