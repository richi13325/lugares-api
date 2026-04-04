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
@Table(name = "p_empresa")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_empresa")
    private Integer id;

    @Column(name = "fld_nombre")
    private String nombre;

    @Column(name = "fld_descripcion")
    private String descripcion;

    @Column(name = "fld_telefono", length = 10)
    private String telefono;

    @Column(name = "fld_correo_electronico")
    private String correoElectronico;

    @Column(name = "fld_estado")
    private String estado;

    @Column(name = "fld_ciudad")
    private String ciudad;

    @Column(name = "fld_direccion")
    private String direccion;

    @Column(name = "fld_fecha_creacion")
    private LocalDate fechaCreacion;

    @Column(name = "fld_fecha_ultima_modificacion")
    private LocalDate fechaUltimaModificacion;
}
