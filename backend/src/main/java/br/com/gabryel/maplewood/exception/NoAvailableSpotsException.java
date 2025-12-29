package br.com.gabryel.maplewood.exception;

import static org.springframework.http.HttpStatus.CONFLICT;

public class NoAvailableSpotsException extends ApiException {
    public NoAvailableSpotsException() {
        super(CONFLICT, "NO_AVAILABLE_SPOTS", "No available spots in this section");
    }
}
