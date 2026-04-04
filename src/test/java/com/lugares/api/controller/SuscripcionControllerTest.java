package com.lugares.api.controller;

import com.lugares.api.dto.response.SuscripcionResponse;
import com.lugares.api.entity.Suscripcion;
import com.lugares.api.exception.ResourceNotFoundException;
import com.lugares.api.service.SuscripcionService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SuscripcionController.class)
class SuscripcionControllerTest extends BaseControllerTest {

    @MockBean
    private SuscripcionService suscripcionService;

    // ================================================================== //
    //  GET /api/suscripciones                                             //
    // ================================================================== //

    @Test
    void listAll_authenticated_returnsOkWithList() throws Exception {
        // given
        Suscripcion entity1 = new Suscripcion();
        entity1.setId(1);
        Suscripcion entity2 = new Suscripcion();
        entity2.setId(2);

        SuscripcionResponse response = new SuscripcionResponse();
        response.setId(1);
        response.setNombre("Basic");

        when(suscripcionService.listAll()).thenReturn(List.of(entity1, entity2));
        when(modelMapper.map(any(), eq(SuscripcionResponse.class))).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/suscripciones").with(asUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(1))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void listAll_unauthenticated_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/suscripciones"))
                .andExpect(status().isForbidden());
    }

    // ================================================================== //
    //  GET /api/suscripciones/{id}                                        //
    // ================================================================== //

    @Test
    void getById_existingId_returnsOkWithSuscripcion() throws Exception {
        // given
        Suscripcion entity = new Suscripcion();
        entity.setId(1);

        SuscripcionResponse response = new SuscripcionResponse();
        response.setId(1);
        response.setNombre("Premium");

        when(suscripcionService.getById(1)).thenReturn(entity);
        when(modelMapper.map(entity, SuscripcionResponse.class)).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/suscripciones/1").with(asUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.nombre").value("Premium"));
    }

    @Test
    void getById_nonExistentId_returnsNotFound() throws Exception {
        // given
        when(suscripcionService.getById(999))
                .thenThrow(new ResourceNotFoundException("Suscripcion", "id", 999));

        // when & then
        mockMvc.perform(get("/api/suscripciones/999").with(asUsuario()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void getById_typeMismatch_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/suscripciones/abc").with(asUsuario()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }
}
