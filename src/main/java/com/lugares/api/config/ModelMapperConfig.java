package com.lugares.api.config;

import com.lugares.api.dto.response.CapsulaCulturalResponse;
import com.lugares.api.entity.CapsulaCultural;
import com.lugares.api.service.StorageService;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class ModelMapperConfig {

    private final StorageService storageService;

    public ModelMapperConfig(@Lazy StorageService storageService) {
        this.storageService = storageService;
    }

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();

        mapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE)
                .setSkipNullEnabled(true);

        mapper.createTypeMap(CapsulaCultural.class, CapsulaCulturalResponse.class)
                .addMappings(m -> m.using(ctx -> storageService.getPublicUrl((String) ctx.getSource()))
                        .map(CapsulaCultural::getImagen, CapsulaCulturalResponse::setImagen));

        return mapper;
    }
}
