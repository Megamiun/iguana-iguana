package br.com.gabryel.maplewood.exception;

import static org.springframework.http.HttpStatus.CONFLICT;

public class AlreadyEnrolledException extends ApiException {
    public AlreadyEnrolledException() {
        super(CONFLICT, "ALREADY_ENROLLED", "Already enrolled in this course");
    }
}
