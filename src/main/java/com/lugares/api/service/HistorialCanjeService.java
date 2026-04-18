package com.lugares.api.service;

import com.lugares.api.entity.Cliente;
import com.lugares.api.entity.HistorialCanje;
import com.lugares.api.entity.Promocion;
import com.lugares.api.exception.BusinessRuleException;
import com.lugares.api.exception.ResourceNotFoundException;
import com.lugares.api.repository.ClienteRepository;
import com.lugares.api.repository.HistorialCanjeRepository;
import com.lugares.api.repository.PromocionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HistorialCanjeService {

    private final HistorialCanjeRepository historialCanjeRepository;
    private final PromocionRepository promocionRepository;
    private final ClienteRepository clienteRepository;

    public List<HistorialCanje> listByCliente(Integer clienteId) {
        return historialCanjeRepository.findByClienteId(clienteId);
    }

    public List<HistorialCanje> listByPromocion(Integer promocionId) {
        return historialCanjeRepository.findByPromocionId(promocionId);
    }

    @Transactional
    public HistorialCanje canjear(Integer promocionId, Integer clienteId, String codigoValidacion) {
        Promocion promocion = promocionRepository.findById(promocionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promocion", "id", promocionId));
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", clienteId));

        if (!promocion.getCodigoValidacion().equals(codigoValidacion)) {
            throw new BusinessRuleException("Codigo de validacion incorrecto");
        }

        HistorialCanje canje = new HistorialCanje();
        canje.setPromocion(promocion);
        canje.setCliente(cliente);
        canje.setFechaHora(LocalDateTime.now());
        canje.setCodigoValidacion(codigoValidacion);

        return historialCanjeRepository.save(canje);
    }

    @Transactional
    public void delete(Integer id) {
        if (!historialCanjeRepository.existsById(id)) {
            throw new ResourceNotFoundException("HistorialCanje", "id", id);
        }
        historialCanjeRepository.deleteById(id);
    }
}
