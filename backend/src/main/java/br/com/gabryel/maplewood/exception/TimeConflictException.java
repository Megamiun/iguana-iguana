package br.com.gabryel.maplewood.exception;

import static org.springframework.http.HttpStatus.CONFLICT;

public class TimeConflictException extends ApiException {
    public TimeConflictException() {
        super(CONFLICT, "TIME_CONFLICT", "Time conflict with existing schedule");
    }
}
