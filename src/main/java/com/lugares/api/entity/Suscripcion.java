package com.lugares.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "p_suscripcion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Suscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_suscripcion")
    private Integer id;

    @Column(name = "fld_nombre")
    private String nombre;

    @Column(name = "fld_descripcion")
    private String descripcion;

    @Column(name = "fld_precio", precision = 10, scale = 0, nullable = false)
    private BigDecimal precio;

    @Column(name = "fld_es_suscripcion_de_cliente")
    private Boolean esSuscripcionDeCliente;
}
