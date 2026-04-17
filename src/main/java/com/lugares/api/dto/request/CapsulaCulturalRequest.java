package com.lugares.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CapsulaCulturalRequest {

    @NotBlank
    @Size(max = 150)
    private String titulo;

    private String descripcion;

    private LocalDateTime fechaPublicacion;

    private Boolean esVisible;
}
