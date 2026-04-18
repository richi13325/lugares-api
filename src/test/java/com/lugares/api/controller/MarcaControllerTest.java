package com.lugares.api.controller;

import com.lugares.api.dto.response.MarcaResponse;
import com.lugares.api.entity.Marca;
import com.lugares.api.exception.ResourceNotFoundException;
import com.lugares.api.mapper.MarcaMapper;
import com.lugares.api.service.MarcaService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MarcaController.class)
class MarcaControllerTest extends BaseControllerTest {

    @MockitoBean
    private MarcaService marcaService;

    @MockitoBean
    private MarcaMapper marcaMapper;

    // ================================================================== //
    //  GET /api/marcas                                                    //
    // ================================================================== //

    @Test
    void listAll_authenticated_returnsOkWithList() throws Exception {
        // given
        Marca entity1 = new Marca();
        entity1.setId(1);
        Marca entity2 = new Marca();
        entity2.setId(2);

        MarcaResponse response = new MarcaResponse();
        response.setId(1);
        response.setNombre("Nike");

        when(marcaService.listAll()).thenReturn(List.of(entity1, entity2));
        when(marcaMapper.toDto(any())).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/marcas").with(asUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(1))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void listAll_unauthenticated_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/marcas"))
                .andExpect(status().isForbidden());
    }

    // ================================================================== //
    //  GET /api/marcas/{id}                                               //
    // ================================================================== //

    @Test
    void getById_existingId_returnsOk() throws Exception {
        // given
        Marca entity = new Marca();
        entity.setId(1);

        MarcaResponse response = new MarcaResponse();
        response.setId(1);
        response.setNombre("Nike");

        when(marcaService.getById(1)).thenReturn(entity);
        when(marcaMapper.toDto(entity)).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/marcas/1").with(asUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.nombre").value("Nike"));
    }

    @Test
    void getById_nonExistentId_returnsNotFound() throws Exception {
        // given
        when(marcaService.getById(999))
                .thenThrow(new ResourceNotFoundException("Marca", "id", 999));

        // when & then
        mockMvc.perform(get("/api/marcas/999").with(asUsuario()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    // ================================================================== //
    //  POST /api/marcas                                                   //
    // ================================================================== //

    @Test
    void create_validEntity_returnsCreated() throws Exception {
        // given
        Marca savedEntity = new Marca();
        savedEntity.setId(1);

        MarcaResponse response = new MarcaResponse();
        response.setId(1);
        response.setNombre("Nueva");

        when(marcaService.create(any())).thenReturn(savedEntity);
        when(marcaMapper.toDto(savedEntity)).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/marcas")
                        .with(asUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Nueva\",\"descripcion\":\"Desc\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Recurso creado"))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    // ================================================================== //
    //  PUT /api/marcas/{id}                                               //
    // ================================================================== //

    @Test
    void update_existingId_returnsOk() throws Exception {
        // given
        Marca updated = new Marca();
        updated.setId(1);

        MarcaResponse response = new MarcaResponse();
        response.setId(1);

        when(marcaService.update(eq(1), any())).thenReturn(updated);
        when(marcaMapper.toDto(updated)).thenReturn(response);

        // when & then
        mockMvc.perform(put("/api/marcas/1")
                        .with(asUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Actualizado\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(1));
    }

    @Test
    void update_nonExistentId_returnsNotFound() throws Exception {
        // given
        when(marcaService.update(eq(999), any()))
                .thenThrow(new ResourceNotFoundException("Marca", "id", 999));

        // when & then
        mockMvc.perform(put("/api/marcas/999")
                        .with(asUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Updated\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    // ================================================================== //
    //  DELETE /api/marcas/{id}                                            //
    // ================================================================== //

    @Test
    void delete_existingId_returnsOkWithNoData() throws Exception {
        mockMvc.perform(delete("/api/marcas/1").with(asUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(1))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void delete_nonExistentId_returnsNotFound() throws Exception {
        // given
        doThrow(new ResourceNotFoundException("Marca", "id", 999))
                .when(marcaService).delete(999);

        // when & then
        mockMvc.perform(delete("/api/marcas/999").with(asUsuario()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }
}
