package br.com.gabryel.maplewood.exception;

import br.com.gabryel.maplewood.model.SemesterType;

import static org.springframework.http.HttpStatus.CONFLICT;

public class ScheduleAlreadyExistsException extends ApiException {

    public ScheduleAlreadyExistsException(SemesterType semesterType, int year) {
        super(
            CONFLICT,
            "SCHEDULE_ALREADY_EXISTS",
            String.format("Schedule already exists for semester id %s %d", semesterType, year)
        );
    }

    public ScheduleAlreadyExistsException(int semesterId) {
        super(
            CONFLICT,
            "SCHEDULE_ALREADY_EXISTS",
            String.format("Schedule already exists for semester id #%s", semesterId)
        );
    }
}
