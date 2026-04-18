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
        return modelMapper.map(entity, CalificacionResponse.class);
    }
}
