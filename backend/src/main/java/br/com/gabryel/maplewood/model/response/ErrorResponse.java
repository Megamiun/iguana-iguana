package br.com.gabryel.maplewood.model.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ErrorResponse(String error, String message, LocalDateTime timestamp, String path) {
    public static ErrorResponse of(String error, String message, String path) {
        return ErrorResponse.builder()
            .error(error)
            .message(message)
            .timestamp(LocalDateTime.now())
            .path(path)
            .build();
    }
}
