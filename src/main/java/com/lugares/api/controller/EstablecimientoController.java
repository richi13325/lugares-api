package com.lugares.api.controller;

import com.lugares.api.common.ApiResponse;
import com.lugares.api.dto.request.EstablecimientoRequest;
import com.lugares.api.dto.request.FiltroEstablecimientoRequest;
import com.lugares.api.dto.response.EstablecimientoDetailResponse;
import com.lugares.api.dto.response.EstablecimientoListResponse;
import com.lugares.api.dto.response.EstablecimientoResponse;
import com.lugares.api.entity.Establecimiento;
import com.lugares.api.mapper.EstablecimientoMapper;
import com.lugares.api.service.EstablecimientoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Establecimientos", description = "Establecimientos registrados en la plataforma (negocios, lugares, puntos de interés)")
@RestController
@RequestMapping("/api/establecimientos")
@RequiredArgsConstructor
public class EstablecimientoController {

    private final EstablecimientoService establecimientoService;
    private final EstablecimientoMapper establecimientoMapper;

    @Operation(
        summary = "Obtener establecimiento por ID",
        description = "Devuelve el detalle completo de un establecimiento. Accesible para cualquier usuario autenticado."
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EstablecimientoDetailResponse>> getById(@PathVariable Integer id) {
        Establecimiento establecimiento = establecimientoService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(establecimientoMapper.toDetailDto(establecimiento)));
    }

    @Operation(
        summary = "Listar establecimientos",
        description = "Devuelve una página de establecimientos, opcionalmente filtrados por nombre. Accesible para cualquier usuario autenticado."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<Page<EstablecimientoListResponse>>> list(
            @RequestParam(required = false) String nombre,
            Pageable pageable) {
        Page<EstablecimientoListResponse> page = establecimientoService.list(nombre, pageable)
                .map(establecimientoMapper::toListDto);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @Operation(
        summary = "Listar establecimientos por tipo",
        description = "Devuelve establecimientos filtrados por tipo de establecimiento. Accesible para cualquier usuario autenticado."
    )
    @GetMapping("/tipo/{tipoId}")
    public ResponseEntity<ApiResponse<Page<EstablecimientoListResponse>>> listByTipo(
            @PathVariable Integer tipoId,
            @RequestParam(required = false) String nombre,
            Pageable pageable) {
        Page<EstablecimientoListResponse> page = establecimientoService.listByTipoEstablecimiento(tipoId, nombre, pageable)
                .map(establecimientoMapper::toListDto);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @Operation(
        summary = "Filtrar establecimientos por etiquetas",
        description = "Devuelve establecimientos que coincidan con las etiquetas indicadas (búsqueda estricta o parcial). Accesible para cualquier usuario autenticado."
    )
    @PostMapping("/filtro")
    public ResponseEntity<ApiResponse<List<EstablecimientoResponse>>> filtrarPorEtiquetas(
            @Valid @RequestBody FiltroEstablecimientoRequest request) {
        List<Establecimiento> resultados = establecimientoService.findByEtiquetas(
                request.getEtiquetaIds(), request.isBusquedaEstricta());
        List<EstablecimientoResponse> response = resultados.stream()
                .map(establecimientoMapper::toDto)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
        summary = "Obtener establecimientos sugeridos para cliente",
        description = "Devuelve establecimientos sugeridos basados en las etiquetas del cliente. Solo el propio CLIENTE puede consultar sus sugerencias."
    )
    @GetMapping("/sugeridos/{clienteId}")
    @PreAuthorize("hasRole('CLIENTE') and #clienteId == authentication.principal.id")
    public ResponseEntity<ApiResponse<List<EstablecimientoResponse>>> sugeridos(@PathVariable Integer clienteId) {
        List<Establecimiento> resultados = establecimientoService.findSugeridos(clienteId);
        List<EstablecimientoResponse> response = resultados.stream()
                .map(establecimientoMapper::toDto)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
        summary = "Crear establecimiento",
        description = "Crea un nuevo establecimiento en la plataforma. Requiere rol USUARIO."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<EstablecimientoDetailResponse>> create(
            @Valid @RequestBody EstablecimientoRequest request) {
        Establecimiento entity = establecimientoMapper.toEntity(request);
        Establecimiento saved = establecimientoService.create(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(establecimientoMapper.toDetailDto(saved)));
    }

    @Operation(
        summary = "Actualizar establecimiento",
        description = "Actualiza los datos de un establecimiento existente. Requiere rol USUARIO."
    )
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EstablecimientoDetailResponse>> update(
            @PathVariable Integer id,
            @Valid @RequestBody EstablecimientoRequest request) {
        Establecimiento entity = establecimientoMapper.toEntity(request);
        Establecimiento updated = establecimientoService.update(id, entity);
        return ResponseEntity.ok(ApiResponse.success(establecimientoMapper.toDetailDto(updated)));
    }

    @Operation(
        summary = "Eliminar establecimiento",
        description = "Elimina un establecimiento del sistema. Requiere rol USUARIO."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        establecimientoService.delete(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}
