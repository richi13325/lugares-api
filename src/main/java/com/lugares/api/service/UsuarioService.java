package com.lugares.api.service;

import com.lugares.api.entity.Usuario;
import com.lugares.api.exception.DuplicateResourceException;
import com.lugares.api.exception.ResourceNotFoundException;
import com.lugares.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public Usuario getById(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
    }

    public Page<Usuario> list(String nombre, Pageable pageable) {
        return usuarioRepository.findByNombreContaining(nombre, pageable);
    }

    @Transactional
    public Usuario create(Usuario usuario) {
        usuarioRepository.findByCorreoElectronico(usuario.getCorreoElectronico())
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("Usuario", "correoElectronico", usuario.getCorreoElectronico());
                });

        usuario.setContrasenia(passwordEncoder.encode(usuario.getContrasenia()));
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario update(Integer id, Usuario datosActualizados) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));

        if (datosActualizados.getNombre() != null && !datosActualizados.getNombre().isBlank()) {
            usuario.setNombre(datosActualizados.getNombre());
        }
        if (datosActualizados.getTelefono() != null) {
            usuario.setTelefono(datosActualizados.getTelefono());
        }
        if (datosActualizados.getCorreoElectronico() != null && !datosActualizados.getCorreoElectronico().isBlank()) {
            usuario.setCorreoElectronico(datosActualizados.getCorreoElectronico());
        }
        if (datosActualizados.getContrasenia() != null && !datosActualizados.getContrasenia().isBlank()) {
            usuario.setContrasenia(passwordEncoder.encode(datosActualizados.getContrasenia()));
        }

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void delete(Integer id) {
        if (!usuarioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuario", "id", id);
        }
        usuarioRepository.deleteById(id);
    }
}
