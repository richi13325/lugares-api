package com.lugares.api.controller;

import com.lugares.api.common.ApiResponse;
import com.lugares.api.dto.request.AuthRequest;
import com.lugares.api.dto.request.ClienteRequest;
import com.lugares.api.dto.response.ClienteResponse;
import com.lugares.api.dto.response.LoginResponse;
import com.lugares.api.entity.Cliente;
import com.lugares.api.entity.Usuario;
import com.lugares.api.service.AuthService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@SecurityRequirements
@Tag(name = "Autenticación", description = "Endpoints públicos — no requieren token")
public class AuthController {

    private final AuthService authService;
    private final ModelMapper modelMapper;

    @PostMapping("/cliente/login")
    public ResponseEntity<ApiResponse<LoginResponse>> loginCliente(@Valid @RequestBody AuthRequest request) {
        Cliente cliente = authService.loginCliente(request.getCorreoElectronico(), request.getContrasenia());
        String token = authService.generateToken(cliente);
        LoginResponse response = new LoginResponse(token, authService.getExpirationTime());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/usuario/login")
    public ResponseEntity<ApiResponse<LoginResponse>> loginUsuario(@Valid @RequestBody AuthRequest request) {
        Usuario usuario = authService.loginUsuario(request.getCorreoElectronico(), request.getContrasenia());
        String token = authService.generateToken(usuario);
        LoginResponse response = new LoginResponse(token, authService.getExpirationTime());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/cliente/register")
    public ResponseEntity<ApiResponse<ClienteResponse>> registerCliente(@Valid @RequestBody ClienteRequest request) {
        Cliente entity = modelMapper.map(request, Cliente.class);
        Cliente saved = authService.registerCliente(entity);
        ClienteResponse response = modelMapper.map(saved, ClienteResponse.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }
}
