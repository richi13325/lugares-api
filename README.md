# 300 Lugares API

API REST para la plataforma 300 Lugares — catálogo de establecimientos, promociones, etiquetas y comentarios de clientes.

## Stack

| Capa | Tecnología |
|---|---|
| Runtime | Java 21 |
| Framework | Spring Boot 3.5.3 |
| Persistencia | Spring Data JPA + PostgreSQL (driver `org.postgresql`) |
| Seguridad | Spring Security + JJWT 0.12.6 (dual-rol: CLIENTE / USUARIO) |
| Docs | SpringDoc OpenAPI 2.8.6 (Swagger UI) |
| Mapping | ModelMapper 3.2.6 + mappers per-recurso |
| Utilidades | Lombok, Spring Validation, Spring Mail |
| Push | Firebase Admin 9.2.0 (FCM) |
| Storage | Supabase (via Spring WebFlux `WebClient`) |
| Config | spring-dotenv 4.0.0 (lectura de `.env`) |
| Build | Maven Wrapper (`./mvnw`) |

## Quick start

### Requisitos

- Java 21
- Maven Wrapper incluido (`./mvnw`)
- PostgreSQL accesible (ver `application.yml` para la URL esperada)

### Variables de entorno

El archivo `application.yml` lee las siguientes variables de entorno (vía `.env` o variables del sistema):

| Variable | Descripción |
|---|---|
| `DB_URL` | JDBC URL de la base de datos PostgreSQL |
| `DB_USERNAME` | Usuario de la base de datos |
| `DB_PASSWORD` | Contraseña de la base de datos |
| `JWT_SECRET_KEY` | Clave secreta para firmar y verificar los JWT |
| `JWT_EXPIRATION_TIME` | Tiempo de expiración del JWT en milisegundos |
| `MAIL_USERNAME` | Usuario del servidor SMTP |
| `MAIL_PASSWORD` | Contraseña del servidor SMTP |
| `MAIL_CONTACT` | Dirección de email destino para formulario de contacto |
| `CORS_ALLOWED_ORIGINS` | Orígenes permitidos para CORS (default: `http://localhost:4200`) |
| `SUPABASE_URL` | URL base del proyecto Supabase |
| `SUPABASE_KEY` | API key de Supabase |
| `SUPABASE_BUCKET` | Nombre del bucket de almacenamiento en Supabase |
| `SPRING_PROFILES_ACTIVE` | Perfil activo: `dev` o `prod` (default: `dev`) |

> El servidor SMTP está configurado en `mail.300lugares.com:465` con SSL. Solo `MAIL_USERNAME` y `MAIL_PASSWORD` son variables — el host y el puerto están en `application.yml`.

### Correr

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

El perfil por defecto es `dev`. Swagger UI disponible en `http://localhost:8080/swagger-ui.html`.

## Perfiles

| Perfil | Swagger UI | SQL logs | Descripción |
|---|---|---|---|
| `dev` | Habilitado | Verbose (`format_sql: true`) | Desarrollo local |
| `prod` | Deshabilitado | Off | Producción — Swagger y api-docs desactivados |

Archivos de configuración:
- `src/main/resources/application.yml` — configuración base y variables de entorno
- `src/main/resources/application-dev.yml` — overrides para dev
- `src/main/resources/application-prod.yml` — overrides para prod

## Autenticación

Ver [docs/auth-matrix.md](docs/auth-matrix.md) para la tabla completa de endpoints con sus reglas de acceso.

**Roles:**
- `CLIENTE`: usuarios finales de la app móvil. Pueden gestionar su propio perfil, comentarios y canjes.
- `USUARIO`: administradores del sistema. Acceden a todos los endpoints de gestión de contenido.

**Flujo JWT:**
1. POST `/auth/cliente/login` o `/auth/usuario/login` → recibe JWT en respuesta.
2. Incluir el token en el header `Authorization: Bearer <token>` en cada request subsiguiente.
3. `JwtAuthenticationFilter` valida el token e inyecta el principal en el `SecurityContext`.

Ver [docs/architecture.md](docs/architecture.md) para el diagrama completo del flujo.

## Testing

```bash
./mvnw test
```

218 tests MockMvc cubriendo los 19 controllers. Los tests usan `@WebMvcTest` (slice — sin base de datos), con helpers `asClienteWithId(int)` y `asUsuarioWithId(int)` en `BaseControllerTest`.

## Estructura del proyecto

```
src/main/java/com/lugares/api/
├── common/         # ApiResponse envelope (éxito, error, paginación)
├── config/         # SecurityConfig, SwaggerConfig, OpenApiGlobalResponsesCustomizer, CorsProperties, JwtAuthenticationFilter
├── controller/     # 19 REST controllers (thin — sin business logic)
├── dto/
│   ├── request/    # DTOs de entrada (validados con Bean Validation)
│   └── response/   # DTOs de salida (proyecciones controladas)
├── entity/         # 22 JPA entities
├── exception/      # Excepciones tipadas + GlobalExceptionHandler
├── mapper/         # ModelMapper per-recurso (13 mappers)
├── repository/     # 17 JPA repositories (Spring Data)
├── security/       # JwtAuthenticationFilter, JwtService
└── service/        # 21 services (lógica de negocio)
```

## Documentación adicional

- [docs/auth-matrix.md](docs/auth-matrix.md) — matriz endpoint → rol → ownership check
- [docs/architecture.md](docs/architecture.md) — capas, JWT flow, mappers, error handling, strategy de tests
