package com.lugares.api.controller;

import com.lugares.api.dto.request.UsuarioRequest;
import com.lugares.api.dto.response.UsuarioListResponse;
import com.lugares.api.dto.response.UsuarioResponse;
import com.lugares.api.entity.Usuario;
import com.lugares.api.exception.DuplicateResourceException;
import com.lugares.api.exception.ResourceNotFoundException;
import com.lugares.api.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsuarioController.class)
class UsuarioControllerTest extends BaseControllerTest {

    @MockBean
    private UsuarioService usuarioService;

    // ================================================================== //
    //  GET /api/usuarios/{id}                                             //
    // ================================================================== //

    @Test
    void getById_existingId_returnsOk() throws Exception {
        // given
        Usuario entity = new Usuario();
        entity.setId(1);

        UsuarioResponse response = new UsuarioResponse();
        response.setId(1);
        response.setNombre("Admin User");
        response.setCorreoElectronico("admin@test.com");

        when(usuarioService.getById(1)).thenReturn(entity);
        when(modelMapper.map(entity, UsuarioResponse.class)).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/usuarios/1").with(asUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.nombre").value("Admin User"))
                .andExpect(jsonPath("$.data.correoElectronico").value("admin@test.com"));
    }

    @Test
    void getById_nonExistentId_returnsNotFound() throws Exception {
        // given
        when(usuarioService.getById(999))
                .thenThrow(new ResourceNotFoundException("Usuario", "id", 999));

        // when & then
        mockMvc.perform(get("/api/usuarios/999").with(asUsuario()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void getById_unauthenticated_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/usuarios/1"))
                .andExpect(status().isForbidden());
    }

    // ================================================================== //
    //  GET /api/usuarios?nombre=X (paginated)                            //
    // ================================================================== //

    @Test
    void list_authenticated_returnsOkWithPaginatedList() throws Exception {
        // given
        Usuario u1 = new Usuario();
        u1.setId(1);
        Page<Usuario> page = new PageImpl<>(List.of(u1), PageRequest.of(0, 10), 1);
        when(usuarioService.list(isNull(), any())).thenReturn(page);

        UsuarioListResponse listResponse = new UsuarioListResponse();
        listResponse.setId(1);
        when(modelMapper.map(any(), eq(UsuarioListResponse.class))).thenReturn(listResponse);

        // when & then
        mockMvc.perform(get("/api/usuarios")
                        .param("page", "0")
                        .param("size", "10")
                        .with(asUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void list_filterByNombre_returnsFilteredResults() throws Exception {
        // given
        Usuario u1 = new Usuario();
        u1.setId(2);
        Page<Usuario> page = new PageImpl<>(List.of(u1), PageRequest.of(0, 10), 1);
        when(usuarioService.list(eq("Carlos"), any())).thenReturn(page);

        UsuarioListResponse listResponse = new UsuarioListResponse();
        listResponse.setId(2);
        when(modelMapper.map(any(), eq(UsuarioListResponse.class))).thenReturn(listResponse);

        // when & then
        mockMvc.perform(get("/api/usuarios")
                        .param("nombre", "Carlos")
                        .param("page", "0")
                        .param("size", "10")
                        .with(asUsuario()))
                .andExpect(status().isOk());

        verify(usuarioService).list(eq("Carlos"), any());
    }

    // ================================================================== //
    //  POST /api/usuarios                                                 //
    // ================================================================== //

    @Test
    void create_validRequest_returnsCreated() throws Exception {
        // given
        Usuario entity = new Usuario();
        entity.setId(1);

        UsuarioResponse response = new UsuarioResponse();
        response.setId(1);
        response.setNombre("Nuevo Usuario");

        when(modelMapper.map(any(UsuarioRequest.class), eq(Usuario.class))).thenReturn(entity);
        when(usuarioService.create(any())).thenReturn(entity);
        when(modelMapper.map(entity, UsuarioResponse.class)).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/usuarios")
                        .with(asUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Nuevo Usuario\",\"correoElectronico\":\"nuevo@test.com\",\"contrasenia\":\"secret123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Recurso creado"));
    }

    @Test
    void create_blankNombre_returnsBadRequest() throws Exception {
        // when & then
        mockMvc.perform(post("/api/usuarios")
                        .with(asUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"\",\"correoElectronico\":\"nuevo@test.com\",\"contrasenia\":\"secret123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.nombre").exists());
    }

    @Test
    void create_blankEmailAndPassword_returnsBadRequest() throws Exception {
        // when & then
        mockMvc.perform(post("/api/usuarios")
                        .with(asUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Alguien\",\"correoElectronico\":\"\",\"contrasenia\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.correoElectronico").exists())
                .andExpect(jsonPath("$.fieldErrors.contrasenia").exists());
    }

    @Test
    void create_passwordTooShort_returnsBadRequest() throws Exception {
        // given — 5 chars, min is 6
        mockMvc.perform(post("/api/usuarios")
                        .with(asUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Alguien\",\"correoElectronico\":\"user@test.com\",\"contrasenia\":\"12345\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.contrasenia")
                        .value("La contrasenia debe tener al menos 6 caracteres"));
    }

    @Test
    void create_duplicateEmail_returnsConflict() throws Exception {
        // given
        String email = "duplicado@test.com";
        Usuario entity = new Usuario();
        when(modelMapper.map(any(UsuarioRequest.class), eq(Usuario.class))).thenReturn(entity);
        when(usuarioService.create(any()))
                .thenThrow(new DuplicateResourceException("Usuario", "correoElectronico", email));

        // when & then
        mockMvc.perform(post("/api/usuarios")
                        .with(asUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Alguien\",\"correoElectronico\":\"" + email + "\",\"contrasenia\":\"secret123\"}"))
                .andExpect(status().isConflict());
    }

    // ================================================================== //
    //  PUT /api/usuarios/{id}                                             //
    // ================================================================== //

    @Test
    void update_validRequest_returnsOk() throws Exception {
        // given
        Usuario entity = new Usuario();
        entity.setId(1);

        UsuarioResponse response = new UsuarioResponse();
        response.setId(1);

        when(modelMapper.map(any(UsuarioRequest.class), eq(Usuario.class))).thenReturn(entity);
        when(usuarioService.update(eq(1), any())).thenReturn(entity);
        when(modelMapper.map(entity, UsuarioResponse.class)).thenReturn(response);

        // when & then
        mockMvc.perform(put("/api/usuarios/1")
                        .with(asUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Actualizado\",\"correoElectronico\":\"act@test.com\",\"contrasenia\":\"newpass\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(1));
    }

    @Test
    void update_nonExistentId_returnsNotFound() throws Exception {
        // given
        Usuario entity = new Usuario();
        when(modelMapper.map(any(UsuarioRequest.class), eq(Usuario.class))).thenReturn(entity);
        when(usuarioService.update(eq(999), any()))
                .thenThrow(new ResourceNotFoundException("Usuario", "id", 999));

        // when & then
        mockMvc.perform(put("/api/usuarios/999")
                        .with(asUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Inexistente\",\"correoElectronico\":\"no@test.com\",\"contrasenia\":\"pass123\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void update_invalidEmail_returnsBadRequest() throws Exception {
        // when & then
        mockMvc.perform(put("/api/usuarios/1")
                        .with(asUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Alguien\",\"correoElectronico\":\"not-email\",\"contrasenia\":\"pass123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.correoElectronico").exists());
    }

    // ================================================================== //
    //  DELETE /api/usuarios/{id}                                          //
    // ================================================================== //

    @Test
    void delete_existingId_returnsOkWithNoData() throws Exception {
        mockMvc.perform(delete("/api/usuarios/1").with(asUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(1))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void delete_nonExistentId_returnsNotFound() throws Exception {
        // given
        doThrow(new ResourceNotFoundException("Usuario", "id", 999))
                .when(usuarioService).delete(999);

        // when & then
        mockMvc.perform(delete("/api/usuarios/999").with(asUsuario()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }
}
