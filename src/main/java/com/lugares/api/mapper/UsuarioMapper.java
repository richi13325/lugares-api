package com.lugares.api.mapper;

import com.lugares.api.dto.request.UsuarioRequest;
import com.lugares.api.dto.response.UsuarioListResponse;
import com.lugares.api.dto.response.UsuarioResponse;
import com.lugares.api.entity.Usuario;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UsuarioMapper {

    private final ModelMapper modelMapper;

    public Usuario toEntity(UsuarioRequest request) {
        return modelMapper.map(request, Usuario.class);
    }

    public UsuarioResponse toDto(Usuario entity) {
        return modelMapper.map(entity, UsuarioResponse.class);
    }

    public UsuarioListResponse toListDto(Usuario entity) {
        return modelMapper.map(entity, UsuarioListResponse.class);
    }
}
