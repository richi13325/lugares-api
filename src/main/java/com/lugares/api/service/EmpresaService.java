package com.lugares.api.service;

import com.lugares.api.entity.Empresa;
import com.lugares.api.exception.ResourceNotFoundException;
import com.lugares.api.repository.EmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmpresaService {

    private final EmpresaRepository empresaRepository;

    public List<Empresa> listAll() {
        return empresaRepository.findAll();
    }

    public Empresa getById(Integer id) {
        return empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", "id", id));
    }

    @Transactional
    public Empresa create(Empresa empresa) {
        return empresaRepository.save(empresa);
    }

    @Transactional
    public Empresa update(Integer id, Empresa datosActualizados) {
        if (!empresaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Empresa", "id", id);
        }
        datosActualizados.setId(id);
        return empresaRepository.save(datosActualizados);
    }

    @Transactional
    public void delete(Integer id) {
        if (!empresaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Empresa", "id", id);
        }
        empresaRepository.deleteById(id);
    }
}
