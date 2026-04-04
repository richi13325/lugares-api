package com.lugares.api.repository;

import com.lugares.api.entity.Establecimiento;
import com.lugares.api.entity.Etiqueta;
import com.lugares.api.entity.EtiquetaEstablecimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EtiquetaEstablecimientoRepository extends JpaRepository<EtiquetaEstablecimiento, Integer> {

    @Query("""
            SELECT ee FROM EtiquetaEstablecimiento ee
            JOIN FETCH ee.etiqueta e
            JOIN FETCH e.categoria
            WHERE ee.establecimiento.id = :establecimientoId
            """)
    List<EtiquetaEstablecimiento> findByEstablecimientoIdWithEtiqueta(@Param("establecimientoId") Integer establecimientoId);

    boolean existsByEstablecimientoAndEtiqueta(Establecimiento establecimiento, Etiqueta etiqueta);

    void deleteByEstablecimientoAndEtiqueta(Establecimiento establecimiento, Etiqueta etiqueta);
}
