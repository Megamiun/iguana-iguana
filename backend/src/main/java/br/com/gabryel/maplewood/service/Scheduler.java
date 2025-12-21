package br.com.gabryel.maplewood.service;

import br.com.gabryel.maplewood.model.Semester;
import br.com.gabryel.maplewood.model.response.ScheduleResponse;
import br.com.gabryel.maplewood.model.response.ScheduleResponse.CourseScheduleResponse;
import br.com.gabryel.maplewood.model.response.ScheduleResponse.ScheduleDurationResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class Scheduler {
    public ScheduleResponse generateSchedule(Semester semester, Integer year) {
        // TODO: Implement actual scheduling algorithm
        // For now, returning mock data matching the frontend structure
        return new ScheduleResponse(List.of(
            new CourseScheduleResponse(
                "MAT101",
                "1",
                "John Smith",
                "Classroom-205",
                List.of(
                    new ScheduleDurationResponse("Monday", 9, 10),
                    new ScheduleDurationResponse("Wednesday", 10, 11),
                    new ScheduleDurationResponse("Friday", 14, 16)
                ),
                0,
                10
            ),
            new CourseScheduleResponse(
                "MAT101",
                "2",
                "Molly Hendrix",
                "Classroom-204",
                List.of(
                    new ScheduleDurationResponse("Monday", 9, 10),
                    new ScheduleDurationResponse("Wednesday", 10, 11),
                    new ScheduleDurationResponse("Friday", 14, 16)
                ),
                5,
                5
            ),
            new CourseScheduleResponse(
                "MAT101",
                "3",
                "John Smith",
                "Classroom-205",
                List.of(
                    new ScheduleDurationResponse("Monday", 10, 11),
                    new ScheduleDurationResponse("Wednesday", 11, 12),
                    new ScheduleDurationResponse("Friday", 12, 14)
                ),
                1,
                9
            ),
            new CourseScheduleResponse(
                "SCI201",
                "1",
                "Dr. Johnson",
                "Science-Lab-A",
                List.of(
                    new ScheduleDurationResponse("Tuesday", 9, 11),
                    new ScheduleDurationResponse("Thursday", 13, 15)
                ),
                2,
                8
            )
        ));
    }
}
