package com.lugares.api.mapper;

import com.lugares.api.dto.request.ClienteRequest;
import com.lugares.api.dto.request.ClienteUpdateRequest;
import com.lugares.api.dto.response.ClienteListResponse;
import com.lugares.api.dto.response.ClienteResponse;
import com.lugares.api.entity.Cliente;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClienteMapper {

    private final ModelMapper modelMapper;

    public Cliente toEntity(ClienteRequest request) {
        return modelMapper.map(request, Cliente.class);
    }

    public Cliente toEntity(ClienteUpdateRequest request) {
        return modelMapper.map(request, Cliente.class);
    }

    public ClienteResponse toDto(Cliente entity) {
        ClienteResponse dto = modelMapper.map(entity, ClienteResponse.class);
        if (entity.getSuscripcion() != null) {
            dto.setSuscripcionNombre(entity.getSuscripcion().getNombre());
        }
        return dto;
    }

    public ClienteListResponse toListDto(Cliente entity) {
        return modelMapper.map(entity, ClienteListResponse.class);
    }
}
