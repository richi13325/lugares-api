package com.lugares.api.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class EmpresaResponse {

    private Integer id;
    private String nombre;
    private String descripcion;
    private String telefono;
    private String correoElectronico;
    private String estado;
    private String ciudad;
    private String direccion;
    private LocalDate fechaCreacion;
}
