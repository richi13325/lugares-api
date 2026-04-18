package com.lugares.api.controller;

import com.lugares.api.common.ApiResponse;
import com.lugares.api.dto.response.HistorialCanjeResponse;
import com.lugares.api.entity.Cliente;
import com.lugares.api.entity.HistorialCanje;
import com.lugares.api.mapper.HistorialCanjeMapper;
import com.lugares.api.service.HistorialCanjeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/historial-canjes")
@RequiredArgsConstructor
public class HistorialCanjeController {

    private final HistorialCanjeService historialCanjeService;
    private final HistorialCanjeMapper historialCanjeMapper;

    @GetMapping("/cliente/{clienteId}")
    @PreAuthorize("hasRole('USUARIO') or (hasRole('CLIENTE') and #clienteId == authentication.principal.id)")
    public ResponseEntity<ApiResponse<List<HistorialCanjeResponse>>> listByCliente(@PathVariable Integer clienteId) {
        List<HistorialCanjeResponse> response = historialCanjeService.listByCliente(clienteId).stream()
                .map(historialCanjeMapper::toDto)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/promocion/{promocionId}")
    public ResponseEntity<ApiResponse<List<HistorialCanjeResponse>>> listByPromocion(@PathVariable Integer promocionId) {
        List<HistorialCanjeResponse> response = historialCanjeService.listByPromocion(promocionId).stream()
                .map(historialCanjeMapper::toDto)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<ApiResponse<HistorialCanjeResponse>> canjear(
            @RequestParam Integer promocionId,
            @RequestParam String codigoValidacion,
            @AuthenticationPrincipal Cliente principal) {
        HistorialCanje canje = historialCanjeService.canjear(promocionId, principal.getId(), codigoValidacion);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(historialCanjeMapper.toDto(canje)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        historialCanjeService.delete(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}
