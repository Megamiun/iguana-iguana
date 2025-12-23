package br.com.gabryel.maplewood.exception;

import static org.springframework.http.HttpStatus.NOT_FOUND;

public class ScheduleNotFoundException extends ApiException {
    public ScheduleNotFoundException(int year, String semesterType) {
        super(NOT_FOUND, "SCHEDULE_NOT_FOUND", String.format("No schedule found for %s %d", semesterType, year));
    }
}
