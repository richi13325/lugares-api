package com.lugares.api.controller;

import com.lugares.api.common.ApiResponse;
import com.lugares.api.dto.request.PromocionRequest;
import com.lugares.api.dto.response.PromocionListResponse;
import com.lugares.api.dto.response.PromocionResponse;
import com.lugares.api.entity.Establecimiento;
import com.lugares.api.entity.Promocion;
import com.lugares.api.entity.Suscripcion;
import com.lugares.api.entity.enums.TipoPromocion;
import com.lugares.api.service.PromocionService;
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
@RequestMapping("/api/promociones")
@RequiredArgsConstructor
public class PromocionController {

    private final PromocionService promocionService;
    private final ModelMapper modelMapper;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PromocionResponse>> getById(@PathVariable Integer id) {
        Promocion promocion = promocionService.getById(id);
        PromocionResponse response = modelMapper.map(promocion, PromocionResponse.class);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PromocionListResponse>>> list(
            @RequestParam(required = false) String nombre,
            Pageable pageable) {
        Page<PromocionListResponse> page = promocionService.list(nombre, pageable)
                .map(p -> modelMapper.map(p, PromocionListResponse.class));
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @GetMapping("/establecimiento/{establecimientoId}")
    public ResponseEntity<ApiResponse<List<PromocionResponse>>> listByEstablecimiento(
            @PathVariable Integer establecimientoId) {
        List<PromocionResponse> response = promocionService.listByEstablecimiento(establecimientoId).stream()
                .map(p -> modelMapper.map(p, PromocionResponse.class))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PromocionResponse>> create(@Valid @RequestBody PromocionRequest request) {
        Promocion entity = mapRequestToEntity(request);
        Promocion saved = promocionService.create(entity);
        PromocionResponse response = modelMapper.map(saved, PromocionResponse.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PromocionResponse>> update(
            @PathVariable Integer id,
            @Valid @RequestBody PromocionRequest request) {
        Promocion entity = mapRequestToEntity(request);
        Promocion updated = promocionService.update(id, entity);
        PromocionResponse response = modelMapper.map(updated, PromocionResponse.class);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        promocionService.delete(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }

    private Promocion mapRequestToEntity(PromocionRequest request) {
        Promocion entity = modelMapper.map(request, Promocion.class);

        if (request.getIdSuscripcion() != null) {
            Suscripcion suscripcion = new Suscripcion();
            suscripcion.setId(request.getIdSuscripcion());
            entity.setSuscripcion(suscripcion);
        }
        if (request.getIdEstablecimiento() != null) {
            Establecimiento establecimiento = new Establecimiento();
            establecimiento.setId(request.getIdEstablecimiento());
            entity.setEstablecimiento(establecimiento);
        }
        if (request.getTipoPromocion() != null) {
            entity.setTipoPromocion(TipoPromocion.valueOf(request.getTipoPromocion()));
        }
        return entity;
    }
}
