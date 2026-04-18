package com.lugares.api.controller;

import com.lugares.api.dto.response.ComentarioResponse;
import com.lugares.api.entity.Comentario;
import com.lugares.api.exception.ResourceNotFoundException;
import com.lugares.api.mapper.ComentarioMapper;
import com.lugares.api.service.ComentarioService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ComentarioController.class)
class ComentarioControllerTest extends BaseControllerTest {

    @MockitoBean
    private ComentarioService comentarioService;

    @MockitoBean
    private ComentarioMapper comentarioMapper;

    // ================================================================== //
    //  GET /api/comentarios/establecimiento/{establecimientoId}           //
    // ================================================================== //

    @Test
    void listByEstablecimiento_existingId_returnsOkWithList() throws Exception {
        // given
        Comentario c1 = new Comentario();
        c1.setId(1);

        ComentarioResponse response = new ComentarioResponse();
        response.setId(1);
        response.setComentario("Excelente lugar");

        when(comentarioService.listByEstablecimiento(10)).thenReturn(List.of(c1));
        when(comentarioMapper.toDto(any())).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/comentarios/establecimiento/10").with(asUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void listByEstablecimiento_nonExistentId_returnsNotFound() throws Exception {
        // given
        when(comentarioService.listByEstablecimiento(999))
                .thenThrow(new ResourceNotFoundException("Establecimiento", "id", 999));

        // when & then
        mockMvc.perform(get("/api/comentarios/establecimiento/999").with(asUsuario()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void listByEstablecimiento_unauthenticated_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/comentarios/establecimiento/10"))
                .andExpect(status().isForbidden());
    }

    // ================================================================== //
    //  POST /api/comentarios                                              //
    // ================================================================== //

    @Test
    void create_validRequest_returnsCreated() throws Exception {
        // given — service receives raw fields, NOT a mapped entity
        Comentario entity = new Comentario();
        entity.setId(1);

        ComentarioResponse response = new ComentarioResponse();
        response.setId(1);
        response.setComentario("Excelente lugar");

        when(comentarioService.create(1, 10, "Excelente lugar")).thenReturn(entity);
        when(comentarioMapper.toDto(entity)).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/comentarios")
                        .with(asClienteWithId(1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idEstablecimiento\":10,\"comentario\":\"Excelente lugar\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Recurso creado"));
    }

    @Test
    void create_nullFields_returnsBadRequest() throws Exception {
        // given — null id and blank comment
        mockMvc.perform(post("/api/comentarios")
                        .with(asClienteWithId(1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"comentario\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.idEstablecimiento").exists())
                .andExpect(jsonPath("$.fieldErrors.comentario").exists());
    }

    @Test
    void create_comentarioTooLong_returnsBadRequest() throws Exception {
        // given — 556 chars, max is 555
        String comentarioLargo = "C".repeat(556);

        mockMvc.perform(post("/api/comentarios")
                        .with(asClienteWithId(1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idEstablecimiento\":10,\"comentario\":\"" + comentarioLargo + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.comentario")
                        .value("El comentario no puede exceder 555 caracteres"));
    }

    // ================================================================== //
    //  DELETE /api/comentarios/{id}                                       //
    // ================================================================== //

    @Test
    void delete_existingId_returnsOkWithNoData() throws Exception {
        mockMvc.perform(delete("/api/comentarios/1").with(asUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(1))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void delete_nonExistentId_returnsNotFound() throws Exception {
        // given
        doThrow(new ResourceNotFoundException("Comentario", "id", 999))
                .when(comentarioService).delete(999);

        // when & then
        mockMvc.perform(delete("/api/comentarios/999").with(asUsuario()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }
}
