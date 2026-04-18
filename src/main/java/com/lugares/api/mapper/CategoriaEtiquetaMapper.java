package com.lugares.api.mapper;

import com.lugares.api.dto.response.CategoriaEtiquetaResponse;
import com.lugares.api.entity.CategoriaEtiqueta;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategoriaEtiquetaMapper {

    private final ModelMapper modelMapper;

    public CategoriaEtiquetaResponse toDto(CategoriaEtiqueta entity) {
        return modelMapper.map(entity, CategoriaEtiquetaResponse.class);
    }
}
