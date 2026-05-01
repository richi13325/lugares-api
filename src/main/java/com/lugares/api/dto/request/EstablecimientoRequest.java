package com.lugares.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.sql.Time;

@Getter
@Setter
public class EstablecimientoRequest {

    @NotNull(message = "La suscripcion es obligatoria")
    private Integer idSuscripcion;

    private Integer idEmpresa;

    @NotNull(message = "El tipo de establecimiento es obligatorio")
    private Integer idTipoEstablecimiento;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String descripcion;
    private String estado;
    private String ciudad;
    private String zona;
    private String direccion;
    private String referenciaGeografica;
    private String coordLatitud;
    private String coordLongitud;
    private String correoElectronico;

    @Size(max = 140, message = "La sugerencia no puede exceder 140 caracteres")
    private String sugerenciaDeLaCasa;

    private String imgRefs;
    private String imgRefs2;
    private String imgRefs3;
    private String imgRefs4;
    private Time horarioApertura;
    private Time horarioCierre;
    private Boolean lunes;
    private Boolean martes;
    private Boolean miercoles;
    private Boolean jueves;
    private Boolean viernes;
    private Boolean sabado;
    private Boolean domingo;
    private String menu;

    @Size(max = 20, message = "El celular no puede exceder 20 caracteres")
    private String celular1;

    @Size(max = 20, message = "El celular no puede exceder 20 caracteres")
    private String celular2;

    @Size(max = 20, message = "El celular no puede exceder 20 caracteres")
    private String celularComentarios;

    private String alimentosBebidas;

    @Size(max = 50, message = "El ticket promedio no puede exceder 50 caracteres")
    private String ticketPromedio;

    private Integer antiguedadAnios;
    private String promoLunes;
    private String promoMartes;
    private String promoMiercoles;
    private String promoJueves;
    private String promoViernes;
    private String promoSabado;
    private String promoDomingo;
    private String promo300Lugares;
}
