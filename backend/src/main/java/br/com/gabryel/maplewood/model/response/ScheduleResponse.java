package br.com.gabryel.maplewood.model.response;

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
        private String section;
        private String teacher;
        private String classroom;
        private List<ScheduleDurationResponse> schedule;
        private Integer availableSpots;
        private Integer filledSpots;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleDurationResponse {
        private String weekday;
        private Integer start;
        private Integer end;
    }
}
