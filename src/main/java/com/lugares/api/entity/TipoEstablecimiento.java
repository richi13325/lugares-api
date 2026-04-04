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

@Entity
@Table(name = "c_tipoestablecimiento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TipoEstablecimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_establecimiento")
    private Integer id;

    @Column(name = "fld_nombre")
    private String nombre;

    @Column(name = "fld_descripcion")
    private String descripcion;
}
