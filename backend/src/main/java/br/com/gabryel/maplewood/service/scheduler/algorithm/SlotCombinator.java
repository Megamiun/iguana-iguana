package br.com.gabryel.maplewood.service.scheduler.algorithm;

import br.com.gabryel.maplewood.config.TimeSchedulingConfig;
import br.com.gabryel.maplewood.model.Weekday;
import br.com.gabryel.maplewood.service.scheduler.ScheduleCalculator.TimeSlot;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class SlotCombinator {
    private final TimeSchedulingConfig timeConfig;

    public SlotCombinator(TimeSchedulingConfig timeConfig) {
        this.timeConfig = timeConfig;
    }

    public Stream<Stream<TimeSlot>> getKCombinations(List<TimeSlot> matchingSlots, int size, int maxInDay) {
        return getKCombinations(
            matchingSlots,
            size,
            matchingSlots.size() - 1,
            null,
            0,
            null,
            0,
            maxInDay
        );
    }

    private Stream<Stream<TimeSlot>> getKCombinations(
        List<TimeSlot> matchingSlots,
        int size,
        int index,
        TimeSlot lastSelected,
        int consecutiveCounter,
        Weekday lastDay,
        int inLastDay,
        int maxInDay
    ) {
        if (size > index + 1)
            return Stream.empty();

        if (size == 0)
            return Stream.of(Stream.empty());

        var timeSlot = matchingSlots.get(index);
        var currentConsecutiveCounter = areConsecutive(timeSlot, lastSelected) ? consecutiveCounter + 1 : 1;

        var currentDay = timeSlot.weekday();
        var inCurrentDay = currentDay == lastDay ? inLastDay + 1 : 1;

        if (currentConsecutiveCounter > timeConfig.getMaxConsecutiveClassHours() || inCurrentDay > maxInDay)
            return getKCombinations(matchingSlots, size, index - 1, lastSelected, currentConsecutiveCounter, currentDay, inCurrentDay, maxInDay);

        if (index == 0)
            return Stream.of(Stream.of(matchingSlots.getFirst()));

        // Lazy concatenation using suppliers. Stream.concat eagerly loads
        return concatLazy(
            // Keeps current item
            () -> getKCombinations(matchingSlots, size - 1, index - 1, timeSlot, currentConsecutiveCounter, currentDay, inCurrentDay, maxInDay)
                .map(next -> Stream.concat(Stream.of(timeSlot), next)),
            // Skip current item
            () -> getKCombinations(matchingSlots, size, index - 1, lastSelected, currentConsecutiveCounter, currentDay, inCurrentDay, maxInDay));
    }

    @SafeVarargs
    private <T> Stream<T> concatLazy(Supplier<Stream<T>>... streams) {
        return Stream.of(streams).flatMap(Supplier::get);
    }

    private static boolean areConsecutive(TimeSlot first, TimeSlot second) {
        return first != null && second != null && second.weekday() == first.weekday() && first.slot() + 1 == second.slot();
    }
}
