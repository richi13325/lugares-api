package com.lugares.api.controller;

import com.lugares.api.dto.response.HistorialCanjeResponse;
import com.lugares.api.entity.HistorialCanje;
import com.lugares.api.exception.BusinessRuleException;
import com.lugares.api.exception.ResourceNotFoundException;
import com.lugares.api.service.HistorialCanjeService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

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

@WebMvcTest(HistorialCanjeController.class)
class HistorialCanjeControllerTest extends BaseControllerTest {

    @MockBean
    private HistorialCanjeService historialCanjeService;

    // ================================================================== //
    //  GET /api/historial-canjes/usuario/{usuarioId}                      //
    // ================================================================== //

    @Test
    void listByUsuario_existingId_returnsOkWithList() throws Exception {
        // given
        HistorialCanje canje = new HistorialCanje();
        canje.setId(1);

        HistorialCanjeResponse response = new HistorialCanjeResponse();
        response.setId(1);

        when(historialCanjeService.listByUsuario(5)).thenReturn(List.of(canje));
        when(modelMapper.map(any(), eq(HistorialCanjeResponse.class))).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/historial-canjes/usuario/5").with(asUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void listByUsuario_nonExistentId_returnsNotFound() throws Exception {
        // given
        when(historialCanjeService.listByUsuario(999))
                .thenThrow(new ResourceNotFoundException("Usuario", "id", 999));

        // when & then
        mockMvc.perform(get("/api/historial-canjes/usuario/999").with(asUsuario()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void listByUsuario_unauthenticated_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/historial-canjes/usuario/5"))
                .andExpect(status().isForbidden());
    }

    // ================================================================== //
    //  GET /api/historial-canjes/promocion/{promocionId}                  //
    // ================================================================== //

    @Test
    void listByPromocion_existingId_returnsOkWithList() throws Exception {
        // given
        HistorialCanje canje = new HistorialCanje();
        canje.setId(1);

        HistorialCanjeResponse response = new HistorialCanjeResponse();
        response.setId(1);

        when(historialCanjeService.listByPromocion(10)).thenReturn(List.of(canje));
        when(modelMapper.map(any(), eq(HistorialCanjeResponse.class))).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/historial-canjes/promocion/10").with(asUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void listByPromocion_nonExistentId_returnsNotFound() throws Exception {
        // given
        when(historialCanjeService.listByPromocion(999))
                .thenThrow(new ResourceNotFoundException("Promocion", "id", 999));

        // when & then
        mockMvc.perform(get("/api/historial-canjes/promocion/999").with(asUsuario()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    // ================================================================== //
    //  POST /api/historial-canjes  (@RequestParam — no JSON body)         //
    // ================================================================== //

    @Test
    void canjear_validParams_returnsCreated() throws Exception {
        // given
        HistorialCanje canje = new HistorialCanje();
        canje.setId(1);

        HistorialCanjeResponse response = new HistorialCanjeResponse();
        response.setId(1);
        response.setCodigoValidacion("ABCD1234");

        when(historialCanjeService.canjear(10, 5, "ABCD1234")).thenReturn(canje);
        when(modelMapper.map(canje, HistorialCanjeResponse.class)).thenReturn(response);

        // when & then — params in query string, NOT in request body
        mockMvc.perform(post("/api/historial-canjes")
                        .with(asUsuario())
                        .param("promocionId", "10")
                        .param("usuarioId", "5")
                        .param("codigoValidacion", "ABCD1234"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Recurso creado"));
    }

    @Test
    void canjear_invalidCode_returnsUnprocessableEntity() throws Exception {
        // given — service throws BusinessRuleException when code is wrong → 422
        when(historialCanjeService.canjear(eq(10), eq(5), eq("WRONGCOD")))
                .thenThrow(new BusinessRuleException("El codigo de validacion no es correcto"));

        // when & then
        mockMvc.perform(post("/api/historial-canjes")
                        .with(asUsuario())
                        .param("promocionId", "10")
                        .param("usuarioId", "5")
                        .param("codigoValidacion", "WRONGCOD"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void canjear_typeMismatch_returnsBadRequest() throws Exception {
        // "abc" cannot be bound to Integer promocionId → MethodArgumentTypeMismatchException → 400
        mockMvc.perform(post("/api/historial-canjes")
                        .with(asUsuario())
                        .param("promocionId", "abc")
                        .param("usuarioId", "5")
                        .param("codigoValidacion", "ABCD1234"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    // ================================================================== //
    //  DELETE /api/historial-canjes/{id}                                  //
    // ================================================================== //

    @Test
    void delete_existingId_returnsOkWithNoData() throws Exception {
        mockMvc.perform(delete("/api/historial-canjes/1").with(asUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(1))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void delete_nonExistentId_returnsNotFound() throws Exception {
        // given
        doThrow(new ResourceNotFoundException("HistorialCanje", "id", 999))
                .when(historialCanjeService).delete(999);

        // when & then
        mockMvc.perform(delete("/api/historial-canjes/999").with(asUsuario()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }
}
