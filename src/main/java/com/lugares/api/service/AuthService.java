package com.lugares.api.service;

import com.lugares.api.entity.Cliente;
import com.lugares.api.entity.Usuario;
import com.lugares.api.exception.ResourceNotFoundException;
import com.lugares.api.repository.ClienteRepository;
import com.lugares.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    @Qualifier("clienteAuthenticationManager")
    private final AuthenticationManager clienteAuthManager;
    @Qualifier("usuarioAuthenticationManager")
    private final AuthenticationManager usuarioAuthManager;

    public Cliente loginCliente(String correo, String contrasenia) {
        clienteAuthManager.authenticate(
                new UsernamePasswordAuthenticationToken(correo, contrasenia));
        return clienteRepository.findByCorreoElectronico(correo)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "correoElectronico", correo));
    }

    public Usuario loginUsuario(String correo, String contrasenia) {
        usuarioAuthManager.authenticate(
                new UsernamePasswordAuthenticationToken(correo, contrasenia));
        return usuarioRepository.findByCorreoElectronico(correo)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "correoElectronico", correo));
    }

    @Transactional
    public Cliente registerCliente(Cliente cliente) {
        cliente.setContrasenia(passwordEncoder.encode(cliente.getContrasenia()));
        return clienteRepository.save(cliente);
    }

    public String generateToken(org.springframework.security.core.userdetails.UserDetails userDetails) {
        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                .orElse("");
        return jwtService.generateToken(Map.of("role", role), userDetails);
    }

    public long getExpirationTime() {
        return jwtService.getExpirationTime();
    }
}
