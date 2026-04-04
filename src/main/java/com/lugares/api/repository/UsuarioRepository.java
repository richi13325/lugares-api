package com.lugares.api.repository;

import com.lugares.api.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByCorreoElectronico(String correoElectronico);

    @Query("""
            SELECT u FROM Usuario u
            WHERE (:nombre IS NULL OR u.nombre LIKE %:nombre%)
            """)
    Page<Usuario> findByNombreContaining(@Param("nombre") String nombre, Pageable pageable);
}
