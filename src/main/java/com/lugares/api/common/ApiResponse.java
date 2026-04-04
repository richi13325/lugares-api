package com.lugares.api.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        int status,
        String message,
        T data,
        LocalDateTime timestamp
) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(1, "Operacion exitosa", data, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(1, message, data, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(1, "Recurso creado", data, LocalDateTime.now());
    }

    public static ApiResponse<Void> noContent() {
        return new ApiResponse<>(1, "Operacion exitosa", null, LocalDateTime.now());
    }
}
