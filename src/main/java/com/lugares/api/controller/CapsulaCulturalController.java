package com.lugares.api.controller;

import com.lugares.api.common.ApiResponse;
import com.lugares.api.dto.request.CapsulaCulturalRequest;
import com.lugares.api.dto.response.CapsulaCulturalResponse;
import com.lugares.api.entity.CapsulaCultural;
import com.lugares.api.mapper.CapsulaCulturalMapper;
import com.lugares.api.service.CapsulaCulturalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/capsulas-culturales")
@RequiredArgsConstructor
public class CapsulaCulturalController {

    private final CapsulaCulturalService capsulaCulturalService;
    private final CapsulaCulturalMapper capsulaCulturalMapper;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CapsulaCulturalResponse>>> listAll() {
        List<CapsulaCulturalResponse> response = capsulaCulturalService.listAll().stream()
                .map(capsulaCulturalMapper::toDto)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CapsulaCulturalResponse>> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(capsulaCulturalMapper.toDto(capsulaCulturalService.getById(id))));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CapsulaCulturalResponse>> create(
            @RequestPart("data") @Valid CapsulaCulturalRequest request,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen) {
        CapsulaCultural entity = capsulaCulturalMapper.toEntity(request);
        CapsulaCultural saved = capsulaCulturalService.create(entity, imagen);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(capsulaCulturalMapper.toDto(saved)));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CapsulaCulturalResponse>> update(
            @PathVariable Integer id,
            @RequestPart("data") @Valid CapsulaCulturalRequest request,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen) {
        CapsulaCultural entity = capsulaCulturalMapper.toEntity(request);
        CapsulaCultural updated = capsulaCulturalService.update(id, entity, imagen);
        return ResponseEntity.ok(ApiResponse.success(capsulaCulturalMapper.toDto(updated)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        capsulaCulturalService.delete(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}
