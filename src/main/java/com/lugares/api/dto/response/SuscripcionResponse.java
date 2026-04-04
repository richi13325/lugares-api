package com.lugares.api.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SuscripcionResponse {

    private Integer id;
    private String nombre;
    private String descripcion;
    private Double precio;
    private Boolean esSuscripcionDeCliente;
}
