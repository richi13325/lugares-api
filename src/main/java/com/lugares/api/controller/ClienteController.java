package com.lugares.api.controller;

import com.lugares.api.common.ApiResponse;
import com.lugares.api.dto.request.ClienteUpdateRequest;
import com.lugares.api.dto.response.ClienteListResponse;
import com.lugares.api.dto.response.ClienteResponse;
import com.lugares.api.entity.Cliente;
import com.lugares.api.mapper.ClienteMapper;
import com.lugares.api.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Clientes", description = "Gestión de clientes finales (usuarios de la app)")
@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;
    private final ClienteMapper clienteMapper;

    @Operation(summary = "Obtener cliente por ID",
            description = "Devuelve el perfil de un cliente. USUARIO puede ver cualquiera; CLIENTE solo el propio.")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USUARIO') or (hasRole('CLIENTE') and #id == authentication.principal.id)")
    public ResponseEntity<ApiResponse<ClienteResponse>> getById(@PathVariable Integer id) {
        Cliente cliente = clienteService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(clienteMapper.toDto(cliente)));
    }

    @Operation(summary = "Listar clientes",
            description = "Devuelve una página de clientes, opcionalmente filtrada por nombre.")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ClienteListResponse>>> list(
            @RequestParam(required = false) String nombre,
            Pageable pageable) {
        Page<ClienteListResponse> page = clienteService.list(nombre, pageable)
                .map(clienteMapper::toListDto);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @Operation(summary = "Actualizar cliente",
            description = "CLIENTE puede actualizar únicamente su propio perfil.")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CLIENTE') and #id == authentication.principal.id")
    public ResponseEntity<ApiResponse<ClienteResponse>> update(
            @PathVariable Integer id,
            @Valid @RequestBody ClienteUpdateRequest request) {
        Cliente entity = clienteMapper.toEntity(request);
        Cliente updated = clienteService.update(id, entity);
        return ResponseEntity.ok(ApiResponse.success(clienteMapper.toDto(updated)));
    }

    @Operation(summary = "Eliminar cliente",
            description = "USUARIO puede eliminar cualquier cliente; CLIENTE solo puede eliminarse a sí mismo.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USUARIO') or (hasRole('CLIENTE') and #id == authentication.principal.id)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        clienteService.delete(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}
