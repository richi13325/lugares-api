package com.lugares.api.service;

import com.lugares.api.entity.Establecimiento;
import com.lugares.api.entity.Promocion;
import com.lugares.api.entity.Suscripcion;
import com.lugares.api.entity.enums.TipoPromocion;
import com.lugares.api.exception.BusinessRuleException;
import com.lugares.api.exception.ResourceNotFoundException;
import com.lugares.api.repository.EstablecimientoRepository;
import com.lugares.api.repository.PromocionRepository;
import com.lugares.api.repository.SuscripcionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PromocionService {

    private final PromocionRepository promocionRepository;
    private final EstablecimientoRepository establecimientoRepository;
    private final SuscripcionRepository suscripcionRepository;
    private final StorageService storageService;

    public Promocion getById(Integer id) {
        return promocionRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promocion", "id", id));
    }

    public Page<Promocion> list(String nombre, Pageable pageable) {
        return promocionRepository.findByNombreContaining(nombre, pageable);
    }

    public List<Promocion> listByEstablecimiento(Integer establecimientoId) {
        return promocionRepository.findByEstablecimientoId(establecimientoId);
    }

    @Transactional
    public Promocion create(Promocion promocion, MultipartFile file) {
        if (file != null && !file.isEmpty()) {
            promocion.setImagen(storageService.uploadFile(file, "promociones"));
        }
        validarEstructura(promocion);
        resolveRelations(promocion);
        return promocionRepository.save(promocion);
    }

    @Transactional
    public Promocion update(Integer id, Promocion datosActualizados, MultipartFile file) {
        Promocion existing = promocionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promocion", "id", id));
        if (file != null && !file.isEmpty()) {
            if (existing.getImagen() != null) {
                storageService.deleteFile(existing.getImagen());
            }
            datosActualizados.setImagen(storageService.uploadFile(file, "promociones"));
        }
        validarEstructura(datosActualizados);
        resolveRelations(datosActualizados);
        datosActualizados.setId(id);
        return promocionRepository.save(datosActualizados);
    }

    @Transactional
    public void delete(Integer id) {
        Promocion existing = promocionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promocion", "id", id));
        if (existing.getImagen() != null) {
            storageService.deleteFile(existing.getImagen());
        }
        promocionRepository.deleteById(id);
    }

    private void validarEstructura(Promocion promocion) {
        if (promocion.getTipoPromocion() == null) {
            throw new BusinessRuleException("El tipo de promocion es obligatorio");
        }

        if (promocion.getTipoPromocion() == TipoPromocion.FECHA) {
            if (promocion.getFechaInicio() == null || promocion.getFechaFin() == null) {
                throw new BusinessRuleException("Las promociones por FECHA requieren fecha inicio y fin");
            }
            if (promocion.getFechaInicio().isAfter(promocion.getFechaFin())) {
                throw new BusinessRuleException("La fecha de inicio no puede ser mayor a la fecha fin");
            }
            promocion.setDiasDisponibles(null);
        }

        if (promocion.getTipoPromocion() == TipoPromocion.SEMANAL) {
            if (promocion.getDiasDisponibles() == null || promocion.getDiasDisponibles().isEmpty()) {
                throw new BusinessRuleException("Las promociones SEMANALES requieren al menos un dia disponible");
            }
            promocion.setFechaInicio(null);
            promocion.setFechaFin(null);
        }
    }

    private void resolveRelations(Promocion promocion) {
        if (promocion.getSuscripcion() != null && promocion.getSuscripcion().getId() != null) {
            Suscripcion suscripcion = suscripcionRepository.findById(promocion.getSuscripcion().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Suscripcion", "id", promocion.getSuscripcion().getId()));
            promocion.setSuscripcion(suscripcion);
        }
        if (promocion.getEstablecimiento() != null && promocion.getEstablecimiento().getId() != null) {
            Establecimiento establecimiento = establecimientoRepository.findById(promocion.getEstablecimiento().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Establecimiento", "id", promocion.getEstablecimiento().getId()));
            promocion.setEstablecimiento(establecimiento);
        }
    }
}
