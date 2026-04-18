package com.lugares.api.controller;

import com.lugares.api.common.ApiResponse;
import com.lugares.api.dto.response.SuscripcionResponse;
import com.lugares.api.mapper.SuscripcionMapper;
import com.lugares.api.service.SuscripcionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/suscripciones")
@RequiredArgsConstructor
public class SuscripcionController {

    private final SuscripcionService suscripcionService;
    private final SuscripcionMapper suscripcionMapper;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SuscripcionResponse>>> listAll() {
        List<SuscripcionResponse> response = suscripcionService.listAll().stream()
                .map(suscripcionMapper::toDto)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SuscripcionResponse>> getById(@PathVariable Integer id) {
        SuscripcionResponse response = suscripcionMapper.toDto(suscripcionService.getById(id));
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
