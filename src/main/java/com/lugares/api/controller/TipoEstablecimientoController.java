package com.lugares.api.controller;

import com.lugares.api.common.ApiResponse;
import com.lugares.api.dto.response.TipoEstablecimientoResponse;
import com.lugares.api.entity.TipoEstablecimiento;
import com.lugares.api.mapper.TipoEstablecimientoMapper;
import com.lugares.api.service.TipoEstablecimientoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tipos-establecimiento")
@RequiredArgsConstructor
public class TipoEstablecimientoController {

    private final TipoEstablecimientoService tipoEstablecimientoService;
    private final TipoEstablecimientoMapper tipoEstablecimientoMapper;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TipoEstablecimientoResponse>>> listAll() {
        List<TipoEstablecimientoResponse> response = tipoEstablecimientoService.listAll().stream()
                .map(tipoEstablecimientoMapper::toDto)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TipoEstablecimientoResponse>> getById(@PathVariable Integer id) {
        TipoEstablecimientoResponse response = tipoEstablecimientoMapper.toDto(tipoEstablecimientoService.getById(id));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TipoEstablecimientoResponse>> create(@RequestBody TipoEstablecimiento tipo) {
        TipoEstablecimiento saved = tipoEstablecimientoService.create(tipo);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(tipoEstablecimientoMapper.toDto(saved)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TipoEstablecimientoResponse>> update(@PathVariable Integer id, @RequestBody TipoEstablecimiento tipo) {
        TipoEstablecimiento updated = tipoEstablecimientoService.update(id, tipo);
        return ResponseEntity.ok(ApiResponse.success(tipoEstablecimientoMapper.toDto(updated)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        tipoEstablecimientoService.delete(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}
