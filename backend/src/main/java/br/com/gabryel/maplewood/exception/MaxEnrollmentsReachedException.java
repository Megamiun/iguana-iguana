package br.com.gabryel.maplewood.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

public class MaxEnrollmentsReachedException extends ApiException {
    public MaxEnrollmentsReachedException(int maxEnrollments) {
        super(BAD_REQUEST, "MAX_ENROLLMENTS_REACHED", "Maximum of " + maxEnrollments + " enrollments per semester reached");
    }
}
