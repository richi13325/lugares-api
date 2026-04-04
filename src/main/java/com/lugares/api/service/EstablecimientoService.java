package com.lugares.api.service;

import com.lugares.api.entity.Empresa;
import com.lugares.api.entity.Establecimiento;
import com.lugares.api.entity.Suscripcion;
import com.lugares.api.entity.TipoEstablecimiento;
import com.lugares.api.exception.ResourceNotFoundException;
import com.lugares.api.repository.EmpresaRepository;
import com.lugares.api.repository.EstablecimientoRepository;
import com.lugares.api.repository.SuscripcionRepository;
import com.lugares.api.repository.TipoEstablecimientoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EstablecimientoService {

    private final EstablecimientoRepository establecimientoRepository;
    private final SuscripcionRepository suscripcionRepository;
    private final EmpresaRepository empresaRepository;
    private final TipoEstablecimientoRepository tipoEstablecimientoRepository;

    public Establecimiento getById(Integer id) {
        return establecimientoRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Establecimiento", "id", id));
    }

    public Page<Establecimiento> list(String nombre, Pageable pageable) {
        return establecimientoRepository.findByNombreContaining(nombre, pageable);
    }

    public Page<Establecimiento> listByTipoEstablecimiento(Integer tipoId, String nombre, Pageable pageable) {
        return establecimientoRepository.findByTipoEstablecimientoId(tipoId, nombre, pageable);
    }

    public List<Establecimiento> findByEtiquetas(List<Integer> etiquetaIds, boolean busquedaEstricta) {
        if (busquedaEstricta) {
            return establecimientoRepository.findByEtiquetasAnd(etiquetaIds, etiquetaIds.size());
        }
        return establecimientoRepository.findByEtiquetasOr(etiquetaIds);
    }

    public List<Establecimiento> findSugeridos(Integer clienteId) {
        return establecimientoRepository.findSugeridosByClienteEtiquetas(clienteId);
    }

    @Transactional
    public Establecimiento create(Establecimiento establecimiento) {
        resolveRelations(establecimiento);
        return establecimientoRepository.save(establecimiento);
    }

    @Transactional
    public Establecimiento update(Integer id, Establecimiento datosActualizados) {
        Establecimiento existente = establecimientoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Establecimiento", "id", id));

        resolveRelations(datosActualizados);

        datosActualizados.setId(existente.getId());
        return establecimientoRepository.save(datosActualizados);
    }

    @Transactional
    public void delete(Integer id) {
        if (!establecimientoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Establecimiento", "id", id);
        }
        establecimientoRepository.deleteById(id);
    }

    private void resolveRelations(Establecimiento establecimiento) {
        if (establecimiento.getSuscripcion() != null && establecimiento.getSuscripcion().getId() != null) {
            Suscripcion suscripcion = suscripcionRepository.findById(establecimiento.getSuscripcion().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Suscripcion", "id", establecimiento.getSuscripcion().getId()));
            establecimiento.setSuscripcion(suscripcion);
        }
        if (establecimiento.getEmpresa() != null && establecimiento.getEmpresa().getId() != null) {
            Empresa empresa = empresaRepository.findById(establecimiento.getEmpresa().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Empresa", "id", establecimiento.getEmpresa().getId()));
            establecimiento.setEmpresa(empresa);
        }
        if (establecimiento.getTipoEstablecimiento() != null && establecimiento.getTipoEstablecimiento().getId() != null) {
            TipoEstablecimiento tipo = tipoEstablecimientoRepository.findById(establecimiento.getTipoEstablecimiento().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("TipoEstablecimiento", "id", establecimiento.getTipoEstablecimiento().getId()));
            establecimiento.setTipoEstablecimiento(tipo);
        }
    }
}
