package com.lugares.api.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ClienteResponse {

    private Integer id;
    private String suscripcionNombre;
    private String nombre;
    private String nombreCorto;
    private String telefono;
    private String correoElectronico;
    private LocalDate fechaNacimiento;
    private String imagenPerfil;
    // SIN campo contrasenia
}
