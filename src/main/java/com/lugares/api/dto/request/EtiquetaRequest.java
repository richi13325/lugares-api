package com.lugares.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EtiquetaRequest {

    @NotNull(message = "La categoria es obligatoria")
    private Integer idCategoria;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 65, message = "El nombre no puede exceder 65 caracteres")
    private String nombre;

    @Size(max = 255, message = "La descripcion no puede exceder 255 caracteres")
    private String descripcion;

    private Boolean esVisible;
}
