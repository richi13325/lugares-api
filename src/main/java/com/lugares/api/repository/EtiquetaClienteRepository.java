package com.lugares.api.repository;

import com.lugares.api.entity.Cliente;
import com.lugares.api.entity.Etiqueta;
import com.lugares.api.entity.EtiquetaCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EtiquetaClienteRepository extends JpaRepository<EtiquetaCliente, Integer> {

    @Query("""
            SELECT ec FROM EtiquetaCliente ec
            JOIN FETCH ec.etiqueta e
            JOIN FETCH e.categoria
            WHERE ec.cliente.id = :clienteId
            """)
    List<EtiquetaCliente> findByClienteIdWithEtiqueta(@Param("clienteId") Integer clienteId);

    boolean existsByClienteAndEtiqueta(Cliente cliente, Etiqueta etiqueta);

    void deleteByClienteAndEtiqueta(Cliente cliente, Etiqueta etiqueta);
}
