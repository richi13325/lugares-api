package com.lugares.api.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.sql.Time;

@Getter
@Setter
public class EstablecimientoDetailResponse {

    private Integer id;
    private String suscripcionNombre;
    private String empresaNombre;
    private String tipoEstablecimientoNombre;
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
    private String celular1;
    private String celular2;
    private String celularComentarios;
    private String alimentosBebidas;
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
