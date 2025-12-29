package br.com.gabryel.maplewood.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

public class GradeLevelRequirementException extends ApiException {
    public GradeLevelRequirementException() {
        super(BAD_REQUEST, "GRADE_LEVEL_REQUIREMENT", "Student grade level does not meet course requirements");
    }
}
