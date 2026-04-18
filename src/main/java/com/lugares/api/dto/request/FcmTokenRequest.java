package com.lugares.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FcmTokenRequest {

    @NotBlank(message = "El token es obligatorio")
    private String token;

    @NotBlank(message = "La plataforma es obligatoria")
    private String plataforma;
}
