package br.com.gabryel.maplewood.controller;

import br.com.gabryel.maplewood.model.SemesterType;
import br.com.gabryel.maplewood.model.response.PageResponse;
import br.com.gabryel.maplewood.model.response.ScheduleResponse;
import br.com.gabryel.maplewood.model.response.SemesterResponse;
import br.com.gabryel.maplewood.service.scheduler.Scheduler;
import br.com.gabryel.maplewood.service.scheduler.SemesterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class ScheduleController {
    private final Scheduler scheduler;
    private final SemesterService semesterService;

    @GetMapping("/semesters")
    public PageResponse<SemesterResponse> getSemesters() {
        return semesterService.getSemestersPageForScheduling();
    }

    @GetMapping("/{year}/{semesterType}")
    public ScheduleResponse getSchedule(
        @PathVariable int year,
        @PathVariable SemesterType semesterType
    ) {
        return scheduler.loadSchedule(semesterType, year);
    }

    @PostMapping("/{year}/{semesterType}")
    public ScheduleResponse generateSchedule(
        @PathVariable int year,
        @PathVariable SemesterType semesterType
    ) {
        return scheduler.generateSchedule(semesterType, year);
    }

    @DeleteMapping("/{year}/{semesterType}")
    public ResponseEntity<Void> deleteSchedule(
        @PathVariable int year,
        @PathVariable SemesterType semesterType
    ) {
        scheduler.deleteSchedule(semesterType, year);
        return ResponseEntity.noContent().build();
    }
}
