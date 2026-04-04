package com.lugares.api.controller;

import com.lugares.api.common.ApiResponse;
import com.lugares.api.dto.request.ComentarioRequest;
import com.lugares.api.dto.response.ComentarioResponse;
import com.lugares.api.entity.Comentario;
import com.lugares.api.service.ComentarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/comentarios")
@RequiredArgsConstructor
public class ComentarioController {

    private final ComentarioService comentarioService;
    private final ModelMapper modelMapper;

    @GetMapping("/establecimiento/{establecimientoId}")
    public ResponseEntity<ApiResponse<List<ComentarioResponse>>> listByEstablecimiento(
            @PathVariable Integer establecimientoId) {
        List<ComentarioResponse> response = comentarioService.listByEstablecimiento(establecimientoId).stream()
                .map(c -> modelMapper.map(c, ComentarioResponse.class))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ComentarioResponse>> create(@Valid @RequestBody ComentarioRequest request) {
        Comentario saved = comentarioService.create(
                request.getIdCliente(), request.getIdEstablecimiento(), request.getComentario());
        ComentarioResponse response = modelMapper.map(saved, ComentarioResponse.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        comentarioService.delete(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}
