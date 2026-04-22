package com.lugares.api.mapper;

import com.lugares.api.dto.request.CapsulaCulturalRequest;
import com.lugares.api.dto.response.CapsulaCulturalResponse;
import com.lugares.api.entity.CapsulaCultural;
import com.lugares.api.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CapsulaCulturalMapper {

    private final ModelMapper modelMapper;
    private final StorageService storageService;

    public CapsulaCultural toEntity(CapsulaCulturalRequest request) {
        return modelMapper.map(request, CapsulaCultural.class);
    }

    public CapsulaCulturalResponse toDto(CapsulaCultural entity) {
        CapsulaCulturalResponse dto = modelMapper.map(entity, CapsulaCulturalResponse.class);
        dto.setImagen(storageService.getPublicUrl(entity.getImagen()));
        return dto;
    }
}
