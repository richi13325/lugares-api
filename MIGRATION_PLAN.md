# MIGRATION PLAN — App 300 Lugares Backend Rewrite

> **Proyecto**: Reescritura completa del backend legacy App300_Lugares
> **Stack destino**: Spring Boot 3.5.x · Java 21 · Spring Data JPA · MySQL · JWT
> **Fecha**: 2026-04-02
> **Método de ejecución**: Sequential Dispatch (fase por fase, con aprobación)

---

## 1. AUDITORIA DEL LEGACY

### 1.1 Inventario General

| Capa | Cantidad | Estado |
|------|----------|--------|
| Entidades JPA | 23 clases (19 modelos + 3 en subpaquetes + 1 enum) | Relaciones rotas |
| Repositorios JPA | 24 interfaces | Funcionales |
| Implementaciones SP | 10 clases `*SpResponseImpl` | Lógica de negocio oculta |
| Stored Procedures | 15 SPs identificados | Reemplazo obligatorio |
| Servicios | 29 clases @Service | Try-catch esparcidos |
| Controladores | 28 clases @RestController | ~90+ try-catch blocks |
| DTOs | 40 clases | Passwords expuestos en respuestas |
| Excepciones custom | 0 | Inexistente |
| Config files | 5 (Security, JWT, Firebase, Mapper, App) | CORS hardcodeado |

### 1.2 Deudas Tecnicas Criticas

#### CRITICO — Seguridad

| Problema | Ubicacion | Impacto |
|----------|-----------|---------|
| `fldContrasenia` expuesta en `ClienteDTO` y `UsuarioDTO` | DTOs usados como response | Passwords hasheados viajan al frontend |
| CORS origins hardcodeados (8 URLs) | `SecurityConfiguration:149` | Requiere recompilacion para cambiar |
| `allowedHeaders(List.of("*"))` + `allowCredentials(true)` | `SecurityConfiguration:151-152` | Vulnerabilidad CORS |
| Firebase credentials en classpath | `FirebaseConfig` | Credenciales en el build artifact |
| Swagger/OpenAPI expuesto sin restriccion | `SecurityConfiguration:107-109` | API docs publicos en produccion |
| `FcmTokenController` sin `@PreAuthorize` | Controller `/api/token` | Endpoint desprotegido |

#### CRITICO — Arquitectura

| Problema | Ubicacion | Impacto |
|----------|-----------|---------|
| **0 excepciones custom** | Carpeta `exception/` vacia | Todo es `RuntimeException` generico |
| **GlobalExceptionHandler** solo maneja 1 excepcion | Solo `MethodArgumentNotValidException` | 90+ try-catch compensando en controllers |
| **ModelMapper sin modo STRICT** | `MapperConfig` — `new ModelMapper()` sin config | Mapeos silenciosos incorrectos |
| **Servicios trabajan con DTOs** | `ClienteService`, `EstablecimientoService` | Violacion de capas: service construye DTOs |
| **@Builder en services** para crear entidades Y DTOs | `EstablecimientoService:155-183` (35 lineas de builder) | Duplicacion masiva save/edit |
| **@Data en entidades JPA** | Todos los modelos | `equals()`/`hashCode()` rotos con proxies Hibernate |

#### ALTO — Stored Procedures con Logica de Negocio Oculta

Los SPs son el corazon del problema. Hay logica de negocio que NO existe en Java — vive exclusivamente en la base de datos:

| SP | Complejidad | Logica Oculta |
|----|-------------|---------------|
| `spListEstablecimientosFiltradosPoretiqueta` | **COMPLEJO** | Filtrado dinamico AND/OR por tags usando CSV parsing. Recibe `esBusquedaEstricta` (boolean) que cambia la logica de JOIN entre tablas `d_etiqueta_x_establecimiento` |
| `spGetEstablecimientoById` | **COMPLEJO** | Query desnormalizado de 40 columnas. Hace JOINs a suscripcion, empresa, tipo_establecimiento y devuelve horarios/promos por dia de la semana como columnas individuales |
| `spListEstablecimientosSugeridosClientesByEitiquetas` | **COMPLEJO** | **Algoritmo de recomendacion**: cruza `d_etiqueta_x_cliente` con `d_etiqueta_x_establecimiento` para sugerir lugares basados en preferencias del cliente |
| `Sp_CreateOrUpdateCliente` | MODERADO | INSERT/UPDATE condicional + parametro `p_fld_salt` registrado pero NUNCA usado (legacy de refactor de passwords) |
| `spCreateOrUpdateUsuario` | MODERADO | INSERT/UPDATE condicional por `id_operacion` (1=INSERT, 2=UPDATE) |
| `spCreateOrUpdateCalificacion` | MODERADO | Upsert de calificacion cliente-establecimiento |
| `spListClientes` | MODERADO | Paginacion manual + filtro por nombre |
| `spListUsuarios` | MODERADO | Paginacion manual + filtro por nombre |
| `spListPromociones` | MODERADO | Paginacion + filtro + parsing de LocalDate |
| `spListEtiquetaAdmin` | MODERADO | Paginacion admin con visibilidad y categoria |
| `spListEtiquetaByIdTipoEstablecimiento` | MODERADO | Paginacion filtrada por tipo |
| `spListComentarioClienteByIdEstablecimiento` | MODERADO | Join con datos de perfil del cliente |
| `spGetclienteByid` | TRIVIAL | SELECT simple — `findById()` lo reemplaza |
| `spListEtiquetaByCliente` | TRIVIAL | Join simple etiqueta-cliente |
| `spListEtiquetasByEstablecimiento` | TRIVIAL | Join simple etiqueta-establecimiento |

#### ALTO — Relaciones JPA Inexistentes

TODAS las entidades usan Integer como FK en vez de `@ManyToOne`. Esto fuerza a los SPs a hacer los JOINs que JPA deberia manejar:

| Entidad | Campo FK (Integer) | Deberia ser @ManyToOne a |
|---------|-------------------|--------------------------|
| `ClienteModel` | `idSuscripcion` | `SuscripcionModel` |
| `EstablecimientoModel` | `fkIdSuscripcion` | `SuscripcionModel` |
| `EstablecimientoModel` | `fkIdEmpresa` | `EmpresaModel` |
| `EstablecimientoModel` | `fkIdTipoEstablecimiento` | `TipoEstablecimientoModel` |
| `EtiquetaModel` | `fkIdCategoria` | `CategoriaEtiquetaModel` |
| `PromocionModel` | `fkIdSuscripcion` | `SuscripcionModel` |
| `PromocionModel` | `fkIdEstablecimiento` | `EstablecimientoModel` |
| `ComentarioClienteModel` | (asumido) FK a Cliente y Establecimiento | `ClienteModel`, `EstablecimientoModel` |
| `EstrellaClienteModel` | (asumido) FK a Cliente y Establecimiento | `ClienteModel`, `EstablecimientoModel` |
| `EtiquetaPorClienteModel` | FK a Cliente y Etiqueta | `ClienteModel`, `EtiquetaModel` |
| `EtiquetaPorEstablecimientoModel` | FK a Establecimiento y Etiqueta | `EstablecimientoModel`, `EtiquetaModel` |
| `EtiquetaPorTipoEstablecimientoModel` | FK a TipoEstablecimiento y Etiqueta | `TipoEstablecimientoModel`, `EtiquetaModel` |

#### MEDIO — Patrones Problematicos en Services

| Patron | Ejemplo | Frecuencia |
|--------|---------|------------|
| Try-catch generico retorna `Optional.empty()` | `ClienteService:54-57` — atrapa `Exception`, loguea, devuelve empty | En TODOS los servicios |
| Service construye DTOs (viola capas) | `ClienteService:43-53` — construye `ClienteDTO` manualmente | ~15 servicios |
| Builder de 35+ lineas duplicado en save/edit | `EstablecimientoService:155-183` vs `208-235` — IDENTICO | 5+ servicios |
| `@AllArgsConstructor` en vez de `@RequiredArgsConstructor` | `ClienteService:22` | Todos los servicios |
| `throws Exception` en firmas de metodo | `EstablecimientoService:70,86,117,153,206` | Generalizado |
| `EntityManager` inyectado pero no siempre usado | `EstablecimientoService:52-53` | 3+ servicios |

#### BAJO — Inconsistencias Menores

- `ApiResponseManager` no usa Lombok (getters/setters/equals manuales)
- jjwt dependencies con versiones inconsistentes (0.12.3, 0.12.5, 0.12.6)
- `spring-boot-starter-webflux` importado pero probablemente solo para `WebClient` (Supabase)
- `spring-dotenv` para variables de entorno (funcional pero no estandar)

---

## 2. DISENO DE LA NUEVA ARQUITECTURA

### 2.1 Estructura de Paquetes

```
com.lugares.api
├── config/                          # Configuracion Spring
│   ├── SecurityConfig.java          # Spring Security + JWT + CORS externalizado
│   ├── JwtAuthenticationFilter.java # Filtro JWT
│   ├── ApplicationConfig.java       # AuthProviders, PasswordEncoder
│   ├── ModelMapperConfig.java       # ModelMapper STRICT + custom mappings
│   ├── FirebaseConfig.java          # Firebase desde env vars
│   └── CorsProperties.java         # @ConfigurationProperties para CORS
│
├── exception/                       # Excepciones y manejo global
│   ├── GlobalExceptionHandler.java  # @RestControllerAdvice
│   ├── ApiError.java                # Estructura de respuesta de error estandarizada
│   ├── ResourceNotFoundException.java
│   ├── BusinessRuleException.java
│   ├── DuplicateResourceException.java
│   └── UnauthorizedActionException.java
│
├── common/                          # Utilidades compartidas
│   └── ApiResponse.java             # Wrapper generico de respuesta (reemplaza ApiResponseManager)
│
├── entity/                          # Entidades JPA (dominio)
│   ├── Cliente.java
│   ├── Usuario.java
│   ├── Establecimiento.java
│   ├── Etiqueta.java
│   ├── CategoriaEtiqueta.java
│   ├── Empresa.java
│   ├── Suscripcion.java
│   ├── TipoEstablecimiento.java
│   ├── Promocion.java
│   ├── PromocionDia.java
│   ├── Comentario.java
│   ├── Calificacion.java           # (ex EstrellaCliente)
│   ├── EtiquetaCliente.java        # Tabla pivote
│   ├── EtiquetaEstablecimiento.java # Tabla pivote
│   ├── EtiquetaTipoEstablecimiento.java # Tabla pivote
│   ├── Marca.java
│   ├── HistorialCanje.java
│   ├── CapsulaCultural.java
│   ├── Notificacion.java
│   ├── FcmToken.java
│   ├── PasswordResetToken.java
│   └── enums/
│       ├── TipoPromocion.java
│       └── AccionNotificacion.java
│
├── repository/                      # Spring Data JPA
│   ├── ClienteRepository.java
│   ├── UsuarioRepository.java
│   ├── EstablecimientoRepository.java
│   ├── EtiquetaRepository.java
│   ├── ... (1 interfaz por entidad, sin Impl)
│   └── projection/                  # Proyecciones para queries optimizadas
│       ├── EstablecimientoResumen.java
│       └── ClienteResumen.java
│
├── service/                         # Logica de negocio (trabaja con Entidades)
│   ├── ClienteService.java
│   ├── UsuarioService.java
│   ├── EstablecimientoService.java
│   ├── EtiquetaService.java
│   ├── PromocionService.java
│   ├── RecomendacionService.java   # Logica del SP de sugerencias
│   ├── CalificacionService.java
│   ├── ComentarioService.java
│   ├── NotificacionService.java
│   ├── AuthService.java            # Unifica LoginCliente + LoginUsuario
│   ├── JwtService.java
│   ├── EmailService.java
│   ├── StorageService.java         # Supabase storage
│   └── PasswordResetService.java
│
├── controller/                      # REST API (trabaja con DTOs)
│   ├── ClienteController.java
│   ├── UsuarioController.java
│   ├── EstablecimientoController.java
│   ├── EstablecimientoPublicController.java
│   ├── EtiquetaController.java
│   ├── PromocionController.java
│   ├── ComentarioController.java
│   ├── CalificacionController.java
│   ├── AuthController.java         # Unifica login cliente + usuario
│   ├── NotificacionController.java
│   ├── ContactoController.java
│   ├── SuscripcionController.java
│   ├── TipoEstablecimientoController.java
│   ├── EmpresaController.java
│   ├── MarcaController.java
│   ├── CapsulaCulturalController.java
│   └── HistorialCanjeController.java
│
└── dto/                             # DTOs (solo para capa controller)
    ├── request/                     # Input DTOs (con validaciones)
    │   ├── ClienteRequest.java
    │   ├── ClienteUpdateRequest.java
    │   ├── UsuarioRequest.java
    │   ├── EstablecimientoRequest.java
    │   ├── EtiquetaRequest.java
    │   ├── AuthRequest.java
    │   ├── FiltroEstablecimientoRequest.java
    │   ├── PromocionRequest.java
    │   ├── ContactoRequest.java
    │   ├── NotificacionRequest.java
    │   ├── FcmTokenRequest.java
    │   ├── ForgotPasswordRequest.java
    │   ├── ResetPasswordRequest.java
    │   └── ValidateCodeRequest.java
    │
    └── response/                    # Output DTOs (sin campos sensibles)
        ├── ClienteResponse.java     # SIN fldContrasenia
        ├── ClienteListResponse.java
        ├── UsuarioResponse.java     # SIN fldContrasenia
        ├── UsuarioListResponse.java
        ├── EstablecimientoResponse.java
        ├── EstablecimientoDetailResponse.java
        ├── EstablecimientoListResponse.java
        ├── EtiquetaResponse.java
        ├── EtiquetaAdminResponse.java
        ├── PromocionResponse.java
        ├── PromocionListResponse.java
        ├── ComentarioResponse.java
        ├── CalificacionResponse.java
        ├── LoginResponse.java
        └── ApiResponse.java         # Wrapper generico
```

### 2.2 Configuracion de ModelMapper (Modo STRICT)

```java
@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        
        // STRICT: campos deben coincidir exactamente en nombre y tipo
        mapper.getConfiguration()
            .setMatchingStrategy(MatchingStrategies.STRICT)
            .setFieldMatchingEnabled(true)
            .setFieldAccessLevel(AccessLevel.PRIVATE)
            .setSkipNullEnabled(true); // Para updates parciales
        
        return mapper;
    }
}
```

El mapeo se usa EXCLUSIVAMENTE en controllers:
```java
// En el controller — UNICA capa que toca DTOs
Entity entity = service.findById(id);                    // Service devuelve Entity
return modelMapper.map(entity, EntityResponse.class);    // Controller mapea a DTO
```

### 2.3 Estructura del GlobalExceptionHandler

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 404 — Recurso no encontrado
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex) { ... }

    // 409 — Conflicto (duplicado)
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiError> handleDuplicate(DuplicateResourceException ex) { ... }

    // 422 — Regla de negocio violada
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiError> handleBusinessRule(BusinessRuleException ex) { ... }

    // 400 — Validacion de campos (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) { ... }

    // 400 — Tipo de argumento incorrecto
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex) { ... }

    // 401 — No autenticado
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuth(AuthenticationException ex) { ... }

    // 403 — No autorizado
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex) { ... }

    // 409 — Conflicto de integridad (FK, unique)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex) { ... }

    // 500 — Fallback general
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneral(Exception ex) { ... }
}
```

Estructura de `ApiError`:
```java
public record ApiError(
    int status,
    String error,
    String message,
    LocalDateTime timestamp,
    String path,
    Map<String, String> fieldErrors  // Solo para errores de validacion
) { }
```

### 2.4 Reglas Inamovibles del Nuevo Proyecto

| Regla | Que significa en la practica |
|-------|-------------------------------|
| DI por constructor | `@RequiredArgsConstructor` + campos `private final`. Prohibido `@Autowired` |
| DTOs solo en controllers | Service recibe/devuelve Entidades. Controller hace `modelMapper.map()` |
| Cero try-catch en controllers | `GlobalExceptionHandler` maneja todo. Services lanzan excepciones tipadas |
| Cero Stored Procedures | Todo JPQL/Query Methods. Logica compleja en Service layer |
| Cero @Builder en services | `ModelMapper.map()` reemplaza builders. Para creates: constructor o factory method |
| Validacion con @Valid | DTOs request usan `@NotNull`, `@NotBlank`, `@Size`, `@Email`, `@Pattern` |
| Passwords NUNCA en responses | DTOs response NO tienen campo password. Punto. |
| CORS externalizado | `@ConfigurationProperties` lee de `application.yml` |
| Paginacion con Pageable | `Page<Entity>` de Spring Data, no paginacion manual por SP |

---

## 3. PLAN DE EJECUCION SECUENCIAL

### FASE 0 — Scaffolding del Proyecto Spring Boot

**Objetivo**: Proyecto Spring Boot 3 funcional, compilable, sin logica de negocio.

| # | Tarea | Entregable |
|---|-------|------------|
| 0.1 | Crear proyecto con Spring Initializr (Web, JPA, Security, Validation, Lombok, MySQL, DevTools) | `pom.xml` configurado |
| 0.2 | Definir estructura de paquetes base | Directorios vacios creados |
| 0.3 | Configurar `application.yml` (profiles: dev, prod) | Config externalizada con `${ENV_VARS}` |
| 0.4 | Configurar `ModelMapperConfig` en modo STRICT | Bean registrado |
| 0.5 | Configurar `.env.example` con todas las variables necesarias | Template de variables |

**Criterio de salida**: `mvn compile` exitoso con contexto Spring levantado.

---

### FASE 1 — Infraestructura Core: Excepciones y Respuesta Estandarizada

**Objetivo**: Sistema de excepciones completo que elimine la necesidad de try-catch en cualquier capa.

| # | Tarea | Entregable |
|---|-------|------------|
| 1.1 | Crear `ApiError` (record) | `exception/ApiError.java` |
| 1.2 | Crear `ApiResponse<T>` (wrapper generico de exito) | `common/ApiResponse.java` |
| 1.3 | Crear `ResourceNotFoundException` | `exception/ResourceNotFoundException.java` |
| 1.4 | Crear `BusinessRuleException` | `exception/BusinessRuleException.java` |
| 1.5 | Crear `DuplicateResourceException` | `exception/DuplicateResourceException.java` |
| 1.6 | Crear `UnauthorizedActionException` | `exception/UnauthorizedActionException.java` |
| 1.7 | Crear `GlobalExceptionHandler` completo (9 handlers) | `exception/GlobalExceptionHandler.java` |

**Criterio de salida**: Excepciones lanzadas desde cualquier punto se traducen automaticamente a respuestas HTTP correctas con formato `ApiError`.

---

### FASE 2 — Entidades JPA con Relaciones Correctas

**Objetivo**: Modelo de dominio completo con `@ManyToOne`/`@OneToMany` donde corresponde.

| # | Tarea | Notas |
|---|-------|-------|
| 2.1 | `Suscripcion` | Entidad base referenciada por Cliente, Establecimiento, Promocion |
| 2.2 | `Empresa` | Referenciada por Establecimiento |
| 2.3 | `CategoriaEtiqueta` | Referenciada por Etiqueta |
| 2.4 | `TipoEstablecimiento` | Referenciada por Establecimiento |
| 2.5 | `Etiqueta` | `@ManyToOne` a `CategoriaEtiqueta` |
| 2.6 | `Cliente` | `@ManyToOne` a `Suscripcion`. Implements `UserDetails` |
| 2.7 | `Usuario` | Implements `UserDetails` |
| 2.8 | `Establecimiento` | `@ManyToOne` a `Suscripcion`, `Empresa`, `TipoEstablecimiento` |
| 2.9 | `Promocion` + `PromocionDia` | `@ManyToOne` a `Establecimiento`, `Suscripcion` |
| 2.10 | `Comentario` | `@ManyToOne` a `Cliente` y `Establecimiento` |
| 2.11 | `Calificacion` | `@ManyToOne` a `Cliente` y `Establecimiento` |
| 2.12 | `EtiquetaCliente` (pivote) | `@ManyToOne` a `Cliente` y `Etiqueta` |
| 2.13 | `EtiquetaEstablecimiento` (pivote) | `@ManyToOne` a `Establecimiento` y `Etiqueta` |
| 2.14 | `EtiquetaTipoEstablecimiento` (pivote) | `@ManyToOne` a `TipoEstablecimiento` y `Etiqueta` |
| 2.15 | `Marca`, `CapsulaCultural`, `HistorialCanje` | Entidades simples |
| 2.16 | `Notificacion`, `FcmToken`, `PasswordResetToken` | Entidades de infraestructura |
| 2.17 | Enums: `TipoPromocion`, `AccionNotificacion` | En subpaquete `entity/enums/` |

**Decisiones clave para entidades**:
- `@Getter` + `@Setter` + `@NoArgsConstructor` + `@AllArgsConstructor` (NO `@Data` — evita `equals/hashCode` rotos)
- `@ManyToOne(fetch = FetchType.LAZY)` siempre (override a EAGER solo si se justifica)
- `@JoinColumn(name = "fk_column")` explicito
- `@Table(name = "tabla_legacy")` para mapear a tablas existentes sin renombrar

**Criterio de salida**: `ddl-auto=validate` pasa contra la BD legacy sin errores.

---

### FASE 3 — Repositorios JPA (Reemplazo de SPs)

**Objetivo**: Eliminar los 10 `*SpResponseImpl` y los 15 stored procedures.

| # | SP Reemplazado | Implementacion JPA | Complejidad |
|---|----------------|-------------------|-------------|
| 3.1 | `spGetclienteByid` | `ClienteRepository.findById()` (ya existe en JPA) | TRIVIAL |
| 3.2 | `spListEtiquetaByCliente` | `EtiquetaClienteRepository.findByCliente(Cliente)` con `@Query` JOIN FETCH | TRIVIAL |
| 3.3 | `spListEtiquetasByEstablecimiento` | `EtiquetaEstablecimientoRepository.findByEstablecimiento()` con `@Query` JOIN FETCH | TRIVIAL |
| 3.4 | `spListClientes` | `ClienteRepository.findByNombreContaining(String, Pageable)` | MODERADO |
| 3.5 | `spListUsuarios` | `UsuarioRepository.findByNombreContaining(String, Pageable)` | MODERADO |
| 3.6 | `spListPromociones` | `PromocionRepository.findByNombreContaining(String, Pageable)` con JOIN a Establecimiento | MODERADO |
| 3.7 | `spListEtiquetaAdmin` | `EtiquetaRepository.findAllAdmin(String, Pageable)` con `@Query` | MODERADO |
| 3.8 | `spListEtiquetaByIdTipoEstablecimiento` | `EtiquetaTipoEstablecimientoRepository.findByTipoId(Integer, Pageable)` | MODERADO |
| 3.9 | `spListComentarioClienteByIdEstablecimiento` | `ComentarioRepository.findByEstablecimiento(Establecimiento)` con JOIN FETCH a Cliente | MODERADO |
| 3.10 | `Sp_CreateOrUpdateCliente` | Eliminado — `ClienteRepository.save()` cubre INSERT/UPDATE nativamente | MODERADO |
| 3.11 | `spCreateOrUpdateUsuario` | Eliminado — `UsuarioRepository.save()` | MODERADO |
| 3.12 | `spCreateOrUpdateCalificacion` | `CalificacionRepository.findByClienteAndEstablecimiento()` + `save()` | MODERADO |
| 3.13 | `spListEstablecimientosFiltradosPoretiqueta` | `@Query` nativo con `FIND_IN_SET` o JPA Specification con predicados dinamicos AND/OR | COMPLEJO |
| 3.14 | `spGetEstablecimientoById` | `EstablecimientoRepository.findById()` + relaciones `@ManyToOne` + projection o service-layer composition | COMPLEJO |
| 3.15 | `spListEstablecimientosSugeridosClientesByEitiquetas` | `@Query` con subquery cruzando tablas pivote, o `RecomendacionService` con logica en Java | COMPLEJO |

**Criterio de salida**: Cero clases `*SpResponseImpl`. Cero llamadas a `EntityManager.createStoredProcedureQuery()`.

---

### FASE 4 — DTOs Request/Response con Validaciones

**Objetivo**: Separacion estricta input/output. Passwords NUNCA en responses. Validaciones completas.

| # | Tarea | Notas |
|---|-------|-------|
| 4.1 | `AuthRequest` | `@NotBlank email`, `@NotBlank password` |
| 4.2 | `ClienteRequest` / `ClienteUpdateRequest` | Create vs Update separados. Password solo en create |
| 4.3 | `ClienteResponse` / `ClienteListResponse` | SIN campo password. Diferentes niveles de detalle |
| 4.4 | `UsuarioRequest` | `@Email`, `@NotBlank`, `@Size` |
| 4.5 | `UsuarioResponse` / `UsuarioListResponse` | SIN campo password |
| 4.6 | `EstablecimientoRequest` | Validaciones para los 48 campos (muchos `@Size`) |
| 4.7 | `EstablecimientoResponse` / `EstablecimientoDetailResponse` / `EstablecimientoListResponse` | 3 niveles de detalle |
| 4.8 | `EtiquetaRequest` / `EtiquetaResponse` / `EtiquetaAdminResponse` | Admin vs public |
| 4.9 | `PromocionRequest` / `PromocionResponse` / `PromocionListResponse` | Con validacion de fechas |
| 4.10 | `ComentarioRequest` / `ComentarioResponse` | `@Size` en texto |
| 4.11 | `FiltroEstablecimientoRequest` | Lista de IDs de etiquetas + boolean `busquedaEstricta` |
| 4.12 | DTOs de infraestructura: Login, Contacto, Notificacion, FcmToken, Password reset | Migracion directa con validaciones agregadas |

**Criterio de salida**: Ningun DTO tiene campo `password`/`contrasenia` en paquete `response/`. Todos los requests tienen `@Valid` annotations.

---

### FASE 5 — Servicios (Logica de Negocio Pura)

**Objetivo**: Servicios que trabajan SOLO con entidades. Cero DTOs. Cero try-catch genericos. Excepciones tipadas.

| # | Servicio | Logica clave a migrar |
|---|----------|----------------------|
| 5.1 | `AuthService` | Unifica `LoginClienteService` + `LoginUsuarioService`. JWT generation |
| 5.2 | `JwtService` | Migrar tal cual (funcional en legacy) |
| 5.3 | `ClienteService` | CRUD limpio. `findById` lanza `ResourceNotFoundException` en vez de Optional.empty() |
| 5.4 | `UsuarioService` | CRUD limpio. Misma limpieza que Cliente |
| 5.5 | `EstablecimientoService` | CRUD + composicion de detalle (reemplaza SP de 40 columnas) |
| 5.6 | `EtiquetaService` | CRUD + asignaciones (cliente, establecimiento, tipo) |
| 5.7 | `PromocionService` | CRUD con validacion de fechas |
| 5.8 | `RecomendacionService` | **NUEVO**: Logica del SP `spListEstablecimientosSugeridosClientesByEitiquetas` migrada a Java |
| 5.9 | `CalificacionService` | Upsert (create or update) usando repository |
| 5.10 | `ComentarioService` | CRUD simple |
| 5.11 | `NotificacionService` | Firebase push notifications |
| 5.12 | `EmailService` | Migrar tal cual (funcional en legacy) |
| 5.13 | `StorageService` | Supabase file upload (migrar tal cual) |
| 5.14 | `PasswordResetService` | Unifica `ForgotPasswordService` + `ResetPasswordService` + `ValidateCodeService` |
| 5.15 | Servicios CRUD simples | `SuscripcionService`, `EmpresaService`, `MarcaService`, `CapsulaCulturalService`, `TipoEstablecimientoService`, `HistorialCanjeService`, `CategoriaEtiquetaService` |

**Patron de los servicios**:
```java
@Service
@RequiredArgsConstructor
public class ClienteService {
    private final ClienteRepository clienteRepository;

    public Cliente findById(Integer id) {
        return clienteRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", id));
    }
    // Sin try-catch. Sin DTOs. Sin builders.
}
```

**Criterio de salida**: Cero imports de `dto.*` en paquete `service/`. Cero bloques try-catch que atrapen `Exception` generico.

---

### FASE 6 — Controladores y Mapeo DTO

**Objetivo**: Controllers delgados. Mapeo DTO exclusivamente aqui. `@Valid` en requests.

| # | Controller | Endpoints clave |
|---|------------|----------------|
| 6.1 | `AuthController` | `POST /auth/cliente/login`, `POST /auth/usuario/login`, `POST /auth/cliente/register` |
| 6.2 | `ClienteController` | CRUD completo con `Pageable` para listado |
| 6.3 | `UsuarioController` | CRUD completo con `Pageable` |
| 6.4 | `EstablecimientoController` (autenticado) | CRUD + filtro por etiquetas + sugeridos |
| 6.5 | `EstablecimientoPublicController` | Endpoints publicos de consulta |
| 6.6 | `EtiquetaController` | CRUD + asignaciones a cliente/establecimiento/tipo |
| 6.7 | `PromocionController` | CRUD con dias disponibles |
| 6.8 | `ComentarioController` + `ComentarioPublicController` | CRUD + listado publico por establecimiento |
| 6.9 | `CalificacionController` | Upsert calificacion |
| 6.10 | `NotificacionController` + `FcmTokenController` | Push notifications + token registration |
| 6.11 | `ContactoController` | Formulario de contacto publico |
| 6.12 | Controllers CRUD simples | Suscripcion, Empresa, Marca, CapsulaCultural, TipoEstablecimiento, HistorialCanje |
| 6.13 | `PasswordResetController` | forgot-password, validate-code, reset-password |

**Patron de los controllers**:
```java
@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {
    private final ClienteService clienteService;
    private final ModelMapper modelMapper;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClienteResponse>> getById(@PathVariable Integer id) {
        Cliente cliente = clienteService.findById(id);  // Lanza ResourceNotFoundException si no existe
        ClienteResponse dto = modelMapper.map(cliente, ClienteResponse.class);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ClienteResponse>> create(@Valid @RequestBody ClienteRequest request) {
        Cliente entity = modelMapper.map(request, Cliente.class);
        Cliente saved = clienteService.create(entity);
        ClienteResponse dto = modelMapper.map(saved, ClienteResponse.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(dto));
    }
    // Cero try-catch. Cero logica de negocio. Solo mapeo y delegacion.
}
```

**Criterio de salida**: Cero `try-catch` en controllers. Cero logica de negocio. Todo el mapeo DTO sucede exclusivamente aqui.

---

### FASE 7 — Seguridad y Configuracion

**Objetivo**: Spring Security configurado, JWT funcional, CORS externalizado.

| # | Tarea | Notas |
|---|-------|-------|
| 7.1 | `SecurityConfig` | Mismas rutas publicas que legacy + CORS desde properties |
| 7.2 | `JwtAuthenticationFilter` | Migrar con roles estandarizados (`ROLE_CLIENTE`, `ROLE_USUARIO`) |
| 7.3 | `ApplicationConfig` | Dos AuthenticationProviders + BCrypt |
| 7.4 | `CorsProperties` | `@ConfigurationProperties(prefix = "app.cors")` — origenes desde `application.yml` |
| 7.5 | `FirebaseConfig` | Credenciales desde variable de entorno (no classpath) |
| 7.6 | Swagger/OpenAPI config | Restringir a profile `dev` unicamente |

**Criterio de salida**: Misma matriz de seguridad que legacy. CORS configurable sin recompilar. Firebase sin archivo en classpath.

---

### FASE 8 — Verificacion y Limpieza

| # | Tarea |
|---|-------|
| 8.1 | Verificar `ddl-auto=validate` contra BD legacy |
| 8.2 | Smoke test de cada endpoint con Swagger UI |
| 8.3 | Verificar que NINGUN response contiene passwords |
| 8.4 | Verificar paginacion con `Pageable` |
| 8.5 | Verificar los 3 SPs complejos migrados (filtrado, detalle, sugerencias) |
| 8.6 | Eliminar cualquier codigo muerto |
| 8.7 | Revisar logs — cero `System.out.println` |

---

## 4. RESUMEN DE DEPENDENCIAS DEL NUEVO POM.XML

```xml
<!-- Core -->
spring-boot-starter-web
spring-boot-starter-data-jpa
spring-boot-starter-validation
spring-boot-starter-security
spring-boot-starter-mail

<!-- Database -->
mysql-connector-j (runtime)

<!-- JWT -->
io.jsonwebtoken:jjwt-api:0.12.6
io.jsonwebtoken:jjwt-impl:0.12.6 (runtime)
io.jsonwebtoken:jjwt-jackson:0.12.6 (runtime)

<!-- Mapping -->
org.modelmapper:modelmapper:3.2.6

<!-- Firebase -->
com.google.firebase:firebase-admin:9.2.0

<!-- HTTP Client (Supabase) -->
spring-boot-starter-webflux

<!-- Developer -->
lombok (provided)
spring-boot-devtools (optional)
spring-boot-starter-test (test)

<!-- API Docs -->
springdoc-openapi-starter-webmvc-ui:2.8.x

<!-- Env vars -->
me.paulschwarz:spring-dotenv:4.0.0
```

> Nota: Versiones de jjwt ALINEADAS a 0.12.6 (legacy tenia 0.12.3/0.12.5/0.12.6 mezclados)

---

## 5. ORDEN DE EJECUCION Y DEPENDENCIAS

```
FASE 0 (Scaffolding)
  └── FASE 1 (Excepciones + ApiResponse)
        └── FASE 2 (Entidades JPA)
              └── FASE 3 (Repositorios — reemplazo de SPs)
              └── FASE 4 (DTOs Request/Response)
                    └── FASE 5 (Servicios)
                          └── FASE 6 (Controladores)
                                └── FASE 7 (Seguridad)
                                      └── FASE 8 (Verificacion)
```

Fases 3 y 4 son paralelizables (no dependen entre si).
Todas las demas son estrictamente secuenciales.

---

**Esperando aprobacion para iniciar Fase 0.**
