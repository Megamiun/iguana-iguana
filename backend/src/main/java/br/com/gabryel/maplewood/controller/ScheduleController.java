package br.com.gabryel.maplewood.controller;

import br.com.gabryel.maplewood.api.SchedulesApi;
import br.com.gabryel.maplewood.api.model.ScheduleResponse;
import br.com.gabryel.maplewood.api.model.SemesterSeason;
import br.com.gabryel.maplewood.model.SemesterType;
import br.com.gabryel.maplewood.service.scheduler.Scheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ScheduleController implements SchedulesApi {
    private final Scheduler scheduler;

    @Override
    public ResponseEntity<ScheduleResponse> getSchedule(Integer year, SemesterSeason semesterType) {
        var semesterCore = SemesterType.valueOf(semesterType.name());
        return ResponseEntity.ok(scheduler.loadSchedule(semesterCore, year));
    }

    @Override
    public ResponseEntity<ScheduleResponse> generateSchedule(Integer year, SemesterSeason semesterType) {
        var semesterCore = SemesterType.valueOf(semesterType.name());
        return ResponseEntity.ok(scheduler.generateSchedule(semesterCore, year));
    }

    @Override
    public ResponseEntity<Void> deleteSchedule(Integer year, SemesterSeason semesterType) {
        var semesterCore = SemesterType.valueOf(semesterType.name());
        scheduler.deleteSchedule(semesterCore, year);
        return ResponseEntity.noContent().build();
    }
}
