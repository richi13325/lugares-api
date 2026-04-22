package com.lugares.api.controller;

import com.lugares.api.common.ApiResponse;
import com.lugares.api.dto.request.ComentarioRequest;
import com.lugares.api.dto.response.ComentarioResponse;
import com.lugares.api.entity.Cliente;
import com.lugares.api.entity.Comentario;
import com.lugares.api.mapper.ComentarioMapper;
import com.lugares.api.service.ComentarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Comentarios", description = "Comentarios de clientes sobre establecimientos")
@RestController
@RequestMapping("/api/comentarios")
@RequiredArgsConstructor
public class ComentarioController {

    private final ComentarioService comentarioService;
    private final ComentarioMapper comentarioMapper;

    @Operation(summary = "Listar comentarios de un establecimiento",
            description = "Devuelve todos los comentarios asociados a un establecimiento dado su ID.")
    @GetMapping("/establecimiento/{establecimientoId}")
    public ResponseEntity<ApiResponse<List<ComentarioResponse>>> listByEstablecimiento(
            @PathVariable Integer establecimientoId) {
        List<ComentarioResponse> response = comentarioService.listByEstablecimiento(establecimientoId).stream()
                .map(comentarioMapper::toDto)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Crear comentario",
            description = "CLIENTE crea un comentario sobre un establecimiento. El cliente se obtiene del JWT.")
    @PostMapping
    public ResponseEntity<ApiResponse<ComentarioResponse>> create(
            @Valid @RequestBody ComentarioRequest request,
            @AuthenticationPrincipal Cliente principal) {
        Comentario saved = comentarioService.create(
                principal.getId(), request.getIdEstablecimiento(), request.getComentario());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(comentarioMapper.toDto(saved)));
    }

    @Operation(summary = "Eliminar comentario",
            description = "USUARIO puede eliminar cualquier comentario; CLIENTE solo puede eliminar el propio.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USUARIO') or (hasRole('CLIENTE') and @comentarioService.isOwner(#id, authentication.principal.id))")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        comentarioService.delete(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}
