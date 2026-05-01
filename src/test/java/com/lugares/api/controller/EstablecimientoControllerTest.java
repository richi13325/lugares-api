package com.lugares.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lugares.api.dto.response.EstablecimientoDetailResponse;
import com.lugares.api.dto.response.EstablecimientoListResponse;
import com.lugares.api.dto.response.EstablecimientoResponse;
import com.lugares.api.entity.Establecimiento;
import com.lugares.api.exception.ResourceNotFoundException;
import com.lugares.api.mapper.EstablecimientoMapper;
import com.lugares.api.service.EstablecimientoService;
import com.lugares.api.service.SupabaseStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EstablecimientoController.class)
class EstablecimientoControllerTest extends BaseControllerTest {

    @MockitoBean
    private EstablecimientoService establecimientoService;

    @MockitoBean
    private EstablecimientoMapper establecimientoMapper;

    @MockitoBean
    private SupabaseStorageService storageService;

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
        when(establecimientoMapper.toDetailDto(entity)).thenReturn(response);

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
    //  GET /api/establecimientos/{id} — 4-image scenario (REQ-007)       //
    // ================================================================== //

    @Test
    void getById_with4Photos_returnsAll4() throws Exception {
        // given
        Establecimiento entity = new Establecimiento();
        entity.setId(5);
        entity.setImgRefs("https://sb.io/img1.jpg");
        entity.setImgRefs2("https://sb.io/img2.jpg");
        entity.setImgRefs3("https://sb.io/img3.jpg");
        entity.setImgRefs4("https://sb.io/img4.jpg");

        EstablecimientoDetailResponse response = new EstablecimientoDetailResponse();
        response.setId(5);
        response.setImgRefs("https://sb.io/img1.jpg");
        response.setImgRefs2("https://sb.io/img2.jpg");
        response.setImgRefs3("https://sb.io/img3.jpg");
        response.setImgRefs4("https://sb.io/img4.jpg");

        when(establecimientoService.getById(5)).thenReturn(entity);
        when(establecimientoMapper.toDetailDto(entity)).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/establecimientos/5").with(asUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.imgRefs").value("https://sb.io/img1.jpg"))
                .andExpect(jsonPath("$.data.imgRefs2").value("https://sb.io/img2.jpg"))
                .andExpect(jsonPath("$.data.imgRefs3").value("https://sb.io/img3.jpg"))
                .andExpect(jsonPath("$.data.imgRefs4").value("https://sb.io/img4.jpg"));
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
        when(establecimientoMapper.toListDto(any())).thenReturn(listResp);

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
        when(establecimientoMapper.toListDto(any())).thenReturn(listResp);

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
        when(establecimientoMapper.toDto(any())).thenReturn(response);

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
        when(establecimientoMapper.toDto(any())).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/establecimientos/sugeridos/5").with(asClienteWithId(5)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void sugeridos_nonExistentCliente_returnsNotFound() throws Exception {
        // given
        when(establecimientoService.findSugeridos(999))
                .thenThrow(new ResourceNotFoundException("Cliente", "id", 999));

        // when & then
        mockMvc.perform(get("/api/establecimientos/sugeridos/999").with(asClienteWithId(999)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void sugeridos_fromDifferentCliente_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/establecimientos/sugeridos/5").with(asClienteWithId(2)))
                .andExpect(status().isForbidden());
    }

    // ================================================================== //
    //  POST /api/establecimientos  (multipart/form-data)                  //
    // ================================================================== //

    private byte[] datosJson(ObjectMapper mapper) throws Exception {
        return mapper.writeValueAsBytes(
                mapper.createObjectNode()
                        .put("idSuscripcion", 1)
                        .put("idTipoEstablecimiento", 2)
                        .put("nombre", "Nuevo Local"));
    }

    @Test
    void create_validRequest_with1Image_returnsCreated() throws Exception {
        // given
        Establecimiento entity = new Establecimiento();
        entity.setId(0);
        entity.setNombre("Nuevo Local");

        Establecimiento saved = new Establecimiento();
        saved.setId(10);
        saved.setNombre("Nuevo Local");
        saved.setImgRefs("https://sb.io/establecimientos/img1.jpg");

        EstablecimientoDetailResponse response = new EstablecimientoDetailResponse();
        response.setId(10);
        response.setNombre("Nuevo Local");
        response.setImgRefs("https://sb.io/establecimientos/img1.jpg");

        when(storageService.uploadFile(any(), eq("establecimientos")))
                .thenReturn("https://sb.io/establecimientos/img1.jpg");
        when(establecimientoMapper.toEntity(any())).thenReturn(entity);
        when(establecimientoService.create(any())).thenReturn(saved);
        when(establecimientoMapper.toDetailDto(saved)).thenReturn(response);

        MockMultipartFile foto = new MockMultipartFile("foto", "img1.jpg", "image/jpeg", new byte[]{1, 2, 3});
        MockPart datos = new MockPart("datos", datosJson(objectMapper));
        datos.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // when & then
        mockMvc.perform(multipart("/api/establecimientos")
                        .file(foto)
                        .part(datos)
                        .with(asUsuario()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Recurso creado"))
                .andExpect(jsonPath("$.data.imgRefs").value("https://sb.io/establecimientos/img1.jpg"));
    }

    @Test
    void create_with2Images_primary_and_secondary_populated() throws Exception {
        // given
        Establecimiento saved = new Establecimiento();
        saved.setId(11);
        saved.setImgRefs("https://sb.io/establecimientos/img1.jpg");
        saved.setImgRefs2("https://sb.io/establecimientos/img2.jpg");

        EstablecimientoDetailResponse response = new EstablecimientoDetailResponse();
        response.setId(11);
        response.setImgRefs("https://sb.io/establecimientos/img1.jpg");
        response.setImgRefs2("https://sb.io/establecimientos/img2.jpg");
        // imgRefs3 and imgRefs4 remain null (not set)

        when(storageService.uploadFile(any(), eq("establecimientos")))
                .thenReturn("https://sb.io/establecimientos/img1.jpg")
                .thenReturn("https://sb.io/establecimientos/img2.jpg");
        when(establecimientoMapper.toEntity(any())).thenReturn(new Establecimiento());
        when(establecimientoService.create(any())).thenReturn(saved);
        when(establecimientoMapper.toDetailDto(saved)).thenReturn(response);

        MockMultipartFile foto = new MockMultipartFile("foto", "img1.jpg", "image/jpeg", new byte[]{1, 2, 3});
        MockMultipartFile foto2 = new MockMultipartFile("foto2", "img2.jpg", "image/jpeg", new byte[]{4, 5, 6});
        MockPart datos = new MockPart("datos", datosJson(objectMapper));
        datos.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // when & then
        mockMvc.perform(multipart("/api/establecimientos")
                        .file(foto)
                        .file(foto2)
                        .part(datos)
                        .with(asUsuario()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.imgRefs").value("https://sb.io/establecimientos/img1.jpg"))
                .andExpect(jsonPath("$.data.imgRefs2").value("https://sb.io/establecimientos/img2.jpg"))
                .andExpect(jsonPath("$.data.imgRefs3").doesNotExist())
                .andExpect(jsonPath("$.data.imgRefs4").doesNotExist());
    }

    @Test
    void create_with4Images_allPopulated() throws Exception {
        // given
        Establecimiento saved = new Establecimiento();
        saved.setId(12);
        saved.setImgRefs("https://sb.io/img1.jpg");
        saved.setImgRefs2("https://sb.io/img2.jpg");
        saved.setImgRefs3("https://sb.io/img3.jpg");
        saved.setImgRefs4("https://sb.io/img4.jpg");

        EstablecimientoDetailResponse response = new EstablecimientoDetailResponse();
        response.setId(12);
        response.setImgRefs("https://sb.io/img1.jpg");
        response.setImgRefs2("https://sb.io/img2.jpg");
        response.setImgRefs3("https://sb.io/img3.jpg");
        response.setImgRefs4("https://sb.io/img4.jpg");

        when(storageService.uploadFile(any(), eq("establecimientos")))
                .thenReturn("https://sb.io/img1.jpg")
                .thenReturn("https://sb.io/img2.jpg")
                .thenReturn("https://sb.io/img3.jpg")
                .thenReturn("https://sb.io/img4.jpg");
        when(establecimientoMapper.toEntity(any())).thenReturn(new Establecimiento());
        when(establecimientoService.create(any())).thenReturn(saved);
        when(establecimientoMapper.toDetailDto(saved)).thenReturn(response);

        MockMultipartFile foto = new MockMultipartFile("foto", "img1.jpg", "image/jpeg", new byte[]{1});
        MockMultipartFile foto2 = new MockMultipartFile("foto2", "img2.jpg", "image/jpeg", new byte[]{2});
        MockMultipartFile foto3 = new MockMultipartFile("foto3", "img3.jpg", "image/jpeg", new byte[]{3});
        MockMultipartFile foto4 = new MockMultipartFile("foto4", "img4.jpg", "image/jpeg", new byte[]{4});
        MockPart datos = new MockPart("datos", datosJson(objectMapper));
        datos.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // when & then
        mockMvc.perform(multipart("/api/establecimientos")
                        .file(foto)
                        .file(foto2)
                        .file(foto3)
                        .file(foto4)
                        .part(datos)
                        .with(asUsuario()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.imgRefs").value("https://sb.io/img1.jpg"))
                .andExpect(jsonPath("$.data.imgRefs2").value("https://sb.io/img2.jpg"))
                .andExpect(jsonPath("$.data.imgRefs3").value("https://sb.io/img3.jpg"))
                .andExpect(jsonPath("$.data.imgRefs4").value("https://sb.io/img4.jpg"));
    }

    @Test
    void create_missingPrimaryImage_returns400() throws Exception {
        // No "foto" part — validarImagen(null, "foto", true) throws IllegalArgumentException → 400
        MockPart datos = new MockPart("datos", datosJson(objectMapper));
        datos.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart("/api/establecimientos")
                        .part(datos)
                        .with(asUsuario()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_nonImageMimeType_returns400() throws Exception {
        // "foto" with application/pdf — validarImagen throws IllegalArgumentException → 400
        MockMultipartFile foto = new MockMultipartFile("foto", "doc.pdf", "application/pdf", new byte[]{1, 2, 3});
        MockPart datos = new MockPart("datos", datosJson(objectMapper));
        datos.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart("/api/establecimientos")
                        .file(foto)
                        .part(datos)
                        .with(asUsuario()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_missingRequiredFields_returnsBadRequest() throws Exception {
        // blank nombre, null idSuscripcion, null idTipoEstablecimiento
        byte[] invalidDatos = objectMapper.writeValueAsBytes(
                objectMapper.createObjectNode().put("nombre", ""));

        MockMultipartFile foto = new MockMultipartFile("foto", "img.jpg", "image/jpeg", new byte[]{1});
        MockPart datos = new MockPart("datos", invalidDatos);
        datos.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart("/api/establecimientos")
                        .file(foto)
                        .part(datos)
                        .with(asUsuario()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.nombre").exists())
                .andExpect(jsonPath("$.fieldErrors.idSuscripcion").exists())
                .andExpect(jsonPath("$.fieldErrors.idTipoEstablecimiento").exists());
    }

    // ================================================================== //
    //  PUT /api/establecimientos/{id}  (multipart/form-data)              //
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

        when(establecimientoMapper.toEntity(any())).thenReturn(entity);
        when(establecimientoService.update(eq(1), any())).thenReturn(updated);
        when(establecimientoMapper.toDetailDto(updated)).thenReturn(response);

        byte[] updateDatos = objectMapper.writeValueAsBytes(
                objectMapper.createObjectNode()
                        .put("idSuscripcion", 1)
                        .put("idTipoEstablecimiento", 2)
                        .put("nombre", "Local Actualizado"));

        MockPart datos = new MockPart("datos", updateDatos);
        datos.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // when & then
        mockMvc.perform(multipart("/api/establecimientos/1")
                        .part(datos)
                        .with(request -> { request.setMethod("PUT"); return request; })
                        .with(asUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nombre").value("Local Actualizado"));
    }

    @Test
    void update_nonExistentId_returnsNotFound() throws Exception {
        // given
        Establecimiento entity = new Establecimiento();
        when(establecimientoMapper.toEntity(any())).thenReturn(entity);
        when(establecimientoService.update(eq(999), any()))
                .thenThrow(new ResourceNotFoundException("Establecimiento", "id", 999));

        byte[] updateDatos = objectMapper.writeValueAsBytes(
                objectMapper.createObjectNode()
                        .put("idSuscripcion", 1)
                        .put("idTipoEstablecimiento", 2)
                        .put("nombre", "Test"));

        MockPart datos = new MockPart("datos", updateDatos);
        datos.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // when & then
        mockMvc.perform(multipart("/api/establecimientos/999")
                        .part(datos)
                        .with(request -> { request.setMethod("PUT"); return request; })
                        .with(asUsuario()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void update_validationError_returnsBadRequest() throws Exception {
        // blank nombre triggers @NotBlank
        byte[] invalidDatos = objectMapper.writeValueAsBytes(
                objectMapper.createObjectNode()
                        .put("idSuscripcion", 1)
                        .put("idTipoEstablecimiento", 2)
                        .put("nombre", ""));

        MockPart datos = new MockPart("datos", invalidDatos);
        datos.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart("/api/establecimientos/1")
                        .part(datos)
                        .with(request -> { request.setMethod("PUT"); return request; })
                        .with(asUsuario()))
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

    // ================================================================== //
    //  POST/PUT /api/establecimientos — DataIntegrityViolationException (P0 #8/#9) //
    // ================================================================== //

    @Test
    void create_serviceThrowsDataIntegrityViolation_returnsConflict() throws Exception {
        // given — service hits a DB constraint (e.g. duplicate unique key on nombre)
        // controller receives DataIntegrityViolationException → GlobalExceptionHandler → 409
        when(establecimientoMapper.toEntity(any())).thenReturn(new Establecimiento());
        when(establecimientoService.create(any()))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry 'Test Local' for key 'nombre'"));

        MockMultipartFile foto = new MockMultipartFile("foto", "img.jpg", "image/jpeg", new byte[]{1});
        MockPart datos = new MockPart("datos", datosJson(objectMapper));
        datos.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // when & then
        mockMvc.perform(multipart("/api/establecimientos")
                        .file(foto)
                        .part(datos)
                        .with(asUsuario()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Data Integrity Violation"));
    }

    @Test
    void update_serviceThrowsDataIntegrityViolation_returnsConflict() throws Exception {
        // given — service update hits a DB constraint
        when(establecimientoMapper.toEntity(any())).thenReturn(new Establecimiento());
        when(establecimientoService.update(eq(1), any()))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry 'Test Local' for key 'nombre'"));

        byte[] updateDatos = objectMapper.writeValueAsBytes(
                objectMapper.createObjectNode()
                        .put("idSuscripcion", 1)
                        .put("idTipoEstablecimiento", 2)
                        .put("nombre", "Test Local"));

        MockPart datos = new MockPart("datos", updateDatos);
        datos.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // when & then
        mockMvc.perform(multipart("/api/establecimientos/1")
                        .part(datos)
                        .with(request -> { request.setMethod("PUT"); return request; })
                        .with(asUsuario()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Data Integrity Violation"));
    }
}
