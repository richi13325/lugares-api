package com.lugares.api.service;

import com.lugares.api.entity.Calificacion;
import com.lugares.api.entity.Cliente;
import com.lugares.api.entity.Establecimiento;
import com.lugares.api.exception.ResourceNotFoundException;
import com.lugares.api.repository.CalificacionRepository;
import com.lugares.api.repository.ClienteRepository;
import com.lugares.api.repository.EstablecimientoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CalificacionService {

    private final CalificacionRepository calificacionRepository;
    private final ClienteRepository clienteRepository;
    private final EstablecimientoRepository establecimientoRepository;

    @Transactional
    public Calificacion createOrUpdate(Integer clienteId, Integer establecimientoId, Byte valor) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", clienteId));
        Establecimiento establecimiento = establecimientoRepository.findById(establecimientoId)
                .orElseThrow(() -> new ResourceNotFoundException("Establecimiento", "id", establecimientoId));

        Calificacion calificacion = calificacionRepository
                .findByClienteAndEstablecimiento(cliente, establecimiento)
                .orElseGet(() -> {
                    Calificacion nueva = new Calificacion();
                    nueva.setCliente(cliente);
                    nueva.setEstablecimiento(establecimiento);
                    return nueva;
                });

        calificacion.setCalificacion(valor);
        return calificacionRepository.save(calificacion);
    }
}
