package br.com.gabryel.maplewood.controller;

import br.com.gabryel.maplewood.model.request.ScheduleGenerationRequest;
import br.com.gabryel.maplewood.model.response.ScheduleResponse;
import br.com.gabryel.maplewood.service.Scheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class ScheduleController {
    private final Scheduler scheduler;

    @PostMapping
    public ScheduleResponse generateSchedule(@RequestBody ScheduleGenerationRequest request) {
        return scheduler.generateSchedule(request.getSemester(), request.getYear());
    }
}
