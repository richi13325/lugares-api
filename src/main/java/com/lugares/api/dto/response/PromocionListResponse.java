package com.lugares.api.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PromocionListResponse {

    private Integer id;
    private String suscripcionNombre;
    private String establecimientoNombre;
    private String nombre;
    private String descripcion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    private String imagen;
}
