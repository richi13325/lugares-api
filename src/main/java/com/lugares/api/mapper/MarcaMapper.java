package com.lugares.api.mapper;

import com.lugares.api.dto.response.MarcaResponse;
import com.lugares.api.entity.Marca;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MarcaMapper {

    private final ModelMapper modelMapper;

    public MarcaResponse toDto(Marca entity) {
        return modelMapper.map(entity, MarcaResponse.class);
    }
}
