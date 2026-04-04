package com.lugares.api.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ComentarioResponse {

    private Integer id;
    private String clienteNombre;
    private String clienteImagenPerfil;
    private LocalDate fechaComentario;
    private String comentario;
}
