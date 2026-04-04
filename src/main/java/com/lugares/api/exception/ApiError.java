package com.lugares.api.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        int status,
        String error,
        String message,
        LocalDateTime timestamp,
        String path,
        Map<String, String> fieldErrors
) {

    public ApiError(int status, String error, String message, String path) {
        this(status, error, message, LocalDateTime.now(), path, null);
    }

    public ApiError(int status, String error, String message, String path, Map<String, String> fieldErrors) {
        this(status, error, message, LocalDateTime.now(), path, fieldErrors);
    }
}
