package com.lugares.api.entity;

import com.lugares.api.entity.enums.TipoPromocion;
import jakarta.persistence.Column;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "p_promociones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Promocion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_promocion")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_id_suscripcion")
    private Suscripcion suscripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_id_establecimiento")
    private Establecimiento establecimiento;

    @Column(name = "fld_nombre")
    private String nombre;

    @Column(name = "fld_descripcion")
    private String descripcion;

    @Lob
    @Column(name = "fld_imagen")
    private byte[] imagen;

    @Column(name = "fld_fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fld_fecha_fin")
    private LocalDate fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_promocion")
    private TipoPromocion tipoPromocion;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "p_promocion_dias", joinColumns = @JoinColumn(name = "fk_id_promocion"))
    @Column(name = "dias_disponibles")
    @Enumerated(EnumType.STRING)
    private Set<DayOfWeek> diasDisponibles;

    @Column(name = "fld_codigo_validacion", length = 8, nullable = false)
    private String codigoValidacion;
}
