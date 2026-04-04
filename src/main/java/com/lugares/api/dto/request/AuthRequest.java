package com.lugares.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthRequest {

    @NotBlank(message = "El correo electronico es obligatorio")
    @Email(message = "Formato de correo invalido")
    private String correoElectronico;

    @NotBlank(message = "La contrasenia es obligatoria")
    private String contrasenia;
}
