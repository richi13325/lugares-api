package com.lugares.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @Size(max = 10, message = "El telefono no puede exceder 10 caracteres")
    private String telefono;

    @NotBlank(message = "El correo electronico es obligatorio")
    @Email(message = "Formato de correo invalido")
    private String correoElectronico;

    @NotBlank(message = "La contrasenia es obligatoria")
    @Size(min = 6, message = "La contrasenia debe tener al menos 6 caracteres")
    private String contrasenia;
}
