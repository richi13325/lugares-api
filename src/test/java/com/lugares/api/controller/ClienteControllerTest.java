package com.lugares.api.controller;

import com.lugares.api.dto.request.ClienteUpdateRequest;
import com.lugares.api.dto.response.ClienteListResponse;
import com.lugares.api.dto.response.ClienteResponse;
import com.lugares.api.entity.Cliente;
import com.lugares.api.exception.ResourceNotFoundException;
import com.lugares.api.mapper.ClienteMapper;
import com.lugares.api.service.ClienteService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClienteController.class)
class ClienteControllerTest extends BaseControllerTest {

    @MockitoBean
    private ClienteService clienteService;

    @MockitoBean
    private ClienteMapper clienteMapper;

    // ================================================================== //
    //  GET /api/clientes/{id}                                             //
    // ================================================================== //

    @Test
    void getById_existingId_returnsOk() throws Exception {
        Cliente entity = new Cliente();
        entity.setId(1);

        ClienteResponse response = new ClienteResponse();
        response.setId(1);
        response.setNombre("Maria Lopez");

        when(clienteService.getById(1)).thenReturn(entity);
        when(clienteMapper.toDto(entity)).thenReturn(response);

        mockMvc.perform(get("/api/clientes/1").with(asUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.nombre").value("Maria Lopez"));
    }

    @Test
    void getById_nonExistentId_returnsNotFound() throws Exception {
        when(clienteService.getById(999))
                .thenThrow(new ResourceNotFoundException("Cliente", "id", 999));

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
    //  GET /api/clientes (paginated)                                      //
    // ================================================================== //

    @Test
    void list_authenticated_returnsOkWithPaginatedList() throws Exception {
        Cliente c1 = new Cliente();
        c1.setId(1);
        Page<Cliente> page = new PageImpl<>(List.of(c1), PageRequest.of(0, 10), 1);
        when(clienteService.list(isNull(), any())).thenReturn(page);

        ClienteListResponse listResponse = new ClienteListResponse();
        listResponse.setId(1);
        when(clienteMapper.toListDto(any())).thenReturn(listResponse);

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
        Cliente c1 = new Cliente();
        c1.setId(2);
        Page<Cliente> page = new PageImpl<>(List.of(c1), PageRequest.of(0, 10), 1);
        when(clienteService.list(eq("Maria"), any())).thenReturn(page);

        ClienteListResponse listResponse = new ClienteListResponse();
        listResponse.setId(2);
        when(clienteMapper.toListDto(any())).thenReturn(listResponse);

        mockMvc.perform(get("/api/clientes")
                        .param("nombre", "Maria")
                        .param("page", "0")
                        .param("size", "10")
                        .with(asUsuario()))
                .andExpect(status().isOk());

        verify(clienteService).list(eq("Maria"), any());
    }

    // ================================================================== //
    //  PUT /api/clientes/{id} (multipart)                                 //
    // ================================================================== //

    @Test
    void update_withImage_returnsOk() throws Exception {
        Cliente entity = new Cliente();
        entity.setId(1);

        ClienteResponse response = new ClienteResponse();
        response.setId(1);
        response.setNombre("Maria Actualizada");
        response.setImagenPerfil("https://supabase.co/storage/v1/object/public/bucket/perfiles/uuid.jpg");

        MockMultipartFile dataPart = new MockMultipartFile(
                "data", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(new ClienteUpdateRequest() {{ setNombre("Maria Actualizada"); }}));
        MockMultipartFile imagenPart = new MockMultipartFile(
                "imagen", "perfil.jpg", MediaType.IMAGE_JPEG_VALUE, "fake-bytes".getBytes());

        when(clienteMapper.toEntity(any(ClienteUpdateRequest.class))).thenReturn(entity);
        when(clienteService.update(eq(1), any(Cliente.class), any())).thenReturn(entity);
        when(clienteMapper.toDto(entity)).thenReturn(response);

        mockMvc.perform(multipart("/api/clientes/1")
                        .file(dataPart)
                        .file(imagenPart)
                        .with(request -> { request.setMethod("PUT"); return request; })
                        .with(asClienteWithId(1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.imagenPerfil").value("https://supabase.co/storage/v1/object/public/bucket/perfiles/uuid.jpg"));
    }

    @Test
    void update_withoutImage_returnsOk() throws Exception {
        Cliente entity = new Cliente();
        entity.setId(1);

        ClienteResponse response = new ClienteResponse();
        response.setId(1);
        response.setNombre("Maria Actualizada");
        response.setImagenPerfil(null);

        MockMultipartFile dataPart = new MockMultipartFile(
                "data", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(new ClienteUpdateRequest() {{ setNombre("Maria Actualizada"); }}));

        when(clienteMapper.toEntity(any(ClienteUpdateRequest.class))).thenReturn(entity);
        when(clienteService.update(eq(1), any(Cliente.class), isNull())).thenReturn(entity);
        when(clienteMapper.toDto(entity)).thenReturn(response);

        mockMvc.perform(multipart("/api/clientes/1")
                        .file(dataPart)
                        .with(request -> { request.setMethod("PUT"); return request; })
                        .with(asClienteWithId(1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.imagenPerfil").isEmpty());
    }

    @Test
    void update_nonExistentId_returnsNotFound() throws Exception {
        Cliente entity = new Cliente();

        MockMultipartFile dataPart = new MockMultipartFile(
                "data", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(new ClienteUpdateRequest() {{ setNombre("Inexistente"); }}));

        when(clienteMapper.toEntity(any(ClienteUpdateRequest.class))).thenReturn(entity);
        when(clienteService.update(eq(999), any(Cliente.class), any()))
                .thenThrow(new ResourceNotFoundException("Cliente", "id", 999));

        mockMvc.perform(multipart("/api/clientes/999")
                        .file(dataPart)
                        .with(request -> { request.setMethod("PUT"); return request; })
                        .with(asClienteWithId(999)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void update_fromDifferentCliente_returnsForbidden() throws Exception {
        MockMultipartFile dataPart = new MockMultipartFile(
                "data", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(new ClienteUpdateRequest() {{ setNombre("Intruder"); }}));

        mockMvc.perform(multipart("/api/clientes/1")
                        .file(dataPart)
                        .with(request -> { request.setMethod("PUT"); return request; })
                        .with(asClienteWithId(2)))
                .andExpect(status().isForbidden());
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
        doThrow(new ResourceNotFoundException("Cliente", "id", 999))
                .when(clienteService).delete(999);

        mockMvc.perform(delete("/api/clientes/999").with(asUsuario()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void getById_clienteAccessingOwnProfile_returnsOk() throws Exception {
        Cliente entity = new Cliente();
        entity.setId(1);

        ClienteResponse response = new ClienteResponse();
        response.setId(1);
        response.setNombre("Maria Lopez");

        when(clienteService.getById(1)).thenReturn(entity);
        when(clienteMapper.toDto(entity)).thenReturn(response);

        mockMvc.perform(get("/api/clientes/1").with(asClienteWithId(1)))
                .andExpect(status().isOk());
    }

    @Test
    void getById_clienteAccessingOtherProfile_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/clientes/1").with(asClienteWithId(2)))
                .andExpect(status().isForbidden());
    }
}
