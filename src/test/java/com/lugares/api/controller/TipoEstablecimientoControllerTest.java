package com.lugares.api.controller;

import com.lugares.api.dto.response.TipoEstablecimientoResponse;
import com.lugares.api.entity.TipoEstablecimiento;
import com.lugares.api.exception.ResourceNotFoundException;
import com.lugares.api.mapper.TipoEstablecimientoMapper;
import com.lugares.api.service.TipoEstablecimientoService;
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

@WebMvcTest(TipoEstablecimientoController.class)
class TipoEstablecimientoControllerTest extends BaseControllerTest {

    @MockitoBean
    private TipoEstablecimientoService tipoEstablecimientoService;

    @MockitoBean
    private TipoEstablecimientoMapper tipoEstablecimientoMapper;

    // ================================================================== //
    //  GET /api/tipos-establecimiento                                     //
    // ================================================================== //

    @Test
    void listAll_authenticated_returnsOkWithList() throws Exception {
        // given
        TipoEstablecimiento entity1 = new TipoEstablecimiento();
        entity1.setId(1);
        TipoEstablecimiento entity2 = new TipoEstablecimiento();
        entity2.setId(2);

        TipoEstablecimientoResponse response = new TipoEstablecimientoResponse();
        response.setId(1);
        response.setNombre("Restaurante");

        when(tipoEstablecimientoService.listAll()).thenReturn(List.of(entity1, entity2));
        when(tipoEstablecimientoMapper.toDto(any())).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/tipos-establecimiento").with(asUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(1))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void listAll_unauthenticated_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/tipos-establecimiento"))
                .andExpect(status().isForbidden());
    }

    // ================================================================== //
    //  GET /api/tipos-establecimiento/{id}                                //
    // ================================================================== //

    @Test
    void getById_existingId_returnsOk() throws Exception {
        // given
        TipoEstablecimiento entity = new TipoEstablecimiento();
        entity.setId(1);

        TipoEstablecimientoResponse response = new TipoEstablecimientoResponse();
        response.setId(1);
        response.setNombre("Restaurante");

        when(tipoEstablecimientoService.getById(1)).thenReturn(entity);
        when(tipoEstablecimientoMapper.toDto(entity)).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/tipos-establecimiento/1").with(asUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.nombre").value("Restaurante"));
    }

    @Test
    void getById_nonExistentId_returnsNotFound() throws Exception {
        // given
        when(tipoEstablecimientoService.getById(999))
                .thenThrow(new ResourceNotFoundException("TipoEstablecimiento", "id", 999));

        // when & then
        mockMvc.perform(get("/api/tipos-establecimiento/999").with(asUsuario()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    // ================================================================== //
    //  POST /api/tipos-establecimiento                                    //
    // ================================================================== //

    @Test
    void create_validEntity_returnsCreated() throws Exception {
        // given
        TipoEstablecimiento savedEntity = new TipoEstablecimiento();
        savedEntity.setId(1);

        TipoEstablecimientoResponse response = new TipoEstablecimientoResponse();
        response.setId(1);
        response.setNombre("Nuevo");

        when(tipoEstablecimientoService.create(any())).thenReturn(savedEntity);
        when(tipoEstablecimientoMapper.toDto(savedEntity)).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/tipos-establecimiento")
                        .with(asUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Nuevo\",\"descripcion\":\"Desc\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Recurso creado"))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    // ================================================================== //
    //  PUT /api/tipos-establecimiento/{id}                                //
    // ================================================================== //

    @Test
    void update_existingId_returnsOk() throws Exception {
        // given
        TipoEstablecimiento updated = new TipoEstablecimiento();
        updated.setId(1);

        TipoEstablecimientoResponse response = new TipoEstablecimientoResponse();
        response.setId(1);

        when(tipoEstablecimientoService.update(eq(1), any())).thenReturn(updated);
        when(tipoEstablecimientoMapper.toDto(updated)).thenReturn(response);

        // when & then
        mockMvc.perform(put("/api/tipos-establecimiento/1")
                        .with(asUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Actualizado\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(1));
    }

    @Test
    void update_nonExistentId_returnsNotFound() throws Exception {
        // given
        when(tipoEstablecimientoService.update(eq(999), any()))
                .thenThrow(new ResourceNotFoundException("TipoEstablecimiento", "id", 999));

        // when & then
        mockMvc.perform(put("/api/tipos-establecimiento/999")
                        .with(asUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Updated\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    // ================================================================== //
    //  DELETE /api/tipos-establecimiento/{id}                             //
    // ================================================================== //

    @Test
    void delete_existingId_returnsOkWithNoData() throws Exception {
        mockMvc.perform(delete("/api/tipos-establecimiento/1").with(asUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(1))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void delete_nonExistentId_returnsNotFound() throws Exception {
        // given
        doThrow(new ResourceNotFoundException("TipoEstablecimiento", "id", 999))
                .when(tipoEstablecimientoService).delete(999);

        // when & then
        mockMvc.perform(delete("/api/tipos-establecimiento/999").with(asUsuario()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }
}
