package com.lugares.api.exception;

import com.google.firebase.messaging.FirebaseMessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 404
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Recurso no encontrado: {}", ex.getMessage());
        ApiError error = new ApiError(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // 409 — Duplicado
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiError> handleDuplicate(DuplicateResourceException ex, HttpServletRequest request) {
        log.warn("Recurso duplicado: {}", ex.getMessage());
        ApiError error = new ApiError(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // 422 — Regla de negocio
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiError> handleBusinessRule(BusinessRuleException ex, HttpServletRequest request) {
        log.warn("Regla de negocio violada: {}", ex.getMessage());
        ApiError error = new ApiError(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "Unprocessable Entity",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }

    // 400 — Validacion de campos (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fe ->
                fieldErrors.put(fe.getField(), fe.getDefaultMessage())
        );
        log.warn("Error de validacion: {} campos invalidos", fieldErrors.size());
        ApiError error = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Error",
                "Errores de validacion en los campos enviados",
                request.getRequestURI(),
                fieldErrors
        );
        return ResponseEntity.badRequest().body(error);
    }

    // 400 — Tipo de argumento incorrecto
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String message = String.format("El parametro '%s' debe ser de tipo %s",
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "desconocido");
        log.warn("Tipo de argumento incorrecto: {}", message);
        ApiError error = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                message,
                request.getRequestURI()
        );
        return ResponseEntity.badRequest().body(error);
    }

    // 401 — No autenticado
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuth(AuthenticationException ex, HttpServletRequest request) {
        log.warn("Error de autenticacion: {}", ex.getMessage());
        ApiError error = new ApiError(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Credenciales invalidas o token expirado",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    // 403 — No autorizado
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Acceso denegado: {}", ex.getMessage());
        ApiError error = new ApiError(
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                "No tiene permisos para realizar esta accion",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    // 409 — Integridad de datos (FK, unique constraints)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.error("Violacion de integridad de datos: {}", ex.getMostSpecificCause().getMessage());
        ApiError error = new ApiError(
                HttpStatus.CONFLICT.value(),
                "Data Integrity Violation",
                "Operacion rechazada por restriccion de integridad en la base de datos",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // 502 — Firebase messaging error
    @ExceptionHandler(FirebaseMessagingException.class)
    public ResponseEntity<ApiError> handleFirebase(FirebaseMessagingException ex, HttpServletRequest request) {
        log.error("Error al enviar notificacion Firebase: {}", ex.getMessage());
        ApiError error = new ApiError(
                HttpStatus.BAD_GATEWAY.value(),
                "Firebase Error",
                "Error al enviar la notificacion push",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(error);
    }

    // 502 — Firebase no inicializado / configuracion
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
        String message = ex.getMessage();
        if (message != null && message.contains("Firebase")) {
            log.error("Firebase no inicializado: {}", ex.getMessage());
            ApiError error = new ApiError(
                    HttpStatus.BAD_GATEWAY.value(),
                    "Firebase Not Initialized",
                    "El servicio de notificaciones push no esta configurado. Contacte al administrador.",
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(error);
        }
        throw ex; // Let other IllegalStateException propagate to the general handler
    }

    // 400 — Argumento invalido (validarImagen, reglas de negocio simples)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Argumento invalido: {}", ex.getMessage());
        ApiError error = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.badRequest().body(error);
    }

    // 500 — Fallback general
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneral(Exception ex, HttpServletRequest request) {
        log.error("Error interno no manejado: ", ex);
        ApiError error = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "Ocurrio un error interno. Contacte al administrador.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
