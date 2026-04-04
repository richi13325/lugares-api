package com.lugares.api.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EstablecimientoListResponse {

    private Integer id;
    private String nombre;
    private String direccion;
    private String referenciaGeografica;
    private String coordLatitud;
    private String coordLongitud;
    private String imgRefs;
    private String horarioApertura;
    private String horarioCierre;
}
