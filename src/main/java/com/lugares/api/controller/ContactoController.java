package com.lugares.api.controller;

import com.lugares.api.common.ApiResponse;
import com.lugares.api.dto.request.ContactoRequest;
import com.lugares.api.service.ContactoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Contacto", description = "Formulario público de contacto")
@RestController
@RequestMapping("/api/contacto")
@RequiredArgsConstructor
public class ContactoController {

    private final ContactoService contactoService;

    @Operation(summary = "Enviar mensaje de contacto",
            description = "Envía un mensaje al equipo de 300 Lugares. Endpoint público, no requiere token.")
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> enviar(@Valid @RequestBody ContactoRequest request) {
        contactoService.enviarContacto(
                request.getNombre(), request.getCorreoElectronico(),
                request.getAsunto(), request.getMensaje());
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}
