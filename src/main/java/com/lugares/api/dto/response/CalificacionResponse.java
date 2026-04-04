package com.lugares.api.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CalificacionResponse {

    private Integer id;
    private Integer idCliente;
    private Integer idEstablecimiento;
    private Byte calificacion;
}
