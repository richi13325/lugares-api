package com.lugares.api.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.sql.Time;

@Getter
@Setter
public class EstablecimientoResponse {

    private Integer id;
    private String nombre;
    private String descripcion;
    private String estado;
    private String ciudad;
    private String zona;
    private String direccion;
    private String referenciaGeografica;
    private String coordLatitud;
    private String coordLongitud;
    private String imgRefs;
    private String imgRefs2;
    private String imgRefs3;
    private String imgRefs4;
    private Time horarioApertura;
    private Time horarioCierre;
}
