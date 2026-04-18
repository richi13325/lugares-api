package com.lugares.api.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class HistorialCanjeResponse {

    private Integer id;
    private String promocionNombre;
    private String clienteNombre;
    private LocalDateTime fechaHora;
    private String codigoValidacion;
}
