package com.lugares.api.repository;

import com.lugares.api.entity.EtiquetaTipoEstablecimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EtiquetaTipoEstablecimientoRepository extends JpaRepository<EtiquetaTipoEstablecimiento, Integer> {

    @Query("""
            SELECT ete FROM EtiquetaTipoEstablecimiento ete
            JOIN FETCH ete.etiqueta e
            JOIN FETCH e.categoria
            WHERE ete.tipoEstablecimiento.id = :tipoId
            """)
    Page<EtiquetaTipoEstablecimiento> findByTipoEstablecimientoIdWithEtiqueta(
            @Param("tipoId") Integer tipoId, Pageable pageable);
}
