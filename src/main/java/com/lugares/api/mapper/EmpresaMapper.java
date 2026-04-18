package com.lugares.api.mapper;

import com.lugares.api.dto.response.EmpresaResponse;
import com.lugares.api.entity.Empresa;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmpresaMapper {

    private final ModelMapper modelMapper;

    public EmpresaResponse toDto(Empresa entity) {
        return modelMapper.map(entity, EmpresaResponse.class);
    }
}
