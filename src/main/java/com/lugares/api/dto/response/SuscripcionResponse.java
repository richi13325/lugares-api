package com.lugares.api.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class SuscripcionResponse {

    private Integer id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private Boolean esSuscripcionDeCliente;
}
