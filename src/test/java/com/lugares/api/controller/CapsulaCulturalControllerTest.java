package com.lugares.api.controller;

import com.lugares.api.dto.request.CapsulaCulturalRequest;
import com.lugares.api.dto.response.CapsulaCulturalResponse;
import com.lugares.api.entity.CapsulaCultural;
import com.lugares.api.exception.ResourceNotFoundException;
import com.lugares.api.mapper.CapsulaCulturalMapper;
import com.lugares.api.service.CapsulaCulturalService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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

@WebMvcTest(CapsulaCulturalController.class)
class CapsulaCulturalControllerTest extends BaseControllerTest {

    @MockitoBean
    private CapsulaCulturalService capsulaCulturalService;

    @MockitoBean
    private CapsulaCulturalMapper capsulaCulturalMapper;

    // ================================================================== //
    //  GET /api/capsulas-culturales                                       //
    // ================================================================== //

    @Test
    void listAll_authenticated_returnsOkWithList() throws Exception {
        CapsulaCultural entity1 = new CapsulaCultural();
        entity1.setId(1);
        CapsulaCultural entity2 = new CapsulaCultural();
        entity2.setId(2);

        CapsulaCulturalResponse response = new CapsulaCulturalResponse();
        response.setId(1);
        response.setTitulo("Historia del Tango");
        response.setEsVisible(true);

        when(capsulaCulturalService.listAll()).thenReturn(List.of(entity1, entity2));
        when(capsulaCulturalMapper.toDto(any())).thenReturn(response);

        mockMvc.perform(get("/api/capsulas-culturales").with(asUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(1))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void listAll_unauthenticated_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/capsulas-culturales"))
                .andExpect(status().isForbidden());
    }

    // ================================================================== //
    //  GET /api/capsulas-culturales/{id}                                  //
    // ================================================================== //

    @Test
    void getById_existingId_returnsOk() throws Exception {
        CapsulaCultural entity = new CapsulaCultural();
        entity.setId(1);

        CapsulaCulturalResponse response = new CapsulaCulturalResponse();
        response.setId(1);
        response.setTitulo("Historia del Tango");
        response.setEsVisible(true);

        when(capsulaCulturalService.getById(1)).thenReturn(entity);
        when(capsulaCulturalMapper.toDto(entity)).thenReturn(response);

        mockMvc.perform(get("/api/capsulas-culturales/1").with(asUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.titulo").value("Historia del Tango"))
                .andExpect(jsonPath("$.data.esVisible").value(true));
    }

    @Test
    void getById_nonExistentId_returnsNotFound() throws Exception {
        when(capsulaCulturalService.getById(999))
                .thenThrow(new ResourceNotFoundException("CapsulaCultural", "id", 999));

        mockMvc.perform(get("/api/capsulas-culturales/999").with(asUsuario()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    // ================================================================== //
    //  POST /api/capsulas-culturales (multipart)                          //
    // ================================================================== //

    @Test
    void create_withImage_returnsCreated() throws Exception {
        CapsulaCultural saved = new CapsulaCultural();
        saved.setId(1);

        CapsulaCulturalResponse response = new CapsulaCulturalResponse();
        response.setId(1);
        response.setTitulo("Nueva Capsula");
        response.setImagen("https://supabase.co/storage/v1/object/public/bucket/capsulas/uuid.jpg");

        MockMultipartFile dataPart = new MockMultipartFile(
                "data", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(new CapsulaCulturalRequest() {{ setTitulo("Nueva Capsula"); }}));
        MockMultipartFile imagenPart = new MockMultipartFile(
                "imagen", "foto.jpg", MediaType.IMAGE_JPEG_VALUE, "fake-bytes".getBytes());

        when(capsulaCulturalMapper.toEntity(any(CapsulaCulturalRequest.class))).thenReturn(new CapsulaCultural());
        when(capsulaCulturalService.create(any(CapsulaCultural.class), any())).thenReturn(saved);
        when(capsulaCulturalMapper.toDto(saved)).thenReturn(response);

        mockMvc.perform(multipart("/api/capsulas-culturales")
                        .file(dataPart)
                        .file(imagenPart)
                        .with(asUsuario()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Recurso creado"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.imagen").value("https://supabase.co/storage/v1/object/public/bucket/capsulas/uuid.jpg"));
    }

    @Test
    void create_withoutImage_returnsCreated() throws Exception {
        CapsulaCultural saved = new CapsulaCultural();
        saved.setId(2);

        CapsulaCulturalResponse response = new CapsulaCulturalResponse();
        response.setId(2);
        response.setTitulo("Capsula sin imagen");
        response.setImagen(null);

        MockMultipartFile dataPart = new MockMultipartFile(
                "data", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(new CapsulaCulturalRequest() {{ setTitulo("Capsula sin imagen"); }}));

        when(capsulaCulturalMapper.toEntity(any(CapsulaCulturalRequest.class))).thenReturn(new CapsulaCultural());
        when(capsulaCulturalService.create(any(CapsulaCultural.class), isNull())).thenReturn(saved);
        when(capsulaCulturalMapper.toDto(saved)).thenReturn(response);

        mockMvc.perform(multipart("/api/capsulas-culturales")
                        .file(dataPart)
                        .with(asUsuario()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.imagen").isEmpty());
    }

    @Test
    void create_missingTitulo_returnsBadRequest() throws Exception {
        MockMultipartFile dataPart = new MockMultipartFile(
                "data", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(new CapsulaCulturalRequest()));

        mockMvc.perform(multipart("/api/capsulas-culturales")
                        .file(dataPart)
                        .with(asUsuario()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.titulo").exists());
    }

    // ================================================================== //
    //  PUT /api/capsulas-culturales/{id} (multipart)                      //
    // ================================================================== //

    @Test
    void update_withImage_returnsOk() throws Exception {
        CapsulaCultural updated = new CapsulaCultural();
        updated.setId(1);

        CapsulaCulturalResponse response = new CapsulaCulturalResponse();
        response.setId(1);
        response.setImagen("https://supabase.co/storage/v1/object/public/bucket/capsulas/new-uuid.jpg");

        MockMultipartFile dataPart = new MockMultipartFile(
                "data", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(new CapsulaCulturalRequest() {{ setTitulo("Actualizado"); }}));
        MockMultipartFile imagenPart = new MockMultipartFile(
                "imagen", "nueva.jpg", MediaType.IMAGE_JPEG_VALUE, "bytes".getBytes());

        when(capsulaCulturalMapper.toEntity(any(CapsulaCulturalRequest.class))).thenReturn(new CapsulaCultural());
        when(capsulaCulturalService.update(eq(1), any(CapsulaCultural.class), any())).thenReturn(updated);
        when(capsulaCulturalMapper.toDto(updated)).thenReturn(response);

        mockMvc.perform(multipart("/api/capsulas-culturales/1")
                        .file(dataPart)
                        .file(imagenPart)
                        .with(request -> { request.setMethod("PUT"); return request; })
                        .with(asUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.imagen").value("https://supabase.co/storage/v1/object/public/bucket/capsulas/new-uuid.jpg"));
    }

    @Test
    void update_nonExistentId_returnsNotFound() throws Exception {
        MockMultipartFile dataPart = new MockMultipartFile(
                "data", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(new CapsulaCulturalRequest() {{ setTitulo("Updated"); }}));

        when(capsulaCulturalMapper.toEntity(any(CapsulaCulturalRequest.class))).thenReturn(new CapsulaCultural());
        when(capsulaCulturalService.update(eq(999), any(CapsulaCultural.class), any()))
                .thenThrow(new ResourceNotFoundException("CapsulaCultural", "id", 999));

        mockMvc.perform(multipart("/api/capsulas-culturales/999")
                        .file(dataPart)
                        .with(request -> { request.setMethod("PUT"); return request; })
                        .with(asUsuario()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    // ================================================================== //
    //  DELETE /api/capsulas-culturales/{id}                               //
    // ================================================================== //

    @Test
    void delete_existingId_returnsOkWithNoData() throws Exception {
        mockMvc.perform(delete("/api/capsulas-culturales/1").with(asUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(1))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void delete_nonExistentId_returnsNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("CapsulaCultural", "id", 999))
                .when(capsulaCulturalService).delete(999);

        mockMvc.perform(delete("/api/capsulas-culturales/999").with(asUsuario()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }
}
