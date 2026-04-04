package com.lugares.api.service;

import com.lugares.api.entity.CategoriaEtiqueta;
import com.lugares.api.exception.ResourceNotFoundException;
import com.lugares.api.repository.CategoriaEtiquetaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaEtiquetaService {

    private final CategoriaEtiquetaRepository categoriaEtiquetaRepository;

    public List<CategoriaEtiqueta> listAll() {
        return categoriaEtiquetaRepository.findAll();
    }

    public CategoriaEtiqueta getById(Integer id) {
        return categoriaEtiquetaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CategoriaEtiqueta", "id", id));
    }

    @Transactional
    public CategoriaEtiqueta create(CategoriaEtiqueta categoria) {
        return categoriaEtiquetaRepository.save(categoria);
    }

    @Transactional
    public CategoriaEtiqueta update(Integer id, CategoriaEtiqueta datosActualizados) {
        if (!categoriaEtiquetaRepository.existsById(id)) {
            throw new ResourceNotFoundException("CategoriaEtiqueta", "id", id);
        }
        datosActualizados.setId(id);
        return categoriaEtiquetaRepository.save(datosActualizados);
    }

    @Transactional
    public void delete(Integer id) {
        if (!categoriaEtiquetaRepository.existsById(id)) {
            throw new ResourceNotFoundException("CategoriaEtiqueta", "id", id);
        }
        categoriaEtiquetaRepository.deleteById(id);
    }
}
