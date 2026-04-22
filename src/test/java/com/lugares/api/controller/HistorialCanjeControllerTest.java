package com.lugares.api.controller;

import com.lugares.api.dto.response.HistorialCanjeResponse;
import com.lugares.api.entity.HistorialCanje;
import com.lugares.api.exception.BusinessRuleException;
import com.lugares.api.exception.ResourceNotFoundException;
import com.lugares.api.mapper.HistorialCanjeMapper;
import com.lugares.api.service.HistorialCanjeService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

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

    @MockitoBean(name = "historialCanjeService")
    private HistorialCanjeService historialCanjeService;

    @MockitoBean
    private HistorialCanjeMapper historialCanjeMapper;

    // ================================================================== //
    //  GET /api/historial-canjes/cliente/{clienteId}                      //
    // ================================================================== //

    @Test
    void listByCliente_existingId_returnsOkWithList() throws Exception {
        // given
        HistorialCanje canje = new HistorialCanje();
        canje.setId(1);

        HistorialCanjeResponse response = new HistorialCanjeResponse();
        response.setId(1);

        when(historialCanjeService.listByCliente(5)).thenReturn(List.of(canje));
        when(historialCanjeMapper.toDto(any())).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/historial-canjes/cliente/5").with(asUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void listByCliente_nonExistentId_returnsNotFound() throws Exception {
        // given
        when(historialCanjeService.listByCliente(999))
                .thenThrow(new ResourceNotFoundException("Cliente", "id", 999));

        // when & then
        mockMvc.perform(get("/api/historial-canjes/cliente/999").with(asUsuario()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void listByCliente_unauthenticated_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/historial-canjes/cliente/5"))
                .andExpect(status().isForbidden());
    }

    @Test
    void listByCliente_clienteAccessingOwnHistory_returnsOk() throws Exception {
        // given — ROLE_CLIENTE with matching id passes SpEL
        HistorialCanje canje = new HistorialCanje();
        canje.setId(1);

        HistorialCanjeResponse response = new HistorialCanjeResponse();
        response.setId(1);

        when(historialCanjeService.listByCliente(5)).thenReturn(List.of(canje));
        when(historialCanjeMapper.toDto(any())).thenReturn(response);

        mockMvc.perform(get("/api/historial-canjes/cliente/5").with(asClienteWithId(5)))
                .andExpect(status().isOk());
    }

    @Test
    void listByCliente_fromDifferentCliente_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/historial-canjes/cliente/5").with(asClienteWithId(2)))
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
        when(historialCanjeMapper.toDto(any())).thenReturn(response);

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
    //  POST /api/historial-canjes  (clienteId from JWT principal)         //
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
        when(historialCanjeMapper.toDto(canje)).thenReturn(response);

        // when & then — clienteId now comes from the JWT principal, not a request param
        mockMvc.perform(post("/api/historial-canjes")
                        .with(asClienteWithId(5))
                        .param("promocionId", "10")
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
                        .with(asClienteWithId(5))
                        .param("promocionId", "10")
                        .param("codigoValidacion", "WRONGCOD"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void canjear_typeMismatch_returnsBadRequest() throws Exception {
        // "abc" cannot be bound to Integer promocionId → MethodArgumentTypeMismatchException → 400
        mockMvc.perform(post("/api/historial-canjes")
                        .with(asClienteWithId(1))
                        .param("promocionId", "abc")
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

    // ================================================================== //
    //  DELETE /api/historial-canjes/{id} — ownership                     //
    // ================================================================== //

    @Test
    void delete_whenClienteNotOwner_thenForbidden() throws Exception {
        // given — cliente 2 tries to delete canje 1 owned by cliente 1
        when(historialCanjeService.isOwner(1, 2)).thenReturn(false);

        mockMvc.perform(delete("/api/historial-canjes/1").with(asClienteWithId(2)))
                .andExpect(status().isForbidden());
    }

    @Test
    void delete_whenClienteOwner_thenOk() throws Exception {
        // given — cliente 1 deletes its own canje 1
        when(historialCanjeService.isOwner(1, 1)).thenReturn(true);

        mockMvc.perform(delete("/api/historial-canjes/1").with(asClienteWithId(1)))
                .andExpect(status().isOk());
    }

    @Test
    void delete_whenUsuario_thenOk() throws Exception {
        // USUARIO branch short-circuits — isOwner is never called
        mockMvc.perform(delete("/api/historial-canjes/1").with(asUsuario()))
                .andExpect(status().isOk());
    }
}
