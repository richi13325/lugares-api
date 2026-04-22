package com.lugares.api.controller;

import com.lugares.api.common.ApiResponse;
import com.lugares.api.dto.response.CapsulaCulturalResponse;
import com.lugares.api.entity.CapsulaCultural;
import com.lugares.api.mapper.CapsulaCulturalMapper;
import com.lugares.api.service.CapsulaCulturalService;
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

@Tag(name = "Cápsulas Culturales", description = "Contenido cultural asociado a lugares o temas")
@RestController
@RequestMapping("/api/capsulas-culturales")
@RequiredArgsConstructor
public class CapsulaCulturalController {

    private final CapsulaCulturalService capsulaCulturalService;
    private final CapsulaCulturalMapper capsulaCulturalMapper;

    @Operation(
        summary = "Listar cápsulas culturales",
        description = "Devuelve todas las cápsulas culturales disponibles. Accesible para cualquier usuario autenticado."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<CapsulaCulturalResponse>>> listAll() {
        List<CapsulaCulturalResponse> response = capsulaCulturalService.listAll().stream()
                .map(capsulaCulturalMapper::toDto)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
        summary = "Obtener cápsula cultural por ID",
        description = "Devuelve el detalle de una cápsula cultural. Accesible para cualquier usuario autenticado."
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CapsulaCulturalResponse>> getById(@PathVariable Integer id) {
        CapsulaCulturalResponse response = capsulaCulturalMapper.toDto(capsulaCulturalService.getById(id));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
        summary = "Crear cápsula cultural",
        description = "Crea una nueva cápsula de contenido cultural. Requiere rol USUARIO."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<CapsulaCulturalResponse>> create(@RequestBody CapsulaCultural capsula) {
        CapsulaCultural saved = capsulaCulturalService.create(capsula);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(capsulaCulturalMapper.toDto(saved)));
    }

    @Operation(
        summary = "Actualizar cápsula cultural",
        description = "Actualiza el contenido de una cápsula cultural existente. Requiere rol USUARIO."
    )
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CapsulaCulturalResponse>> update(@PathVariable Integer id, @RequestBody CapsulaCultural capsula) {
        CapsulaCultural updated = capsulaCulturalService.update(id, capsula);
        return ResponseEntity.ok(ApiResponse.success(capsulaCulturalMapper.toDto(updated)));
    }

    @Operation(
        summary = "Eliminar cápsula cultural",
        description = "Elimina una cápsula cultural del sistema. Requiere rol USUARIO."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        capsulaCulturalService.delete(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}
