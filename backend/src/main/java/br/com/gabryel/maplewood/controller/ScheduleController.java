package br.com.gabryel.maplewood.controller;

import br.com.gabryel.maplewood.model.request.ScheduleGenerationRequest;
import br.com.gabryel.maplewood.model.response.PageResponse;
import br.com.gabryel.maplewood.model.response.ScheduleResponse;
import br.com.gabryel.maplewood.model.response.SemesterResponse;
import br.com.gabryel.maplewood.service.scheduler.Scheduler;
import br.com.gabryel.maplewood.service.scheduler.SemesterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @PostMapping
    public ScheduleResponse generateSchedule(@RequestBody ScheduleGenerationRequest request) {
        return scheduler.generateSchedule(request.getSemester(), request.getYear());
    }
}
