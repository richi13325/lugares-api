package com.lugares.api.controller;

import com.lugares.api.common.ApiResponse;
import com.lugares.api.dto.request.AuthRequest;
import com.lugares.api.dto.request.ClienteRequest;
import com.lugares.api.dto.response.ClienteResponse;
import com.lugares.api.dto.response.LoginResponse;
import com.lugares.api.entity.Cliente;
import com.lugares.api.entity.Usuario;
import com.lugares.api.service.AuthService;
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
public class AuthController {

    private final AuthService authService;
    private final ModelMapper modelMapper;

    @PostMapping("/cliente/login")
    public ResponseEntity<ApiResponse<LoginResponse>> loginCliente(@Valid @RequestBody AuthRequest request) {
        Cliente cliente = authService.loginCliente(request.getCorreoElectronico(), request.getContrasenia());
        String token = authService.generateToken(cliente, "ROLE_CLIENTE");
        LoginResponse response = new LoginResponse(token, authService.getExpirationTime());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/usuario/login")
    public ResponseEntity<ApiResponse<LoginResponse>> loginUsuario(@Valid @RequestBody AuthRequest request) {
        Usuario usuario = authService.loginUsuario(request.getCorreoElectronico(), request.getContrasenia());
        String token = authService.generateToken(usuario, "ROLE_USUARIO");
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
