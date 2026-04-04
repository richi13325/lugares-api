package com.lugares.api.controller;

import com.lugares.api.common.ApiResponse;
import com.lugares.api.dto.response.CategoriaEtiquetaResponse;
import com.lugares.api.entity.CategoriaEtiqueta;
import com.lugares.api.service.CategoriaEtiquetaService;
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
@RequestMapping("/api/categorias-etiqueta")
@RequiredArgsConstructor
public class CategoriaEtiquetaController {

    private final CategoriaEtiquetaService categoriaEtiquetaService;
    private final ModelMapper modelMapper;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoriaEtiquetaResponse>>> listAll() {
        List<CategoriaEtiquetaResponse> response = categoriaEtiquetaService.listAll().stream()
                .map(c -> modelMapper.map(c, CategoriaEtiquetaResponse.class))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoriaEtiquetaResponse>> getById(@PathVariable Integer id) {
        CategoriaEtiquetaResponse response = modelMapper.map(categoriaEtiquetaService.getById(id), CategoriaEtiquetaResponse.class);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CategoriaEtiquetaResponse>> create(@RequestBody CategoriaEtiqueta categoria) {
        CategoriaEtiqueta saved = categoriaEtiquetaService.create(categoria);
        CategoriaEtiquetaResponse response = modelMapper.map(saved, CategoriaEtiquetaResponse.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoriaEtiquetaResponse>> update(@PathVariable Integer id, @RequestBody CategoriaEtiqueta categoria) {
        CategoriaEtiqueta updated = categoriaEtiquetaService.update(id, categoria);
        CategoriaEtiquetaResponse response = modelMapper.map(updated, CategoriaEtiquetaResponse.class);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        categoriaEtiquetaService.delete(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}
