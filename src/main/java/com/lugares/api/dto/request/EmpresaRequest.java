package com.lugares.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmpresaRequest {

    private Integer id;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String descripcion;

    @Size(max = 10, message = "El telefono no puede exceder 10 caracteres")
    private String telefono;

    private String correoElectronico;

    private String estado;

    private String ciudad;

    private String direccion;
}