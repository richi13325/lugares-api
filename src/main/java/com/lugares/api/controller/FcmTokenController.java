package com.lugares.api.controller;

import com.lugares.api.common.ApiResponse;
import com.lugares.api.dto.request.FcmTokenRequest;
import com.lugares.api.entity.Cliente;
import com.lugares.api.entity.FcmToken;
import com.lugares.api.service.FcmTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Tokens FCM", description = "Registro de tokens de dispositivo para push notifications")
@RestController
@RequestMapping("/api/fcm-tokens")
@RequiredArgsConstructor
public class FcmTokenController {

    private final FcmTokenService fcmTokenService;
    private final ModelMapper modelMapper;

    @Operation(summary = "Registrar token FCM",
            description = "Registra el token de dispositivo del cliente autenticado para recibir notificaciones push. Requiere rol CLIENTE.")
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> registrar(
            @Valid @RequestBody FcmTokenRequest request,
            @AuthenticationPrincipal Cliente principal) {
        FcmToken entity = modelMapper.map(request, FcmToken.class);
        entity.setIdCliente(principal.getId().longValue());
        fcmTokenService.registrar(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.noContent());
    }
}
