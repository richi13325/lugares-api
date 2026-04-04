package com.lugares.api.config;

import com.lugares.api.repository.ClienteRepository;
import com.lugares.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;

    @Bean
    public UserDetailsService clienteUserDetailsService() {
        return username -> clienteRepository.findByCorreoElectronico(username)
                .orElseThrow(() -> new UsernameNotFoundException("Cliente no encontrado: " + username));
    }

    @Bean
    public UserDetailsService usuarioUserDetailsService() {
        return username -> usuarioRepository.findByCorreoElectronico(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider clienteAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(clienteUserDetailsService());
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationProvider usuarioAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(usuarioUserDetailsService());
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    @Primary
    public AuthenticationManager globalAuthenticationManager() {
        return new ProviderManager(List.of(clienteAuthenticationProvider(), usuarioAuthenticationProvider()));
    }

    @Bean
    public AuthenticationManager clienteAuthenticationManager() {
        return new ProviderManager(clienteAuthenticationProvider());
    }

    @Bean
    public AuthenticationManager usuarioAuthenticationManager() {
        return new ProviderManager(usuarioAuthenticationProvider());
    }
}
