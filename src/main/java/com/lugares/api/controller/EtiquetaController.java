package com.lugares.api.controller;

import com.lugares.api.common.ApiResponse;
import com.lugares.api.dto.request.EtiquetaRequest;
import com.lugares.api.dto.response.EtiquetaAdminResponse;
import com.lugares.api.dto.response.EtiquetaResponse;
import com.lugares.api.entity.CategoriaEtiqueta;
import com.lugares.api.entity.Etiqueta;
import com.lugares.api.entity.EtiquetaCliente;
import com.lugares.api.entity.EtiquetaEstablecimiento;
import com.lugares.api.entity.EtiquetaTipoEstablecimiento;
import com.lugares.api.service.EtiquetaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
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

@RestController
@RequestMapping("/api/etiquetas")
@RequiredArgsConstructor
public class EtiquetaController {

    private final EtiquetaService etiquetaService;
    private final ModelMapper modelMapper;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EtiquetaResponse>> getById(@PathVariable Integer id) {
        Etiqueta etiqueta = etiquetaService.getById(id);
        EtiquetaResponse response = modelMapper.map(etiqueta, EtiquetaResponse.class);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<Page<EtiquetaAdminResponse>>> listAdmin(
            @RequestParam(required = false) String nombre,
            Pageable pageable) {
        Page<EtiquetaAdminResponse> page = etiquetaService.listAdmin(nombre, pageable)
                .map(e -> modelMapper.map(e, EtiquetaAdminResponse.class));
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @GetMapping("/visibles")
    public ResponseEntity<ApiResponse<List<EtiquetaResponse>>> listVisibles() {
        List<EtiquetaResponse> response = etiquetaService.listVisibles().stream()
                .map(e -> modelMapper.map(e, EtiquetaResponse.class))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EtiquetaAdminResponse>> create(@Valid @RequestBody EtiquetaRequest request) {
        Etiqueta entity = mapRequestToEntity(request);
        Etiqueta saved = etiquetaService.create(entity);
        EtiquetaAdminResponse response = modelMapper.map(saved, EtiquetaAdminResponse.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EtiquetaAdminResponse>> update(
            @PathVariable Integer id,
            @Valid @RequestBody EtiquetaRequest request) {
        Etiqueta entity = mapRequestToEntity(request);
        Etiqueta updated = etiquetaService.update(id, entity);
        EtiquetaAdminResponse response = modelMapper.map(updated, EtiquetaAdminResponse.class);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        etiquetaService.delete(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }

    // --- Asignaciones Cliente ---

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<ApiResponse<List<EtiquetaResponse>>> listByCliente(@PathVariable Integer clienteId) {
        List<EtiquetaResponse> response = etiquetaService.listByCliente(clienteId).stream()
                .map(ec -> modelMapper.map(ec.getEtiqueta(), EtiquetaResponse.class))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/cliente/{clienteId}/{etiquetaId}")
    public ResponseEntity<ApiResponse<Void>> assignToCliente(
            @PathVariable Integer clienteId,
            @PathVariable Integer etiquetaId) {
        etiquetaService.assignToCliente(clienteId, etiquetaId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.noContent());
    }

    @DeleteMapping("/cliente/{clienteId}/{etiquetaId}")
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
                .map(ee -> modelMapper.map(ee.getEtiqueta(), EtiquetaResponse.class))
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
                .map(ete -> modelMapper.map(ete.getEtiqueta(), EtiquetaResponse.class));
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    private Etiqueta mapRequestToEntity(EtiquetaRequest request) {
        Etiqueta entity = modelMapper.map(request, Etiqueta.class);
        if (request.getIdCategoria() != null) {
            CategoriaEtiqueta categoria = new CategoriaEtiqueta();
            categoria.setId(request.getIdCategoria());
            entity.setCategoria(categoria);
        }
        return entity;
    }
}
