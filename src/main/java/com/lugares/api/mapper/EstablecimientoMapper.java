package com.lugares.api.mapper;

import com.lugares.api.dto.request.EstablecimientoRequest;
import com.lugares.api.dto.response.EstablecimientoDetailResponse;
import com.lugares.api.dto.response.EstablecimientoListResponse;
import com.lugares.api.dto.response.EstablecimientoResponse;
import com.lugares.api.entity.Empresa;
import com.lugares.api.entity.Establecimiento;
import com.lugares.api.entity.Suscripcion;
import com.lugares.api.entity.TipoEstablecimiento;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EstablecimientoMapper {

    private final ModelMapper modelMapper;

    public Establecimiento toEntity(EstablecimientoRequest request) {
        Establecimiento entity = modelMapper.map(request, Establecimiento.class);

        if (request.getIdSuscripcion() != null) {
            Suscripcion suscripcion = new Suscripcion();
            suscripcion.setId(request.getIdSuscripcion());
            entity.setSuscripcion(suscripcion);
        }
        if (request.getIdEmpresa() != null) {
            Empresa empresa = new Empresa();
            empresa.setId(request.getIdEmpresa());
            entity.setEmpresa(empresa);
        }
        if (request.getIdTipoEstablecimiento() != null) {
            TipoEstablecimiento tipo = new TipoEstablecimiento();
            tipo.setId(request.getIdTipoEstablecimiento());
            entity.setTipoEstablecimiento(tipo);
        }
        return entity;
    }

    public EstablecimientoDetailResponse toDetailDto(Establecimiento entity) {
        EstablecimientoDetailResponse dto = modelMapper.map(entity, EstablecimientoDetailResponse.class);
        if (entity.getSuscripcion() != null) {
            dto.setSuscripcionNombre(entity.getSuscripcion().getNombre());
        }
        if (entity.getEmpresa() != null) {
            dto.setEmpresaNombre(entity.getEmpresa().getNombre());
        }
        if (entity.getTipoEstablecimiento() != null) {
            dto.setTipoEstablecimientoNombre(entity.getTipoEstablecimiento().getNombre());
        }
        return dto;
    }

    public EstablecimientoListResponse toListDto(Establecimiento entity) {
        return modelMapper.map(entity, EstablecimientoListResponse.class);
    }

    public EstablecimientoResponse toDto(Establecimiento entity) {
        return modelMapper.map(entity, EstablecimientoResponse.class);
    }
}
