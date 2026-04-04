package com.lugares.api.service;

import com.lugares.api.entity.TipoEstablecimiento;
import com.lugares.api.exception.ResourceNotFoundException;
import com.lugares.api.repository.TipoEstablecimientoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TipoEstablecimientoService {

    private final TipoEstablecimientoRepository tipoEstablecimientoRepository;

    public List<TipoEstablecimiento> listAll() {
        return tipoEstablecimientoRepository.findAll();
    }

    public TipoEstablecimiento getById(Integer id) {
        return tipoEstablecimientoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TipoEstablecimiento", "id", id));
    }

    @Transactional
    public TipoEstablecimiento create(TipoEstablecimiento tipo) {
        return tipoEstablecimientoRepository.save(tipo);
    }

    @Transactional
    public TipoEstablecimiento update(Integer id, TipoEstablecimiento datosActualizados) {
        if (!tipoEstablecimientoRepository.existsById(id)) {
            throw new ResourceNotFoundException("TipoEstablecimiento", "id", id);
        }
        datosActualizados.setId(id);
        return tipoEstablecimientoRepository.save(datosActualizados);
    }

    @Transactional
    public void delete(Integer id) {
        if (!tipoEstablecimientoRepository.existsById(id)) {
            throw new ResourceNotFoundException("TipoEstablecimiento", "id", id);
        }
        tipoEstablecimientoRepository.deleteById(id);
    }
}
