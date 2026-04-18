package com.lugares.api.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthenticationManager globalAuthenticationManager;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CorsProperties corsProperties;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs*/**").permitAll()
                        .requestMatchers("/api/contacto/**").permitAll()

                        .requestMatchers(HttpMethod.GET,
                                "/api/establecimientos/**",
                                "/api/promociones/**",
                                "/api/suscripciones/**",
                                "/api/tipos-establecimiento/**",
                                "/api/categorias-etiqueta/**",
                                "/api/capsulas-culturales/**",
                                "/api/comentarios/establecimiento/**",
                                "/api/etiquetas/visibles",
                                "/api/etiquetas/{id:\\d+}",
                                "/api/etiquetas/establecimiento/**",
                                "/api/etiquetas/tipo-establecimiento/**"
                        ).authenticated()

                        .requestMatchers("/api/calificaciones/**", "/api/fcm-tokens/**").hasRole("CLIENTE")
                        .requestMatchers(HttpMethod.POST, "/api/comentarios").hasRole("CLIENTE")

                        .requestMatchers("/api/usuarios/**", "/api/empresas/**", "/api/marcas/**").hasRole("USUARIO")
                        .requestMatchers("/api/notificaciones/**").hasRole("USUARIO")
                        .requestMatchers("/api/etiquetas/admin").hasRole("USUARIO")
                        .requestMatchers(HttpMethod.DELETE, "/api/comentarios/**").hasRole("USUARIO")

                        .requestMatchers(HttpMethod.POST,
                                "/api/establecimientos",
                                "/api/promociones",
                                "/api/etiquetas",
                                "/api/capsulas-culturales",
                                "/api/categorias-etiqueta",
                                "/api/tipos-establecimiento"
                        ).hasRole("USUARIO")
                        .requestMatchers(HttpMethod.PUT,
                                "/api/establecimientos/**",
                                "/api/promociones/**",
                                "/api/etiquetas/{id:\\d+}",
                                "/api/capsulas-culturales/**",
                                "/api/categorias-etiqueta/**",
                                "/api/tipos-establecimiento/**"
                        ).hasRole("USUARIO")
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/establecimientos/**",
                                "/api/promociones/**",
                                "/api/etiquetas/{id:\\d+}",
                                "/api/capsulas-culturales/**",
                                "/api/categorias-etiqueta/**",
                                "/api/tipos-establecimiento/**"
                        ).hasRole("USUARIO")

                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationManager(globalAuthenticationManager)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(corsProperties.getAllowedOrigins());
        configuration.setAllowedMethods(corsProperties.getAllowedMethods());
        configuration.setAllowedHeaders(corsProperties.getAllowedHeaders());
        configuration.setAllowCredentials(corsProperties.isAllowCredentials());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
