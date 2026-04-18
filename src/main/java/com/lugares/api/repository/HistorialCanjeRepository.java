package com.lugares.api.repository;

import com.lugares.api.entity.HistorialCanje;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistorialCanjeRepository extends JpaRepository<HistorialCanje, Integer> {

    List<HistorialCanje> findByPromocionId(Integer promocionId);

    List<HistorialCanje> findByClienteId(Integer clienteId);
}
