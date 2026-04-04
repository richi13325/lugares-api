package com.lugares.api.controller;

import com.lugares.api.dto.request.AuthRequest;
import com.lugares.api.dto.request.ClienteRequest;
import com.lugares.api.dto.response.ClienteResponse;
import com.lugares.api.entity.Cliente;
import com.lugares.api.entity.Usuario;
import com.lugares.api.exception.DuplicateResourceException;
import com.lugares.api.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest extends BaseControllerTest {

    @MockBean
    private AuthService authService;

    // ================================================================== //
    //  POST /auth/cliente/login                                           //
    // ================================================================== //

    @Test
    void loginCliente_validCredentials_returnsOkWithToken() throws Exception {
        // given
        Cliente cliente = new Cliente();
        cliente.setCorreoElectronico("test@test.com");

        when(authService.loginCliente("test@test.com", "password123")).thenReturn(cliente);
        when(authService.generateToken(cliente, "ROLE_CLIENTE")).thenReturn("jwt-token");
        when(authService.getExpirationTime()).thenReturn(3600000L);

        AuthRequest request = new AuthRequest();
        request.setCorreoElectronico("test@test.com");
        request.setContrasenia("password123");

        // when & then
        mockMvc.perform(post("/auth/cliente/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(1))
                .andExpect(jsonPath("$.message").value("Operacion exitosa"))
                .andExpect(jsonPath("$.data.token").value("jwt-token"))
                .andExpect(jsonPath("$.data.expiresIn").value(3600000));
    }

    @Test
    void loginCliente_blankEmail_returnsBadRequest() throws Exception {
        // given
        AuthRequest request = new AuthRequest();
        request.setCorreoElectronico("");
        request.setContrasenia("password123");

        // when & then
        mockMvc.perform(post("/auth/cliente/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.correoElectronico").exists());
    }

    @Test
    void loginCliente_invalidEmailFormat_returnsBadRequest() throws Exception {
        // given
        AuthRequest request = new AuthRequest();
        request.setCorreoElectronico("not-an-email");
        request.setContrasenia("password123");

        // when & then
        mockMvc.perform(post("/auth/cliente/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.correoElectronico").value("Formato de correo invalido"));
    }

    @Test
    void loginCliente_blankPassword_returnsBadRequest() throws Exception {
        // given
        AuthRequest request = new AuthRequest();
        request.setCorreoElectronico("test@test.com");
        request.setContrasenia("");

        // when & then
        mockMvc.perform(post("/auth/cliente/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.contrasenia").value("La contrasenia es obligatoria"));
    }

    @Test
    void loginCliente_badCredentials_returnsUnauthorized() throws Exception {
        // given
        when(authService.loginCliente(any(), any()))
                .thenThrow(new BadCredentialsException("Credenciales invalidas"));

        AuthRequest request = new AuthRequest();
        request.setCorreoElectronico("test@test.com");
        request.setContrasenia("wrongpass");

        // when & then
        mockMvc.perform(post("/auth/cliente/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    // ================================================================== //
    //  POST /auth/usuario/login                                           //
    // ================================================================== //

    @Test
    void loginUsuario_validCredentials_returnsOkWithToken() throws Exception {
        // given
        Usuario usuario = new Usuario();
        usuario.setCorreoElectronico("admin@test.com");

        when(authService.loginUsuario("admin@test.com", "adminpass")).thenReturn(usuario);
        when(authService.generateToken(usuario, "ROLE_USUARIO")).thenReturn("jwt-usuario-token");
        when(authService.getExpirationTime()).thenReturn(3600000L);

        AuthRequest request = new AuthRequest();
        request.setCorreoElectronico("admin@test.com");
        request.setContrasenia("adminpass");

        // when & then
        mockMvc.perform(post("/auth/usuario/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(1))
                .andExpect(jsonPath("$.data.token").value("jwt-usuario-token"))
                .andExpect(jsonPath("$.data.expiresIn").value(3600000));
    }

    @Test
    void loginUsuario_bothFieldsBlank_returnsBadRequestWithBothFieldErrors() throws Exception {
        // given — empty JSON object, both required fields absent/blank
        AuthRequest request = new AuthRequest();
        request.setCorreoElectronico("");
        request.setContrasenia("");

        // when & then
        mockMvc.perform(post("/auth/usuario/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.correoElectronico").exists())
                .andExpect(jsonPath("$.fieldErrors.contrasenia").exists());
    }

    // ================================================================== //
    //  POST /auth/cliente/register                                        //
    // ================================================================== //

    @Test
    void registerCliente_validRequest_returnsCreated() throws Exception {
        // given
        Cliente savedCliente = new Cliente();
        savedCliente.setId(1);
        savedCliente.setCorreoElectronico("nuevo@test.com");
        savedCliente.setNombre("Nuevo Cliente");

        ClienteResponse clienteResponse = new ClienteResponse();
        clienteResponse.setId(1);
        clienteResponse.setCorreoElectronico("nuevo@test.com");
        clienteResponse.setNombre("Nuevo Cliente");

        when(modelMapper.map(any(), eq(Cliente.class))).thenReturn(savedCliente);
        when(authService.registerCliente(savedCliente)).thenReturn(savedCliente);
        when(modelMapper.map(savedCliente, ClienteResponse.class)).thenReturn(clienteResponse);

        ClienteRequest request = new ClienteRequest();
        request.setNombre("Nuevo Cliente");
        request.setCorreoElectronico("nuevo@test.com");
        request.setContrasenia("secret123");

        // when & then
        mockMvc.perform(post("/auth/cliente/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(1))
                .andExpect(jsonPath("$.message").value("Recurso creado"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.correoElectronico").value("nuevo@test.com"));
    }

    @Test
    void registerCliente_missingRequiredFields_returnsBadRequestWithFieldErrors() throws Exception {
        // given — nombre, correoElectronico, contrasenia all blank
        ClienteRequest request = new ClienteRequest();
        request.setNombre("");
        request.setCorreoElectronico("");
        request.setContrasenia("");

        // when & then
        mockMvc.perform(post("/auth/cliente/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.nombre").exists())
                .andExpect(jsonPath("$.fieldErrors.correoElectronico").exists())
                .andExpect(jsonPath("$.fieldErrors.contrasenia").exists());
    }

    @Test
    void registerCliente_duplicateEmail_returnsConflict() throws Exception {
        // given
        Cliente entity = new Cliente();

        when(modelMapper.map(any(), eq(Cliente.class))).thenReturn(entity);
        when(authService.registerCliente(any()))
                .thenThrow(new DuplicateResourceException("Cliente", "correoElectronico", "dup@test.com"));

        ClienteRequest request = new ClienteRequest();
        request.setNombre("Duplicado");
        request.setCorreoElectronico("dup@test.com");
        request.setContrasenia("pass123");

        // when & then
        mockMvc.perform(post("/auth/cliente/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    void registerCliente_passwordTooShort_returnsBadRequest() throws Exception {
        // given — contrasenia has only 5 characters
        ClienteRequest request = new ClienteRequest();
        request.setNombre("Test User");
        request.setCorreoElectronico("test@test.com");
        request.setContrasenia("abc12"); // 5 chars — below @Size(min=6)

        // when & then
        mockMvc.perform(post("/auth/cliente/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.contrasenia")
                        .value("La contrasenia debe tener al menos 6 caracteres"));
    }
}
