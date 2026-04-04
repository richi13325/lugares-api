package com.lugares.api.controller;

import com.lugares.api.dto.response.EstablecimientoDetailResponse;
import com.lugares.api.dto.response.EstablecimientoListResponse;
import com.lugares.api.dto.response.EstablecimientoResponse;
import com.lugares.api.entity.Establecimiento;
import com.lugares.api.exception.ResourceNotFoundException;
import com.lugares.api.service.EstablecimientoService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EstablecimientoController.class)
class EstablecimientoControllerTest extends BaseControllerTest {

    @MockBean
    private EstablecimientoService establecimientoService;

    // ================================================================== //
    //  GET /api/establecimientos/{id}                                     //
    // ================================================================== //

    @Test
    void getById_existingId_returnsOk() throws Exception {
        // given
        Establecimiento entity = new Establecimiento();
        entity.setId(1);
        entity.setNombre("La Trattoria");

        EstablecimientoDetailResponse response = new EstablecimientoDetailResponse();
        response.setId(1);
        response.setNombre("La Trattoria");

        when(establecimientoService.getById(1)).thenReturn(entity);
        when(modelMapper.map(entity, EstablecimientoDetailResponse.class)).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/establecimientos/1").with(asUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.nombre").value("La Trattoria"));
    }

    @Test
    void getById_nonExistentId_returnsNotFound() throws Exception {
        // given
        when(establecimientoService.getById(999))
                .thenThrow(new ResourceNotFoundException("Establecimiento", "id", 999));

        // when & then
        mockMvc.perform(get("/api/establecimientos/999").with(asUsuario()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void getById_typeMismatch_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/establecimientos/abc").with(asUsuario()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void getById_unauthenticated_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/establecimientos/1"))
                .andExpect(status().isForbidden());
    }

    // ================================================================== //
    //  GET /api/establecimientos  (paginated list)                        //
    // ================================================================== //

    @Test
    void list_authenticated_returnsOkWithPage() throws Exception {
        // given
        Establecimiento e1 = new Establecimiento();
        e1.setId(1);
        Page<Establecimiento> page = new PageImpl<>(List.of(e1), PageRequest.of(0, 10), 1);

        EstablecimientoListResponse listResp = new EstablecimientoListResponse();
        listResp.setId(1);
        listResp.setNombre("Test");

        when(establecimientoService.list(isNull(), any())).thenReturn(page);
        when(modelMapper.map(any(), eq(EstablecimientoListResponse.class))).thenReturn(listResp);

        // when & then
        mockMvc.perform(get("/api/establecimientos").with(asUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void list_filterByNombre_callsServiceWithNombre() throws Exception {
        // given
        Page<Establecimiento> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(establecimientoService.list(eq("Pizza"), any())).thenReturn(emptyPage);

        // when & then
        mockMvc.perform(get("/api/establecimientos?nombre=Pizza").with(asUsuario()))
                .andExpect(status().isOk());

        verify(establecimientoService).list(eq("Pizza"), any());
    }

    // ================================================================== //
    //  GET /api/establecimientos/tipo/{tipoId}                            //
    // ================================================================== //

    @Test
    void listByTipo_existingTipo_returnsOkWithPage() throws Exception {
        // given
        Establecimiento e1 = new Establecimiento();
        e1.setId(1);
        Page<Establecimiento> page = new PageImpl<>(List.of(e1), PageRequest.of(0, 10), 1);

        EstablecimientoListResponse listResp = new EstablecimientoListResponse();
        listResp.setId(1);
        listResp.setNombre("Restaurante Central");

        when(establecimientoService.listByTipoEstablecimiento(eq(1), isNull(), any())).thenReturn(page);
        when(modelMapper.map(any(), eq(EstablecimientoListResponse.class))).thenReturn(listResp);

        // when & then
        mockMvc.perform(get("/api/establecimientos/tipo/1?page=0&size=10").with(asUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    // ================================================================== //
    //  POST /api/establecimientos/filtro                                  //
    // ================================================================== //

    @Test
    void filtrarPorEtiquetas_validRequest_returnsOkWithList() throws Exception {
        // given
        Establecimiento e1 = new Establecimiento();
        e1.setId(1);

        EstablecimientoResponse response = new EstablecimientoResponse();
        response.setId(1);
        response.setNombre("Resto");

        when(establecimientoService.findByEtiquetas(any(), eq(true))).thenReturn(List.of(e1));
        when(modelMapper.map(any(), eq(EstablecimientoResponse.class))).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/establecimientos/filtro")
                        .with(asUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"etiquetaIds\":[1,2],\"busquedaEstricta\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void filtrarPorEtiquetas_emptyEtiquetaIds_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/establecimientos/filtro")
                        .with(asUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"etiquetaIds\":[],\"busquedaEstricta\":false}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.etiquetaIds").exists());
    }

    @Test
    void filtrarPorEtiquetas_nullEtiquetaIds_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/establecimientos/filtro")
                        .with(asUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.etiquetaIds").exists());
    }

    // ================================================================== //
    //  GET /api/establecimientos/sugeridos/{clienteId}                    //
    // ================================================================== //

    @Test
    void sugeridos_existingCliente_returnsOkWithList() throws Exception {
        // given
        Establecimiento e1 = new Establecimiento();
        e1.setId(1);

        EstablecimientoResponse response = new EstablecimientoResponse();
        response.setId(1);
        response.setNombre("Sugerido");

        when(establecimientoService.findSugeridos(5)).thenReturn(List.of(e1));
        when(modelMapper.map(any(), eq(EstablecimientoResponse.class))).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/establecimientos/sugeridos/5").with(asUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void sugeridos_nonExistentCliente_returnsNotFound() throws Exception {
        // given
        when(establecimientoService.findSugeridos(999))
                .thenThrow(new ResourceNotFoundException("Cliente", "id", 999));

        // when & then
        mockMvc.perform(get("/api/establecimientos/sugeridos/999").with(asUsuario()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    // ================================================================== //
    //  POST /api/establecimientos                                         //
    // ================================================================== //

    @Test
    void create_validRequest_returnsCreated() throws Exception {
        // given — mapRequestToEntity calls modelMapper.map(request, Establecimiento.class) first
        Establecimiento entity = new Establecimiento();
        entity.setId(0);
        entity.setNombre("Nuevo Local");

        Establecimiento saved = new Establecimiento();
        saved.setId(10);
        saved.setNombre("Nuevo Local");

        EstablecimientoDetailResponse response = new EstablecimientoDetailResponse();
        response.setId(10);
        response.setNombre("Nuevo Local");

        when(modelMapper.map(any(), eq(Establecimiento.class))).thenReturn(entity);
        when(establecimientoService.create(any())).thenReturn(saved);
        when(modelMapper.map(saved, EstablecimientoDetailResponse.class)).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/establecimientos")
                        .with(asUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idSuscripcion\":1,\"idTipoEstablecimiento\":2,\"nombre\":\"Nuevo Local\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Recurso creado"));
    }

    @Test
    void create_missingRequiredFields_returnsBadRequest() throws Exception {
        // blank nombre, null idSuscripcion, null idTipoEstablecimiento
        mockMvc.perform(post("/api/establecimientos")
                        .with(asUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.nombre").exists())
                .andExpect(jsonPath("$.fieldErrors.idSuscripcion").exists())
                .andExpect(jsonPath("$.fieldErrors.idTipoEstablecimiento").exists());
    }

    // ================================================================== //
    //  PUT /api/establecimientos/{id}                                     //
    // ================================================================== //

    @Test
    void update_existingId_returnsOk() throws Exception {
        // given
        Establecimiento entity = new Establecimiento();
        entity.setId(1);

        Establecimiento updated = new Establecimiento();
        updated.setId(1);
        updated.setNombre("Local Actualizado");

        EstablecimientoDetailResponse response = new EstablecimientoDetailResponse();
        response.setId(1);
        response.setNombre("Local Actualizado");

        when(modelMapper.map(any(), eq(Establecimiento.class))).thenReturn(entity);
        when(establecimientoService.update(eq(1), any())).thenReturn(updated);
        when(modelMapper.map(updated, EstablecimientoDetailResponse.class)).thenReturn(response);

        // when & then
        mockMvc.perform(put("/api/establecimientos/1")
                        .with(asUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idSuscripcion\":1,\"idTipoEstablecimiento\":2,\"nombre\":\"Local Actualizado\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nombre").value("Local Actualizado"));
    }

    @Test
    void update_nonExistentId_returnsNotFound() throws Exception {
        // given
        Establecimiento entity = new Establecimiento();
        when(modelMapper.map(any(), eq(Establecimiento.class))).thenReturn(entity);
        when(establecimientoService.update(eq(999), any()))
                .thenThrow(new ResourceNotFoundException("Establecimiento", "id", 999));

        // when & then
        mockMvc.perform(put("/api/establecimientos/999")
                        .with(asUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idSuscripcion\":1,\"idTipoEstablecimiento\":2,\"nombre\":\"Test\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void update_validationError_returnsBadRequest() throws Exception {
        // blank nombre triggers @NotBlank
        mockMvc.perform(put("/api/establecimientos/1")
                        .with(asUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idSuscripcion\":1,\"idTipoEstablecimiento\":2,\"nombre\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.nombre").exists());
    }

    // ================================================================== //
    //  DELETE /api/establecimientos/{id}                                  //
    // ================================================================== //

    @Test
    void delete_existingId_returnsOkWithNoData() throws Exception {
        mockMvc.perform(delete("/api/establecimientos/1").with(asUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(1))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void delete_nonExistentId_returnsNotFound() throws Exception {
        // given
        doThrow(new ResourceNotFoundException("Establecimiento", "id", 999))
                .when(establecimientoService).delete(999);

        // when & then
        mockMvc.perform(delete("/api/establecimientos/999").with(asUsuario()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }
}
