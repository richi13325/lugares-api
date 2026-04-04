package com.lugares.api.repository;

import com.lugares.api.entity.Establecimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EstablecimientoRepository extends JpaRepository<Establecimiento, Integer> {

    @Query("""
            SELECT e FROM Establecimiento e
            LEFT JOIN FETCH e.suscripcion
            LEFT JOIN FETCH e.empresa
            LEFT JOIN FETCH e.tipoEstablecimiento
            WHERE e.id = :id
            """)
    Optional<Establecimiento> findByIdWithRelations(@Param("id") Integer id);

    @Query("""
            SELECT e FROM Establecimiento e
            WHERE (:nombre IS NULL OR e.nombre LIKE %:nombre%)
            """)
    Page<Establecimiento> findByNombreContaining(@Param("nombre") String nombre, Pageable pageable);

    @Query("""
            SELECT e FROM Establecimiento e
            WHERE (:nombre IS NULL OR e.nombre LIKE %:nombre%)
            AND e.tipoEstablecimiento.id = :tipoId
            """)
    Page<Establecimiento> findByTipoEstablecimientoId(
            @Param("tipoId") Integer tipoId,
            @Param("nombre") String nombre,
            Pageable pageable);

    /**
     * Reemplaza spListEstablecimientosFiltradosPoretiqueta (modo OR):
     * Establecimientos que tengan AL MENOS UNA de las etiquetas indicadas.
     */
    @Query("""
            SELECT DISTINCT e FROM Establecimiento e
            JOIN EtiquetaEstablecimiento ee ON ee.establecimiento = e
            WHERE ee.etiqueta.id IN :etiquetaIds
            """)
    List<Establecimiento> findByEtiquetasOr(@Param("etiquetaIds") List<Integer> etiquetaIds);

    /**
     * Reemplaza spListEstablecimientosFiltradosPoretiqueta (modo AND):
     * Establecimientos que tengan TODAS las etiquetas indicadas.
     */
    @Query("""
            SELECT e FROM Establecimiento e
            JOIN EtiquetaEstablecimiento ee ON ee.establecimiento = e
            WHERE ee.etiqueta.id IN :etiquetaIds
            GROUP BY e
            HAVING COUNT(DISTINCT ee.etiqueta.id) = :totalEtiquetas
            """)
    List<Establecimiento> findByEtiquetasAnd(
            @Param("etiquetaIds") List<Integer> etiquetaIds,
            @Param("totalEtiquetas") long totalEtiquetas);

    /**
     * Reemplaza spListEstablecimientosSugeridosClientesByEitiquetas:
     * Establecimientos que comparten etiquetas con las preferencias del cliente.
     */
    @Query("""
            SELECT DISTINCT e FROM Establecimiento e
            JOIN EtiquetaEstablecimiento ee ON ee.establecimiento = e
            WHERE ee.etiqueta.id IN (
                SELECT ec.etiqueta.id FROM EtiquetaCliente ec
                WHERE ec.cliente.id = :clienteId
            )
            """)
    List<Establecimiento> findSugeridosByClienteEtiquetas(@Param("clienteId") Integer clienteId);
}
