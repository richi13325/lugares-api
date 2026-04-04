package com.lugares.api.service;

import com.lugares.api.entity.Cliente;
import com.lugares.api.entity.Comentario;
import com.lugares.api.entity.Establecimiento;
import com.lugares.api.exception.ResourceNotFoundException;
import com.lugares.api.repository.ClienteRepository;
import com.lugares.api.repository.ComentarioRepository;
import com.lugares.api.repository.EstablecimientoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final ClienteRepository clienteRepository;
    private final EstablecimientoRepository establecimientoRepository;

    public List<Comentario> listByEstablecimiento(Integer establecimientoId) {
        return comentarioRepository.findByEstablecimientoIdWithCliente(establecimientoId);
    }

    @Transactional
    public Comentario create(Integer clienteId, Integer establecimientoId, String texto) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", clienteId));
        Establecimiento establecimiento = establecimientoRepository.findById(establecimientoId)
                .orElseThrow(() -> new ResourceNotFoundException("Establecimiento", "id", establecimientoId));

        Comentario comentario = new Comentario();
        comentario.setCliente(cliente);
        comentario.setEstablecimiento(establecimiento);
        comentario.setComentario(texto);
        comentario.setFechaComentario(LocalDate.now());

        return comentarioRepository.save(comentario);
    }

    @Transactional
    public void delete(Integer id) {
        if (!comentarioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Comentario", "id", id);
        }
        comentarioRepository.deleteById(id);
    }
}
