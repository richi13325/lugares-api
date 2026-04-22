package com.lugares.api.controller;

import com.lugares.api.common.ApiResponse;
import com.lugares.api.dto.request.UsuarioRequest;
import com.lugares.api.dto.response.UsuarioListResponse;
import com.lugares.api.dto.response.UsuarioResponse;
import com.lugares.api.entity.Usuario;
import com.lugares.api.mapper.UsuarioMapper;
import com.lugares.api.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Usuarios", description = "Administradores del sistema")
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioMapper usuarioMapper;

    @Operation(summary = "Obtener usuario por ID",
            description = "Devuelve el perfil de un usuario administrador.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UsuarioResponse>> getById(@PathVariable Integer id) {
        Usuario usuario = usuarioService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(usuarioMapper.toDto(usuario)));
    }

    @Operation(summary = "Listar usuarios",
            description = "Devuelve una página de usuarios administradores, opcionalmente filtrada por nombre.")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<UsuarioListResponse>>> list(
            @RequestParam(required = false) String nombre,
            Pageable pageable) {
        Page<UsuarioListResponse> page = usuarioService.list(nombre, pageable)
                .map(usuarioMapper::toListDto);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @Operation(summary = "Crear usuario",
            description = "Crea un nuevo usuario administrador.")
    @PostMapping
    public ResponseEntity<ApiResponse<UsuarioResponse>> create(@Valid @RequestBody UsuarioRequest request) {
        Usuario entity = usuarioMapper.toEntity(request);
        Usuario saved = usuarioService.create(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(usuarioMapper.toDto(saved)));
    }

    @Operation(summary = "Actualizar usuario",
            description = "Actualiza un usuario administrador. Solo USUARIO puede realizar esta operación.")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USUARIO')")
    public ResponseEntity<ApiResponse<UsuarioResponse>> update(
            @PathVariable Integer id,
            @Valid @RequestBody UsuarioRequest request) {
        Usuario entity = usuarioMapper.toEntity(request);
        Usuario updated = usuarioService.update(id, entity);
        return ResponseEntity.ok(ApiResponse.success(usuarioMapper.toDto(updated)));
    }

    @Operation(summary = "Eliminar usuario",
            description = "Elimina un usuario administrador. Solo USUARIO puede realizar esta operación.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USUARIO')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        usuarioService.delete(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}
