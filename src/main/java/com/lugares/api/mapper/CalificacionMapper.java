package com.lugares.api.mapper;

import com.lugares.api.dto.response.CalificacionResponse;
import com.lugares.api.entity.Calificacion;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CalificacionMapper {

    private final ModelMapper modelMapper;

    public CalificacionResponse toDto(Calificacion entity) {
        CalificacionResponse dto = modelMapper.map(entity, CalificacionResponse.class);
        if (entity.getCliente() != null) {
            dto.setIdCliente(entity.getCliente().getId());
        }
        if (entity.getEstablecimiento() != null) {
            dto.setIdEstablecimiento(entity.getEstablecimiento().getId());
        }
        return dto;
    }
}
