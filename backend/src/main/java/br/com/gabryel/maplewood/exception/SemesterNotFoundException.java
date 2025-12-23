package br.com.gabryel.maplewood.exception;

import static org.springframework.http.HttpStatus.NOT_FOUND;

public class SemesterNotFoundException extends ApiException {
    public SemesterNotFoundException(int year, String semesterType) {
        super(
            NOT_FOUND,
            "SEMESTER_NOT_FOUND",
            String.format("Semester not found for %s %d", semesterType, year)
        );
    }

    public SemesterNotFoundException(int id) {
        super(
            NOT_FOUND,
            "SEMESTER_NOT_FOUND",
            String.format("Semester not found for id #%s", id)
        );
    }
}
