package com.lugares.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "p_historial_canjes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HistorialCanje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_canje")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_id_promocion", nullable = false)
    private Promocion promocion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_id_cliente", nullable = false)
    private Cliente cliente;

    @Column(name = "fld_fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "fld_codigo_validacion", nullable = false)
    private String codigoValidacion;
}
