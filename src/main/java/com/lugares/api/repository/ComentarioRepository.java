package com.lugares.api.repository;

import com.lugares.api.entity.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ComentarioRepository extends JpaRepository<Comentario, Integer> {

    @Query("""
            SELECT c FROM Comentario c
            JOIN FETCH c.cliente
            WHERE c.establecimiento.id = :establecimientoId
            ORDER BY c.fechaComentario DESC
            """)
    List<Comentario> findByEstablecimientoIdWithCliente(@Param("establecimientoId") Integer establecimientoId);
}
