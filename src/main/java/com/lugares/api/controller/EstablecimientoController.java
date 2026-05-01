package com.lugares.api.controller;

import com.lugares.api.common.ApiResponse;
import com.lugares.api.dto.request.EstablecimientoRequest;
import com.lugares.api.dto.request.FiltroEstablecimientoRequest;
import com.lugares.api.dto.response.EstablecimientoDetailResponse;
import com.lugares.api.dto.response.EstablecimientoListResponse;
import com.lugares.api.dto.response.EstablecimientoResponse;
import com.lugares.api.entity.Establecimiento;
import com.lugares.api.mapper.EstablecimientoMapper;
import com.lugares.api.service.EstablecimientoService;
import com.lugares.api.service.SupabaseStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Establecimientos", description = "Establecimientos registrados en la plataforma (negocios, lugares, puntos de interés)")
@RestController
@RequestMapping("/api/establecimientos")
@RequiredArgsConstructor
public class EstablecimientoController {

    private final EstablecimientoService establecimientoService;
    private final EstablecimientoMapper establecimientoMapper;
    private final SupabaseStorageService storageService;

    @Operation(
        summary = "Obtener establecimiento por ID",
        description = "Devuelve el detalle completo de un establecimiento. Accesible para cualquier usuario autenticado."
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EstablecimientoDetailResponse>> getById(@PathVariable Integer id) {
        Establecimiento establecimiento = establecimientoService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(establecimientoMapper.toDetailDto(establecimiento)));
    }

    @Operation(
        summary = "Listar establecimientos",
        description = "Devuelve una página de establecimientos, opcionalmente filtrados por nombre. Accesible para cualquier usuario autenticado."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<Page<EstablecimientoListResponse>>> list(
            @RequestParam(required = false) String nombre,
            Pageable pageable) {
        Page<EstablecimientoListResponse> page = establecimientoService.list(nombre, pageable)
                .map(establecimientoMapper::toListDto);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @Operation(
        summary = "Listar establecimientos por tipo",
        description = "Devuelve establecimientos filtrados por tipo de establecimiento. Accesible para cualquier usuario autenticado."
    )
    @GetMapping("/tipo/{tipoId}")
    public ResponseEntity<ApiResponse<Page<EstablecimientoListResponse>>> listByTipo(
            @PathVariable Integer tipoId,
            @RequestParam(required = false) String nombre,
            Pageable pageable) {
        Page<EstablecimientoListResponse> page = establecimientoService.listByTipoEstablecimiento(tipoId, nombre, pageable)
                .map(establecimientoMapper::toListDto);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @Operation(
        summary = "Filtrar establecimientos por etiquetas",
        description = "Devuelve establecimientos que coincidan con las etiquetas indicadas (búsqueda estricta o parcial). Accesible para cualquier usuario autenticado."
    )
    @PostMapping("/filtro")
    public ResponseEntity<ApiResponse<List<EstablecimientoResponse>>> filtrarPorEtiquetas(
            @Valid @RequestBody FiltroEstablecimientoRequest request) {
        List<Establecimiento> resultados = establecimientoService.findByEtiquetas(
                request.getEtiquetaIds(), request.isBusquedaEstricta());
        List<EstablecimientoResponse> response = resultados.stream()
                .map(establecimientoMapper::toDto)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
        summary = "Obtener establecimientos sugeridos para cliente",
        description = "Devuelve establecimientos sugeridos basados en las etiquetas del cliente. Solo el propio CLIENTE puede consultar sus sugerencias."
    )
    @GetMapping("/sugeridos/{clienteId}")
    @PreAuthorize("hasRole('CLIENTE') and #clienteId == authentication.principal.id")
    public ResponseEntity<ApiResponse<List<EstablecimientoResponse>>> sugeridos(@PathVariable Integer clienteId) {
        List<Establecimiento> resultados = establecimientoService.findSugeridos(clienteId);
        List<EstablecimientoResponse> response = resultados.stream()
                .map(establecimientoMapper::toDto)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
        summary = "Crear establecimiento (multipart/form-data)",
        description = """
            Crea un nuevo establecimiento en la plataforma con soporte para hasta 4 imágenes.
            La petición debe ser de tipo `multipart/form-data` y contener las siguientes partes:
            - **datos**: Un objeto JSON con la información del establecimiento (`EstablecimientoRequest`). Obligatorio.
            - **foto**: Imagen principal del establecimiento. Obligatoria. Debe ser una imagen (image/*).
            - **foto2**: Segunda imagen. Opcional.
            - **foto3**: Tercera imagen. Opcional.
            - **foto4**: Cuarta imagen. Opcional.

            Cada imagen se sube a Supabase Storage y la URL pública se persiste en los campos
            `imgRefs`, `imgRefs2`, `imgRefs3`, `imgRefs4` del establecimiento (REQ-006/007).

            **Permisos:** Requiere rol USUARIO.
            """
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<EstablecimientoDetailResponse>> create(
            @Parameter(description = "Datos del establecimiento en formato JSON")
            @RequestPart("datos") @Valid EstablecimientoRequest request,
            @Parameter(description = "Imagen principal (obligatoria)")
            @RequestPart(value = "foto", required = false) MultipartFile foto,
            @Parameter(description = "Segunda imagen (opcional)")
            @RequestPart(value = "foto2", required = false) MultipartFile foto2,
            @Parameter(description = "Tercera imagen (opcional)")
            @RequestPart(value = "foto3", required = false) MultipartFile foto3,
            @Parameter(description = "Cuarta imagen (opcional)")
            @RequestPart(value = "foto4", required = false) MultipartFile foto4) throws Exception {

        validarImagen(foto, "foto", true);
        validarImagen(foto2, "foto2", false);
        validarImagen(foto3, "foto3", false);
        validarImagen(foto4, "foto4", false);

        String publicUrl = storageService.uploadFile(foto, "establecimientos");
        request.setImgRefs(publicUrl);

        if (foto2 != null && !foto2.isEmpty()) {
            request.setImgRefs2(storageService.uploadFile(foto2, "establecimientos"));
        }
        if (foto3 != null && !foto3.isEmpty()) {
            request.setImgRefs3(storageService.uploadFile(foto3, "establecimientos"));
        }
        if (foto4 != null && !foto4.isEmpty()) {
            request.setImgRefs4(storageService.uploadFile(foto4, "establecimientos"));
        }

        Establecimiento entity = establecimientoMapper.toEntity(request);
        Establecimiento saved = establecimientoService.create(entity);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(establecimientoMapper.toDetailDto(saved)));
    }

    @Operation(
        summary = "Actualizar establecimiento (multipart/form-data)",
        description = """
            Actualiza los datos de un establecimiento existente con soporte para hasta 4 imágenes.
            La petición debe ser de tipo `multipart/form-data` y contener las siguientes partes:
            - **datos**: Un objeto JSON con los datos a actualizar (`EstablecimientoRequest`). Obligatorio.
              Debe incluir los links originales de las imágenes existentes en `imgRefs`, `imgRefs2`, etc.
            - **foto**: Nueva imagen principal. Opcional — si se envía, reemplaza la foto existente.
            - **foto2**: Nueva segunda imagen. Opcional.
            - **foto3**: Nueva tercera imagen. Opcional.
            - **foto4**: Nueva cuarta imagen. Opcional.

            Para cada imagen nueva recibida, el archivo anterior se elimina de Supabase Storage
            antes de subir el nuevo (REQ-006/007).

            **Permisos:** Requiere rol USUARIO.
            """
    )
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<EstablecimientoDetailResponse>> update(
            @PathVariable Integer id,
            @Parameter(description = "Datos actualizados en formato JSON")
            @RequestPart("datos") @Valid EstablecimientoRequest request,
            @Parameter(description = "Nueva imagen principal (opcional — si no se envía, se conserva la actual)")
            @RequestPart(value = "foto", required = false) MultipartFile foto,
            @Parameter(description = "Nueva segunda imagen (opcional)")
            @RequestPart(value = "foto2", required = false) MultipartFile foto2,
            @Parameter(description = "Nueva tercera imagen (opcional)")
            @RequestPart(value = "foto3", required = false) MultipartFile foto3,
            @Parameter(description = "Nueva cuarta imagen (opcional)")
            @RequestPart(value = "foto4", required = false) MultipartFile foto4) throws Exception {

        validarImagen(foto, "foto", false);
        validarImagen(foto2, "foto2", false);
        validarImagen(foto3, "foto3", false);
        validarImagen(foto4, "foto4", false);

        if (foto != null && !foto.isEmpty()) {
            String oldFileName = storageService.extractFileNameFromUrlWithFolder(request.getImgRefs());
            if (oldFileName != null) storageService.deleteFile(oldFileName);
            request.setImgRefs(storageService.uploadFile(foto, "establecimientos"));
        }
        if (foto2 != null && !foto2.isEmpty()) {
            String oldFileName = storageService.extractFileNameFromUrlWithFolder(request.getImgRefs2());
            if (oldFileName != null) storageService.deleteFile(oldFileName);
            request.setImgRefs2(storageService.uploadFile(foto2, "establecimientos"));
        }
        if (foto3 != null && !foto3.isEmpty()) {
            String oldFileName = storageService.extractFileNameFromUrlWithFolder(request.getImgRefs3());
            if (oldFileName != null) storageService.deleteFile(oldFileName);
            request.setImgRefs3(storageService.uploadFile(foto3, "establecimientos"));
        }
        if (foto4 != null && !foto4.isEmpty()) {
            String oldFileName = storageService.extractFileNameFromUrlWithFolder(request.getImgRefs4());
            if (oldFileName != null) storageService.deleteFile(oldFileName);
            request.setImgRefs4(storageService.uploadFile(foto4, "establecimientos"));
        }

        Establecimiento entity = establecimientoMapper.toEntity(request);
        Establecimiento updated = establecimientoService.update(id, entity);
        return ResponseEntity.ok(ApiResponse.success(establecimientoMapper.toDetailDto(updated)));
    }

    @Operation(
        summary = "Eliminar establecimiento",
        description = "Elimina un establecimiento del sistema. Requiere rol USUARIO."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        establecimientoService.delete(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }

    // ------------------------------------------------------------------ //
    //  Private helpers                                                    //
    // ------------------------------------------------------------------ //

    /**
     * Validates a multipart image part.
     *
     * @param file       the uploaded file (may be null)
     * @param partName   the part name used in the error message (e.g. "foto")
     * @param obligatoria whether the file is required
     * @throws IllegalArgumentException if validation fails; handled globally as HTTP 400
     */
    private void validarImagen(MultipartFile file, String partName, boolean obligatoria) {
        if (file == null || file.isEmpty()) {
            if (obligatoria) {
                throw new IllegalArgumentException("El archivo '" + partName + "' es obligatorio");
            }
            return;
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException(
                    "El archivo '" + partName + "' debe ser una imagen (recibido: " + contentType + ")");
        }
    }
}
