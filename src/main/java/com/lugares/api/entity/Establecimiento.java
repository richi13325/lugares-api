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

import java.sql.Time;

@Entity
@Table(name = "p_establecimiento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Establecimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_establecimiento")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_id_suscripcion")
    private Suscripcion suscripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_id_empresa")
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_id_tipo_establecimiento")
    private TipoEstablecimiento tipoEstablecimiento;

    @Column(name = "fld_nombre", nullable = false)
    private String nombre;

    @Column(name = "fld_descripcion", length = 510)
    private String descripcion;

    @Column(name = "fld_estado", nullable = false)
    private String estado;

    @Column(name = "fld_ciudad", nullable = false)
    private String ciudad;

    @Column(name = "fld_zona")
    private String zona;

    @Column(name = "fld_direccion", nullable = false)
    private String direccion;

    @Column(name = "fld_referencia_geografica", nullable = false)
    private String referenciaGeografica;

    @Column(name = "fld_coord_latitud", nullable = false)
    private String coordLatitud;

    @Column(name = "fld_coord_longitud", nullable = false)
    private String coordLongitud;

    @Column(name = "fld_correo_electronico")
    private String correoElectronico;

    @Column(name = "fld_sugerencia_de_la_casa")
    private String sugerenciaDeLaCasa;

    @Column(name = "fld_img_refs", columnDefinition = "TEXT")
    private String imgRefs;

    @Column(name = "fld_img_refs_2", length = 500)
    private String imgRefs2;

    @Column(name = "fld_img_refs_3", length = 500)
    private String imgRefs3;

    @Column(name = "fld_img_refs_4", length = 500)
    private String imgRefs4;

    @Column(name = "fld_horario_apertura")
    private Time horarioApertura;

    @Column(name = "fld_horario_cierre")
    private Time horarioCierre;

    @Column(name = "fld_lunes")
    private Boolean lunes;

    @Column(name = "fld_martes")
    private Boolean martes;

    @Column(name = "fld_miercoles")
    private Boolean miercoles;

    @Column(name = "fld_jueves")
    private Boolean jueves;

    @Column(name = "fld_viernes")
    private Boolean viernes;

    @Column(name = "fld_sabado")
    private Boolean sabado;

    @Column(name = "fld_domingo")
    private Boolean domingo;

    @Column(name = "fld_menu", columnDefinition = "TEXT")
    private String menu;

    @Column(name = "fld_celular_1", length = 20)
    private String celular1;

    @Column(name = "fld_celular_2", length = 20)
    private String celular2;

    @Column(name = "fld_celular_comentarios", length = 20)
    private String celularComentarios;

    @Column(name = "fld_alimentos_bebidas", columnDefinition = "TEXT")
    private String alimentosBebidas;

    @Column(name = "fld_ticket_promedio", length = 50)
    private String ticketPromedio;

    @Column(name = "fld_antiguedad_anios")
    private Integer antiguedadAnios;

    @Column(name = "fld_promo_lunes", columnDefinition = "TEXT")
    private String promoLunes;

    @Column(name = "fld_promo_martes", columnDefinition = "TEXT")
    private String promoMartes;

    @Column(name = "fld_promo_miercoles", columnDefinition = "TEXT")
    private String promoMiercoles;

    @Column(name = "fld_promo_jueves", columnDefinition = "TEXT")
    private String promoJueves;

    @Column(name = "fld_promo_viernes", columnDefinition = "TEXT")
    private String promoViernes;

    @Column(name = "fld_promo_sabado", columnDefinition = "TEXT")
    private String promoSabado;

    @Column(name = "fld_promo_domingo", columnDefinition = "TEXT")
    private String promoDomingo;

    @Column(name = "fld_promo_300_lugares", columnDefinition = "TEXT")
    private String promo300Lugares;
}
