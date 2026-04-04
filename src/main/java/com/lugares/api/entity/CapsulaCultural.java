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

import java.time.LocalDateTime;

@Entity
@Table(name = "p_capsulas_culturales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CapsulaCultural {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_capsula")
    private Integer id;

    @Column(name = "fld_titulo", nullable = false, length = 150)
    private String titulo;

    @Column(name = "fld_descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "fld_imagen", length = 500)
    private String imagen;

    @Column(name = "fld_fecha_publicacion")
    private LocalDateTime fechaPublicacion;

    @Column(name = "fld_es_visible")
    private Boolean esVisible;
}
