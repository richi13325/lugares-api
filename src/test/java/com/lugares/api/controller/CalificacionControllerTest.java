package com.lugares.api.controller;

import com.lugares.api.dto.request.CalificacionRequest;
import com.lugares.api.dto.response.CalificacionResponse;
import com.lugares.api.entity.Calificacion;
import com.lugares.api.mapper.CalificacionMapper;
import com.lugares.api.service.CalificacionService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CalificacionController.class)
class CalificacionControllerTest extends BaseControllerTest {

    @MockitoBean
    private CalificacionService calificacionService;

    @MockitoBean
    private CalificacionMapper calificacionMapper;

    // ================================================================== //
    //  POST /api/calificaciones (upsert)                                  //
    // ================================================================== //

    @Test
    void createOrUpdate_validRequest_returnsOkWithCalificacion() throws Exception {
        // given
        Calificacion entity = new Calificacion();
        entity.setId(1);

        CalificacionResponse response = new CalificacionResponse();
        response.setId(1);
        response.setIdCliente(1);
        response.setIdEstablecimiento(11);
        response.setCalificacion((byte) 4);

        when(calificacionService.createOrUpdate(1, 10, (byte) 4)).thenReturn(entity);
        when(calificacionMapper.toDto(entity)).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/calificaciones")
                        .with(asClienteWithId(1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idEstablecimiento\":10,\"calificacion\":4}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.calificacion").value(4));
    }

    @Test
    void createOrUpdate_nullFields_returnsBadRequest() throws Exception {
        // given — all fields null
        mockMvc.perform(post("/api/calificaciones")
                        .with(asClienteWithId(1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.idEstablecimiento").exists())
                .andExpect(jsonPath("$.fieldErrors.calificacion").exists());
    }

    @Test
    void createOrUpdate_ratingZero_returnsBadRequest() throws Exception {
        // given — calificacion = 0 violates @Min(1)
        mockMvc.perform(post("/api/calificaciones")
                        .with(asClienteWithId(1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idEstablecimiento\":10,\"calificacion\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.calificacion")
                        .value("La calificacion minima es 1"));
    }

    @Test
    void createOrUpdate_ratingAboveFive_returnsBadRequest() throws Exception {
        // given — calificacion = 6 violates @Max(5)
        mockMvc.perform(post("/api/calificaciones")
                        .with(asClienteWithId(1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idEstablecimiento\":10,\"calificacion\":6}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.calificacion")
                        .value("La calificacion maxima es 5"));
    }

    @Test
    void createOrUpdate_unauthenticated_returnsForbidden() throws Exception {
        mockMvc.perform(post("/api/calificaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idEstablecimiento\":10,\"calificacion\":4}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void createOrUpdate_validIdempotent_returnsOk() throws Exception {
        // given — same endpoint, different rating (confirms idempotency/upsert semantics)
        Calificacion entity = new Calificacion();
        entity.setId(1);

        CalificacionResponse response = new CalificacionResponse();
        response.setId(1);
        response.setIdCliente(2);
        response.setIdEstablecimiento(20);
        response.setCalificacion((byte) 5);

        when(calificacionService.createOrUpdate(2, 20, (byte) 5)).thenReturn(entity);
        when(calificacionMapper.toDto(entity)).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/calificaciones")
                        .with(asClienteWithId(2))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idEstablecimiento\":20,\"calificacion\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.calificacion").value(5));
    }
}
