package com.lugares.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
public class PromocionRequest {

    @NotNull(message = "La suscripcion es obligatoria")
    private Integer idSuscripcion;

    @NotNull(message = "El establecimiento es obligatorio")
    private Integer idEstablecimiento;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String descripcion;

    private LocalDate fechaInicio;

    private LocalDate fechaFin;

    private String tipoPromocion;

    private Set<DayOfWeek> diasDisponibles;

    @NotBlank(message = "El codigo de validacion es obligatorio")
    @Size(min = 8, max = 8, message = "El codigo de validacion debe tener exactamente 8 caracteres")
    private String codigoValidacion;
}
