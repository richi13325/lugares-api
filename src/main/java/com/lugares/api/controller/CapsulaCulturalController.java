package com.lugares.api.controller;

import com.lugares.api.common.ApiResponse;
import com.lugares.api.dto.response.CapsulaCulturalResponse;
import com.lugares.api.entity.CapsulaCultural;
import com.lugares.api.service.CapsulaCulturalService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
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
    private final ModelMapper modelMapper;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CapsulaCulturalResponse>>> listAll() {
        List<CapsulaCulturalResponse> response = capsulaCulturalService.listAll().stream()
                .map(c -> modelMapper.map(c, CapsulaCulturalResponse.class))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CapsulaCulturalResponse>> getById(@PathVariable Integer id) {
        CapsulaCulturalResponse response = modelMapper.map(capsulaCulturalService.getById(id), CapsulaCulturalResponse.class);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CapsulaCulturalResponse>> create(@RequestBody CapsulaCultural capsula) {
        CapsulaCultural saved = capsulaCulturalService.create(capsula);
        CapsulaCulturalResponse response = modelMapper.map(saved, CapsulaCulturalResponse.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CapsulaCulturalResponse>> update(@PathVariable Integer id, @RequestBody CapsulaCultural capsula) {
        CapsulaCultural updated = capsulaCulturalService.update(id, capsula);
        CapsulaCulturalResponse response = modelMapper.map(updated, CapsulaCulturalResponse.class);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        capsulaCulturalService.delete(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}
