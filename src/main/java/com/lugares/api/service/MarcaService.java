package com.lugares.api.service;

import com.lugares.api.entity.Marca;
import com.lugares.api.exception.ResourceNotFoundException;
import com.lugares.api.repository.MarcaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MarcaService {

    private final MarcaRepository marcaRepository;

    public List<Marca> listAll() {
        return marcaRepository.findAll();
    }

    public Marca getById(Integer id) {
        return marcaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Marca", "id", id));
    }

    @Transactional
    public Marca create(Marca marca) {
        return marcaRepository.save(marca);
    }

    @Transactional
    public Marca update(Integer id, Marca datosActualizados) {
        if (!marcaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Marca", "id", id);
        }
        datosActualizados.setId(id);
        return marcaRepository.save(datosActualizados);
    }

    @Transactional
    public void delete(Integer id) {
        if (!marcaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Marca", "id", id);
        }
        marcaRepository.deleteById(id);
    }
}
