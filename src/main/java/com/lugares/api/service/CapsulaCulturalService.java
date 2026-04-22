package com.lugares.api.service;

import com.lugares.api.entity.CapsulaCultural;
import com.lugares.api.exception.ResourceNotFoundException;
import com.lugares.api.repository.CapsulaCulturalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CapsulaCulturalService {

    private final CapsulaCulturalRepository capsulaCulturalRepository;
    private final StorageService storageService;

    public List<CapsulaCultural> listAll() {
        return capsulaCulturalRepository.findAll();
    }

    public CapsulaCultural getById(Integer id) {
        return capsulaCulturalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CapsulaCultural", "id", id));
    }

    @Transactional
    public CapsulaCultural create(CapsulaCultural capsula, MultipartFile file) {
        if (file != null && !file.isEmpty()) {
            capsula.setImagen(storageService.uploadFile(file, "capsulas"));
        }
        return capsulaCulturalRepository.save(capsula);
    }

    @Transactional
    public CapsulaCultural update(Integer id, CapsulaCultural datosActualizados, MultipartFile file) {
        CapsulaCultural existing = capsulaCulturalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CapsulaCultural", "id", id));
        if (file != null && !file.isEmpty()) {
            if (existing.getImagen() != null) {
                storageService.deleteFile(existing.getImagen());
            }
            datosActualizados.setImagen(storageService.uploadFile(file, "capsulas"));
        }
        datosActualizados.setId(id);
        return capsulaCulturalRepository.save(datosActualizados);
    }

    @Transactional
    public void delete(Integer id) {
        CapsulaCultural existing = capsulaCulturalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CapsulaCultural", "id", id));
        if (existing.getImagen() != null) {
            storageService.deleteFile(existing.getImagen());
        }
        capsulaCulturalRepository.deleteById(id);
    }
}
