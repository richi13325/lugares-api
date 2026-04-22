package com.lugares.api.controller;

import com.lugares.api.common.ApiResponse;
import com.lugares.api.dto.request.PromocionRequest;
import com.lugares.api.dto.response.PromocionListResponse;
import com.lugares.api.dto.response.PromocionResponse;
import com.lugares.api.entity.Promocion;
import com.lugares.api.mapper.PromocionMapper;
import com.lugares.api.service.PromocionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

@Tag(name = "Promociones", description = "Promociones vigentes ofrecidas por establecimientos")
@RestController
@RequestMapping("/api/promociones")
@RequiredArgsConstructor
public class PromocionController {

    private final PromocionService promocionService;
    private final PromocionMapper promocionMapper;

    @Operation(
        summary = "Obtener promoción por ID",
        description = "Devuelve el detalle de una promoción específica. Accesible para cualquier usuario autenticado."
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PromocionResponse>> getById(@PathVariable Integer id) {
        Promocion promocion = promocionService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(promocionMapper.toDto(promocion)));
    }

    @Operation(
        summary = "Listar promociones",
        description = "Devuelve una página de promociones, opcionalmente filtradas por nombre. Accesible para cualquier usuario autenticado."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<Page<PromocionListResponse>>> list(
            @RequestParam(required = false) String nombre,
            Pageable pageable) {
        Page<PromocionListResponse> page = promocionService.list(nombre, pageable)
                .map(promocionMapper::toListDto);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @Operation(
        summary = "Listar promociones por establecimiento",
        description = "Devuelve todas las promociones activas de un establecimiento específico. Accesible para cualquier usuario autenticado."
    )
    @GetMapping("/establecimiento/{establecimientoId}")
    public ResponseEntity<ApiResponse<List<PromocionResponse>>> listByEstablecimiento(
            @PathVariable Integer establecimientoId) {
        List<PromocionResponse> response = promocionService.listByEstablecimiento(establecimientoId).stream()
                .map(promocionMapper::toDto)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
        summary = "Crear promoción",
        description = "Crea una nueva promoción asociada a un establecimiento. Requiere rol USUARIO."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<PromocionResponse>> create(@Valid @RequestBody PromocionRequest request) {
        Promocion entity = promocionMapper.toEntity(request);
        Promocion saved = promocionService.create(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(promocionMapper.toDto(saved)));
    }

    @Operation(
        summary = "Actualizar promoción",
        description = "Actualiza los datos de una promoción existente. Requiere rol USUARIO."
    )
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PromocionResponse>> update(
            @PathVariable Integer id,
            @Valid @RequestBody PromocionRequest request) {
        Promocion entity = promocionMapper.toEntity(request);
        Promocion updated = promocionService.update(id, entity);
        return ResponseEntity.ok(ApiResponse.success(promocionMapper.toDto(updated)));
    }

    @Operation(
        summary = "Eliminar promoción",
        description = "Elimina una promoción del sistema. Requiere rol USUARIO."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        promocionService.delete(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}
