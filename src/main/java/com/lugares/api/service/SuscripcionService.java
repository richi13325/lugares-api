package com.lugares.api.service;

import com.lugares.api.entity.Suscripcion;
import com.lugares.api.exception.ResourceNotFoundException;
import com.lugares.api.repository.SuscripcionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SuscripcionService {

    private final SuscripcionRepository suscripcionRepository;

    public List<Suscripcion> listAll() {
        return suscripcionRepository.findAll();
    }

    public Suscripcion getById(Integer id) {
        return suscripcionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Suscripcion", "id", id));
    }
}
