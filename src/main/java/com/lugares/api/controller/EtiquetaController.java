package com.lugares.api.controller;

import com.lugares.api.common.ApiResponse;
import com.lugares.api.dto.request.EtiquetaRequest;
import com.lugares.api.dto.response.EtiquetaAdminResponse;
import com.lugares.api.dto.response.EtiquetaResponse;
import com.lugares.api.entity.Etiqueta;
import com.lugares.api.mapper.EtiquetaMapper;
import com.lugares.api.service.EtiquetaService;
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

@RestController
@RequestMapping("/api/etiquetas")
@RequiredArgsConstructor
public class EtiquetaController {

    private final EtiquetaService etiquetaService;
    private final EtiquetaMapper etiquetaMapper;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EtiquetaResponse>> getById(@PathVariable Integer id) {
        Etiqueta etiqueta = etiquetaService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(etiquetaMapper.toDto(etiqueta)));
    }

    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<Page<EtiquetaAdminResponse>>> listAdmin(
            @RequestParam(required = false) String nombre,
            Pageable pageable) {
        Page<EtiquetaAdminResponse> page = etiquetaService.listAdmin(nombre, pageable)
                .map(etiquetaMapper::toAdminDto);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @GetMapping("/visibles")
    public ResponseEntity<ApiResponse<List<EtiquetaResponse>>> listVisibles() {
        List<EtiquetaResponse> response = etiquetaService.listVisibles().stream()
                .map(etiquetaMapper::toDto)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EtiquetaAdminResponse>> create(@Valid @RequestBody EtiquetaRequest request) {
        Etiqueta entity = etiquetaMapper.toEntity(request);
        Etiqueta saved = etiquetaService.create(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(etiquetaMapper.toAdminDto(saved)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EtiquetaAdminResponse>> update(
            @PathVariable Integer id,
            @Valid @RequestBody EtiquetaRequest request) {
        Etiqueta entity = etiquetaMapper.toEntity(request);
        Etiqueta updated = etiquetaService.update(id, entity);
        return ResponseEntity.ok(ApiResponse.success(etiquetaMapper.toAdminDto(updated)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        etiquetaService.delete(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }

    // --- Asignaciones Cliente ---

    @GetMapping("/cliente/{clienteId}")
    @PreAuthorize("hasRole('CLIENTE') and #clienteId == authentication.principal.id")
    public ResponseEntity<ApiResponse<List<EtiquetaResponse>>> listByCliente(@PathVariable Integer clienteId) {
        List<EtiquetaResponse> response = etiquetaService.listByCliente(clienteId).stream()
                .map(ec -> etiquetaMapper.toDto(ec.getEtiqueta()))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/cliente/{clienteId}/{etiquetaId}")
    @PreAuthorize("hasRole('CLIENTE') and #clienteId == authentication.principal.id")
    public ResponseEntity<ApiResponse<Void>> assignToCliente(
            @PathVariable Integer clienteId,
            @PathVariable Integer etiquetaId) {
        etiquetaService.assignToCliente(clienteId, etiquetaId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.noContent());
    }

    @DeleteMapping("/cliente/{clienteId}/{etiquetaId}")
    @PreAuthorize("hasRole('CLIENTE') and #clienteId == authentication.principal.id")
    public ResponseEntity<ApiResponse<Void>> removeFromCliente(
            @PathVariable Integer clienteId,
            @PathVariable Integer etiquetaId) {
        etiquetaService.removeFromCliente(clienteId, etiquetaId);
        return ResponseEntity.ok(ApiResponse.noContent());
    }

    // --- Asignaciones Establecimiento ---

    @GetMapping("/establecimiento/{establecimientoId}")
    public ResponseEntity<ApiResponse<List<EtiquetaResponse>>> listByEstablecimiento(
            @PathVariable Integer establecimientoId) {
        List<EtiquetaResponse> response = etiquetaService.listByEstablecimiento(establecimientoId).stream()
                .map(ee -> etiquetaMapper.toDto(ee.getEtiqueta()))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/establecimiento/{establecimientoId}/{etiquetaId}")
    public ResponseEntity<ApiResponse<Void>> assignToEstablecimiento(
            @PathVariable Integer establecimientoId,
            @PathVariable Integer etiquetaId) {
        etiquetaService.assignToEstablecimiento(establecimientoId, etiquetaId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.noContent());
    }

    @DeleteMapping("/establecimiento/{establecimientoId}/{etiquetaId}")
    public ResponseEntity<ApiResponse<Void>> removeFromEstablecimiento(
            @PathVariable Integer establecimientoId,
            @PathVariable Integer etiquetaId) {
        etiquetaService.removeFromEstablecimiento(establecimientoId, etiquetaId);
        return ResponseEntity.ok(ApiResponse.noContent());
    }

    // --- Etiquetas por TipoEstablecimiento ---

    @GetMapping("/tipo-establecimiento/{tipoId}")
    public ResponseEntity<ApiResponse<Page<EtiquetaResponse>>> listByTipoEstablecimiento(
            @PathVariable Integer tipoId,
            Pageable pageable) {
        Page<EtiquetaResponse> page = etiquetaService.listByTipoEstablecimiento(tipoId, pageable)
                .map(ete -> etiquetaMapper.toDto(ete.getEtiqueta()));
        return ResponseEntity.ok(ApiResponse.success(page));
    }
}
