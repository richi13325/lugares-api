package com.lugares.api.mapper;

import com.lugares.api.dto.response.SuscripcionResponse;
import com.lugares.api.entity.Suscripcion;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SuscripcionMapper {

    private final ModelMapper modelMapper;

    public SuscripcionResponse toDto(Suscripcion entity) {
        return modelMapper.map(entity, SuscripcionResponse.class);
    }
}
