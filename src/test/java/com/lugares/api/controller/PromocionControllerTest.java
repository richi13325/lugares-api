package com.lugares.api.controller;

import com.lugares.api.dto.response.PromocionListResponse;
import com.lugares.api.dto.response.PromocionResponse;
import com.lugares.api.entity.Promocion;
import com.lugares.api.exception.ResourceNotFoundException;
import com.lugares.api.service.PromocionService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PromocionController.class)
class PromocionControllerTest extends BaseControllerTest {

    @MockBean
    private PromocionService promocionService;

    // ================================================================== //
    //  GET /api/promociones/{id}                                          //
    // ================================================================== //

    @Test
    void getById_existingId_returnsOk() throws Exception {
        // given
        Promocion entity = new Promocion();
        entity.setId(1);
        entity.setNombre("2x1 en pizzas");

        PromocionResponse response = new PromocionResponse();
        response.setId(1);
        response.setNombre("2x1 en pizzas");

        when(promocionService.getById(1)).thenReturn(entity);
        when(modelMapper.map(entity, PromocionResponse.class)).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/promociones/1").with(asUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.nombre").value("2x1 en pizzas"));
    }

    @Test
    void getById_nonExistentId_returnsNotFound() throws Exception {
        // given
        when(promocionService.getById(999))
                .thenThrow(new ResourceNotFoundException("Promocion", "id", 999));

        // when & then
        mockMvc.perform(get("/api/promociones/999").with(asUsuario()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void getById_unauthenticated_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/promociones/1"))
                .andExpect(status().isForbidden());
    }

    // ================================================================== //
    //  GET /api/promociones  (paginated list)                             //
    // ================================================================== //

    @Test
    void list_authenticated_returnsOkWithPage() throws Exception {
        // given
        Promocion p1 = new Promocion();
        p1.setId(1);
        Page<Promocion> page = new PageImpl<>(List.of(p1), PageRequest.of(0, 10), 1);

        PromocionListResponse listResp = new PromocionListResponse();
        listResp.setId(1);
        listResp.setNombre("Promo test");

        when(promocionService.list(isNull(), any())).thenReturn(page);
        when(modelMapper.map(any(), eq(PromocionListResponse.class))).thenReturn(listResp);

        // when & then
        mockMvc.perform(get("/api/promociones").with(asUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    // ================================================================== //
    //  GET /api/promociones/establecimiento/{establecimientoId}           //
    // ================================================================== //

    @Test
    void listByEstablecimiento_existingId_returnsOkWithList() throws Exception {
        // given
        Promocion p1 = new Promocion();
        p1.setId(1);

        PromocionResponse response = new PromocionResponse();
        response.setId(1);
        response.setNombre("Promo local");

        when(promocionService.listByEstablecimiento(1)).thenReturn(List.of(p1));
        when(modelMapper.map(any(), eq(PromocionResponse.class))).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/promociones/establecimiento/1").with(asUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void listByEstablecimiento_nonExistentId_returnsNotFound() throws Exception {
        // given
        when(promocionService.listByEstablecimiento(999))
                .thenThrow(new ResourceNotFoundException("Establecimiento", "id", 999));

        // when & then
        mockMvc.perform(get("/api/promociones/establecimiento/999").with(asUsuario()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    // ================================================================== //
    //  POST /api/promociones                                              //
    // ================================================================== //

    @Test
    void create_validRequest_returnsCreated() throws Exception {
        // given — mapRequestToEntity calls modelMapper.map(request, Promocion.class) first
        Promocion entity = new Promocion();
        entity.setId(0);

        Promocion saved = new Promocion();
        saved.setId(10);
        saved.setNombre("Promo nueva");

        PromocionResponse response = new PromocionResponse();
        response.setId(10);
        response.setNombre("Promo nueva");
        response.setCodigoValidacion("ABCD1234");

        when(modelMapper.map(any(), eq(Promocion.class))).thenReturn(entity);
        when(promocionService.create(any())).thenReturn(saved);
        when(modelMapper.map(saved, PromocionResponse.class)).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/promociones")
                        .with(asUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idSuscripcion\":1,\"idEstablecimiento\":2,\"nombre\":\"Promo nueva\",\"codigoValidacion\":\"ABCD1234\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Recurso creado"));
    }

    @Test
    void create_missingRequiredFields_returnsBadRequest() throws Exception {
        // blank nombre, null idSuscripcion, null idEstablecimiento, null codigoValidacion
        mockMvc.perform(post("/api/promociones")
                        .with(asUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.nombre").exists())
                .andExpect(jsonPath("$.fieldErrors.idSuscripcion").exists())
                .andExpect(jsonPath("$.fieldErrors.idEstablecimiento").exists());
    }

    @Test
    void create_codigoTooShort_returnsBadRequest() throws Exception {
        // "ABCDEFG" is 7 chars — violates @Size(min=8)
        mockMvc.perform(post("/api/promociones")
                        .with(asUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idSuscripcion\":1,\"idEstablecimiento\":2,\"nombre\":\"Promo\",\"codigoValidacion\":\"ABCDEFG\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.codigoValidacion")
                        .value("El codigo de validacion debe tener exactamente 8 caracteres"));
    }

    @Test
    void create_codigoTooLong_returnsBadRequest() throws Exception {
        // "ABCDEFGHI" is 9 chars — violates @Size(max=8)
        mockMvc.perform(post("/api/promociones")
                        .with(asUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idSuscripcion\":1,\"idEstablecimiento\":2,\"nombre\":\"Promo\",\"codigoValidacion\":\"ABCDEFGHI\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.codigoValidacion")
                        .value("El codigo de validacion debe tener exactamente 8 caracteres"));
    }

    // ================================================================== //
    //  PUT /api/promociones/{id}                                          //
    // ================================================================== //

    @Test
    void update_existingId_returnsOk() throws Exception {
        // given
        Promocion entity = new Promocion();
        entity.setId(1);

        Promocion updated = new Promocion();
        updated.setId(1);
        updated.setNombre("Promo actualizada");

        PromocionResponse response = new PromocionResponse();
        response.setId(1);
        response.setNombre("Promo actualizada");

        when(modelMapper.map(any(), eq(Promocion.class))).thenReturn(entity);
        when(promocionService.update(eq(1), any())).thenReturn(updated);
        when(modelMapper.map(updated, PromocionResponse.class)).thenReturn(response);

        // when & then
        mockMvc.perform(put("/api/promociones/1")
                        .with(asUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idSuscripcion\":1,\"idEstablecimiento\":2,\"nombre\":\"Promo actualizada\",\"codigoValidacion\":\"ABCD1234\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nombre").value("Promo actualizada"));
    }

    @Test
    void update_nonExistentId_returnsNotFound() throws Exception {
        // given
        Promocion entity = new Promocion();
        when(modelMapper.map(any(), eq(Promocion.class))).thenReturn(entity);
        when(promocionService.update(eq(999), any()))
                .thenThrow(new ResourceNotFoundException("Promocion", "id", 999));

        // when & then
        mockMvc.perform(put("/api/promociones/999")
                        .with(asUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idSuscripcion\":1,\"idEstablecimiento\":2,\"nombre\":\"Test\",\"codigoValidacion\":\"ABCD1234\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void update_validationError_returnsBadRequest() throws Exception {
        // blank nombre triggers @NotBlank
        mockMvc.perform(put("/api/promociones/1")
                        .with(asUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idSuscripcion\":1,\"idEstablecimiento\":2,\"nombre\":\"\",\"codigoValidacion\":\"ABCD1234\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.nombre").exists());
    }

    // ================================================================== //
    //  DELETE /api/promociones/{id}                                       //
    // ================================================================== //

    @Test
    void delete_existingId_returnsOkWithNoData() throws Exception {
        mockMvc.perform(delete("/api/promociones/1").with(asUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(1))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void delete_nonExistentId_returnsNotFound() throws Exception {
        // given
        doThrow(new ResourceNotFoundException("Promocion", "id", 999))
                .when(promocionService).delete(999);

        // when & then
        mockMvc.perform(delete("/api/promociones/999").with(asUsuario()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }
}
