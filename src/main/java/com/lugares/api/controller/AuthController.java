package com.lugares.api.controller;

import com.lugares.api.common.ApiResponse;
import com.lugares.api.dto.request.AuthRequest;
import com.lugares.api.dto.request.ClienteRequest;
import com.lugares.api.dto.response.ClienteResponse;
import com.lugares.api.dto.response.LoginResponse;
import com.lugares.api.entity.Cliente;
import com.lugares.api.entity.Usuario;
import com.lugares.api.mapper.ClienteMapper;
import com.lugares.api.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Autenticación", description = "Login, registro y refresh de JWT")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final ClienteMapper clienteMapper;

    @Operation(summary = "Login de cliente",
            description = "Autentica un cliente y devuelve un JWT con rol CLIENTE. Endpoint público, no requiere token.")
    @PostMapping("/cliente/login")
    public ResponseEntity<ApiResponse<LoginResponse>> loginCliente(@Valid @RequestBody AuthRequest request) {
        Cliente cliente = authService.loginCliente(request.getCorreoElectronico(), request.getContrasenia());
        String token = authService.generateToken(cliente, "ROLE_CLIENTE");
        LoginResponse response = new LoginResponse(token, authService.getExpirationTime());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Login de usuario administrador",
            description = "Autentica un usuario administrador y devuelve un JWT con rol USUARIO. Endpoint público, no requiere token.")
    @PostMapping("/usuario/login")
    public ResponseEntity<ApiResponse<LoginResponse>> loginUsuario(@Valid @RequestBody AuthRequest request) {
        Usuario usuario = authService.loginUsuario(request.getCorreoElectronico(), request.getContrasenia());
        String token = authService.generateToken(usuario, "ROLE_USUARIO");
        LoginResponse response = new LoginResponse(token, authService.getExpirationTime());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Registro de cliente",
            description = "Crea una nueva cuenta de cliente y devuelve el perfil creado. Endpoint público, no requiere token.")
    @PostMapping("/cliente/register")
    public ResponseEntity<ApiResponse<ClienteResponse>> registerCliente(@Valid @RequestBody ClienteRequest request) {
        Cliente entity = clienteMapper.toEntity(request);
        Cliente saved = authService.registerCliente(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(clienteMapper.toDto(saved)));
    }
}
