package com.lugares.api.repository;

import com.lugares.api.entity.Etiqueta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EtiquetaRepository extends JpaRepository<Etiqueta, Integer> {

    @Query("""
            SELECT e FROM Etiqueta e JOIN FETCH e.categoria
            WHERE (:nombre IS NULL OR e.nombre LIKE %:nombre%)
            """)
    Page<Etiqueta> findAllAdmin(@Param("nombre") String nombre, Pageable pageable);

    List<Etiqueta> findByEsVisibleTrue();

    long countByCategoriaId(Integer categoriaId);
}
