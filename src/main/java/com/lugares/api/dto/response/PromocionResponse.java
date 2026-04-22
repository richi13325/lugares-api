package com.lugares.api.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
public class PromocionResponse {

    private Integer id;
    private String suscripcionNombre;
    private String establecimientoNombre;
    private String nombre;
    private String descripcion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String tipoPromocion;
    private Set<DayOfWeek> diasDisponibles;
    private String codigoValidacion;

    private String imagen;
}
