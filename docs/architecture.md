# Architecture

## 1. Layered Architecture

Each layer has a single responsibility. No layer skips another.

```
┌────────────────────────────────────────────────┐
│                  Controller                    │  REST, HTTP, @Valid, @AuthenticationPrincipal
│              (thin — no business logic)        │
└──────────────────────┬─────────────────────────┘
                       │ calls
┌──────────────────────▼─────────────────────────┐
│                   Service                      │  Business logic, @Transactional
│           (owns the business rules)            │
└──────────────────────┬─────────────────────────┘
                       │ calls
┌──────────────────────▼─────────────────────────┐
│                 Repository                     │  Spring Data JPA interfaces
│           (data access — no logic)             │
└──────────────────────┬─────────────────────────┘
                       │ maps to/from
┌──────────────────────▼─────────────────────────┐
│                   Entity                       │  JPA @Entity, Hibernate
│           (database schema representation)     │
└────────────────────────────────────────────────┘
```

Controllers receive DTOs (request/response) and never expose entities directly. Mappers handle the translation between layers.

---

## 2. JWT Authentication Flow

```
Client                       API
  │                           │
  │  POST /auth/cliente/login │
  │  { correo, contrasenia }  │
  │──────────────────────────▶│
  │                           │──▶ AuthService.loginCliente()
  │                           │       └─▶ validates credentials
  │                           │──▶ JwtService.generateToken(cliente, "ROLE_CLIENTE")
  │                           │       └─▶ signs JWT (HS256, secret from env)
  │◀──────────────────────────│
  │  { token, expirationTime }│
  │                           │
  │  GET /api/clientes/42     │
  │  Authorization: Bearer ... │
  │──────────────────────────▶│
  │                           │──▶ JwtAuthenticationFilter
  │                           │       ├─▶ extracts token from header
  │                           │       ├─▶ JwtService.validateToken()
  │                           │       └─▶ sets UsernamePasswordAuthenticationToken
  │                           │              in SecurityContextHolder
  │                           │──▶ SecurityConfig evaluates URL rules
  │                           │──▶ @PreAuthorize evaluates method rules (if any)
  │                           │──▶ Controller method executes
  │◀──────────────────────────│
  │  200 { data: {...} }       │
```

The JWT payload contains: `sub` (entity id as string), `role` (e.g. `ROLE_CLIENTE`), `iat`, `exp`.

---

## 3. Role Model

The API has exactly two roles. There is no super-admin or hierarchical role system.

| Role | Who | What they can do |
|---|---|---|
| `ROLE_CLIENTE` | End users of the mobile app | Manage their own profile, comments, canje history, FCM tokens, calificaciones, etiquetas |
| `ROLE_USUARIO` | System administrators | Full CRUD on content (establecimientos, marcas, empresas, etiquetas, promociones, etc.) + send push notifications |

**Why no super-admin?** USUARIO is the only admin role. A super-admin tier would add complexity with no clear benefit at this scale. If a USUARIO account is locked or compromised, recovery is handled at the database level, not via a higher role.

**Why USUARIO cannot access CLIENTE-only endpoints?** Some endpoints (calificaciones, fcm-tokens) are semantically tied to the mobile app experience. A USUARIO admin would need a separate CLIENTE account to test those flows, which is intentional.

---

## 4. Authorization Enforcement

Authorization uses two independent layers. Both must pass.

### Layer 1 — URL-level (SecurityConfig)

Fast, coarse-grained. Evaluated before the request reaches the controller. Configured in `SecurityConfig.securityFilterChain()`.

Use when: the role alone determines access (e.g. "only USUARIO can POST to /api/marcas").

### Layer 2 — Method-level (@PreAuthorize)

Fine-grained. Evaluated inside the method invocation pipeline. Uses Spring SpEL expressions.

Use when: access depends on the relationship between the caller and the specific resource (ownership), not just the role.

**Examples in this codebase:**

```java
// CLIENTE solo puede ver su propio perfil; USUARIO puede ver cualquiera
@PreAuthorize("hasRole('USUARIO') or (hasRole('CLIENTE') and #id == authentication.principal.id)")

// CLIENTE solo puede borrar su propio comentario (verificado via service)
@PreAuthorize("hasRole('USUARIO') or (hasRole('CLIENTE') and @comentarioService.isOwner(#id, authentication.principal.id))")
```

`isOwner()` methods are `@Transactional(readOnly = true)` on the service. They load the resource and compare `cliente.id` with the principal id. They return `false` on `null` (resource not found) — the service layer will throw `ResourceNotFoundException` when the actual delete is attempted.

---

## 5. Mapper Pattern

Controllers never expose JPA entities. All input/output goes through DTOs.

**Structure:**
- `dto/request/` — input DTOs, annotated with Bean Validation (`@NotBlank`, `@Email`, etc.)
- `dto/response/` — output DTOs, projections of entity data; some resources have multiple (e.g. `EstablecimientoResponse` vs `EstablecimientoDetailResponse` vs `EstablecimientoListResponse`)
- `mapper/` — one mapper class per resource (e.g. `ClienteMapper`, `EstablecimientoMapper`)

**Why per-resource mappers instead of a single global ModelMapper?**
- Strict schema control: `toDto()` only maps what we want to expose, not the full entity graph.
- Navigated fields: some response DTOs include nested data from related entities (e.g. establecimiento name in a calificacion response). A global ModelMapper would require complex configuration or silently include unwanted data.
- Testability: each mapper is a plain Spring component and can be unit-tested independently.

ModelMapper is used as a base and hand-tweaked per mapper as needed.

---

## 6. Error Handling

Zero try-catch in controllers or services.

**Components:**
- `GlobalExceptionHandler` (`@RestControllerAdvice`) — catches all exceptions and returns a structured `ApiResponse` with the appropriate HTTP status.
- Typed exceptions in `exception/`:
  - `ResourceNotFoundException` → 404
  - `DuplicateResourceException` → 409
  - Other domain-specific exceptions as needed
- `ApiResponse<T>` — the standard envelope for all responses:
  ```json
  { "status": "success", "message": "...", "data": { ... } }
  ```
  or for errors:
  ```json
  { "status": "error", "message": "Resource not found", "data": null }
  ```

Services throw typed exceptions. The handler catches them and maps to HTTP. This keeps business logic out of HTTP concerns and makes the exception flow explicit.

---

## 7. SpringDoc Integration

Swagger UI is available at `http://localhost:8080/swagger-ui.html` in the `dev` profile. Disabled in `prod` via `application-prod.yml`.

**Convention for annotations:**
- `@Tag(name, description)` — at class level, immediately above `@RestController`. Groups endpoints in Swagger UI.
- `@Operation(summary, description)` — on every public endpoint method. `description` states who can call it.
- No `@ApiResponse` on methods — global 401/403/500 responses are injected by `OpenApiGlobalResponsesCustomizer`.

**`OpenApiGlobalResponsesCustomizer`** (`config/OpenApiGlobalResponsesCustomizer.java`):
- Implements `OpenApiCustomizer` as a `@Bean`.
- No `@Profile` annotation — runs in all profiles so `/v3/api-docs` is accurate even if enabled in prod.
- Adds 401, 403, and 500 responses to every operation via `addApiResponse()` (merge — does not overwrite existing per-method responses).

**`SwaggerConfig`** (`config/SwaggerConfig.java`):
- `@Profile("dev")` — only active in dev.
- Configures the OpenAPI info (title, version, description) and the Bearer JWT security scheme.

---

## 8. Test Strategy

All tests use `@WebMvcTest` (MockMvc slice). No database, no real Spring context — just the controller layer with mocked services.

**Key components:**
- `BaseControllerTest` — base class for all controller tests. Provides:
  - `asCliente()` — sets up MockMvc with a CLIENTE principal
  - `asClienteWithId(int id)` — CLIENTE principal with specific id (for ownership tests)
  - `asUsuario()` — USUARIO principal
  - `asUsuarioWithId(int id)` — USUARIO principal with specific id
- Each controller has its own `*ControllerTest` class with `@WebMvcTest(TheController.class)`.
- Services are mocked with `@MockBean`.

**What is tested:**
- Happy path: correct response body and HTTP status.
- Auth boundaries: 401 without token, 403 with wrong role or wrong owner.
- Ownership checks: forbidden when not owner, allowed when owner.

**Coverage:** 218 tests across 19 controllers.

**What is NOT tested here:**
- Service logic (covered by unit tests if present).
- Database interactions (use integration tests with `@SpringBootTest` + TestContainers for that).
