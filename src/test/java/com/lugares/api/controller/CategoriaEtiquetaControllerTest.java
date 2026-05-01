package com.lugares.api.controller;

import com.lugares.api.dto.response.CategoriaEtiquetaResponse;
import com.lugares.api.entity.CategoriaEtiqueta;
import com.lugares.api.exception.BusinessRuleException;
import com.lugares.api.exception.ResourceNotFoundException;
import com.lugares.api.mapper.CategoriaEtiquetaMapper;
import com.lugares.api.service.CategoriaEtiquetaService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;

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

@WebMvcTest(CategoriaEtiquetaController.class)
class CategoriaEtiquetaControllerTest extends BaseControllerTest {

    @MockitoBean
    private CategoriaEtiquetaService categoriaEtiquetaService;

    @MockitoBean
    private CategoriaEtiquetaMapper categoriaEtiquetaMapper;

    // ================================================================== //
    //  GET /api/categorias-etiqueta                                       //
    // ================================================================== //

    @Test
    void listAll_authenticated_returnsOkWithList() throws Exception {
        // given
        CategoriaEtiqueta entity1 = new CategoriaEtiqueta();
        entity1.setId(1);
        CategoriaEtiqueta entity2 = new CategoriaEtiqueta();
        entity2.setId(2);

        CategoriaEtiquetaResponse response = new CategoriaEtiquetaResponse();
        response.setId(1);
        response.setNombre("Comida");

        when(categoriaEtiquetaService.listAll()).thenReturn(List.of(entity1, entity2));
        when(categoriaEtiquetaMapper.toDto(any())).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/categorias-etiqueta").with(asUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(1))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void listAll_unauthenticated_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/categorias-etiqueta"))
                .andExpect(status().isForbidden());
    }

    // ================================================================== //
    //  GET /api/categorias-etiqueta/{id}                                  //
    // ================================================================== //

    @Test
    void getById_existingId_returnsOk() throws Exception {
        // given
        CategoriaEtiqueta entity = new CategoriaEtiqueta();
        entity.setId(1);

        CategoriaEtiquetaResponse response = new CategoriaEtiquetaResponse();
        response.setId(1);
        response.setNombre("Comida");

        when(categoriaEtiquetaService.getById(1)).thenReturn(entity);
        when(categoriaEtiquetaMapper.toDto(entity)).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/categorias-etiqueta/1").with(asUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.nombre").value("Comida"));
    }

    @Test
    void getById_nonExistentId_returnsNotFound() throws Exception {
        // given
        when(categoriaEtiquetaService.getById(999))
                .thenThrow(new ResourceNotFoundException("CategoriaEtiqueta", "id", 999));

        // when & then
        mockMvc.perform(get("/api/categorias-etiqueta/999").with(asUsuario()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    // ================================================================== //
    //  POST /api/categorias-etiqueta                                      //
    // ================================================================== //

    @Test
    void create_validEntity_returnsCreated() throws Exception {
        // given
        CategoriaEtiqueta savedEntity = new CategoriaEtiqueta();
        savedEntity.setId(1);

        CategoriaEtiquetaResponse response = new CategoriaEtiquetaResponse();
        response.setId(1);
        response.setNombre("Nueva");

        when(categoriaEtiquetaService.create(any())).thenReturn(savedEntity);
        when(categoriaEtiquetaMapper.toDto(savedEntity)).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/categorias-etiqueta")
                        .with(asUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Nueva\",\"descripcion\":\"Desc\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Recurso creado"))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    // ================================================================== //
    //  PUT /api/categorias-etiqueta/{id}                                  //
    // ================================================================== //

    @Test
    void update_existingId_returnsOk() throws Exception {
        // given
        CategoriaEtiqueta updated = new CategoriaEtiqueta();
        updated.setId(1);

        CategoriaEtiquetaResponse response = new CategoriaEtiquetaResponse();
        response.setId(1);

        when(categoriaEtiquetaService.update(eq(1), any())).thenReturn(updated);
        when(categoriaEtiquetaMapper.toDto(updated)).thenReturn(response);

        // when & then
        mockMvc.perform(put("/api/categorias-etiqueta/1")
                        .with(asUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Actualizado\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(1));
    }

    @Test
    void update_nonExistentId_returnsNotFound() throws Exception {
        // given
        when(categoriaEtiquetaService.update(eq(999), any()))
                .thenThrow(new ResourceNotFoundException("CategoriaEtiqueta", "id", 999));

        // when & then
        mockMvc.perform(put("/api/categorias-etiqueta/999")
                        .with(asUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Updated\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    // ================================================================== //
    //  DELETE /api/categorias-etiqueta/{id}                               //
    // ================================================================== //

    @Test
    void delete_existingId_returnsOkWithNoData() throws Exception {
        mockMvc.perform(delete("/api/categorias-etiqueta/1").with(asUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(1))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void delete_nonExistentId_returnsNotFound() throws Exception {
        // given
        doThrow(new ResourceNotFoundException("CategoriaEtiqueta", "id", 999))
                .when(categoriaEtiquetaService).delete(999);

        // when & then
        mockMvc.perform(delete("/api/categorias-etiqueta/999").with(asUsuario()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    // ================================================================== //
    //  DELETE /api/categorias-etiqueta/{id} — FK guard branch (P0 #7)  //
    // ================================================================== //

    @Test
    void delete_categoryInUse_returnsUnprocessableEntity() throws Exception {
        // given — category has etiquetas, FK guard in service throws BusinessRuleException
        doThrow(new BusinessRuleException(
                "No se puede eliminar la categoria 'Comida' porque tiene 3 etiqueta(s) asociada(s). " +
                "Elimine o reasigne las etiquetas primero."))
                .when(categoriaEtiquetaService).delete(1);

        // when & then — BusinessRuleException → 422
        mockMvc.perform(delete("/api/categorias-etiqueta/1").with(asUsuario()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("Unprocessable Entity"))
                .andExpect(jsonPath("$.message").value(
                        "No se puede eliminar la categoria 'Comida' porque tiene 3 etiqueta(s) asociada(s). " +
                        "Elimine o reasigne las etiquetas primero."));
    }
}
