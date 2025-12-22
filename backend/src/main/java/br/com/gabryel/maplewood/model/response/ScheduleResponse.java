package br.com.gabryel.maplewood.model.response;

import br.com.gabryel.maplewood.model.Weekday;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleResponse {
    private List<CourseScheduleResponse> courses;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseScheduleResponse {
        private String name;
        private int section;
        private String teacher;
        private String classroom;
        private List<ScheduleDurationResponse> schedule;
        private int availableSpots;
        private int filledSpots;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleDurationResponse {
        private Weekday weekday;
        private int start;
        private int end;
    }
}
