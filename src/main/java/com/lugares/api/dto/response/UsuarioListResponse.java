package com.lugares.api.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioListResponse {

    private Integer id;
    private String nombre;
    private String telefono;
    private String correoElectronico;
}
