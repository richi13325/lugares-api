package com.lugares.api.repository;

import com.lugares.api.entity.Promocion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PromocionRepository extends JpaRepository<Promocion, Integer> {

    @Query("""
            SELECT p FROM Promocion p
            LEFT JOIN FETCH p.suscripcion
            LEFT JOIN FETCH p.establecimiento
            WHERE p.id = :id
            """)
    Optional<Promocion> findByIdWithRelations(@Param("id") Integer id);

    @Query("""
            SELECT p FROM Promocion p
            JOIN FETCH p.establecimiento
            JOIN FETCH p.suscripcion
            WHERE (:nombre IS NULL OR p.nombre LIKE %:nombre%)
            """)
    Page<Promocion> findByNombreContaining(@Param("nombre") String nombre, Pageable pageable);

    List<Promocion> findByEstablecimientoId(Integer establecimientoId);
}
