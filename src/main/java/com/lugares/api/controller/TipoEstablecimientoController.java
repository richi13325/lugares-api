package com.lugares.api.controller;

import com.lugares.api.common.ApiResponse;
import com.lugares.api.dto.response.TipoEstablecimientoResponse;
import com.lugares.api.entity.TipoEstablecimiento;
import com.lugares.api.mapper.TipoEstablecimientoMapper;
import com.lugares.api.service.TipoEstablecimientoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Tipos de Establecimiento", description = "Clasificación de establecimientos (restaurante, cafetería, etc.)")
@RestController
@RequestMapping("/api/tipos-establecimiento")
@RequiredArgsConstructor
public class TipoEstablecimientoController {

    private final TipoEstablecimientoService tipoEstablecimientoService;
    private final TipoEstablecimientoMapper tipoEstablecimientoMapper;

    @Operation(
        summary = "Listar tipos de establecimiento",
        description = "Devuelve todos los tipos de establecimiento disponibles. Accesible para cualquier usuario autenticado."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<TipoEstablecimientoResponse>>> listAll() {
        List<TipoEstablecimientoResponse> response = tipoEstablecimientoService.listAll().stream()
                .map(tipoEstablecimientoMapper::toDto)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
        summary = "Obtener tipo de establecimiento por ID",
        description = "Devuelve el detalle de un tipo de establecimiento. Accesible para cualquier usuario autenticado."
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TipoEstablecimientoResponse>> getById(@PathVariable Integer id) {
        TipoEstablecimientoResponse response = tipoEstablecimientoMapper.toDto(tipoEstablecimientoService.getById(id));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
        summary = "Crear tipo de establecimiento",
        description = "Crea una nueva clasificación de establecimiento. Requiere rol USUARIO."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<TipoEstablecimientoResponse>> create(@RequestBody TipoEstablecimiento tipo) {
        TipoEstablecimiento saved = tipoEstablecimientoService.create(tipo);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(tipoEstablecimientoMapper.toDto(saved)));
    }

    @Operation(
        summary = "Actualizar tipo de establecimiento",
        description = "Actualiza los datos de un tipo de establecimiento existente. Requiere rol USUARIO."
    )
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TipoEstablecimientoResponse>> update(@PathVariable Integer id, @RequestBody TipoEstablecimiento tipo) {
        TipoEstablecimiento updated = tipoEstablecimientoService.update(id, tipo);
        return ResponseEntity.ok(ApiResponse.success(tipoEstablecimientoMapper.toDto(updated)));
    }

    @Operation(
        summary = "Eliminar tipo de establecimiento",
        description = "Elimina una clasificación de establecimiento del sistema. Requiere rol USUARIO."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        tipoEstablecimientoService.delete(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}
