package com.lugares.api.controller;

import com.lugares.api.common.ApiResponse;
import com.lugares.api.dto.request.PromocionRequest;
import com.lugares.api.dto.response.PromocionListResponse;
import com.lugares.api.dto.response.PromocionResponse;
import com.lugares.api.entity.Promocion;
import com.lugares.api.mapper.PromocionMapper;
import com.lugares.api.service.PromocionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/promociones")
@RequiredArgsConstructor
public class PromocionController {

    private final PromocionService promocionService;
    private final PromocionMapper promocionMapper;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PromocionResponse>> getById(@PathVariable Integer id) {
        Promocion promocion = promocionService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(promocionMapper.toDto(promocion)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PromocionListResponse>>> list(
            @RequestParam(required = false) String nombre,
            Pageable pageable) {
        Page<PromocionListResponse> page = promocionService.list(nombre, pageable)
                .map(promocionMapper::toListDto);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @GetMapping("/establecimiento/{establecimientoId}")
    public ResponseEntity<ApiResponse<List<PromocionResponse>>> listByEstablecimiento(
            @PathVariable Integer establecimientoId) {
        List<PromocionResponse> response = promocionService.listByEstablecimiento(establecimientoId).stream()
                .map(promocionMapper::toDto)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PromocionResponse>> create(
            @RequestPart("data") @Valid PromocionRequest request,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen) {
        Promocion entity = promocionMapper.toEntity(request);
        Promocion saved = promocionService.create(entity, imagen);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(promocionMapper.toDto(saved)));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PromocionResponse>> update(
            @PathVariable Integer id,
            @RequestPart("data") @Valid PromocionRequest request,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen) {
        Promocion entity = promocionMapper.toEntity(request);
        Promocion updated = promocionService.update(id, entity, imagen);
        return ResponseEntity.ok(ApiResponse.success(promocionMapper.toDto(updated)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        promocionService.delete(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}
