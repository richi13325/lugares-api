package com.lugares.api.controller;

import com.lugares.api.common.ApiResponse;
import com.lugares.api.dto.request.ForgotPasswordRequest;
import com.lugares.api.dto.request.ResetPasswordRequest;
import com.lugares.api.dto.request.ValidateCodeRequest;
import com.lugares.api.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth/password")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/forgot")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.forgotPassword(request.getCorreoElectronico(), request.getTipoUsuario());
        return ResponseEntity.ok(ApiResponse.success("Codigo enviado al correo electronico", null));
    }

    @PostMapping("/validate-code")
    public ResponseEntity<ApiResponse<Map<String, String>>> validateCode(@Valid @RequestBody ValidateCodeRequest request) {
        String secureToken = passwordResetService.validateCode(request.getCorreoElectronico(), request.getCodigo());
        return ResponseEntity.ok(ApiResponse.success(Map.of("token", secureToken)));
    }

    @PostMapping("/reset")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.getToken(), request.getNuevaContrasenia());
        return ResponseEntity.ok(ApiResponse.success("Contrasenia actualizada correctamente", null));
    }
}
