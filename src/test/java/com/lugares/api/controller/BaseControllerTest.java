package com.lugares.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lugares.api.config.CorsProperties;
import com.lugares.api.config.JwtAuthenticationFilter;
import com.lugares.api.config.SecurityConfig;
import com.lugares.api.repository.ClienteRepository;
import com.lugares.api.repository.UsuarioRepository;
import com.lugares.api.service.JwtService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

/**
 * Abstract base class for all controller slice tests.
 * Imports the real SecurityConfig and JwtAuthenticationFilter so that
 * authentication/authorization rules are tested correctly.
 *
 * Every concrete test class must be annotated with @WebMvcTest(SomeController.class).
 */
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
public abstract class BaseControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    // ------------------------------------------------------------------ //
    //  Mocks required by JwtAuthenticationFilter                          //
    // ------------------------------------------------------------------ //

    @MockBean
    protected JwtService jwtService;

    @MockBean
    protected UsuarioRepository usuarioRepository;

    @MockBean
    protected ClienteRepository clienteRepository;

    // ------------------------------------------------------------------ //
    //  Mocks required by SecurityConfig                                   //
    // ------------------------------------------------------------------ //

    /**
     * SecurityConfig has @RequiredArgsConstructor and declares
     * "private final AuthenticationManager globalAuthenticationManager".
     * @WebMvcTest does not load ApplicationConfig, so this bean would
     * be missing without the MockBean below.
     */
    @MockBean(name = "globalAuthenticationManager")
    protected AuthenticationManager globalAuthenticationManager;

    @MockBean
    protected CorsProperties corsProperties;

    // ------------------------------------------------------------------ //
    //  Shared mock used by controllers that map DTOs                      //
    // ------------------------------------------------------------------ //

    @MockBean
    protected ModelMapper modelMapper;

    // ------------------------------------------------------------------ //
    //  Security helpers                                                   //
    // ------------------------------------------------------------------ //

    protected static RequestPostProcessor asUsuario() {
        return user("admin@test.com").roles("USUARIO");
    }

    protected static RequestPostProcessor asCliente() {
        return user("cliente@test.com").roles("CLIENTE");
    }
}
