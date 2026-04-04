package com.lugares.api.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EtiquetaResponse {

    private Integer id;
    private String nombre;
    private String descripcion;
    private Boolean esVisible;
}
