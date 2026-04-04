package com.lugares.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FcmTokenRequest {

    @NotBlank(message = "El token es obligatorio")
    private String token;

    @NotNull(message = "El id del cliente es obligatorio")
    private Long idCliente;

    @NotBlank(message = "La plataforma es obligatoria")
    private String plataforma;
}
