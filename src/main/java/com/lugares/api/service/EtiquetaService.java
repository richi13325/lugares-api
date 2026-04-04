package com.lugares.api.service;

import com.lugares.api.entity.CategoriaEtiqueta;
import com.lugares.api.entity.Cliente;
import com.lugares.api.entity.Establecimiento;
import com.lugares.api.entity.Etiqueta;
import com.lugares.api.entity.EtiquetaCliente;
import com.lugares.api.entity.EtiquetaEstablecimiento;
import com.lugares.api.exception.BusinessRuleException;
import com.lugares.api.exception.ResourceNotFoundException;
import com.lugares.api.repository.CategoriaEtiquetaRepository;
import com.lugares.api.repository.ClienteRepository;
import com.lugares.api.repository.EstablecimientoRepository;
import com.lugares.api.repository.EtiquetaClienteRepository;
import com.lugares.api.repository.EtiquetaEstablecimientoRepository;
import com.lugares.api.repository.EtiquetaRepository;
import com.lugares.api.repository.EtiquetaTipoEstablecimientoRepository;
import com.lugares.api.entity.EtiquetaTipoEstablecimiento;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EtiquetaService {

    private final EtiquetaRepository etiquetaRepository;
    private final CategoriaEtiquetaRepository categoriaEtiquetaRepository;
    private final EtiquetaClienteRepository etiquetaClienteRepository;
    private final EtiquetaEstablecimientoRepository etiquetaEstablecimientoRepository;
    private final EtiquetaTipoEstablecimientoRepository etiquetaTipoEstablecimientoRepository;
    private final ClienteRepository clienteRepository;
    private final EstablecimientoRepository establecimientoRepository;

    // --- Etiquetas CRUD ---

    public Etiqueta getById(Integer id) {
        return etiquetaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Etiqueta", "id", id));
    }

    public Page<Etiqueta> listAdmin(String nombre, Pageable pageable) {
        return etiquetaRepository.findAllAdmin(nombre, pageable);
    }

    public List<Etiqueta> listVisibles() {
        return etiquetaRepository.findByEsVisibleTrue();
    }

    @Transactional
    public Etiqueta create(Etiqueta etiqueta) {
        if (etiqueta.getCategoria() != null && etiqueta.getCategoria().getId() != null) {
            CategoriaEtiqueta categoria = categoriaEtiquetaRepository.findById(etiqueta.getCategoria().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("CategoriaEtiqueta", "id", etiqueta.getCategoria().getId()));
            etiqueta.setCategoria(categoria);
        }
        return etiquetaRepository.save(etiqueta);
    }

    @Transactional
    public Etiqueta update(Integer id, Etiqueta datosActualizados) {
        Etiqueta etiqueta = etiquetaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Etiqueta", "id", id));

        if (datosActualizados.getNombre() != null) {
            etiqueta.setNombre(datosActualizados.getNombre());
        }
        if (datosActualizados.getDescripcion() != null) {
            etiqueta.setDescripcion(datosActualizados.getDescripcion());
        }
        if (datosActualizados.getEsVisible() != null) {
            etiqueta.setEsVisible(datosActualizados.getEsVisible());
        }
        if (datosActualizados.getCategoria() != null && datosActualizados.getCategoria().getId() != null) {
            CategoriaEtiqueta categoria = categoriaEtiquetaRepository.findById(datosActualizados.getCategoria().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("CategoriaEtiqueta", "id", datosActualizados.getCategoria().getId()));
            etiqueta.setCategoria(categoria);
        }

        return etiquetaRepository.save(etiqueta);
    }

    @Transactional
    public void delete(Integer id) {
        if (!etiquetaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Etiqueta", "id", id);
        }
        etiquetaRepository.deleteById(id);
    }

    // --- Etiquetas por Cliente ---

    public List<EtiquetaCliente> listByCliente(Integer clienteId) {
        return etiquetaClienteRepository.findByClienteIdWithEtiqueta(clienteId);
    }

    @Transactional
    public EtiquetaCliente assignToCliente(Integer clienteId, Integer etiquetaId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", clienteId));
        Etiqueta etiqueta = etiquetaRepository.findById(etiquetaId)
                .orElseThrow(() -> new ResourceNotFoundException("Etiqueta", "id", etiquetaId));

        if (etiquetaClienteRepository.existsByClienteAndEtiqueta(cliente, etiqueta)) {
            throw new BusinessRuleException("El cliente ya tiene asignada esta etiqueta");
        }

        EtiquetaCliente ec = new EtiquetaCliente();
        ec.setCliente(cliente);
        ec.setEtiqueta(etiqueta);
        return etiquetaClienteRepository.save(ec);
    }

    @Transactional
    public void removeFromCliente(Integer clienteId, Integer etiquetaId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", clienteId));
        Etiqueta etiqueta = etiquetaRepository.findById(etiquetaId)
                .orElseThrow(() -> new ResourceNotFoundException("Etiqueta", "id", etiquetaId));

        etiquetaClienteRepository.deleteByClienteAndEtiqueta(cliente, etiqueta);
    }

    // --- Etiquetas por Establecimiento ---

    public List<EtiquetaEstablecimiento> listByEstablecimiento(Integer establecimientoId) {
        return etiquetaEstablecimientoRepository.findByEstablecimientoIdWithEtiqueta(establecimientoId);
    }

    @Transactional
    public EtiquetaEstablecimiento assignToEstablecimiento(Integer establecimientoId, Integer etiquetaId) {
        Establecimiento establecimiento = establecimientoRepository.findById(establecimientoId)
                .orElseThrow(() -> new ResourceNotFoundException("Establecimiento", "id", establecimientoId));
        Etiqueta etiqueta = etiquetaRepository.findById(etiquetaId)
                .orElseThrow(() -> new ResourceNotFoundException("Etiqueta", "id", etiquetaId));

        if (etiquetaEstablecimientoRepository.existsByEstablecimientoAndEtiqueta(establecimiento, etiqueta)) {
            throw new BusinessRuleException("El establecimiento ya tiene asignada esta etiqueta");
        }

        EtiquetaEstablecimiento ee = new EtiquetaEstablecimiento();
        ee.setEstablecimiento(establecimiento);
        ee.setEtiqueta(etiqueta);
        return etiquetaEstablecimientoRepository.save(ee);
    }

    @Transactional
    public void removeFromEstablecimiento(Integer establecimientoId, Integer etiquetaId) {
        Establecimiento establecimiento = establecimientoRepository.findById(establecimientoId)
                .orElseThrow(() -> new ResourceNotFoundException("Establecimiento", "id", establecimientoId));
        Etiqueta etiqueta = etiquetaRepository.findById(etiquetaId)
                .orElseThrow(() -> new ResourceNotFoundException("Etiqueta", "id", etiquetaId));

        etiquetaEstablecimientoRepository.deleteByEstablecimientoAndEtiqueta(establecimiento, etiqueta);
    }

    // --- Etiquetas por TipoEstablecimiento ---

    public Page<EtiquetaTipoEstablecimiento> listByTipoEstablecimiento(Integer tipoId, Pageable pageable) {
        return etiquetaTipoEstablecimientoRepository.findByTipoEstablecimientoIdWithEtiqueta(tipoId, pageable);
    }
}
