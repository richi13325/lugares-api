package com.lugares.api.mapper;

import com.lugares.api.dto.response.TipoEstablecimientoResponse;
import com.lugares.api.entity.TipoEstablecimiento;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TipoEstablecimientoMapper {

    private final ModelMapper modelMapper;

    public TipoEstablecimientoResponse toDto(TipoEstablecimiento entity) {
        return modelMapper.map(entity, TipoEstablecimientoResponse.class);
    }
}
