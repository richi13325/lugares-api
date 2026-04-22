package com.lugares.api.controller;

import com.lugares.api.common.ApiResponse;
import com.lugares.api.dto.response.EmpresaResponse;
import com.lugares.api.entity.Empresa;
import com.lugares.api.mapper.EmpresaMapper;
import com.lugares.api.service.EmpresaService;
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

@Tag(name = "Empresas", description = "Empresas dueñas de establecimientos/marcas")
@RestController
@RequestMapping("/api/empresas")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaService empresaService;
    private final EmpresaMapper empresaMapper;

    @Operation(
        summary = "Listar empresas",
        description = "Devuelve todas las empresas registradas en el sistema. Requiere rol USUARIO."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<EmpresaResponse>>> listAll() {
        List<EmpresaResponse> response = empresaService.listAll().stream()
                .map(empresaMapper::toDto)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
        summary = "Obtener empresa por ID",
        description = "Devuelve el detalle de una empresa. Requiere rol USUARIO."
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmpresaResponse>> getById(@PathVariable Integer id) {
        EmpresaResponse response = empresaMapper.toDto(empresaService.getById(id));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
        summary = "Crear empresa",
        description = "Registra una nueva empresa en el sistema. Requiere rol USUARIO."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<EmpresaResponse>> create(@RequestBody Empresa empresa) {
        Empresa saved = empresaService.create(empresa);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(empresaMapper.toDto(saved)));
    }

    @Operation(
        summary = "Actualizar empresa",
        description = "Actualiza los datos de una empresa existente. Requiere rol USUARIO."
    )
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EmpresaResponse>> update(@PathVariable Integer id, @RequestBody Empresa empresa) {
        Empresa updated = empresaService.update(id, empresa);
        return ResponseEntity.ok(ApiResponse.success(empresaMapper.toDto(updated)));
    }

    @Operation(
        summary = "Eliminar empresa",
        description = "Elimina una empresa del sistema. Requiere rol USUARIO."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        empresaService.delete(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}
