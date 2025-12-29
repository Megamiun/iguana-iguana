package br.com.gabryel.maplewood.mapper;

import br.com.gabryel.maplewood.api.model.ScheduleDurationResponse;
import br.com.gabryel.maplewood.api.model.TimeSlotResponse;
import br.com.gabryel.maplewood.api.model.Weekday;
import br.com.gabryel.maplewood.model.db.CourseSection;
import br.com.gabryel.maplewood.model.db.CourseSectionTimeSlot;
import br.com.gabryel.maplewood.model.dto.WeekdayTimeRange;
import br.com.gabryel.maplewood.repository.CourseSectionStudentRepository;
import br.com.gabryel.maplewood.repository.CourseSectionTimeSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class ScheduleMapper {
    private final CourseSectionTimeSlotRepository courseSectionTimeSlotRepository;
    private final CourseSectionStudentRepository courseSectionStudentRepository;
    private final ResponseMapper responseMapper;

    public Stream<TimeSlotResponse> toTimeSlotResponses(CourseSection section) {
        var slots = courseSectionTimeSlotRepository.findByCourseSectionId(section.getId());
        var sectionEnrollments = courseSectionStudentRepository.findByCourseSectionId(section.getId());
        var filledSpots = sectionEnrollments.size();
        var availableSpots = section.getClassroom().getCapacity() - filledSpots;

        return slots.stream().map(slot -> toTimeSlotResponse(slot, section, filledSpots, availableSpots));
    }

    public TimeSlotResponse toTimeSlotResponse(
        CourseSectionTimeSlot slot,
        CourseSection section,
        int filledSpots,
        int availableSpots
    ) {
        return new TimeSlotResponse()
            .weekday(Weekday.valueOf(slot.getWeekday().name()))
            .start(slot.getStartHour())
            .end(slot.getEndHour())
            .courseCode(section.getCourse().getCode())
            .courseName(section.getCourse().getName())
            .section(section.getSectionNumber())
            .classroom(section.getClassroom().getName())
            .classroomId(section.getClassroom().getId())
            .teacher(responseMapper.formatFullName(
                section.getTeacher().getFirstName(),
                section.getTeacher().getLastName()
            ))
            .teacherId(section.getTeacher().getId())
            .filledSpots(filledSpots)
            .availableSpots(availableSpots);
    }
}
