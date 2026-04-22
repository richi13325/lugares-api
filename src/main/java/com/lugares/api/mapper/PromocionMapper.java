package com.lugares.api.mapper;

import com.lugares.api.dto.request.PromocionRequest;
import com.lugares.api.dto.response.PromocionListResponse;
import com.lugares.api.dto.response.PromocionResponse;
import com.lugares.api.entity.Establecimiento;
import com.lugares.api.entity.Promocion;
import com.lugares.api.entity.Suscripcion;
import com.lugares.api.entity.enums.TipoPromocion;
import com.lugares.api.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PromocionMapper {

    private final ModelMapper modelMapper;
    private final StorageService storageService;

    public Promocion toEntity(PromocionRequest request) {
        Promocion entity = modelMapper.map(request, Promocion.class);

        if (request.getIdSuscripcion() != null) {
            Suscripcion suscripcion = new Suscripcion();
            suscripcion.setId(request.getIdSuscripcion());
            entity.setSuscripcion(suscripcion);
        }
        if (request.getIdEstablecimiento() != null) {
            Establecimiento establecimiento = new Establecimiento();
            establecimiento.setId(request.getIdEstablecimiento());
            entity.setEstablecimiento(establecimiento);
        }
        if (request.getTipoPromocion() != null) {
            entity.setTipoPromocion(TipoPromocion.valueOf(request.getTipoPromocion()));
        }
        return entity;
    }

    public PromocionResponse toDto(Promocion entity) {
        PromocionResponse dto = modelMapper.map(entity, PromocionResponse.class);
        if (entity.getSuscripcion() != null) {
            dto.setSuscripcionNombre(entity.getSuscripcion().getNombre());
        }
        if (entity.getEstablecimiento() != null) {
            dto.setEstablecimientoNombre(entity.getEstablecimiento().getNombre());
        }
        dto.setImagen(storageService.getPublicUrl(entity.getImagen()));
        return dto;
    }

    public PromocionListResponse toListDto(Promocion entity) {
        PromocionListResponse dto = modelMapper.map(entity, PromocionListResponse.class);
        if (entity.getSuscripcion() != null) {
            dto.setSuscripcionNombre(entity.getSuscripcion().getNombre());
        }
        if (entity.getEstablecimiento() != null) {
            dto.setEstablecimientoNombre(entity.getEstablecimiento().getNombre());
        }
        dto.setImagen(storageService.getPublicUrl(entity.getImagen()));
        return dto;
    }
}
