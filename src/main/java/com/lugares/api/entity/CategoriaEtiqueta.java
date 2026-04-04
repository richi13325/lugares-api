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

import java.time.LocalDate;

@Entity
@Table(name = "p_categoria_etiqueta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaEtiqueta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private Integer id;

    @Column(name = "fld_nombre", length = 65)
    private String nombre;

    @Column(name = "fld_descripcion", length = 255)
    private String descripcion;

    @Column(name = "fld_fecha_creacion")
    private LocalDate fechaCreacion;

    @Column(name = "fld_fecha_ultima_modificacion")
    private LocalDate fechaUltimaModificacion;
}
