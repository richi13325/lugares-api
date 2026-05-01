package com.lugares.api.controller;

import com.lugares.api.common.ApiResponse;
import com.lugares.api.dto.request.CategoriaEtiquetaRequest;
import com.lugares.api.dto.response.CategoriaEtiquetaResponse;
import com.lugares.api.mapper.CategoriaEtiquetaMapper;
import com.lugares.api.service.CategoriaEtiquetaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

@Tag(name = "Categorías de Etiqueta", description = "Agrupación de etiquetas por categoría")
@RestController
@RequestMapping("/api/categorias-etiqueta")
@RequiredArgsConstructor
public class CategoriaEtiquetaController {

    private final CategoriaEtiquetaService categoriaEtiquetaService;
    private final CategoriaEtiquetaMapper categoriaEtiquetaMapper;

    @Operation(
        summary = "Listar categorías de etiqueta",
        description = "Devuelve todas las categorías de etiqueta disponibles. Accesible para cualquier usuario autenticado."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoriaEtiquetaResponse>>> listAll() {
        List<CategoriaEtiquetaResponse> response = categoriaEtiquetaService.listAll().stream()
                .map(categoriaEtiquetaMapper::toDto)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
        summary = "Obtener categoría de etiqueta por ID",
        description = "Devuelve el detalle de una categoría de etiqueta. Accesible para cualquier usuario autenticado."
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoriaEtiquetaResponse>> getById(@PathVariable Integer id) {
        CategoriaEtiquetaResponse response = categoriaEtiquetaMapper.toDto(categoriaEtiquetaService.getById(id));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
        summary = "Crear categoría de etiqueta",
        description = "Crea una nueva categoría para agrupar etiquetas. Requiere rol USUARIO."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<CategoriaEtiquetaResponse>> create(@Valid @RequestBody CategoriaEtiquetaRequest request) {
        var saved = categoriaEtiquetaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(categoriaEtiquetaMapper.toDto(saved)));
    }

    @Operation(
        summary = "Actualizar categoría de etiqueta",
        description = "Actualiza el nombre o datos de una categoría de etiqueta existente. Requiere rol USUARIO."
    )
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoriaEtiquetaResponse>> update(@PathVariable Integer id, @Valid @RequestBody CategoriaEtiquetaRequest request) {
        var updated = categoriaEtiquetaService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(categoriaEtiquetaMapper.toDto(updated)));
    }

    @Operation(
        summary = "Eliminar categoría de etiqueta",
        description = "Elimina una categoría de etiqueta del sistema. Requiere rol USUARIO."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        categoriaEtiquetaService.delete(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}