package com.lugares.api.controller;

import com.lugares.api.dto.response.EmpresaResponse;
import com.lugares.api.entity.Empresa;
import com.lugares.api.exception.ResourceNotFoundException;
import com.lugares.api.service.EmpresaService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import java.time.LocalDate;
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

@WebMvcTest(EmpresaController.class)
class EmpresaControllerTest extends BaseControllerTest {

    @MockBean
    private EmpresaService empresaService;

    // ================================================================== //
    //  GET /api/empresas                                                  //
    // ================================================================== //

    @Test
    void listAll_authenticated_returnsOkWithList() throws Exception {
        // given
        Empresa entity1 = new Empresa();
        entity1.setId(1);
        Empresa entity2 = new Empresa();
        entity2.setId(2);

        EmpresaResponse response = new EmpresaResponse();
        response.setId(1);
        response.setNombre("Acme Corp");

        when(empresaService.listAll()).thenReturn(List.of(entity1, entity2));
        when(modelMapper.map(any(), eq(EmpresaResponse.class))).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/empresas").with(asUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(1))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void listAll_unauthenticated_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/empresas"))
                .andExpect(status().isForbidden());
    }

    // ================================================================== //
    //  GET /api/empresas/{id}                                             //
    // ================================================================== //

    @Test
    void getById_existingId_returnsOk() throws Exception {
        // given
        Empresa entity = new Empresa();
        entity.setId(1);

        EmpresaResponse response = new EmpresaResponse();
        response.setId(1);
        response.setNombre("Acme Corp");
        response.setCorreoElectronico("contacto@acme.com");
        response.setEstado("Activo");

        when(empresaService.getById(1)).thenReturn(entity);
        when(modelMapper.map(entity, EmpresaResponse.class)).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/empresas/1").with(asUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.nombre").value("Acme Corp"))
                .andExpect(jsonPath("$.data.correoElectronico").value("contacto@acme.com"))
                .andExpect(jsonPath("$.data.estado").value("Activo"));
    }

    @Test
    void getById_nonExistentId_returnsNotFound() throws Exception {
        // given
        when(empresaService.getById(999))
                .thenThrow(new ResourceNotFoundException("Empresa", "id", 999));

        // when & then
        mockMvc.perform(get("/api/empresas/999").with(asUsuario()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    // ================================================================== //
    //  POST /api/empresas                                                 //
    // ================================================================== //

    @Test
    void create_validEntity_returnsCreated() throws Exception {
        // given
        Empresa savedEntity = new Empresa();
        savedEntity.setId(1);

        EmpresaResponse response = new EmpresaResponse();
        response.setId(1);
        response.setNombre("Nueva Empresa");

        when(empresaService.create(any())).thenReturn(savedEntity);
        when(modelMapper.map(savedEntity, EmpresaResponse.class)).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/empresas")
                        .with(asUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Nueva Empresa\",\"descripcion\":\"Desc\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Recurso creado"))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    // ================================================================== //
    //  PUT /api/empresas/{id}                                             //
    // ================================================================== //

    @Test
    void update_existingId_returnsOk() throws Exception {
        // given
        Empresa updated = new Empresa();
        updated.setId(1);

        EmpresaResponse response = new EmpresaResponse();
        response.setId(1);

        when(empresaService.update(eq(1), any())).thenReturn(updated);
        when(modelMapper.map(updated, EmpresaResponse.class)).thenReturn(response);

        // when & then
        mockMvc.perform(put("/api/empresas/1")
                        .with(asUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Actualizado\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(1));
    }

    @Test
    void update_nonExistentId_returnsNotFound() throws Exception {
        // given
        when(empresaService.update(eq(999), any()))
                .thenThrow(new ResourceNotFoundException("Empresa", "id", 999));

        // when & then
        mockMvc.perform(put("/api/empresas/999")
                        .with(asUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Updated\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    // ================================================================== //
    //  DELETE /api/empresas/{id}                                          //
    // ================================================================== //

    @Test
    void delete_existingId_returnsOkWithNoData() throws Exception {
        mockMvc.perform(delete("/api/empresas/1").with(asUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(1))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void delete_nonExistentId_returnsNotFound() throws Exception {
        // given
        doThrow(new ResourceNotFoundException("Empresa", "id", 999))
                .when(empresaService).delete(999);

        // when & then
        mockMvc.perform(delete("/api/empresas/999").with(asUsuario()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }
}
