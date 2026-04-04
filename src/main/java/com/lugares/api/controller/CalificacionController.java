package com.lugares.api.controller;

import com.lugares.api.common.ApiResponse;
import com.lugares.api.dto.request.CalificacionRequest;
import com.lugares.api.dto.response.CalificacionResponse;
import com.lugares.api.entity.Calificacion;
import com.lugares.api.service.CalificacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/calificaciones")
@RequiredArgsConstructor
public class CalificacionController {

    private final CalificacionService calificacionService;
    private final ModelMapper modelMapper;

    @PostMapping
    public ResponseEntity<ApiResponse<CalificacionResponse>> createOrUpdate(
            @Valid @RequestBody CalificacionRequest request) {
        Calificacion calificacion = calificacionService.createOrUpdate(
                request.getIdCliente(), request.getIdEstablecimiento(), request.getCalificacion());
        CalificacionResponse response = modelMapper.map(calificacion, CalificacionResponse.class);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
