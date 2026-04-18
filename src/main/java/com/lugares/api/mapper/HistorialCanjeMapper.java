package com.lugares.api.mapper;

import com.lugares.api.dto.response.HistorialCanjeResponse;
import com.lugares.api.entity.HistorialCanje;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HistorialCanjeMapper {

    private final ModelMapper modelMapper;

    public HistorialCanjeResponse toDto(HistorialCanje entity) {
        HistorialCanjeResponse dto = modelMapper.map(entity, HistorialCanjeResponse.class);
        if (entity.getPromocion() != null) {
            dto.setPromocionNombre(entity.getPromocion().getNombre());
        }
        if (entity.getCliente() != null) {
            dto.setClienteNombre(entity.getCliente().getNombre());
        }
        return dto;
    }
}
