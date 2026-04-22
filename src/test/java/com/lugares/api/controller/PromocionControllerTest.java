package com.lugares.api.controller;

import com.lugares.api.dto.request.PromocionRequest;
import com.lugares.api.dto.response.PromocionListResponse;
import com.lugares.api.dto.response.PromocionResponse;
import com.lugares.api.entity.Promocion;
import com.lugares.api.exception.ResourceNotFoundException;
import com.lugares.api.mapper.PromocionMapper;
import com.lugares.api.service.PromocionService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PromocionController.class)
class PromocionControllerTest extends BaseControllerTest {

    @MockitoBean
    private PromocionService promocionService;

    @MockitoBean
    private PromocionMapper promocionMapper;

    // ================================================================== //
    //  GET /api/promociones/{id}                                          //
    // ================================================================== //

    @Test
    void getById_existingId_returnsOk() throws Exception {
        Promocion entity = new Promocion();
        entity.setId(1);
        entity.setNombre("2x1 en pizzas");

        PromocionResponse response = new PromocionResponse();
        response.setId(1);
        response.setNombre("2x1 en pizzas");

        when(promocionService.getById(1)).thenReturn(entity);
        when(promocionMapper.toDto(entity)).thenReturn(response);

        mockMvc.perform(get("/api/promociones/1").with(asUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.nombre").value("2x1 en pizzas"));
    }

    @Test
    void getById_nonExistentId_returnsNotFound() throws Exception {
        when(promocionService.getById(999))
                .thenThrow(new ResourceNotFoundException("Promocion", "id", 999));

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
    //  GET /api/promociones (paginated)                                   //
    // ================================================================== //

    @Test
    void list_authenticated_returnsOkWithPage() throws Exception {
        Promocion p1 = new Promocion();
        p1.setId(1);
        Page<Promocion> page = new PageImpl<>(List.of(p1), PageRequest.of(0, 10), 1);

        PromocionListResponse listResp = new PromocionListResponse();
        listResp.setId(1);
        listResp.setNombre("Promo test");

        when(promocionService.list(isNull(), any())).thenReturn(page);
        when(promocionMapper.toListDto(any())).thenReturn(listResp);

        mockMvc.perform(get("/api/promociones").with(asUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    // ================================================================== //
    //  GET /api/promociones/establecimiento/{id}                          //
    // ================================================================== //

    @Test
    void listByEstablecimiento_existingId_returnsOkWithList() throws Exception {
        Promocion p1 = new Promocion();
        p1.setId(1);

        PromocionResponse response = new PromocionResponse();
        response.setId(1);
        response.setNombre("Promo local");

        when(promocionService.listByEstablecimiento(1)).thenReturn(List.of(p1));
        when(promocionMapper.toDto(any())).thenReturn(response);

        mockMvc.perform(get("/api/promociones/establecimiento/1").with(asUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void listByEstablecimiento_nonExistentId_returnsNotFound() throws Exception {
        when(promocionService.listByEstablecimiento(999))
                .thenThrow(new ResourceNotFoundException("Establecimiento", "id", 999));

        mockMvc.perform(get("/api/promociones/establecimiento/999").with(asUsuario()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    // ================================================================== //
    //  POST /api/promociones (multipart)                                  //
    // ================================================================== //

    @Test
    void create_withImage_returnsCreated() throws Exception {
        Promocion saved = new Promocion();
        saved.setId(10);
        saved.setNombre("Promo nueva");

        PromocionResponse response = new PromocionResponse();
        response.setId(10);
        response.setNombre("Promo nueva");
        response.setCodigoValidacion("ABCD1234");
        response.setImagen("https://supabase.co/storage/v1/object/public/bucket/promociones/uuid.jpg");

        PromocionRequest req = new PromocionRequest();
        req.setIdSuscripcion(1);
        req.setIdEstablecimiento(2);
        req.setNombre("Promo nueva");
        req.setCodigoValidacion("ABCD1234");
        req.setTipoPromocion("PERMANENTE");

        MockMultipartFile dataPart = new MockMultipartFile(
                "data", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(req));
        MockMultipartFile imagenPart = new MockMultipartFile(
                "imagen", "promo.jpg", MediaType.IMAGE_JPEG_VALUE, "fake-bytes".getBytes());

        when(promocionMapper.toEntity(any(PromocionRequest.class))).thenReturn(new Promocion());
        when(promocionService.create(any(Promocion.class), any())).thenReturn(saved);
        when(promocionMapper.toDto(saved)).thenReturn(response);

        mockMvc.perform(multipart("/api/promociones")
                        .file(dataPart)
                        .file(imagenPart)
                        .with(asUsuario()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Recurso creado"))
                .andExpect(jsonPath("$.data.imagen").value("https://supabase.co/storage/v1/object/public/bucket/promociones/uuid.jpg"));
    }

    @Test
    void create_withoutImage_returnsCreated() throws Exception {
        Promocion saved = new Promocion();
        saved.setId(11);

        PromocionResponse response = new PromocionResponse();
        response.setId(11);
        response.setNombre("Promo sin imagen");
        response.setCodigoValidacion("ABCD1234");
        response.setImagen(null);

        PromocionRequest req = new PromocionRequest();
        req.setIdSuscripcion(1);
        req.setIdEstablecimiento(2);
        req.setNombre("Promo sin imagen");
        req.setCodigoValidacion("ABCD1234");
        req.setTipoPromocion("PERMANENTE");

        MockMultipartFile dataPart = new MockMultipartFile(
                "data", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(req));

        when(promocionMapper.toEntity(any(PromocionRequest.class))).thenReturn(new Promocion());
        when(promocionService.create(any(Promocion.class), isNull())).thenReturn(saved);
        when(promocionMapper.toDto(saved)).thenReturn(response);

        mockMvc.perform(multipart("/api/promociones")
                        .file(dataPart)
                        .with(asUsuario()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.imagen").isEmpty());
    }

    @Test
    void create_missingRequiredFields_returnsBadRequest() throws Exception {
        PromocionRequest req = new PromocionRequest();
        req.setNombre("");

        MockMultipartFile dataPart = new MockMultipartFile(
                "data", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(req));

        mockMvc.perform(multipart("/api/promociones")
                        .file(dataPart)
                        .with(asUsuario()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.nombre").exists())
                .andExpect(jsonPath("$.fieldErrors.idSuscripcion").exists())
                .andExpect(jsonPath("$.fieldErrors.idEstablecimiento").exists());
    }

    // ================================================================== //
    //  PUT /api/promociones/{id} (multipart)                              //
    // ================================================================== //

    @Test
    void update_withImage_returnsOk() throws Exception {
        Promocion updated = new Promocion();
        updated.setId(1);
        updated.setNombre("Promo actualizada");

        PromocionResponse response = new PromocionResponse();
        response.setId(1);
        response.setNombre("Promo actualizada");
        response.setImagen("https://supabase.co/storage/v1/object/public/bucket/promociones/new-uuid.jpg");

        PromocionRequest req = new PromocionRequest();
        req.setIdSuscripcion(1);
        req.setIdEstablecimiento(2);
        req.setNombre("Promo actualizada");
        req.setCodigoValidacion("ABCD1234");
        req.setTipoPromocion("PERMANENTE");

        MockMultipartFile dataPart = new MockMultipartFile(
                "data", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(req));
        MockMultipartFile imagenPart = new MockMultipartFile(
                "imagen", "nueva.jpg", MediaType.IMAGE_JPEG_VALUE, "bytes".getBytes());

        when(promocionMapper.toEntity(any(PromocionRequest.class))).thenReturn(new Promocion());
        when(promocionService.update(eq(1), any(Promocion.class), any())).thenReturn(updated);
        when(promocionMapper.toDto(updated)).thenReturn(response);

        mockMvc.perform(multipart("/api/promociones/1")
                        .file(dataPart)
                        .file(imagenPart)
                        .with(request -> { request.setMethod("PUT"); return request; })
                        .with(asUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nombre").value("Promo actualizada"))
                .andExpect(jsonPath("$.data.imagen").value("https://supabase.co/storage/v1/object/public/bucket/promociones/new-uuid.jpg"));
    }

    @Test
    void update_nonExistentId_returnsNotFound() throws Exception {
        PromocionRequest req = new PromocionRequest();
        req.setIdSuscripcion(1);
        req.setIdEstablecimiento(2);
        req.setNombre("Test");
        req.setCodigoValidacion("ABCD1234");
        req.setTipoPromocion("PERMANENTE");

        MockMultipartFile dataPart = new MockMultipartFile(
                "data", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(req));

        when(promocionMapper.toEntity(any(PromocionRequest.class))).thenReturn(new Promocion());
        when(promocionService.update(eq(999), any(Promocion.class), any()))
                .thenThrow(new ResourceNotFoundException("Promocion", "id", 999));

        mockMvc.perform(multipart("/api/promociones/999")
                        .file(dataPart)
                        .with(request -> { request.setMethod("PUT"); return request; })
                        .with(asUsuario()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
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
        doThrow(new ResourceNotFoundException("Promocion", "id", 999))
                .when(promocionService).delete(999);

        mockMvc.perform(delete("/api/promociones/999").with(asUsuario()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }
}
