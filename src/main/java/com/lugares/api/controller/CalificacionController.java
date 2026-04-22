package com.lugares.api.controller;

import com.lugares.api.common.ApiResponse;
import com.lugares.api.dto.request.CalificacionRequest;
import com.lugares.api.dto.response.CalificacionResponse;
import com.lugares.api.entity.Calificacion;
import com.lugares.api.entity.Cliente;
import com.lugares.api.mapper.CalificacionMapper;
import com.lugares.api.service.CalificacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Calificaciones", description = "Calificaciones de clientes a establecimientos")
@RestController
@RequestMapping("/api/calificaciones")
@RequiredArgsConstructor
public class CalificacionController {

    private final CalificacionService calificacionService;
    private final CalificacionMapper calificacionMapper;

    @Operation(summary = "Crear o actualizar calificación",
            description = "El cliente autenticado califica un establecimiento (crea o actualiza su calificación previa). Requiere rol CLIENTE.")
    @PostMapping
    public ResponseEntity<ApiResponse<CalificacionResponse>> createOrUpdate(
            @Valid @RequestBody CalificacionRequest request,
            @AuthenticationPrincipal Cliente principal) {
        Calificacion calificacion = calificacionService.createOrUpdate(
                principal.getId(), request.getIdEstablecimiento(), request.getCalificacion());
        return ResponseEntity.ok(ApiResponse.success(calificacionMapper.toDto(calificacion)));
    }
}
