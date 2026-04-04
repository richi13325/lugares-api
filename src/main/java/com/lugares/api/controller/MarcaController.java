package com.lugares.api.controller;

import com.lugares.api.common.ApiResponse;
import com.lugares.api.dto.response.MarcaResponse;
import com.lugares.api.entity.Marca;
import com.lugares.api.service.MarcaService;
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
@RequestMapping("/api/marcas")
@RequiredArgsConstructor
public class MarcaController {

    private final MarcaService marcaService;
    private final ModelMapper modelMapper;

    @GetMapping
    public ResponseEntity<ApiResponse<List<MarcaResponse>>> listAll() {
        List<MarcaResponse> response = marcaService.listAll().stream()
                .map(m -> modelMapper.map(m, MarcaResponse.class))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MarcaResponse>> getById(@PathVariable Integer id) {
        MarcaResponse response = modelMapper.map(marcaService.getById(id), MarcaResponse.class);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MarcaResponse>> create(@RequestBody Marca marca) {
        Marca saved = marcaService.create(marca);
        MarcaResponse response = modelMapper.map(saved, MarcaResponse.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MarcaResponse>> update(@PathVariable Integer id, @RequestBody Marca marca) {
        Marca updated = marcaService.update(id, marca);
        MarcaResponse response = modelMapper.map(updated, MarcaResponse.class);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        marcaService.delete(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}
