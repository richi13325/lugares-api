package com.lugares.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ComentarioRequest {

    @NotNull(message = "El establecimiento es obligatorio")
    private Integer idEstablecimiento;

    @NotBlank(message = "El comentario es obligatorio")
    @Size(max = 555, message = "El comentario no puede exceder 555 caracteres")
    private String comentario;
}
