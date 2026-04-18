package com.lugares.api.mapper;

import com.lugares.api.dto.response.CapsulaCulturalResponse;
import com.lugares.api.entity.CapsulaCultural;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CapsulaCulturalMapper {

    private final ModelMapper modelMapper;

    public CapsulaCulturalResponse toDto(CapsulaCultural entity) {
        return modelMapper.map(entity, CapsulaCulturalResponse.class);
    }
}
