package com.lugares.api.repository;

import com.lugares.api.entity.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    Optional<Cliente> findByCorreoElectronico(String correoElectronico);

    @Query("SELECT c FROM Cliente c LEFT JOIN FETCH c.suscripcion WHERE c.id = :id")
    Optional<Cliente> findByIdWithSuscripcion(@Param("id") Integer id);

    @Query("""
            SELECT c FROM Cliente c
            WHERE (:nombre IS NULL OR c.nombre LIKE %:nombre%)
            """)
    Page<Cliente> findByNombreContaining(@Param("nombre") String nombre, Pageable pageable);
}
