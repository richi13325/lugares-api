package com.lugares.api.service;

import com.lugares.api.dto.request.EmpresaRequest;
import com.lugares.api.entity.Empresa;
import com.lugares.api.exception.ResourceNotFoundException;
import com.lugares.api.mapper.EmpresaMapper;
import com.lugares.api.repository.EmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final EmpresaMapper empresaMapper;

    public List<Empresa> listAll() {
        return empresaRepository.findAll();
    }

    public Empresa getById(Integer id) {
        return empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", "id", id));
    }

    @Transactional
    public Empresa create(EmpresaRequest request) {
        Empresa entity = empresaMapper.toEntity(request);
        entity.setFechaCreacion(LocalDate.now());
        entity.setFechaUltimaModificacion(LocalDate.now());
        return empresaRepository.save(entity);
    }

    @Transactional
    public Empresa update(Integer id, EmpresaRequest request) {
        Empresa existente = empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", "id", id));
        existente.setFechaUltimaModificacion(LocalDate.now());
        empresaMapper.update(request, existente);
        return empresaRepository.save(existente);
    }

    @Transactional
    public void delete(Integer id) {
        if (!empresaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Empresa", "id", id);
        }
        empresaRepository.deleteById(id);
    }
}