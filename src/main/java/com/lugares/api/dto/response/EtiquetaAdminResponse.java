package com.lugares.api.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EtiquetaAdminResponse {

    private Integer id;
    private String categoriaNombre;
    private String nombre;
    private String descripcion;
    private Boolean esVisible;
}
