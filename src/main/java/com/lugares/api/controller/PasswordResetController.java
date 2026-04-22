package com.lugares.api.controller;

import com.lugares.api.common.ApiResponse;
import com.lugares.api.dto.request.ForgotPasswordRequest;
import com.lugares.api.dto.request.ResetPasswordRequest;
import com.lugares.api.dto.request.ValidateCodeRequest;
import com.lugares.api.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Reset de contraseña", description = "Flujo de recuperación de contraseña por email")
@RestController
@RequestMapping("/auth/password")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @Operation(summary = "Solicitar reset de contraseña",
            description = "Envía un código de verificación al correo del usuario. Endpoint público, no requiere token.")
    @PostMapping("/forgot")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.forgotPassword(request.getCorreoElectronico(), request.getTipoUsuario());
        return ResponseEntity.ok(ApiResponse.success("Codigo enviado al correo electronico", null));
    }

    @Operation(summary = "Validar código de reset",
            description = "Valida el código recibido por email y devuelve un token seguro para el paso de reset. Endpoint público, no requiere token.")
    @PostMapping("/validate-code")
    public ResponseEntity<ApiResponse<Map<String, String>>> validateCode(@Valid @RequestBody ValidateCodeRequest request) {
        String secureToken = passwordResetService.validateCode(request.getCorreoElectronico(), request.getCodigo());
        return ResponseEntity.ok(ApiResponse.success(Map.of("token", secureToken)));
    }

    @Operation(summary = "Resetear contraseña",
            description = "Cambia la contraseña usando el token seguro obtenido en el paso anterior. Endpoint público, no requiere token.")
    @PostMapping("/reset")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.getToken(), request.getNuevaContrasenia());
        return ResponseEntity.ok(ApiResponse.success("Contrasenia actualizada correctamente", null));
    }
}
