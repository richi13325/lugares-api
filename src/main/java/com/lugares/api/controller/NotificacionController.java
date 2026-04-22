package com.lugares.api.controller;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.lugares.api.common.ApiResponse;
import com.lugares.api.dto.request.NotificacionRequest;
import com.lugares.api.service.NotificacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Notificaciones", description = "Notificaciones push para clientes (rol USUARIO administra envíos)")
@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;

    @Operation(summary = "Enviar notificación push a un cliente",
            description = "Envía una notificación push via FCM al dispositivo del cliente indicado. Requiere rol USUARIO.")
    @PostMapping("/cliente/{clienteId}")
    public ResponseEntity<ApiResponse<Void>> enviarACliente(
            @PathVariable Long clienteId,
            @Valid @RequestBody NotificacionRequest request) throws FirebaseMessagingException {
        notificacionService.enviarACliente(clienteId, request.getTitulo(), request.getMensaje());
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}
