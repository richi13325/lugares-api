package com.lugares.api.controller;

import com.lugares.api.common.ApiResponse;
import com.lugares.api.dto.request.EstablecimientoRequest;
import com.lugares.api.dto.request.FiltroEstablecimientoRequest;
import com.lugares.api.dto.response.EstablecimientoDetailResponse;
import com.lugares.api.dto.response.EstablecimientoListResponse;
import com.lugares.api.dto.response.EstablecimientoResponse;
import com.lugares.api.entity.Empresa;
import com.lugares.api.entity.Establecimiento;
import com.lugares.api.entity.Suscripcion;
import com.lugares.api.entity.TipoEstablecimiento;
import com.lugares.api.service.EstablecimientoService;
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
@RequestMapping("/api/establecimientos")
@RequiredArgsConstructor
public class EstablecimientoController {

    private final EstablecimientoService establecimientoService;
    private final ModelMapper modelMapper;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EstablecimientoDetailResponse>> getById(@PathVariable Integer id) {
        Establecimiento establecimiento = establecimientoService.getById(id);
        EstablecimientoDetailResponse response = modelMapper.map(establecimiento, EstablecimientoDetailResponse.class);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<EstablecimientoListResponse>>> list(
            @RequestParam(required = false) String nombre,
            Pageable pageable) {
        Page<EstablecimientoListResponse> page = establecimientoService.list(nombre, pageable)
                .map(e -> modelMapper.map(e, EstablecimientoListResponse.class));
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @GetMapping("/tipo/{tipoId}")
    public ResponseEntity<ApiResponse<Page<EstablecimientoListResponse>>> listByTipo(
            @PathVariable Integer tipoId,
            @RequestParam(required = false) String nombre,
            Pageable pageable) {
        Page<EstablecimientoListResponse> page = establecimientoService.listByTipoEstablecimiento(tipoId, nombre, pageable)
                .map(e -> modelMapper.map(e, EstablecimientoListResponse.class));
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @PostMapping("/filtro")
    public ResponseEntity<ApiResponse<List<EstablecimientoResponse>>> filtrarPorEtiquetas(
            @Valid @RequestBody FiltroEstablecimientoRequest request) {
        List<Establecimiento> resultados = establecimientoService.findByEtiquetas(
                request.getEtiquetaIds(), request.isBusquedaEstricta());
        List<EstablecimientoResponse> response = resultados.stream()
                .map(e -> modelMapper.map(e, EstablecimientoResponse.class))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/sugeridos/{clienteId}")
    public ResponseEntity<ApiResponse<List<EstablecimientoResponse>>> sugeridos(@PathVariable Integer clienteId) {
        List<Establecimiento> resultados = establecimientoService.findSugeridos(clienteId);
        List<EstablecimientoResponse> response = resultados.stream()
                .map(e -> modelMapper.map(e, EstablecimientoResponse.class))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EstablecimientoDetailResponse>> create(
            @Valid @RequestBody EstablecimientoRequest request) {
        Establecimiento entity = mapRequestToEntity(request);
        Establecimiento saved = establecimientoService.create(entity);
        EstablecimientoDetailResponse response = modelMapper.map(saved, EstablecimientoDetailResponse.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EstablecimientoDetailResponse>> update(
            @PathVariable Integer id,
            @Valid @RequestBody EstablecimientoRequest request) {
        Establecimiento entity = mapRequestToEntity(request);
        Establecimiento updated = establecimientoService.update(id, entity);
        EstablecimientoDetailResponse response = modelMapper.map(updated, EstablecimientoDetailResponse.class);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        establecimientoService.delete(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }

    private Establecimiento mapRequestToEntity(EstablecimientoRequest request) {
        Establecimiento entity = modelMapper.map(request, Establecimiento.class);

        if (request.getIdSuscripcion() != null) {
            Suscripcion suscripcion = new Suscripcion();
            suscripcion.setId(request.getIdSuscripcion());
            entity.setSuscripcion(suscripcion);
        }
        if (request.getIdEmpresa() != null) {
            Empresa empresa = new Empresa();
            empresa.setId(request.getIdEmpresa());
            entity.setEmpresa(empresa);
        }
        if (request.getIdTipoEstablecimiento() != null) {
            TipoEstablecimiento tipo = new TipoEstablecimiento();
            tipo.setId(request.getIdTipoEstablecimiento());
            entity.setTipoEstablecimiento(tipo);
        }
        return entity;
    }
}
