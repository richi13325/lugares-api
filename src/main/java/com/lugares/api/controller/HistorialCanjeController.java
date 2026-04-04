package com.lugares.api.controller;

import com.lugares.api.common.ApiResponse;
import com.lugares.api.dto.response.HistorialCanjeResponse;
import com.lugares.api.entity.HistorialCanje;
import com.lugares.api.service.HistorialCanjeService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private final ModelMapper modelMapper;

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<ApiResponse<List<HistorialCanjeResponse>>> listByUsuario(@PathVariable Integer usuarioId) {
        List<HistorialCanjeResponse> response = historialCanjeService.listByUsuario(usuarioId).stream()
                .map(h -> modelMapper.map(h, HistorialCanjeResponse.class))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/promocion/{promocionId}")
    public ResponseEntity<ApiResponse<List<HistorialCanjeResponse>>> listByPromocion(@PathVariable Integer promocionId) {
        List<HistorialCanjeResponse> response = historialCanjeService.listByPromocion(promocionId).stream()
                .map(h -> modelMapper.map(h, HistorialCanjeResponse.class))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<HistorialCanjeResponse>> canjear(
            @RequestParam Integer promocionId,
            @RequestParam Integer usuarioId,
            @RequestParam String codigoValidacion) {
        HistorialCanje canje = historialCanjeService.canjear(promocionId, usuarioId, codigoValidacion);
        HistorialCanjeResponse response = modelMapper.map(canje, HistorialCanjeResponse.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        historialCanjeService.delete(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}
