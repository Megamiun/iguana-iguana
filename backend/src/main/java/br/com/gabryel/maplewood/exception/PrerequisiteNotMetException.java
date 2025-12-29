package br.com.gabryel.maplewood.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

public class PrerequisiteNotMetException extends ApiException {
    public PrerequisiteNotMetException() {
        super(BAD_REQUEST, "PREREQUISITE_NOT_MET", "Student has not completed prerequisite courses");
    }
}
