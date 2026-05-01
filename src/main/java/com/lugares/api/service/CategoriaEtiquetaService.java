package com.lugares.api.service;

import com.lugares.api.dto.request.CategoriaEtiquetaRequest;
import com.lugares.api.entity.CategoriaEtiqueta;
import com.lugares.api.exception.BusinessRuleException;
import com.lugares.api.exception.ResourceNotFoundException;
import com.lugares.api.mapper.CategoriaEtiquetaMapper;
import com.lugares.api.repository.CategoriaEtiquetaRepository;
import com.lugares.api.repository.EtiquetaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaEtiquetaService {

    private final CategoriaEtiquetaRepository categoriaEtiquetaRepository;
    private final EtiquetaRepository etiquetaRepository;
    private final CategoriaEtiquetaMapper categoriaEtiquetaMapper;

    public List<CategoriaEtiqueta> listAll() {
        return categoriaEtiquetaRepository.findAll();
    }

    public CategoriaEtiqueta getById(Integer id) {
        return categoriaEtiquetaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CategoriaEtiqueta", "id", id));
    }

    @Transactional
    public CategoriaEtiqueta create(CategoriaEtiquetaRequest request) {
        CategoriaEtiqueta entity = categoriaEtiquetaMapper.toEntity(request);
        entity.setFechaCreacion(LocalDate.now());
        entity.setFechaUltimaModificacion(LocalDate.now());
        return categoriaEtiquetaRepository.save(entity);
    }

    @Transactional
    public CategoriaEtiqueta update(Integer id, CategoriaEtiquetaRequest request) {
        CategoriaEtiqueta existente = categoriaEtiquetaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CategoriaEtiqueta", "id", id));
        existente.setFechaUltimaModificacion(LocalDate.now());
        categoriaEtiquetaMapper.update(request, existente);
        return categoriaEtiquetaRepository.save(existente);
    }

    @Transactional
    public void delete(Integer id) {
        CategoriaEtiqueta existente = categoriaEtiquetaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CategoriaEtiqueta", "id", id));

        long count = etiquetaRepository.countByCategoriaId(id);
        if (count > 0) {
            throw new BusinessRuleException(
                    "No se puede eliminar la categoría '" + existente.getNombre() +
                    "' porque tiene " + count + " etiqueta(s) asociada(s). Elimine o reasigne las etiquetas primero.");
        }

        categoriaEtiquetaRepository.deleteById(id);
    }
}