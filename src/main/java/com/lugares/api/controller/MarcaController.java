package com.lugares.api.controller;

import com.lugares.api.common.ApiResponse;
import com.lugares.api.dto.response.MarcaResponse;
import com.lugares.api.entity.Marca;
import com.lugares.api.mapper.MarcaMapper;
import com.lugares.api.service.MarcaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Marcas", description = "Marcas registradas en el sistema")
@RestController
@RequestMapping("/api/marcas")
@RequiredArgsConstructor
public class MarcaController {

    private final MarcaService marcaService;
    private final MarcaMapper marcaMapper;

    @Operation(
        summary = "Listar marcas",
        description = "Devuelve todas las marcas registradas en el sistema. Requiere rol USUARIO."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<MarcaResponse>>> listAll() {
        List<MarcaResponse> response = marcaService.listAll().stream()
                .map(marcaMapper::toDto)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
        summary = "Obtener marca por ID",
        description = "Devuelve el detalle de una marca. Requiere rol USUARIO."
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MarcaResponse>> getById(@PathVariable Integer id) {
        MarcaResponse response = marcaMapper.toDto(marcaService.getById(id));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
        summary = "Crear marca",
        description = "Registra una nueva marca en el sistema. Requiere rol USUARIO."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<MarcaResponse>> create(@RequestBody Marca marca) {
        Marca saved = marcaService.create(marca);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(marcaMapper.toDto(saved)));
    }

    @Operation(
        summary = "Actualizar marca",
        description = "Actualiza los datos de una marca existente. Requiere rol USUARIO."
    )
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MarcaResponse>> update(@PathVariable Integer id, @RequestBody Marca marca) {
        Marca updated = marcaService.update(id, marca);
        return ResponseEntity.ok(ApiResponse.success(marcaMapper.toDto(updated)));
    }

    @Operation(
        summary = "Eliminar marca",
        description = "Elimina una marca del sistema. Requiere rol USUARIO."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        marcaService.delete(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}
