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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "p_cliente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cliente implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_suscripcion")
    private Suscripcion suscripcion;

    @Column(name = "fld_nombre")
    private String nombre;

    @Column(name = "fld_nombre_corto")
    private String nombreCorto;

    @Column(name = "fld_telefono", length = 10)
    private String telefono;

    @Column(name = "fld_correo_electronico", nullable = false)
    private String correoElectronico;

    @Column(name = "fld_fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "fld_contrasenia")
    private String contrasenia;

    @Column(name = "fld_imagen_perfil")
    private String imagenPerfil;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(() -> "ROLE_CLIENTE");
    }

    @Override
    public String getPassword() {
        return this.contrasenia;
    }

    @Override
    public String getUsername() {
        if (this.correoElectronico != null && !this.correoElectronico.isBlank()) {
            return this.correoElectronico;
        }
        return this.nombreCorto != null ? this.nombreCorto : "";
    }
}
