package com.lugares.api.mapper;

import com.lugares.api.dto.response.ComentarioResponse;
import com.lugares.api.entity.Comentario;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ComentarioMapper {

    private final ModelMapper modelMapper;

    public ComentarioResponse toDto(Comentario entity) {
        ComentarioResponse dto = modelMapper.map(entity, ComentarioResponse.class);
        if (entity.getCliente() != null) {
            dto.setClienteNombre(entity.getCliente().getNombre());
            dto.setClienteImagenPerfil(entity.getCliente().getImagenPerfil());
        }
        return dto;
    }
}
