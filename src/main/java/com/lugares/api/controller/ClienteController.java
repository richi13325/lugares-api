package com.lugares.api.controller;

import com.lugares.api.common.ApiResponse;
import com.lugares.api.dto.request.ClienteUpdateRequest;
import com.lugares.api.dto.response.ClienteListResponse;
import com.lugares.api.dto.response.ClienteResponse;
import com.lugares.api.entity.Cliente;
import com.lugares.api.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
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
    private final ModelMapper modelMapper;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClienteResponse>> getById(@PathVariable Integer id) {
        Cliente cliente = clienteService.getById(id);
        ClienteResponse response = modelMapper.map(cliente, ClienteResponse.class);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ClienteListResponse>>> list(
            @RequestParam(required = false) String nombre,
            Pageable pageable) {
        Page<ClienteListResponse> page = clienteService.list(nombre, pageable)
                .map(c -> modelMapper.map(c, ClienteListResponse.class));
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ClienteResponse>> update(
            @PathVariable Integer id,
            @Valid @RequestBody ClienteUpdateRequest request) {
        Cliente entity = modelMapper.map(request, Cliente.class);
        Cliente updated = clienteService.update(id, entity);
        ClienteResponse response = modelMapper.map(updated, ClienteResponse.class);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        clienteService.delete(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}
