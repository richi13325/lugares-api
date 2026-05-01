package com.lugares.api.config;

import com.lugares.api.entity.*;
import com.lugares.api.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Configuration
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final SuscripcionRepository suscripcionRepository;
    private final EmpresaRepository empresaRepository;
    private final TipoEstablecimientoRepository tipoEstablecimientoRepository;
    private final CategoriaEtiquetaRepository categoriaEtiquetaRepository;
    private final ClienteRepository clienteRepository;
    private final EstablecimientoRepository establecimientoRepository;
    private final EtiquetaRepository etiquetaRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            log.info("Seed data already exists — skipping");
            return;
        }

        log.info("Seeding dev data...");

        // Tier 0: No dependencies
        Usuario usuario = new Usuario();
        usuario.setNombre("Dev Admin");
        usuario.setTelefono("5551234567");
        usuario.setCorreoElectronico("admin@300lugares.com");
        usuario.setContrasenia(passwordEncoder.encode("admin123"));
        usuarioRepository.save(usuario);

        Suscripcion suscCliente = new Suscripcion();
        suscCliente.setNombre("Básica Cliente");
        suscCliente.setDescripcion("Suscripción gratuita para clientes");
        suscCliente.setPrecio(BigDecimal.ZERO);
        suscCliente.setEsSuscripcionDeCliente(true);
        suscripcionRepository.save(suscCliente);

        Suscripcion suscEstab = new Suscripcion();
        suscEstab.setNombre("Premium Establecimiento");
        suscEstab.setDescripcion("Suscripción para establecimientos");
        suscEstab.setPrecio(BigDecimal.valueOf(299));
        suscEstab.setEsSuscripcionDeCliente(false);
        suscripcionRepository.save(suscEstab);

        Empresa empresa = new Empresa();
        empresa.setNombre("Empresa Demo");
        empresa.setDescripcion("Empresa de prueba para desarrollo");
        empresa.setTelefono("5559876543");
        empresa.setCorreoElectronico("empresa@demo.com");
        empresa.setEstado("CDMX");
        empresa.setCiudad("Ciudad de México");
        empresa.setDireccion("Av. Reforma 100");
        empresa.setFechaCreacion(LocalDate.now());
        empresa.setFechaUltimaModificacion(LocalDate.now());
        empresaRepository.save(empresa);

        TipoEstablecimiento tipoRestaurante = new TipoEstablecimiento();
        tipoRestaurante.setNombre("Restaurante");
        tipoRestaurante.setDescripcion("Establecimiento de comida");
        tipoEstablecimientoRepository.save(tipoRestaurante);

        TipoEstablecimiento tipoBar = new TipoEstablecimiento();
        tipoBar.setNombre("Bar");
        tipoBar.setDescripcion("Establecimiento de bebidas");
        tipoEstablecimientoRepository.save(tipoBar);

        CategoriaEtiqueta categoria = new CategoriaEtiqueta();
        categoria.setNombre("Ambiente");
        categoria.setDescripcion("Etiquetas relacionadas al ambiente del lugar");
        categoria.setFechaCreacion(LocalDate.now());
        categoria.setFechaUltimaModificacion(LocalDate.now());
        categoriaEtiquetaRepository.save(categoria);

        // Tier 1: Depends on Tier 0
        Cliente cliente = new Cliente();
        cliente.setSuscripcion(suscCliente);
        cliente.setNombre("Cliente Test");
        cliente.setNombreCorto("clientetest");
        cliente.setTelefono("5551112222");
        cliente.setCorreoElectronico("cliente@test.com");
        cliente.setFechaNacimiento(LocalDate.of(1995, 6, 15));
        cliente.setContrasenia(passwordEncoder.encode("cliente123"));
        clienteRepository.save(cliente);

        Etiqueta etiqueta = new Etiqueta();
        etiqueta.setNombre("Terraza");
        etiqueta.setDescripcion("Lugar con terraza");
        etiqueta.setEsVisible(true);
        etiqueta.setCategoria(categoria);
        etiquetaRepository.save(etiqueta);

        Establecimiento estab = new Establecimiento();
        estab.setSuscripcion(suscEstab);
        estab.setEmpresa(empresa);
        estab.setTipoEstablecimiento(tipoRestaurante);
        estab.setNombre("Restaurante Demo");
        estab.setDescripcion("Restaurante de prueba para desarrollo");
        estab.setEstado("CDMX");
        estab.setCiudad("Ciudad de México");
        estab.setZona("Centro");
        estab.setDireccion("Calle 5 de Mayo 10");
        estab.setCoordLatitud("19.4326");
        estab.setCoordLongitud("-99.1332");
        estab.setCorreoElectronico("demo@restaurante.com");
        estab.setCelular1("5553334444");
        estab.setLunes(true);
        estab.setMartes(true);
        estab.setMiercoles(true);
        estab.setJueves(true);
        estab.setViernes(true);
        estab.setSabado(true);
        estab.setDomingo(false);
        estab.setTicketPromedio("$200-400");
        estab.setAntiguedadAnios(5);
        establecimientoRepository.save(estab);

        log.info("Seed data created successfully");
        log.info("  Usuario: admin@300lugares.com / admin123");
        log.info("  Cliente: cliente@test.com / cliente123");
    }
}
