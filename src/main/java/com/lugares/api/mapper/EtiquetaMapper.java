package com.lugares.api.mapper;

import com.lugares.api.dto.request.EtiquetaRequest;
import com.lugares.api.dto.response.EtiquetaAdminResponse;
import com.lugares.api.dto.response.EtiquetaResponse;
import com.lugares.api.entity.CategoriaEtiqueta;
import com.lugares.api.entity.Etiqueta;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EtiquetaMapper {

    private final ModelMapper modelMapper;

    public Etiqueta toEntity(EtiquetaRequest request) {
        Etiqueta entity = modelMapper.map(request, Etiqueta.class);
        if (request.getIdCategoria() != null) {
            CategoriaEtiqueta categoria = new CategoriaEtiqueta();
            categoria.setId(request.getIdCategoria());
            entity.setCategoria(categoria);
        }
        return entity;
    }

    public EtiquetaResponse toDto(Etiqueta entity) {
        return modelMapper.map(entity, EtiquetaResponse.class);
    }

    public EtiquetaAdminResponse toAdminDto(Etiqueta entity) {
        EtiquetaAdminResponse dto = modelMapper.map(entity, EtiquetaAdminResponse.class);
        if (entity.getCategoria() != null) {
            dto.setCategoriaNombre(entity.getCategoria().getNombre());
        }
        return dto;
    }
}
