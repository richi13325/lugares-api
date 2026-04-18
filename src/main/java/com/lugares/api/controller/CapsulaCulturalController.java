package com.lugares.api.controller;

import com.lugares.api.common.ApiResponse;
import com.lugares.api.dto.response.CapsulaCulturalResponse;
import com.lugares.api.entity.CapsulaCultural;
import com.lugares.api.mapper.CapsulaCulturalMapper;
import com.lugares.api.service.CapsulaCulturalService;
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
        CapsulaCulturalResponse response = capsulaCulturalMapper.toDto(capsulaCulturalService.getById(id));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CapsulaCulturalResponse>> create(@RequestBody CapsulaCultural capsula) {
        CapsulaCultural saved = capsulaCulturalService.create(capsula);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(capsulaCulturalMapper.toDto(saved)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CapsulaCulturalResponse>> update(@PathVariable Integer id, @RequestBody CapsulaCultural capsula) {
        CapsulaCultural updated = capsulaCulturalService.update(id, capsula);
        return ResponseEntity.ok(ApiResponse.success(capsulaCulturalMapper.toDto(updated)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        capsulaCulturalService.delete(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}
