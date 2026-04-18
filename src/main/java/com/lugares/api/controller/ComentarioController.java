package com.lugares.api.controller;

import com.lugares.api.common.ApiResponse;
import com.lugares.api.dto.request.ComentarioRequest;
import com.lugares.api.dto.response.ComentarioResponse;
import com.lugares.api.entity.Cliente;
import com.lugares.api.entity.Comentario;
import com.lugares.api.mapper.ComentarioMapper;
import com.lugares.api.service.ComentarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private final ComentarioMapper comentarioMapper;

    @GetMapping("/establecimiento/{establecimientoId}")
    public ResponseEntity<ApiResponse<List<ComentarioResponse>>> listByEstablecimiento(
            @PathVariable Integer establecimientoId) {
        List<ComentarioResponse> response = comentarioService.listByEstablecimiento(establecimientoId).stream()
                .map(comentarioMapper::toDto)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ComentarioResponse>> create(
            @Valid @RequestBody ComentarioRequest request,
            @AuthenticationPrincipal Cliente principal) {
        Comentario saved = comentarioService.create(
                principal.getId(), request.getIdEstablecimiento(), request.getComentario());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(comentarioMapper.toDto(saved)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        comentarioService.delete(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}
