package br.com.gabryel.maplewood.service.scheduler;

import br.com.gabryel.maplewood.service.scheduler.ClassroomDataService.ClassroomData;
import br.com.gabryel.maplewood.service.scheduler.CourseDataService.CourseData;
import br.com.gabryel.maplewood.service.scheduler.StudentDataService.StudentData;
import br.com.gabryel.maplewood.service.scheduler.TeacherDataService.TeacherData;

import java.util.List;

public record CourseSection(
    CourseData course,
    TeacherData teacher,
    ClassroomData classroom,
    List<TimeSlot> timeSlots,
    List<StudentData> students,
    int section
) { }
