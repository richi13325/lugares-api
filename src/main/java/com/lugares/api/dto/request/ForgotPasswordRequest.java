package com.lugares.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForgotPasswordRequest {

    @NotBlank(message = "El correo electronico es obligatorio")
    @Email(message = "Formato de correo invalido")
    private String correoElectronico;

    @NotBlank(message = "El tipo de usuario es obligatorio")
    private String tipoUsuario;
}
