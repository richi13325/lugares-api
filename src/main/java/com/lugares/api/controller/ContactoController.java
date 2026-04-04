package com.lugares.api.controller;

import com.lugares.api.common.ApiResponse;
import com.lugares.api.dto.request.ContactoRequest;
import com.lugares.api.service.ContactoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contacto")
@RequiredArgsConstructor
public class ContactoController {

    private final ContactoService contactoService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> enviar(@Valid @RequestBody ContactoRequest request) {
        contactoService.enviarContacto(
                request.getNombre(), request.getCorreoElectronico(),
                request.getAsunto(), request.getMensaje());
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}
