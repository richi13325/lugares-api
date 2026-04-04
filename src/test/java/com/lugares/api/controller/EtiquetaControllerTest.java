package com.lugares.api.controller;

import com.lugares.api.dto.response.EtiquetaAdminResponse;
import com.lugares.api.dto.response.EtiquetaResponse;
import com.lugares.api.entity.Etiqueta;
import com.lugares.api.entity.EtiquetaCliente;
import com.lugares.api.entity.EtiquetaEstablecimiento;
import com.lugares.api.entity.EtiquetaTipoEstablecimiento;
import com.lugares.api.exception.DuplicateResourceException;
import com.lugares.api.exception.ResourceNotFoundException;
import com.lugares.api.service.EtiquetaService;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EtiquetaController.class)
class EtiquetaControllerTest extends BaseControllerTest {

    @MockBean
    private EtiquetaService etiquetaService;

    // ================================================================== //
    //  GET /api/etiquetas/{id}                                            //
    // ================================================================== //

    @Test
    void getById_existingId_returnsOk() throws Exception {
        // given
        Etiqueta entity = new Etiqueta();
        entity.setId(1);
        entity.setNombre("Vegano");

        EtiquetaResponse response = new EtiquetaResponse();
        response.setId(1);
        response.setNombre("Vegano");

        when(etiquetaService.getById(1)).thenReturn(entity);
        when(modelMapper.map(entity, EtiquetaResponse.class)).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/etiquetas/1").with(asUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.nombre").value("Vegano"));
    }

    @Test
    void getById_nonExistentId_returnsNotFound() throws Exception {
        // given
        when(etiquetaService.getById(999))
                .thenThrow(new ResourceNotFoundException("Etiqueta", "id", 999));

        // when & then
        mockMvc.perform(get("/api/etiquetas/999").with(asUsuario()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void getById_unauthenticated_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/etiquetas/1"))
                .andExpect(status().isForbidden());
    }

    // ================================================================== //
    //  GET /api/etiquetas/admin  (paginated)                              //
    // ================================================================== //

    @Test
    void listAdmin_authenticated_returnsOkWithPage() throws Exception {
        // given
        Etiqueta e1 = new Etiqueta();
        e1.setId(1);
        Page<Etiqueta> page = new PageImpl<>(List.of(e1), PageRequest.of(0, 10), 1);

        EtiquetaAdminResponse adminResp = new EtiquetaAdminResponse();
        adminResp.setId(1);
        adminResp.setNombre("Vegano");

        when(etiquetaService.listAdmin(isNull(), any())).thenReturn(page);
        when(modelMapper.map(any(), eq(EtiquetaAdminResponse.class))).thenReturn(adminResp);

        // when & then
        mockMvc.perform(get("/api/etiquetas/admin").with(asUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    // ================================================================== //
    //  GET /api/etiquetas/visibles                                        //
    // ================================================================== //

    @Test
    void listVisibles_authenticated_returnsOkWithList() throws Exception {
        // given
        Etiqueta e1 = new Etiqueta();
        e1.setId(1);

        EtiquetaResponse response = new EtiquetaResponse();
        response.setId(1);
        response.setNombre("Vegano");

        when(etiquetaService.listVisibles()).thenReturn(List.of(e1));
        when(modelMapper.map(any(), eq(EtiquetaResponse.class))).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/etiquetas/visibles").with(asUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    // ================================================================== //
    //  POST /api/etiquetas                                                //
    // ================================================================== //

    @Test
    void create_validRequest_returnsCreated() throws Exception {
        // given — mapRequestToEntity calls modelMapper.map(request, Etiqueta.class)
        Etiqueta entity = new Etiqueta();
        entity.setId(0);

        Etiqueta saved = new Etiqueta();
        saved.setId(5);
        saved.setNombre("Test");

        EtiquetaAdminResponse response = new EtiquetaAdminResponse();
        response.setId(5);
        response.setNombre("Test");

        when(modelMapper.map(any(), eq(Etiqueta.class))).thenReturn(entity);
        when(etiquetaService.create(any())).thenReturn(saved);
        when(modelMapper.map(saved, EtiquetaAdminResponse.class)).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/etiquetas")
                        .with(asUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idCategoria\":1,\"nombre\":\"Test\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Recurso creado"));
    }

    @Test
    void create_missingRequiredFields_returnsBadRequest() throws Exception {
        // null idCategoria, blank nombre
        mockMvc.perform(post("/api/etiquetas")
                        .with(asUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.idCategoria").exists())
                .andExpect(jsonPath("$.fieldErrors.nombre").exists());
    }

    @Test
    void create_nombreTooLong_returnsBadRequest() throws Exception {
        // 66 chars — violates @Size(max=65)
        String longNombre = "A".repeat(66);
        mockMvc.perform(post("/api/etiquetas")
                        .with(asUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idCategoria\":1,\"nombre\":\"" + longNombre + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.nombre").exists());
    }

    @Test
    void create_duplicateNombre_returnsConflict() throws Exception {
        // given
        Etiqueta entity = new Etiqueta();
        when(modelMapper.map(any(), eq(Etiqueta.class))).thenReturn(entity);
        when(etiquetaService.create(any()))
                .thenThrow(new DuplicateResourceException("Etiqueta", "nombre", "Vegano"));

        // when & then
        mockMvc.perform(post("/api/etiquetas")
                        .with(asUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idCategoria\":1,\"nombre\":\"Vegano\"}"))
                .andExpect(status().isConflict());
    }

    // ================================================================== //
    //  PUT /api/etiquetas/{id}                                            //
    // ================================================================== //

    @Test
    void update_existingId_returnsOk() throws Exception {
        // given
        Etiqueta entity = new Etiqueta();
        entity.setId(1);

        Etiqueta updated = new Etiqueta();
        updated.setId(1);
        updated.setNombre("Actualizado");

        EtiquetaAdminResponse response = new EtiquetaAdminResponse();
        response.setId(1);
        response.setNombre("Actualizado");

        when(modelMapper.map(any(), eq(Etiqueta.class))).thenReturn(entity);
        when(etiquetaService.update(eq(1), any())).thenReturn(updated);
        when(modelMapper.map(updated, EtiquetaAdminResponse.class)).thenReturn(response);

        // when & then
        mockMvc.perform(put("/api/etiquetas/1")
                        .with(asUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idCategoria\":1,\"nombre\":\"Actualizado\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nombre").value("Actualizado"));
    }

    @Test
    void update_nonExistentId_returnsNotFound() throws Exception {
        // given
        Etiqueta entity = new Etiqueta();
        when(modelMapper.map(any(), eq(Etiqueta.class))).thenReturn(entity);
        when(etiquetaService.update(eq(999), any()))
                .thenThrow(new ResourceNotFoundException("Etiqueta", "id", 999));

        // when & then
        mockMvc.perform(put("/api/etiquetas/999")
                        .with(asUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idCategoria\":1,\"nombre\":\"Test\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    // ================================================================== //
    //  DELETE /api/etiquetas/{id}                                         //
    // ================================================================== //

    @Test
    void delete_existingId_returnsOkWithNoData() throws Exception {
        mockMvc.perform(delete("/api/etiquetas/1").with(asUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(1))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void delete_nonExistentId_returnsNotFound() throws Exception {
        // given
        doThrow(new ResourceNotFoundException("Etiqueta", "id", 999))
                .when(etiquetaService).delete(999);

        // when & then
        mockMvc.perform(delete("/api/etiquetas/999").with(asUsuario()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    // ================================================================== //
    //  GET /api/etiquetas/cliente/{clienteId}  (wrapper entity)           //
    // ================================================================== //

    @Test
    void listByCliente_existingCliente_returnsOkWithList() throws Exception {
        // given — controller maps ec.getEtiqueta(), not ec directly
        Etiqueta etiqueta = new Etiqueta();
        etiqueta.setId(1);
        etiqueta.setNombre("Vegano");

        EtiquetaCliente wrapper = mock(EtiquetaCliente.class);
        when(wrapper.getEtiqueta()).thenReturn(etiqueta);

        EtiquetaResponse response = new EtiquetaResponse();
        response.setId(1);
        response.setNombre("Vegano");

        when(etiquetaService.listByCliente(1)).thenReturn(List.of(wrapper));
        when(modelMapper.map(etiqueta, EtiquetaResponse.class)).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/etiquetas/cliente/1").with(asUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void listByCliente_nonExistentCliente_returnsNotFound() throws Exception {
        // given
        when(etiquetaService.listByCliente(999))
                .thenThrow(new ResourceNotFoundException("Cliente", "id", 999));

        // when & then
        mockMvc.perform(get("/api/etiquetas/cliente/999").with(asUsuario()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    // ================================================================== //
    //  POST /api/etiquetas/cliente/{clienteId}/{etiquetaId}               //
    // ================================================================== //

    @Test
    void assignToCliente_valid_returnsCreatedWithNoData() throws Exception {
        // service returns void — no additional mock needed
        mockMvc.perform(post("/api/etiquetas/cliente/1/10").with(asUsuario()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void assignToCliente_duplicate_returnsConflict() throws Exception {
        // given
        doThrow(new DuplicateResourceException("EtiquetaCliente", "etiquetaId", "10"))
                .when(etiquetaService).assignToCliente(1, 10);

        // when & then
        mockMvc.perform(post("/api/etiquetas/cliente/1/10").with(asUsuario()))
                .andExpect(status().isConflict());
    }

    // ================================================================== //
    //  DELETE /api/etiquetas/cliente/{clienteId}/{etiquetaId}             //
    // ================================================================== //

    @Test
    void removeFromCliente_valid_returnsOkWithNoData() throws Exception {
        mockMvc.perform(delete("/api/etiquetas/cliente/1/10").with(asUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void removeFromCliente_nonExistent_returnsNotFound() throws Exception {
        // given
        doThrow(new ResourceNotFoundException("EtiquetaCliente", "etiquetaId", 10))
                .when(etiquetaService).removeFromCliente(1, 10);

        // when & then
        mockMvc.perform(delete("/api/etiquetas/cliente/1/10").with(asUsuario()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    // ================================================================== //
    //  GET /api/etiquetas/establecimiento/{establecimientoId}             //
    // ================================================================== //

    @Test
    void listByEstablecimiento_existingId_returnsOkWithList() throws Exception {
        // given — controller maps ee.getEtiqueta(), not ee directly
        Etiqueta etiqueta = new Etiqueta();
        etiqueta.setId(1);
        etiqueta.setNombre("Accesible");

        EtiquetaEstablecimiento wrapper = mock(EtiquetaEstablecimiento.class);
        when(wrapper.getEtiqueta()).thenReturn(etiqueta);

        EtiquetaResponse response = new EtiquetaResponse();
        response.setId(1);
        response.setNombre("Accesible");

        when(etiquetaService.listByEstablecimiento(1)).thenReturn(List.of(wrapper));
        when(modelMapper.map(etiqueta, EtiquetaResponse.class)).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/etiquetas/establecimiento/1").with(asUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    // ================================================================== //
    //  POST /api/etiquetas/establecimiento/{establecimientoId}/{etiquetaId}//
    // ================================================================== //

    @Test
    void assignToEstablecimiento_valid_returnsCreatedWithNoData() throws Exception {
        mockMvc.perform(post("/api/etiquetas/establecimiento/1/10").with(asUsuario()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void assignToEstablecimiento_duplicate_returnsConflict() throws Exception {
        // given
        doThrow(new DuplicateResourceException("EtiquetaEstablecimiento", "etiquetaId", "10"))
                .when(etiquetaService).assignToEstablecimiento(1, 10);

        // when & then
        mockMvc.perform(post("/api/etiquetas/establecimiento/1/10").with(asUsuario()))
                .andExpect(status().isConflict());
    }

    // ================================================================== //
    //  DELETE /api/etiquetas/establecimiento/{establecimientoId}/{etiquetaId}//
    // ================================================================== //

    @Test
    void removeFromEstablecimiento_valid_returnsOkWithNoData() throws Exception {
        mockMvc.perform(delete("/api/etiquetas/establecimiento/1/10").with(asUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void removeFromEstablecimiento_nonExistent_returnsNotFound() throws Exception {
        // given
        doThrow(new ResourceNotFoundException("EtiquetaEstablecimiento", "etiquetaId", 10))
                .when(etiquetaService).removeFromEstablecimiento(1, 10);

        // when & then
        mockMvc.perform(delete("/api/etiquetas/establecimiento/1/10").with(asUsuario()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    // ================================================================== //
    //  GET /api/etiquetas/tipo-establecimiento/{tipoId}  (wrapper + Page) //
    // ================================================================== //

    @Test
    void listByTipoEstablecimiento_existingTipo_returnsOkWithPage() throws Exception {
        // given — controller maps ete.getEtiqueta(), not ete directly
        Etiqueta etiqueta = new Etiqueta();
        etiqueta.setId(1);

        EtiquetaTipoEstablecimiento wrapper = mock(EtiquetaTipoEstablecimiento.class);
        when(wrapper.getEtiqueta()).thenReturn(etiqueta);

        Page<EtiquetaTipoEstablecimiento> page =
                new PageImpl<>(List.of(wrapper), PageRequest.of(0, 10), 1);

        EtiquetaResponse response = new EtiquetaResponse();
        response.setId(1);
        response.setNombre("Pet Friendly");

        when(etiquetaService.listByTipoEstablecimiento(eq(1), any())).thenReturn(page);
        when(modelMapper.map(any(), eq(EtiquetaResponse.class))).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/etiquetas/tipo-establecimiento/1").with(asUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }
}
