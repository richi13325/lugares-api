package com.lugares.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CapsulaCulturalRequest {

    @NotBlank(message = "El titulo es obligatorio")
    private String titulo;

    private String descripcion;

    private LocalDateTime fechaPublicacion;

    private Boolean esVisible;
}
