package com.lugares.api.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CapsulaCulturalResponse {

    private Integer id;
    private String titulo;
    private String descripcion;
    private String imagen;
    private LocalDateTime fechaPublicacion;
    private Boolean esVisible;
}
