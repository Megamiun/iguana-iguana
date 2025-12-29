package br.com.gabryel.maplewood.model.dto;

import br.com.gabryel.maplewood.model.Weekday;

public record WeekdayTimeRange(Weekday weekday, int start, int end) {
    public boolean intersects(WeekdayTimeRange timeRange) {
        return weekday == timeRange.weekday && start < timeRange.end && end > timeRange.start;
    }
}
