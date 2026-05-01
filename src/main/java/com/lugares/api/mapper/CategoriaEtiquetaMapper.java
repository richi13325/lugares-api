package com.lugares.api.mapper;

import com.lugares.api.dto.request.CategoriaEtiquetaRequest;
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

    public CategoriaEtiqueta toEntity(CategoriaEtiquetaRequest request) {
        return modelMapper.map(request, CategoriaEtiqueta.class);
    }

    public void update(CategoriaEtiquetaRequest source, CategoriaEtiqueta target) {
        modelMapper.map(source, target);
    }
}
