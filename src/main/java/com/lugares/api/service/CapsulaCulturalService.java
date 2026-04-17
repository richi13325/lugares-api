package com.lugares.api.service;

import com.lugares.api.entity.CapsulaCultural;
import com.lugares.api.exception.ResourceNotFoundException;
import com.lugares.api.repository.CapsulaCulturalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CapsulaCulturalService {

    private final CapsulaCulturalRepository capsulaCulturalRepository;

    public List<CapsulaCultural> listAll() {
        return capsulaCulturalRepository.findAll();
    }

    public CapsulaCultural getById(Integer id) {
        return capsulaCulturalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CapsulaCultural", "id", id));
    }

    @Transactional
    public CapsulaCultural create(CapsulaCultural capsula) {
        return capsulaCulturalRepository.save(capsula);
    }

    @Transactional
    public CapsulaCultural update(Integer id, CapsulaCultural datosActualizados) {
        CapsulaCultural existing = capsulaCulturalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CapsulaCultural", "id", id));
        existing.setTitulo(datosActualizados.getTitulo());
        existing.setDescripcion(datosActualizados.getDescripcion());
        existing.setImagen(datosActualizados.getImagen());
        existing.setFechaPublicacion(datosActualizados.getFechaPublicacion());
        existing.setEsVisible(datosActualizados.getEsVisible());
        return capsulaCulturalRepository.save(existing);
    }

    @Transactional
    public void delete(Integer id) {
        if (!capsulaCulturalRepository.existsById(id)) {
            throw new ResourceNotFoundException("CapsulaCultural", "id", id);
        }
        capsulaCulturalRepository.deleteById(id);
    }
}
