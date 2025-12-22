package br.com.gabryel.maplewood.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

import static java.util.stream.IntStream.range;

@Data
@ConfigurationProperties(prefix = "maplewood.scheduling.time")
public class TimeSchedulingConfig {
    private List<TimeRange> available;
    private int maxConsecutiveClassHours;

    @Data
    public static class TimeRange {
        private int start;
        private int end;

        public List<Integer> getSlots() {
            return range(start, end).boxed().toList();
        }
    }

    public List<Integer> getSlots() {
        return available.stream().flatMap(slot -> slot.getSlots().stream()).toList();
    }
}
