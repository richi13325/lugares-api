package com.lugares.api.controller;

import com.lugares.api.common.ApiResponse;
import com.lugares.api.dto.request.ClienteUpdateRequest;
import com.lugares.api.dto.response.ClienteListResponse;
import com.lugares.api.dto.response.ClienteResponse;
import com.lugares.api.entity.Cliente;
import com.lugares.api.mapper.ClienteMapper;
import com.lugares.api.service.ClienteService;
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

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;
    private final ClienteMapper clienteMapper;

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USUARIO') or (hasRole('CLIENTE') and #id == authentication.principal.id)")
    public ResponseEntity<ApiResponse<ClienteResponse>> getById(@PathVariable Integer id) {
        Cliente cliente = clienteService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(clienteMapper.toDto(cliente)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ClienteListResponse>>> list(
            @RequestParam(required = false) String nombre,
            Pageable pageable) {
        Page<ClienteListResponse> page = clienteService.list(nombre, pageable)
                .map(clienteMapper::toListDto);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CLIENTE') and #id == authentication.principal.id")
    public ResponseEntity<ApiResponse<ClienteResponse>> update(
            @PathVariable Integer id,
            @Valid @RequestBody ClienteUpdateRequest request) {
        Cliente entity = clienteMapper.toEntity(request);
        Cliente updated = clienteService.update(id, entity);
        return ResponseEntity.ok(ApiResponse.success(clienteMapper.toDto(updated)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        clienteService.delete(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}
