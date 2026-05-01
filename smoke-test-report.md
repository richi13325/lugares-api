# Smoke Test Report — newRepo API

**Date**: 2026-04-29
**Environment**: Local Docker MySQL (`db_a559f5_test` prod dump)
**Profile**: `dev` (`ddl-auto=update`)

---

## Login Credentials Used

### CLIENTE (Rol: `ROLE_CLIENTE`)
```json
{
  "fldCorreoElectronico": "cliente.demo@example.com",
  "fldContrasenia": "<REDACTED_TEST_PASSWORD>"
}
```

### USUARIO (Rol: `ROLE_USUARIO`)
```json
{
  "fldCorreoElectronico": "usuario.demo@example.com",
  "fldContrasenia": "<REDACTED_TEST_PASSWORD>"
}
```
> Note: Same email works for both roles (dual-role account in production DB).

---

## ✅ Endpoints FUNCIONALES

### Auth (Anonymous)
| Method | Endpoint | Status | Notes |
|--------|----------|--------|-------|
| POST | `/auth/cliente/login` | 200 ✅ | Returns JWT |
| POST | `/auth/usuario/login` | 200 ✅ | Returns JWT |
| POST | `/auth/password/forgot` | 422 ⚠️ | Validación correcta (falta `tipoUsuario`) |

### CLIENTE Endpoints
| Method | Endpoint | Status | Notes |
|--------|----------|--------|-------|
| GET | `/api/establecimientos` | 200 ✅ | Lista con paginación |
| GET | `/api/establecimientos/{id}` | 200 ✅ | Detail completo, `descripcion` no truncado (510 chars OK) |
| GET | `/api/establecimientos/tipo/{tipoId}` | 200 ✅ | Filtro por tipo |
| POST | `/api/establecimientos/filtro` | 200 ✅ | Filtro por etiquetas (body: `etiquetaIds`, `busquedaEstricta`) |
| GET | `/api/establecimientos/sugeridos/{clienteId}` | 200 ✅ | Recomendaciones (vacío = sin tags asignados) |
| GET | `/api/promociones` | 200 ✅ | Lista con relaciones |
| GET | `/api/promociones/{id}` | 200 ✅ | Detail |
| GET | `/api/promociones/establecimiento/{id}` | 200 ✅ | Por establecimiento |
| GET | `/api/etiquetas` | 200 ✅ | Todas las etiquetas |
| GET | `/api/etiquetas/visibles` | 200 ✅ | Solo visibles |
| GET | `/api/etiquetas/{id}` | 200 ✅ | Detail |
| GET | `/api/etiquetas/establecimiento/{id}` | 200 ✅ | Asignadas a establecimiento |
| GET | `/api/etiquetas/tipo-establecimiento/{tipoId}` | 200 ✅ | Por tipo |
| GET | `/api/etiquetas/cliente/{clienteId}` | 200 ✅ | Asignadas a cliente |
| POST | `/api/etiquetas/cliente/{clienteId}/{etiquetaId}` | 200 ✅ | Asigna etiqueta a cliente |
| DELETE | `/api/etiquetas/cliente/{clienteId}/{etiquetaId}` | 200 ✅ | Desasigna |
| GET | `/api/comentarios/establecimiento/{id}` | 200 ✅ | Lista vacía (sin comentarios en BD) |
| POST | `/api/comentarios` | 200 ✅ | Crea comentario (FK verify: id=12, cliente=181, estab=29) |
| DELETE | `/api/comentarios/{id}` | 200 ✅ | Borra comentario propio |
| POST | `/api/calificaciones` | 200 ✅ | Crea/actualiza calificación (FK verify: id=12, cliente=181, estab=2) |
| GET | `/api/clientes` | 200 ✅ | Lista paginada (138 clientes) |
| GET | `/api/clientes/{id}` | 200 ✅ | Detail con relaciones |
| PUT | `/api/clientes/{id}` | 200 ✅ | Update self |
| GET | `/api/historial-canjes/cliente/{clienteId}` | 200 ✅ | 3 registros canjeados |
| GET | `/api/historial-canjes/promocion/{promocionId}` | 200 ✅ | Por promoción |
| POST | `/api/historial-canjes` | 200 ✅ | Crea canje |
| DELETE | `/api/historial-canjes/{id}` | 200 ✅ | Borra (owner) |
| POST | `/api/fcm-tokens` | 200 ✅ | Token de 300+ chars aceptado |
| GET | `/api/capsulas-culturales` | 200 ✅ | Lista |
| GET | `/api/capsulas-culturales/{id}` | 200 ✅ | Detail |
| GET | `/api/suscripciones` | 200 ✅ | Lista (6 suscripciones) |
| GET | `/api/suscripciones/{id}` | 200 ✅ | Detail |
| GET | `/api/categorias-etiqueta` | 200 ✅ | Lista (6 categorías) |
| GET | `/api/tipos-establecimiento` | 200 ✅ | Lista |
| GET | `/api/tipos-establecimiento/{id}` | 200 ✅ | Detail |
| POST | `/api/contacto` | 422 ✅ | Validación correcta (falta `asunto`) |

### USUARIO (Admin) Endpoints
| Method | Endpoint | Status | Notes |
|--------|----------|--------|-------|
| GET | `/api/usuarios` | 200 ✅ | Lista (12 usuarios) |
| GET | `/api/usuarios/{id}` | 200 ✅ | Detail |
| POST | `/api/usuarios` | 201 ✅ | Crea usuario |
| PUT | `/api/usuarios/{id}` | 200 ✅ | Update |
| GET | `/api/marcas` | 200 ✅ | Lista (13 marcas) |
| GET | `/api/marcas/{id}` | 200 ✅ | Detail |
| POST | `/api/marcas` | 201 ✅ | Crea marca (id=26) |
| PUT | `/api/marcas/{id}` | 200 ✅ | Update (id=26) |
| DELETE | `/api/marcas/{id}` | 200 ✅ | Borra (id=26) |
| GET | `/api/empresas` | 200 ✅ | Lista (1 empresa) |
| GET | `/api/empresas/{id}` | 200 ✅ | Detail |
| GET | `/api/etiquetas/admin` | 200 ✅ | Admin view con categoría |
| POST | `/api/etiquetas` | 201 ✅ | Crea etiqueta (id=39) |
| PUT | `/api/etiquetas/{id}` | 200 ✅ | Update (id=39) |
| DELETE | `/api/etiquetas/{id}` | 200 ✅ | Borra (id=39) |
| POST | `/api/promociones` | 201 ✅ | Crea promoción (id=56, requiere `idSuscripcion`, `codigoValidacion` 8 chars) |
| PUT | `/api/promociones/{id}` | 200 ✅ | Update (id=56) |
| DELETE | `/api/promociones/{id}` | 200 ✅ | Borra (id=56) |
| POST | `/api/capsulas-culturales` | 201 ✅ | Crea cápsula (id=17) |
| PUT | `/api/capsulas-culturales/{id}` | 200 ✅ | Update (id=17) |
| DELETE | `/api/capsulas-culturales/{id}` | 200 ✅ | Borra (id=17) |
| POST | `/api/notificaciones/cliente/{clienteId}` | 500 ❌ | **BUG — Firebase no configurado en local** |

---

## ❌ Endpoints CON ERRORES

### 1. `GET /api/calificaciones?establecimientoId=X` (CLIENTE)
**Error**: 500 Internal Server Error

**Causa**: `CalificacionMapper.toDto()` no extrae los IDs (`idCliente`, `idEstablecimiento`) de las entidades relacionadas (`Cliente`, `Establecimiento`). La data se GUARDA correctamente en BD (verificado con query directa: `fk_id_cliente=181`, `fk_id_establecimiento=2`), pero al LEER el mapper retorna `null` en esos campos.

**Severity**: MEDIUM — La escritura funciona, solo la lectura del response DTO falla.

**Fix**: Modificar `CalificacionMapper.toDto()` para mapear manualmente:
```java
CalificacionResponse dto = modelMapper.map(entity, CalificacionResponse.class);
dto.setIdCliente(entity.getCliente().getId());
dto.setIdEstablecimiento(entity.getEstablecimiento().getId());
return dto;
```

---

### 2. `GET /api/fcm-tokens` (CLIENTE)
**Error**: 500 Internal Server Error

**Causa probable**: El mapper intenta acceder a una relación null o hay un null pointer en el getter de la entidad `FcmToken` relacionado con `activo` o `fechaRegistro`.

**Severity**: MEDIUM — POST funciona correctamente.

---

### 3. `POST /api/contacto` con `asunto` incluido (Anonymous)
**Error**: 500 Internal Server Error

**Causa**: `ContactoService.sendEmail()` falla — probablemente пытается enviar un email real через SMTP que no está configurado en el entorno local.

**Severity**: LOW — El endpoint recibe el request correctamente pero falla al enviar email.

---

### 4. `POST /auth/password/forgot` con `tipoUsuario` (Anonymous)
**Error**: 500 Internal Server Error

**Causa**: `PasswordResetService` genera código y guarda en BD pero falla al enviar email de recuperación.

**Severity**: LOW — Misma raíz que #3 (email sending).

---

### 5. `POST /api/empresas` (USUARIO)
**Error**: 409 Data Integrity Violation — tanto POST como PUT y DELETE

**Causa**: La tabla `p_empresa` probablemente tiene una FK hacia `p_suscripcion` que está impide crear/actualizar/empresa. O hay un trigger legacy en la BD que está interfiriendo.

**Investigación requerida**: Verificar esquema de `p_empresa` en prod dump — buscar FK constraints y triggers relacionados.

**Severity**: HIGH — CRUD completo de empresas no funciona.

---

### 6. `POST /api/categorias-etiqueta` (USUARIO)
**Error**: 409 Data Integrity Violation — tanto POST como PUT

**Causa**: Posiblemente tiene FK hacia otra tabla que impide crear/actualizar categorías.

**Severity**: MEDIUM — CRUD de categorías no funciona.

---

### 7. `DELETE /api/categorias-etiqueta/{id}` (USUARIO)
**Error**: 500 Internal Server Error

**Causa**: Intenta eliminar una categoría que tiene `etiquetas` relacionadas (FK constraint en BD).

**Severity**: LOW — Expected behavior, pero el error debería ser 409 con mensaje claro.

---

### 8. `POST /api/establecimientos` (USUARIO)
**Error**: 500 Internal Server Error

**Causa probable**: Falta algún campo requerido o la lógica de negocio valida algo que no está presente.

**Severity**: HIGH — Creation of establishments blocked.

---

### 9. `PUT /api/establecimientos/{id}` (USUARIO)
**Error**: 500 Internal Server Error

**Causa probable**: Mismo root cause que #8.

**Severity**: HIGH — Update of establishments blocked.

---

### 10. `POST /api/notificaciones/cliente/{clienteId}` (USUARIO)
**Error**: 500 Internal Server Error

**Causa**: Firebase Admin SDK no está configurado en el entorno local — las credenciales de producción no están disponibles en Docker local.

**Severity**: LOW — Expected en dev sin Firebase credentials.

---

## 📋 Resumen de Bugs

| # | Endpoint | Method | Severity | Root Cause |
|---|----------|-------|----------|------------|
| 1 | `/api/calificaciones` | GET | MEDIUM | CalificacionMapper no extrae IDs de entidades |
| 2 | `/api/fcm-tokens` | GET | MEDIUM | FcmToken mapper/service null pointer |
| 3 | `/api/contacto` | POST | LOW | SMTP no configurado en dev |
| 4 | `/auth/password/forgot` | POST | LOW | SMTP no configurado en dev |
| 5 | `/api/empresas` | POST/PUT/DELETE | HIGH | FK constraint en BD (requiere investigación) |
| 6 | `/api/categorias-etiqueta` | POST/PUT | MEDIUM | FK constraint en BD (requiere investigación) |
| 7 | `/api/categorias-etiqueta/{id}` | DELETE | LOW | FK con etiquetas existentes, error 500 en vez de 409 |
| 8 | `/api/establecimientos` | POST | HIGH | Validación/servicio falla (requiere debug) |
| 9 | `/api/establecimientos/{id}` | PUT | HIGH | Mismo root cause que #8 |
| 10 | `/api/notificaciones/cliente/{id}` | POST | LOW | Firebase no configurado en dev |

---

## 🔧 Recomendaciones de Fix (Prioridad)

### P0 — Criticos (Bloquean migración)
1. **Fix #5**: Investigar FK de `p_empresa` — sin esto no se pueden gestionar empresas
2. **Fix #8/#9**: Debuggear `EstablecimientoService.create/update` — sin esto no se pueden gestionar establecimientos

### P1 — Altos (Afectan funcionalidad)
3. **Fix #1**: `CalificacionMapper` — los IDs vienen null en response DTO
4. **Fix #6**: Investigar FK de `c_categorias_etiqueta`

### P2 — Medios ( UX pero no bloquean)
5. **Fix #2**: `FcmTokenService` GET — null pointer en lectura
6. **Fix #7**: `CategoriaEtiquetaService.delete` — debe lanzar 409 con mensaje claro

### P3 — Bajos (Dev environment only)
7. **Fix #3/#4/#10**: Configurar SMTP mock o deshabilitar envío de email en dev
