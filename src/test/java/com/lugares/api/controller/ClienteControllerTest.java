package com.lugares.api.controller;

import com.lugares.api.dto.request.ClienteUpdateRequest;
import com.lugares.api.dto.response.ClienteListResponse;
import com.lugares.api.dto.response.ClienteResponse;
import com.lugares.api.entity.Cliente;
import com.lugares.api.exception.ResourceNotFoundException;
import com.lugares.api.service.ClienteService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClienteController.class)
class ClienteControllerTest extends BaseControllerTest {

    @MockBean
    private ClienteService clienteService;

    // ================================================================== //
    //  GET /api/clientes/{id}                                             //
    // ================================================================== //

    @Test
    void getById_existingId_returnsOk() throws Exception {
        // given
        Cliente entity = new Cliente();
        entity.setId(1);

        ClienteResponse response = new ClienteResponse();
        response.setId(1);
        response.setNombre("Maria Lopez");

        when(clienteService.getById(1)).thenReturn(entity);
        when(modelMapper.map(entity, ClienteResponse.class)).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/clientes/1").with(asUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.nombre").value("Maria Lopez"));
    }

    @Test
    void getById_nonExistentId_returnsNotFound() throws Exception {
        // given
        when(clienteService.getById(999))
                .thenThrow(new ResourceNotFoundException("Cliente", "id", 999));

        // when & then
        mockMvc.perform(get("/api/clientes/999").with(asUsuario()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void getById_unauthenticated_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/clientes/1"))
                .andExpect(status().isForbidden());
    }

    // ================================================================== //
    //  GET /api/clientes?nombre=X (paginated)                            //
    // ================================================================== //

    @Test
    void list_authenticated_returnsOkWithPaginatedList() throws Exception {
        // given
        Cliente c1 = new Cliente();
        c1.setId(1);
        Page<Cliente> page = new PageImpl<>(List.of(c1), PageRequest.of(0, 10), 1);
        when(clienteService.list(isNull(), any())).thenReturn(page);

        ClienteListResponse listResponse = new ClienteListResponse();
        listResponse.setId(1);
        when(modelMapper.map(any(), eq(ClienteListResponse.class))).thenReturn(listResponse);

        // when & then
        mockMvc.perform(get("/api/clientes")
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
        Cliente c1 = new Cliente();
        c1.setId(2);
        Page<Cliente> page = new PageImpl<>(List.of(c1), PageRequest.of(0, 10), 1);
        when(clienteService.list(eq("Maria"), any())).thenReturn(page);

        ClienteListResponse listResponse = new ClienteListResponse();
        listResponse.setId(2);
        when(modelMapper.map(any(), eq(ClienteListResponse.class))).thenReturn(listResponse);

        // when & then
        mockMvc.perform(get("/api/clientes")
                        .param("nombre", "Maria")
                        .param("page", "0")
                        .param("size", "10")
                        .with(asUsuario()))
                .andExpect(status().isOk());

        verify(clienteService).list(eq("Maria"), any());
    }

    // ================================================================== //
    //  PUT /api/clientes/{id}                                             //
    // ================================================================== //

    @Test
    void update_validRequest_returnsOk() throws Exception {
        // given
        Cliente entity = new Cliente();
        entity.setId(1);

        ClienteResponse response = new ClienteResponse();
        response.setId(1);
        response.setNombre("Maria Actualizada");

        when(modelMapper.map(any(ClienteUpdateRequest.class), eq(Cliente.class))).thenReturn(entity);
        when(clienteService.update(eq(1), any())).thenReturn(entity);
        when(modelMapper.map(entity, ClienteResponse.class)).thenReturn(response);

        // when & then
        mockMvc.perform(put("/api/clientes/1")
                        .with(asUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Maria Actualizada\",\"correoElectronico\":\"maria@test.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void update_invalidEmail_returnsBadRequest() throws Exception {
        // when & then
        mockMvc.perform(put("/api/clientes/1")
                        .with(asUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correoElectronico\":\"not-email\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.correoElectronico").exists());
    }

    @Test
    void update_telefonoTooLong_returnsBadRequest() throws Exception {
        // given — 11 chars, max is 10
        String telefonoLargo = "12345678901";

        // when & then
        mockMvc.perform(put("/api/clientes/1")
                        .with(asUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"telefono\":\"" + telefonoLargo + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.telefono").exists());
    }

    @Test
    void update_nonExistentId_returnsNotFound() throws Exception {
        // given
        Cliente entity = new Cliente();
        when(modelMapper.map(any(ClienteUpdateRequest.class), eq(Cliente.class))).thenReturn(entity);
        when(clienteService.update(eq(999), any()))
                .thenThrow(new ResourceNotFoundException("Cliente", "id", 999));

        // when & then
        mockMvc.perform(put("/api/clientes/999")
                        .with(asUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Inexistente\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    // ================================================================== //
    //  DELETE /api/clientes/{id}                                          //
    // ================================================================== //

    @Test
    void delete_existingId_returnsOkWithNoData() throws Exception {
        mockMvc.perform(delete("/api/clientes/1").with(asUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(1))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void delete_nonExistentId_returnsNotFound() throws Exception {
        // given
        doThrow(new ResourceNotFoundException("Cliente", "id", 999))
                .when(clienteService).delete(999);

        // when & then
        mockMvc.perform(delete("/api/clientes/999").with(asUsuario()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }
}
