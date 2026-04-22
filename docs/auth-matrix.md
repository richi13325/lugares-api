# Authorization Matrix

La API aplica autorización en dos capas independientes:

1. **URL-level (SecurityConfig)** — La primera barrera. Evalúa el rol del JWT antes de que el request llegue al controller. Cubre la mayoría de los casos donde el rol basta para decidir.
2. **Method-level (@PreAuthorize)** — Segunda barrera, solo donde el rol no alcanza. Se usa para *ownership checks* (ej. un CLIENTE solo puede borrar su propio comentario) o para reglas que dependen de datos de la request (ej. `#id == authentication.principal.id`).

Cuando un endpoint tiene ambas capas, **ambas deben pasar** para que el request sea aceptado.

**Leyenda:**
- `✓` — acceso permitido
- `✗` — acceso denegado (401 sin token, 403 con token de rol incorrecto)
- `self` — solo si el id del request coincide con el id del principal autenticado
- `owner` — solo si es dueño del recurso (verificado via servicio `isOwner()`)
- `self/any` — USUARIO puede acceder a cualquiera; CLIENTE solo al propio

---

## AuthController (`/auth/**`)

| Method | Path | Anonymous | CLIENTE | USUARIO | Ownership check | Source |
|---|---|---|---|---|---|---|
| POST | `/auth/cliente/login` | ✓ | ✓ | ✓ | — | URL |
| POST | `/auth/usuario/login` | ✓ | ✓ | ✓ | — | URL |
| POST | `/auth/cliente/register` | ✓ | ✓ | ✓ | — | URL |

## PasswordResetController (`/auth/password/**`)

| Method | Path | Anonymous | CLIENTE | USUARIO | Ownership check | Source |
|---|---|---|---|---|---|---|
| POST | `/auth/password/forgot` | ✓ | ✓ | ✓ | — | URL |
| POST | `/auth/password/validate-code` | ✓ | ✓ | ✓ | — | URL |
| POST | `/auth/password/reset` | ✓ | ✓ | ✓ | — | URL |

## ContactoController (`/api/contacto/**`)

| Method | Path | Anonymous | CLIENTE | USUARIO | Ownership check | Source |
|---|---|---|---|---|---|---|
| POST | `/api/contacto` | ✓ | ✓ | ✓ | — | URL |

## ClienteController (`/api/clientes/**`)

| Method | Path | Anonymous | CLIENTE | USUARIO | Ownership check | Source |
|---|---|---|---|---|---|---|
| GET | `/api/clientes` | ✗ | ✓ | ✓ | — | URL (`anyRequest().authenticated()`) |
| GET | `/api/clientes/{id}` | ✗ | self | ✓ | `#id == principal.id` | Both |
| PUT | `/api/clientes/{id}` | ✗ | self | ✗ | `#id == principal.id` | Both |
| DELETE | `/api/clientes/{id}` | ✗ | self | ✓ | `#id == principal.id` | Both |

> `GET /api/clientes` cae al bloque `anyRequest().authenticated()` — cualquier rol puede listar, sin ownership.

## UsuarioController (`/api/usuarios/**`)

| Method | Path | Anonymous | CLIENTE | USUARIO | Ownership check | Source |
|---|---|---|---|---|---|---|
| GET | `/api/usuarios` | ✗ | ✗ | ✓ | — | URL |
| GET | `/api/usuarios/{id}` | ✗ | ✗ | ✓ | — | URL |
| POST | `/api/usuarios` | ✗ | ✗ | ✓ | — | URL |
| PUT | `/api/usuarios/{id}` | ✗ | ✗ | ✓ | Explícita `hasRole('USUARIO')` | Both |
| DELETE | `/api/usuarios/{id}` | ✗ | ✗ | ✓ | Explícita `hasRole('USUARIO')` | Both |

> `PUT` y `DELETE` tienen `@PreAuthorize("hasRole('USUARIO')")` explícito aunque la URL ya lo cubre. Esto documenta la intención y previene regresiones si la URL rule cambia.

## ComentarioController (`/api/comentarios/**`)

| Method | Path | Anonymous | CLIENTE | USUARIO | Ownership check | Source |
|---|---|---|---|---|---|---|
| GET | `/api/comentarios/establecimiento/{id}` | ✗ | ✓ | ✓ | — | URL (`GET /api/establecimientos/**` → `authenticated()`) |
| POST | `/api/comentarios` | ✗ | ✓ | ✗ | — | URL |
| DELETE | `/api/comentarios/{id}` | ✗ | owner | ✓ | `@comentarioService.isOwner(#id, principal.id)` | Both |

> `DELETE /api/comentarios/**` → `authenticated()` en SecurityConfig (relajado intencionalmente para permitir que `@PreAuthorize` arbitre ownership de CLIENTE).

## HistorialCanjeController (`/api/historial-canjes/**`)

| Method | Path | Anonymous | CLIENTE | USUARIO | Ownership check | Source |
|---|---|---|---|---|---|---|
| GET | `/api/historial-canjes/cliente/{clienteId}` | ✗ | self | ✓ | `#clienteId == principal.id` | @PreAuthorize |
| GET | `/api/historial-canjes/promocion/{promocionId}` | ✗ | ✓ | ✓ | — | URL (`anyRequest().authenticated()`) |
| POST | `/api/historial-canjes` | ✗ | ✓ | ✗ | — | @PreAuthorize |
| DELETE | `/api/historial-canjes/{id}` | ✗ | owner | ✓ | `@historialCanjeService.isOwner(#id, principal.id)` | @PreAuthorize |

## EstablecimientoController (`/api/establecimientos/**`)

| Method | Path | Anonymous | CLIENTE | USUARIO | Ownership check | Source |
|---|---|---|---|---|---|---|
| GET | `/api/establecimientos` | ✗ | ✓ | ✓ | — | URL |
| GET | `/api/establecimientos/{id}` | ✗ | ✓ | ✓ | — | URL |
| GET | `/api/establecimientos/tipo/{tipoId}` | ✗ | ✓ | ✓ | — | URL |
| POST | `/api/establecimientos/filtro` | ✗ | ✓ | ✓ | — | URL |
| GET | `/api/establecimientos/sugeridos/{clienteId}` | ✗ | self | ✗ | `#clienteId == principal.id` | Both |
| POST | `/api/establecimientos` | ✗ | ✗ | ✓ | — | URL |
| PUT | `/api/establecimientos/{id}` | ✗ | ✗ | ✓ | — | URL |
| DELETE | `/api/establecimientos/{id}` | ✗ | ✗ | ✓ | — | URL |

## EtiquetaController (`/api/etiquetas/**`)

| Method | Path | Anonymous | CLIENTE | USUARIO | Ownership check | Source |
|---|---|---|---|---|---|---|
| GET | `/api/etiquetas/{id}` | ✗ | ✓ | ✓ | — | URL |
| GET | `/api/etiquetas/visibles` | ✗ | ✓ | ✓ | — | URL |
| GET | `/api/etiquetas/admin` | ✗ | ✗ | ✓ | — | URL |
| GET | `/api/etiquetas/establecimiento/{id}` | ✗ | ✓ | ✓ | — | URL |
| GET | `/api/etiquetas/tipo-establecimiento/{tipoId}` | ✗ | ✓ | ✓ | — | URL |
| POST | `/api/etiquetas` | ✗ | ✗ | ✓ | — | URL |
| PUT | `/api/etiquetas/{id}` | ✗ | ✗ | ✓ | — | URL |
| DELETE | `/api/etiquetas/{id}` | ✗ | ✗ | ✓ | — | URL |
| GET | `/api/etiquetas/cliente/{clienteId}` | ✗ | self | ✗ | `#clienteId == principal.id` | @PreAuthorize |
| POST | `/api/etiquetas/cliente/{clienteId}/{etiquetaId}` | ✗ | self | ✗ | `#clienteId == principal.id` | @PreAuthorize |
| DELETE | `/api/etiquetas/cliente/{clienteId}/{etiquetaId}` | ✗ | self | ✗ | `#clienteId == principal.id` | @PreAuthorize |
| POST | `/api/etiquetas/establecimiento/{establecimientoId}/{etiquetaId}` | ✗ | ✗ | ✓ | — | @PreAuthorize |
| DELETE | `/api/etiquetas/establecimiento/{establecimientoId}/{etiquetaId}` | ✗ | ✗ | ✓ | — | @PreAuthorize |

## PromocionController (`/api/promociones/**`)

| Method | Path | Anonymous | CLIENTE | USUARIO | Ownership check | Source |
|---|---|---|---|---|---|---|
| GET | `/api/promociones` | ✗ | ✓ | ✓ | — | URL |
| GET | `/api/promociones/{id}` | ✗ | ✓ | ✓ | — | URL |
| GET | `/api/promociones/establecimiento/{id}` | ✗ | ✓ | ✓ | — | URL |
| POST | `/api/promociones` | ✗ | ✗ | ✓ | — | URL |
| PUT | `/api/promociones/{id}` | ✗ | ✗ | ✓ | — | URL |
| DELETE | `/api/promociones/{id}` | ✗ | ✗ | ✓ | — | URL |

## CategoriaEtiquetaController (`/api/categorias-etiqueta/**`)

| Method | Path | Anonymous | CLIENTE | USUARIO | Ownership check | Source |
|---|---|---|---|---|---|---|
| GET | `/api/categorias-etiqueta` | ✗ | ✓ | ✓ | — | URL |
| GET | `/api/categorias-etiqueta/{id}` | ✗ | ✓ | ✓ | — | URL |
| POST | `/api/categorias-etiqueta` | ✗ | ✗ | ✓ | — | URL |
| PUT | `/api/categorias-etiqueta/{id}` | ✗ | ✗ | ✓ | — | URL |
| DELETE | `/api/categorias-etiqueta/{id}` | ✗ | ✗ | ✓ | — | URL |

## TipoEstablecimientoController (`/api/tipos-establecimiento/**`)

| Method | Path | Anonymous | CLIENTE | USUARIO | Ownership check | Source |
|---|---|---|---|---|---|---|
| GET | `/api/tipos-establecimiento` | ✗ | ✓ | ✓ | — | URL |
| GET | `/api/tipos-establecimiento/{id}` | ✗ | ✓ | ✓ | — | URL |
| POST | `/api/tipos-establecimiento` | ✗ | ✗ | ✓ | — | URL |
| PUT | `/api/tipos-establecimiento/{id}` | ✗ | ✗ | ✓ | — | URL |
| DELETE | `/api/tipos-establecimiento/{id}` | ✗ | ✗ | ✓ | — | URL |

## CapsulaCulturalController (`/api/capsulas-culturales/**`)

| Method | Path | Anonymous | CLIENTE | USUARIO | Ownership check | Source |
|---|---|---|---|---|---|---|
| GET | `/api/capsulas-culturales` | ✗ | ✓ | ✓ | — | URL |
| GET | `/api/capsulas-culturales/{id}` | ✗ | ✓ | ✓ | — | URL |
| POST | `/api/capsulas-culturales` | ✗ | ✗ | ✓ | — | URL |
| PUT | `/api/capsulas-culturales/{id}` | ✗ | ✗ | ✓ | — | URL |
| DELETE | `/api/capsulas-culturales/{id}` | ✗ | ✗ | ✓ | — | URL |

## MarcaController (`/api/marcas/**`)

| Method | Path | Anonymous | CLIENTE | USUARIO | Ownership check | Source |
|---|---|---|---|---|---|---|
| GET | `/api/marcas` | ✗ | ✗ | ✓ | — | URL |
| GET | `/api/marcas/{id}` | ✗ | ✗ | ✓ | — | URL |
| POST | `/api/marcas` | ✗ | ✗ | ✓ | — | URL |
| PUT | `/api/marcas/{id}` | ✗ | ✗ | ✓ | — | URL |
| DELETE | `/api/marcas/{id}` | ✗ | ✗ | ✓ | — | URL |

## EmpresaController (`/api/empresas/**`)

| Method | Path | Anonymous | CLIENTE | USUARIO | Ownership check | Source |
|---|---|---|---|---|---|---|
| GET | `/api/empresas` | ✗ | ✗ | ✓ | — | URL |
| GET | `/api/empresas/{id}` | ✗ | ✗ | ✓ | — | URL |
| POST | `/api/empresas` | ✗ | ✗ | ✓ | — | URL |
| PUT | `/api/empresas/{id}` | ✗ | ✗ | ✓ | — | URL |
| DELETE | `/api/empresas/{id}` | ✗ | ✗ | ✓ | — | URL |

## SuscripcionController (`/api/suscripciones/**`)

| Method | Path | Anonymous | CLIENTE | USUARIO | Ownership check | Source |
|---|---|---|---|---|---|---|
| GET | `/api/suscripciones` | ✗ | ✓ | ✓ | — | URL |
| GET | `/api/suscripciones/{id}` | ✗ | ✓ | ✓ | — | URL |

## CalificacionController (`/api/calificaciones/**`)

| Method | Path | Anonymous | CLIENTE | USUARIO | Ownership check | Source |
|---|---|---|---|---|---|---|
| POST | `/api/calificaciones` | ✗ | ✓ | ✗ | El cliente se obtiene del JWT | URL |

## FcmTokenController (`/api/fcm-tokens/**`)

| Method | Path | Anonymous | CLIENTE | USUARIO | Ownership check | Source |
|---|---|---|---|---|---|---|
| POST | `/api/fcm-tokens` | ✗ | ✓ | ✗ | El cliente se obtiene del JWT | URL |

## NotificacionController (`/api/notificaciones/**`)

| Method | Path | Anonymous | CLIENTE | USUARIO | Ownership check | Source |
|---|---|---|---|---|---|---|
| POST | `/api/notificaciones/cliente/{clienteId}` | ✗ | ✗ | ✓ | — | URL |

---

## Observaciones y casos especiales

1. **`GET /api/clientes`** no tiene URL rule específica — cae al bloque `anyRequest().authenticated()`, por lo que cualquier rol autenticado puede listar clientes. Si esto es indeseado, agregar una regla explícita `hasRole('USUARIO')` en SecurityConfig.

2. **`POST /api/establecimientos/filtro`** usa POST pero es semánticamente un GET (búsqueda). Cae al bloque `anyRequest().authenticated()` — accesible a ambos roles. No tiene URL rule específica para POST en ese path.

3. **`GET /api/historial-canjes/promocion/{promocionId}`** cae al bloque `anyRequest().authenticated()` — ambos roles pueden verlo, sin ownership. Si un CLIENTE no debería ver canjes de otras personas, se necesita filtrado a nivel de servicio o una regla adicional.

4. **`/api/calificaciones/**` y `/api/fcm-tokens/**`** son CLIENTE-only por URL rule. USUARIO no tiene acceso aunque quisiera enviar notificaciones desde el mismo token.
