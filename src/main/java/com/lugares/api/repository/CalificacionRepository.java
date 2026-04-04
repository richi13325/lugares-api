package com.lugares.api.repository;

import com.lugares.api.entity.Calificacion;
import com.lugares.api.entity.Cliente;
import com.lugares.api.entity.Establecimiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CalificacionRepository extends JpaRepository<Calificacion, Integer> {

    Optional<Calificacion> findByClienteAndEstablecimiento(Cliente cliente, Establecimiento establecimiento);
}
