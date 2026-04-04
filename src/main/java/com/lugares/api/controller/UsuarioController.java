package com.lugares.api.controller;

import com.lugares.api.common.ApiResponse;
import com.lugares.api.dto.request.UsuarioRequest;
import com.lugares.api.dto.response.UsuarioListResponse;
import com.lugares.api.dto.response.UsuarioResponse;
import com.lugares.api.entity.Usuario;
import com.lugares.api.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final ModelMapper modelMapper;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UsuarioResponse>> getById(@PathVariable Integer id) {
        Usuario usuario = usuarioService.getById(id);
        UsuarioResponse response = modelMapper.map(usuario, UsuarioResponse.class);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<UsuarioListResponse>>> list(
            @RequestParam(required = false) String nombre,
            Pageable pageable) {
        Page<UsuarioListResponse> page = usuarioService.list(nombre, pageable)
                .map(u -> modelMapper.map(u, UsuarioListResponse.class));
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UsuarioResponse>> create(@Valid @RequestBody UsuarioRequest request) {
        Usuario entity = modelMapper.map(request, Usuario.class);
        Usuario saved = usuarioService.create(entity);
        UsuarioResponse response = modelMapper.map(saved, UsuarioResponse.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UsuarioResponse>> update(
            @PathVariable Integer id,
            @Valid @RequestBody UsuarioRequest request) {
        Usuario entity = modelMapper.map(request, Usuario.class);
        Usuario updated = usuarioService.update(id, entity);
        UsuarioResponse response = modelMapper.map(updated, UsuarioResponse.class);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        usuarioService.delete(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}
