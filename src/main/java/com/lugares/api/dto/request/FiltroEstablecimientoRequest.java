package com.lugares.api.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FiltroEstablecimientoRequest {

    @NotEmpty(message = "Debe indicar al menos una etiqueta")
    private List<Integer> etiquetaIds;

    private boolean busquedaEstricta;
}
