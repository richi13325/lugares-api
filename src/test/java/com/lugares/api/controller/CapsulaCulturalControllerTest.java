package com.lugares.api.controller;

import com.lugares.api.dto.response.CapsulaCulturalResponse;
import com.lugares.api.entity.CapsulaCultural;
import com.lugares.api.exception.ResourceNotFoundException;
import com.lugares.api.mapper.CapsulaCulturalMapper;
import com.lugares.api.service.CapsulaCulturalService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CapsulaCulturalController.class)
class CapsulaCulturalControllerTest extends BaseControllerTest {

    @MockitoBean
    private CapsulaCulturalService capsulaCulturalService;

    @MockitoBean
    private CapsulaCulturalMapper capsulaCulturalMapper;

    // ================================================================== //
    //  GET /api/capsulas-culturales                                       //
    // ================================================================== //

    @Test
    void listAll_authenticated_returnsOkWithList() throws Exception {
        // given
        CapsulaCultural entity1 = new CapsulaCultural();
        entity1.setId(1);
        CapsulaCultural entity2 = new CapsulaCultural();
        entity2.setId(2);

        CapsulaCulturalResponse response = new CapsulaCulturalResponse();
        response.setId(1);
        response.setTitulo("Historia del Tango");
        response.setEsVisible(true);

        when(capsulaCulturalService.listAll()).thenReturn(List.of(entity1, entity2));
        when(capsulaCulturalMapper.toDto(any())).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/capsulas-culturales").with(asUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(1))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void listAll_unauthenticated_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/capsulas-culturales"))
                .andExpect(status().isForbidden());
    }

    // ================================================================== //
    //  GET /api/capsulas-culturales/{id}                                  //
    // ================================================================== //

    @Test
    void getById_existingId_returnsOk() throws Exception {
        // given
        CapsulaCultural entity = new CapsulaCultural();
        entity.setId(1);

        CapsulaCulturalResponse response = new CapsulaCulturalResponse();
        response.setId(1);
        response.setTitulo("Historia del Tango");
        response.setEsVisible(true);

        when(capsulaCulturalService.getById(1)).thenReturn(entity);
        when(capsulaCulturalMapper.toDto(entity)).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/capsulas-culturales/1").with(asUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.titulo").value("Historia del Tango"))
                .andExpect(jsonPath("$.data.esVisible").value(true));
    }

    @Test
    void getById_nonExistentId_returnsNotFound() throws Exception {
        // given
        when(capsulaCulturalService.getById(999))
                .thenThrow(new ResourceNotFoundException("CapsulaCultural", "id", 999));

        // when & then
        mockMvc.perform(get("/api/capsulas-culturales/999").with(asUsuario()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    // ================================================================== //
    //  POST /api/capsulas-culturales                                      //
    // ================================================================== //

    @Test
    void create_validEntity_returnsCreated() throws Exception {
        // given
        CapsulaCultural savedEntity = new CapsulaCultural();
        savedEntity.setId(1);

        CapsulaCulturalResponse response = new CapsulaCulturalResponse();
        response.setId(1);
        response.setTitulo("Nueva Capsula");

        when(capsulaCulturalService.create(any())).thenReturn(savedEntity);
        when(capsulaCulturalMapper.toDto(savedEntity)).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/capsulas-culturales")
                        .with(asUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Nueva Capsula\",\"descripcion\":\"Desc\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Recurso creado"))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    // ================================================================== //
    //  PUT /api/capsulas-culturales/{id}                                  //
    // ================================================================== //

    @Test
    void update_existingId_returnsOk() throws Exception {
        // given
        CapsulaCultural updated = new CapsulaCultural();
        updated.setId(1);

        CapsulaCulturalResponse response = new CapsulaCulturalResponse();
        response.setId(1);

        when(capsulaCulturalService.update(eq(1), any())).thenReturn(updated);
        when(capsulaCulturalMapper.toDto(updated)).thenReturn(response);

        // when & then
        mockMvc.perform(put("/api/capsulas-culturales/1")
                        .with(asUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Actualizado\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(1));
    }

    @Test
    void update_nonExistentId_returnsNotFound() throws Exception {
        // given
        when(capsulaCulturalService.update(eq(999), any()))
                .thenThrow(new ResourceNotFoundException("CapsulaCultural", "id", 999));

        // when & then
        mockMvc.perform(put("/api/capsulas-culturales/999")
                        .with(asUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Updated\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    // ================================================================== //
    //  DELETE /api/capsulas-culturales/{id}                               //
    // ================================================================== //

    @Test
    void delete_existingId_returnsOkWithNoData() throws Exception {
        mockMvc.perform(delete("/api/capsulas-culturales/1").with(asUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(1))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void delete_nonExistentId_returnsNotFound() throws Exception {
        // given
        doThrow(new ResourceNotFoundException("CapsulaCultural", "id", 999))
                .when(capsulaCulturalService).delete(999);

        // when & then
        mockMvc.perform(delete("/api/capsulas-culturales/999").with(asUsuario()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }
}
