package com.lugares.api.controller;

import com.lugares.api.common.ApiResponse;
import com.lugares.api.dto.request.CapsulaCulturalRequest;
import com.lugares.api.dto.response.CapsulaCulturalResponse;
import com.lugares.api.entity.CapsulaCultural;
import com.lugares.api.service.CapsulaCulturalService;
import com.lugares.api.service.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/capsulas-culturales")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_USUARIO')")
@Tag(name = "Cápsulas Culturales", description = "Gestión de cápsulas culturales. Lectura: ROLE_CLIENTE o ROLE_USUARIO. Escritura: solo ROLE_USUARIO.")
public class CapsulaCulturalController {

    private final CapsulaCulturalService capsulaCulturalService;
    private final StorageService storageService;
    private final ModelMapper modelMapper;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_CLIENTE', 'ROLE_USUARIO')")
    @Operation(summary = "Listar todas las cápsulas culturales")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode ="200", description = "Listado obtenido",
                    content = @Content(schema = @Schema(implementation = CapsulaCulturalResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode ="403", description = "Sin autorización")
    })
    public ResponseEntity<ApiResponse<List<CapsulaCulturalResponse>>> listAll() {
        List<CapsulaCulturalResponse> response = capsulaCulturalService.listAll().stream()
                .map(c -> modelMapper.map(c, CapsulaCulturalResponse.class))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_CLIENTE', 'ROLE_USUARIO')")
    @Operation(summary = "Obtener cápsula por ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode ="200", description = "Cápsula encontrada",
                    content = @Content(schema = @Schema(implementation = CapsulaCulturalResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode ="404", description = "No existe la cápsula con ese ID"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode ="403", description = "Sin autorización")
    })
    public ResponseEntity<ApiResponse<CapsulaCulturalResponse>> getById(
            @Parameter(description = "ID de la cápsula", required = true, example = "1")
            @PathVariable Integer id) {
        CapsulaCulturalResponse response = modelMapper.map(capsulaCulturalService.getById(id), CapsulaCulturalResponse.class);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Crear cápsula cultural",
            description = "Enviar como `multipart/form-data`. La parte `datos` es un JSON con los campos del request. La parte `foto` es la imagen (opcional, formatos: JPG, PNG, WEBP)."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode ="201", description = "Cápsula creada",
                    content = @Content(schema = @Schema(implementation = CapsulaCulturalResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode ="400", description = "Datos inválidos — ver detalle en el campo `fieldErrors`"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode ="403", description = "Sin autorización — requiere ROLE_USUARIO")
    })
    public ResponseEntity<ApiResponse<CapsulaCulturalResponse>> create(
            @Parameter(description = "Datos de la cápsula", schema = @Schema(implementation = CapsulaCulturalRequest.class))
            @Valid @RequestPart("datos") CapsulaCulturalRequest request,
            @Parameter(description = "Imagen opcional. Formatos: JPG, PNG, WEBP.")
            @RequestPart(value = "foto", required = false) MultipartFile foto) {
        CapsulaCultural entity = modelMapper.map(request, CapsulaCultural.class);
        if (foto != null && !foto.isEmpty()) {
            entity.setImagen(storageService.uploadFile(foto, "capsulas"));
        }
        CapsulaCulturalResponse response = modelMapper.map(capsulaCulturalService.create(entity), CapsulaCulturalResponse.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Actualizar cápsula cultural",
            description = "Enviar como `multipart/form-data`. La parte `datos` es un JSON con los campos a actualizar. Si se envía `foto`, reemplaza la imagen actual y elimina la anterior del bucket. Si no se envía `foto`, la imagen existente se conserva."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode ="200", description = "Cápsula actualizada",
                    content = @Content(schema = @Schema(implementation = CapsulaCulturalResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode ="400", description = "Datos inválidos — ver detalle en el campo `fieldErrors`"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode ="404", description = "No existe la cápsula con ese ID"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode ="403", description = "Sin autorización — requiere ROLE_USUARIO")
    })
    public ResponseEntity<ApiResponse<CapsulaCulturalResponse>> update(
            @Parameter(description = "ID de la cápsula a actualizar", required = true, example = "1")
            @PathVariable Integer id,
            @Parameter(description = "Datos a actualizar", schema = @Schema(implementation = CapsulaCulturalRequest.class))
            @Valid @RequestPart("datos") CapsulaCulturalRequest request,
            @Parameter(description = "Nueva imagen opcional. Formatos: JPG, PNG, WEBP.")
            @RequestPart(value = "foto", required = false) MultipartFile foto) {
        CapsulaCultural entity = modelMapper.map(request, CapsulaCultural.class);
        if (foto != null && !foto.isEmpty()) {
            CapsulaCultural existing = capsulaCulturalService.getById(id);
            if (existing.getImagen() != null) storageService.deleteFile(existing.getImagen());
            entity.setImagen(storageService.uploadFile(foto, "capsulas"));
        }
        CapsulaCulturalResponse response = modelMapper.map(capsulaCulturalService.update(id, entity), CapsulaCulturalResponse.class);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar cápsula cultural",
            description = "Elimina la cápsula y su imagen del bucket si existe."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode ="200", description = "Cápsula eliminada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode ="404", description = "No existe la cápsula con ese ID"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode ="403", description = "Sin autorización — requiere ROLE_USUARIO")
    })
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "ID de la cápsula a eliminar", required = true, example = "1")
            @PathVariable Integer id) {
        CapsulaCultural existing = capsulaCulturalService.getById(id);
        if (existing.getImagen() != null) storageService.deleteFile(existing.getImagen());
        capsulaCulturalService.delete(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}
