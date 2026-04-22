package com.lugares.api.service;

import com.lugares.api.entity.Cliente;
import com.lugares.api.entity.Suscripcion;
import com.lugares.api.exception.DuplicateResourceException;
import com.lugares.api.exception.ResourceNotFoundException;
import com.lugares.api.repository.ClienteRepository;
import com.lugares.api.repository.SuscripcionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final SuscripcionRepository suscripcionRepository;
    private final PasswordEncoder passwordEncoder;
    private final StorageService storageService;

    public Cliente getById(Integer id) {
        return clienteRepository.findByIdWithSuscripcion(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", id));
    }

    public Page<Cliente> list(String nombre, Pageable pageable) {
        return clienteRepository.findByNombreContaining(nombre, pageable);
    }

    @Transactional
    public Cliente create(Cliente cliente) {
        clienteRepository.findByCorreoElectronico(cliente.getCorreoElectronico())
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("Cliente", "correoElectronico", cliente.getCorreoElectronico());
                });

        cliente.setContrasenia(passwordEncoder.encode(cliente.getPassword()));

        if (cliente.getSuscripcion() != null && cliente.getSuscripcion().getId() != null) {
            Suscripcion suscripcion = suscripcionRepository.findById(cliente.getSuscripcion().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Suscripcion", "id", cliente.getSuscripcion().getId()));
            cliente.setSuscripcion(suscripcion);
        }

        return clienteRepository.save(cliente);
    }

    @Transactional
    public Cliente update(Integer id, Cliente datosActualizados, MultipartFile file) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", id));

        if (datosActualizados.getNombre() != null && !datosActualizados.getNombre().isBlank()) {
            cliente.setNombre(datosActualizados.getNombre());
        }
        if (datosActualizados.getNombreCorto() != null && !datosActualizados.getNombreCorto().isBlank()) {
            cliente.setNombreCorto(datosActualizados.getNombreCorto());
        }
        if (datosActualizados.getCorreoElectronico() != null && !datosActualizados.getCorreoElectronico().isBlank()) {
            cliente.setCorreoElectronico(datosActualizados.getCorreoElectronico());
        }
        if (datosActualizados.getContrasenia() != null && !datosActualizados.getContrasenia().isBlank()) {
            cliente.setContrasenia(passwordEncoder.encode(datosActualizados.getContrasenia()));
        }
        if (datosActualizados.getTelefono() != null) {
            cliente.setTelefono(datosActualizados.getTelefono());
        }
        if (file != null && !file.isEmpty()) {
            cliente.setImagenPerfil(storageService.uploadFile(file, "perfiles"));
        }
        if (datosActualizados.getFechaNacimiento() != null) {
            cliente.setFechaNacimiento(datosActualizados.getFechaNacimiento());
        }
        if (datosActualizados.getSuscripcion() != null && datosActualizados.getSuscripcion().getId() != null) {
            Suscripcion suscripcion = suscripcionRepository.findById(datosActualizados.getSuscripcion().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Suscripcion", "id", datosActualizados.getSuscripcion().getId()));
            cliente.setSuscripcion(suscripcion);
        }

        return clienteRepository.save(cliente);
    }

    @Transactional
    public void delete(Integer id) {
        if (!clienteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cliente", "id", id);
        }
        clienteRepository.deleteById(id);
    }
}
