# Production Migration Verification Plan

**Project:** `newRepo` — 300lugares-api (Spring Boot 3.5 / Java 21)  
**Date:** 2026-04-27  
**Author:** sdd-explore phase  
**Scope:** Verify JPA entities and Flyway migrations are production-safe before switching `DB_URL` to `db_a559f5_test`.

---

## 1. Executive Summary

The new `newRepo` API was developed against a fresh VPS DB (`newrepo`) where Hibernate auto-generated the schema from JPA entities. That generated schema diverges from the production DB (`db_a559f5_test`) in **14 distinct ways** across schema type, varchar length, column names, missing columns, and missing constraints. Additionally, 12 tables exist in production but not in the VPS DB: 8 are confirmed DEAD (zero references in either codebase), 2 are PARTIAL (referenced via stored procedures or denormalized away), and 2 are ACTIVE (the filter and its parent `p_establecimiento` fork). The single most dangerous discrepancy is `Calificacion.java` mapping column `fkd_id_establecimiento` (Hibernate-generated typo) while production uses `fk_id_establecimiento` — this causes every write to `d_estrella_x_cliente` to fail with a column-not-found error at runtime. Eight additional issues carry data-loss or silent-corruption risk. No migration script can be safely skipped; a `V3` through `V7` migration batch is required before cutover.

---

## 2. Verification of Missing Tables (12)

Evidence methodology: searched `App300_Lugares` source tree (187 Java files) via `grep -rn` on table name, model class name, and derived Java identifiers. "No references found" means zero matches across all `.java` files in `src/`.

---

### 2.1 `c_mediosdepago` — **DEAD**

| Field | Value |
|-------|-------|
| Schema | `id_medio_de_pago INT PK`, `fld_nombre varchar(65) NOT NULL`, `fld_descripcion varchar(255)` |
| Old repo references | None. Searched: `c_mediosdepago`, `MediosDePago`, `MedioDePago`, `mediosdepago`. Zero matches. |
| Old repo entity | Not mapped. |
| New repo entity | Not mapped. |
| Classification | **DEAD** |

**Recommendation:** No action required. Table has no FK children that `newRepo` entities read (its only child `d_mdepago_x_establecimiento` is also DEAD — see 2.4). Document as orphaned legacy catalog. Do NOT create entity.

---

### 2.2 `c_permisos_cliente` — **DEAD**

| Field | Value |
|-------|-------|
| Schema | `id_permiso INT PK`, `fld_nombre varchar(65) UNIQUE NOT NULL`, `fld_descripcion`, `fld_fecha_creacion`, `fld_fecha_modificacion` |
| Old repo references | None. Searched: `c_permisos_cliente`, `PermisosCliente`, `PermisoCliente`, `permisos_cliente`. Zero matches. |
| Old repo entity | Not mapped. |
| New repo entity | Not mapped. |
| Classification | **DEAD** |

**Recommendation:** No action required. Its child table `d_permisos_x_suscripcion` is also DEAD. Both can be ignored.

---

### 2.3 `c_telefonodeestablecimiento` — **DEAD**

| Field | Value |
|-------|-------|
| Schema | `Id_Telefono INT PK AUTO_INCREMENT`, `Fk_Id_Establecimiento INT NOT NULL FK→p_establecimiento`, `Fld_Telefono varchar(10) NOT NULL` |
| Old repo references | None. Searched: `c_telefonodeestablecimiento`, `TelefonoDeEstablecimiento`, `telefono.*establecimiento`. Zero matches. |
| Old repo entity | Not mapped. |
| New repo entity | Not mapped. |
| Classification | **DEAD** |

**Evidence that confirms DEAD:** `p_establecimiento` was updated to carry `fld_celular_1` and `fld_celular_2` directly (prod schema lines 450–451; `Establecimiento.java` lines 119–123). The phone-number table was superseded by inline columns on the establishment entity. User's suspicion confirmed.

---

### 2.4 `d_marca_x_establecimiento` — **DEAD**

| Field | Value |
|-------|-------|
| Schema | 3-column join table: `Id INT PK`, `Id_Establecimiento FK→p_establecimiento`, `Id_Marca FK→c_marcas` |
| Old repo references | None. Searched: `d_marca_x_establecimiento`, `MarcaXEstablecimiento`, `MarcaPorEstablecimiento`, `MarcaEstablecimiento`. Zero matches. |
| Old repo entity | Not mapped. |
| New repo entity | Not mapped. |
| Classification | **DEAD** |

**Recommendation:** `Marca` entity already exists in `newRepo` (`c_marcas` table). The join table is unused. No action needed unless brand-per-establishment feature is planned.

---

### 2.5 `d_mdepago_x_establecimiento` — **DEAD**

| Field | Value |
|-------|-------|
| Schema | Join: `Id_Establecimiento FK→p_establecimiento`, `Id_MedioDePago FK→c_mediosdepago` |
| Old repo references | None. Searched: `d_mdepago_x_establecimiento`, `MdePagoXEstablecimiento`, `mdepago`. Zero matches. |
| Old repo entity | Not mapped. |
| New repo entity | Not mapped. |
| Classification | **DEAD** |

---

### 2.6 `d_permisos_x_suscripcion` — **DEAD**

| Field | Value |
|-------|-------|
| Schema | Join: `fk_id_suscripcion FK→p_suscripcion`, `fk_id_permiso FK→c_permisos_cliente` |
| Old repo references | None. Searched: `d_permisos_x_suscripcion`, `PermisosXSuscripcion`. Zero matches. |
| Old repo entity | Not mapped. |
| New repo entity | Not mapped. |
| Classification | **DEAD** |

---

### 2.7 `d_ruta_x_tipoestablecimiento` — **DEAD**

| Field | Value |
|-------|-------|
| Schema | Join: `Id_Ruta FK→p_rutas`, `Id_TipoEstablecimiento FK→c_tipoestablecimiento` |
| Old repo references | None. Searched: `d_ruta_x_tipoestablecimiento`, `RutaXTipoEstablecimiento`. Zero matches. |
| Old repo entity | Not mapped. |
| New repo entity | Not mapped. |
| Classification | **DEAD** |

---

### 2.8 `d_rutapersonalizada_x_establecimeinto` — **DEAD**

| Field | Value |
|-------|-------|
| Schema | Join: `Id_Establecimiento FK→p_establecimiento`, `Id_Favorito FK→p_rutapersonalizada`. Note: table name contains typo `establecimeinto`. |
| Old repo references | None. Searched: `d_rutapersonalizada_x_establecimeinto`, `RutaPersonalizadaXEstablecimiento`. Zero matches. |
| Old repo entity | Not mapped. |
| New repo entity | Not mapped. |
| Classification | **DEAD** |

---

### 2.9 `d_tipoestablecimiento_x_establecimiento` — **PARTIAL (denormalized away)**

| Field | Value |
|-------|-------|
| Schema | Join: `Id_TipoEstablecimiento`, `Id_Establecimiento FK→p_establecimiento` — no FK on `Id_TipoEstablecimiento` to `c_tipoestablecimiento` (missing in prod schema lines 298–306) |
| Old repo references | None. Searched: `d_tipoestablecimiento_x_establecimiento`, `TipoEstablecimientoXEstablecimiento`. Zero matches. |
| Old repo entity | Not mapped. `EstablecimientoModel.java:81` maps `fk_id_tipo_establecimiento` as a direct `@Column(name = "fk_id_tipo_establecimiento")` on the establishment row. |
| New repo entity | `Establecimiento.java:42` uses `@ManyToOne` on `TipoEstablecimiento` via `fk_id_tipo_establecimiento` column. |
| Classification | **PARTIAL — denormalized** |

**Evidence:** Old repo (`EstablecimientoModel.java` line 81–82) stores the tipo as a scalar FK column on `p_establecimiento` directly. Production schema confirms `fk_id_tipo_establecimiento` column on `p_establecimiento` (line 424) with FK to `c_tipoestablecimiento` (line 468–470). The join table exists in production but is not used — the relationship has been collapsed into a direct column. Both old and new repos agree on this design.

**Recommendation:** No entity needed for the join table. newRepo's existing mapping is correct. The join table is effectively orphaned legacy data.

---

### 2.10 `p_filtroestablecimiento` — **DEAD (orphaned legacy table)**

| Field | Value |
|-------|-------|
| Schema | 43-column table (PK `Id_Filtro`, FK `Fk_d_Establecimiento → p_establecimiento`) with 44 stale rows. |
| Old repo references | No model class in old repo (user-verified). The SPs `spListEstablecimientosFiltradosPoretiqueta` query `p_etiqueta` (tags), NOT `p_filtroestablecimiento` — the naming "FiltradosPorEtiqueta" means "filtered by tag", not "filter table". |
| Old repo functional equivalent | `p_etiqueta` + tag join tables provide the actual filtering. |
| New repo equivalent | `EstablecimientoRepository.java` already replaces the SPs with JPQL: `findByEtiquetasOr` (line 49), `findByEtiquetasAnd` (line 62), `findSugeridosByClienteEtiquetas` (line 78). |
| Classification | **DEAD — superseded by tag/etiqueta system, never had a backing model** |

**Initial sub-agent misclassification:** The sub-agent inferred ACTIVE from the SP name `spListEstablecimientosFiltradosPoretiqueta`, but those SPs operate on `p_etiqueta`, not on `p_filtroestablecimiento`. User confirmed there is no model class for this table in the old repo and the etiqueta system is the live filter mechanism. Re-classified to DEAD.

**Recommendation:** No action required. No entity needed. Table can be ignored entirely on cutover.

---

### 2.11 `p_rutapersonalizada` — **DEAD**

| Field | Value |
|-------|-------|
| Schema | `Id_Favorito INT PK`, `FK_Id_Cliente FK→p_cliente`, `Fld_Nombre varchar(65) NOT NULL`, `Fld_Descripcion`, `Fld_EsPublica tinyint NOT NULL` |
| Old repo references | None. Searched: `p_rutapersonalizada`, `RutaPersonalizada`, `rutapersonalizada`. Zero matches across old repo. |
| Old repo entity | Not mapped. |
| New repo entity | Not mapped. |
| Classification | **DEAD** |

---

### 2.12 `p_rutas` — **DEAD**

| Field | Value |
|-------|-------|
| Schema | `Id_Ruta INT PK`, `Fld_Nombre varchar(65) NOT NULL`, `Fld_Descripcion`, `Fld_FechaCreacion date NOT NULL` |
| Old repo references | None. Searched: `p_rutas`, `RutaModel`, literal string `"p_rutas"`. Zero matches. `"ruta"` appears only in Spanish for "URL path" in Swagger comments — not a table reference. |
| Old repo entity | Not mapped. |
| New repo entity | Not mapped. |
| Classification | **DEAD** |

---

### Summary Table

| # | Table | Classification | newRepo Action |
|---|-------|----------------|----------------|
| 1 | `c_mediosdepago` | DEAD | None |
| 2 | `c_permisos_cliente` | DEAD | None |
| 3 | `c_telefonodeestablecimiento` | DEAD (superseded by `fld_celular_1/2`) | None |
| 4 | `d_marca_x_establecimiento` | DEAD | None |
| 5 | `d_mdepago_x_establecimiento` | DEAD | None |
| 6 | `d_permisos_x_suscripcion` | DEAD | None |
| 7 | `d_ruta_x_tipoestablecimiento` | DEAD | None |
| 8 | `d_rutapersonalizada_x_establecimeinto` | DEAD | None |
| 9 | `d_tipoestablecimiento_x_establecimiento` | PARTIAL — denormalized; join table orphaned | None — current entity mapping correct |
| 10 | `p_filtroestablecimiento` | DEAD (orphaned; tag system replaced it; JPQL already in EstablecimientoRepository) | None |
| 11 | `p_rutapersonalizada` | DEAD | None |
| 12 | `p_rutas` | DEAD | None |

**Count:** 9 DEAD, 1 PARTIAL (denormalized), 0 ACTIVE, 2 implicit DEAD. **No entities need to be created.** All filtering features already covered by existing JPQL queries on the etiqueta system.

---

## 3. Schema Discrepancies — Action Plan

### 3.1 Column name typo — `d_estrella_x_cliente.fk_id_establecimiento`

| Field | Detail |
|-------|--------|
| **Severity** | CRITICAL |
| **PROD** | `fk_id_establecimiento` (prod schema line 129) |
| **VPS** | `fkd_id_establecimiento` (vps schema line 88; Hibernate-generated typo based on entity) |
| **Entity** | `Calificacion.java:35` — `@JoinColumn(name = "fkd_id_establecimiento")` |
| **mysql-migration-rules.md Rule 1** | Rule 1 documents `fkd_id_establecimiento` as an "intentional typo" that must be preserved. **This is wrong.** Rule 1 was written against the VPS schema, which is Hibernate-generated — NOT the production schema. Production has the correct `fk_id_establecimiento`. Rule 1 is contradicted by the production dump. |
| **Impact** | Every INSERT and SELECT joining `d_estrella_x_cliente` will throw `Unknown column 'fkd_id_establecimiento' in 'field list'` against production. All rating/calificacion endpoints fail. |
| **Required action** | **Option A (correct):** Fix `Calificacion.java` to use `@JoinColumn(name = "fk_id_establecimiento")`. Update `mysql-migration-rules.md` Rule 1 to remove the typo entry — it is not a prod typo, it is a VPS artifact. No Flyway migration needed (no prod column rename required). **Option B (wrong):** Do NOT write a migration to rename the column in prod — prod already has the correct name. |
| **Affected file** | `src/main/java/com/lugares/api/entity/Calificacion.java:35` |

---

### 3.2 `p_suscripcion.fld_precio` — type mismatch `decimal(10,0)` vs `double`

| Field | Detail |
|-------|--------|
| **Severity** | HIGH |
| **PROD** | `decimal(10,0) NOT NULL` — integer-precision decimal, no fractional part |
| **VPS** | `double` — floating-point, 64-bit IEEE |
| **Entity** | `Suscripcion.java:34` — `private Double precio` |
| **Impact** | On read: MySQL silently coerces decimal to double — no data loss on read. On write: inserting a double like `1999.99` into `decimal(10,0)` rounds to `2000`. Silent monetary data corruption if fractional prices are ever used. Currently prices are integers (e.g. `200`, `500`) so risk is latent but real if the business adds fractional pricing. |
| **Required action** | Change `Suscripcion.java` to use `BigDecimal precio` with `@Column(precision = 10, scale = 0)`. Add Flyway V3 migration: `ALTER TABLE p_suscripcion MODIFY fld_precio decimal(10,0) NOT NULL` — no-op on prod (already correct type), fixes VPS alignment. |
| **Affected file** | `src/main/java/com/lugares/api/entity/Suscripcion.java:34` |

---

### 3.3 `p_promociones.fld_imagen` — tinyblob vs prod longblob

| Field | Detail |
|-------|--------|
| **Severity** | CRITICAL |
| **PROD** | `longblob` — max 4 GB |
| **VPS** | `tinyblob` — max 255 bytes |
| **Entity** | `Promocion.java:54-56` — `@Lob byte[] imagen` |
| **Impact** | Any image stored in prod (up to 4 GB) will be silently truncated to 255 bytes on read if Hibernate maps it as tinyblob. Writes exceeding 255 bytes will throw `Data too long for column 'fld_imagen'`. Production has images stored in this column (table has live data). |
| **Required action** | Add Flyway V4 migration: `ALTER TABLE p_promociones MODIFY fld_imagen longblob`. No entity change needed (`@Lob` maps to the correct JPA Lob type — the issue is the DB column type in VPS). This migration is a no-op against prod (already longblob). |
| **Affected file** | `src/main/java/com/lugares/api/entity/Promocion.java:54` |

---

### 3.4 `p_promociones.tipo_promocion` — varchar(10) vs enum('FECHA','SEMANAL')

| Field | Detail |
|-------|--------|
| **Severity** | HIGH |
| **PROD** | `varchar(10)` — accepts any string |
| **VPS** | `enum('FECHA','SEMANAL')` |
| **Entity** | `Promocion.java:64-66` — `@Enumerated(EnumType.STRING) TipoPromocion tipoPromocion` |
| **Impact** | On prod: inserting an enum value works (MySQL stores it as string). On VPS: any value outside `{'FECHA','SEMANAL'}` causes `Data truncated for column`. The enum constraint in VPS is stricter than prod. After migration, this column will become enum on prod — meaning any existing prod data outside those two values will be blocked. **Check prod data first.** |
| **Required action** | Before writing V5 migration: run `SELECT DISTINCT tipo_promocion FROM p_promociones` on prod to verify no out-of-enum values exist. Then: Flyway V5 `ALTER TABLE p_promociones MODIFY tipo_promocion enum('FECHA','SEMANAL')`. If unexpected values exist, the business must decide mapping before migration. |
| **Affected file** | `src/main/java/com/lugares/api/entity/Promocion.java:65` |

---

### 3.5 `p_promocion_dias.dias_disponibles` — varchar(20) vs enum(7 days)

| Field | Detail |
|-------|--------|
| **Severity** | HIGH |
| **PROD** | `varchar(20)` |
| **VPS** | `enum('FRIDAY','MONDAY','SATURDAY','SUNDAY','THURSDAY','TUESDAY','WEDNESDAY')` |
| **Entity** | `Promocion.java:70-72` — `@Enumerated(EnumType.STRING) Set<DayOfWeek> diasDisponibles` |
| **Impact** | Same issue as 3.4: VPS enum is stricter than prod varchar. Java `DayOfWeek` serializes as `MONDAY`, `TUESDAY` etc. — matches enum values. Risk: any existing prod data in a different format (e.g. `Lunes`, `L`, `Monday`) will fail validation when the column is converted to enum. |
| **Required action** | Run `SELECT DISTINCT dias_disponibles FROM p_promocion_dias` on prod before writing migration. Verify all values are uppercase English day names. Add V5 or V6 migration to ALTER the column. |
| **Affected file** | `src/main/java/com/lugares/api/entity/Promocion.java:70` |

---

### 3.6 `p_historial_canjes.fld_codigo_validacion` — nullable varchar(50) vs NOT NULL varchar(255)

| Field | Detail |
|-------|--------|
| **Severity** | HIGH |
| **PROD** | `varchar(50) DEFAULT NULL` — nullable, 50 chars max |
| **VPS** | `varchar(255) NOT NULL` |
| **Entity** | `HistorialCanje.java:43-44` — `@Column(name = "fld_codigo_validacion", nullable = false)` |
| **Impact** | Two sub-problems: (a) `nullable = false` on entity will fail for any existing prod rows where `fld_codigo_validacion IS NULL` — Hibernate validate will reject. (b) Any existing code that stored codes > 50 chars (unlikely but possible with varchar(255) entity) will truncate on prod. |
| **Required action** | Check prod data: `SELECT COUNT(*) FROM p_historial_canjes WHERE fld_codigo_validacion IS NULL`. If any rows are null: either backfill with a default value, or change entity to `nullable = true` until backfill is confirmed. Add Flyway V6 to normalize: `ALTER TABLE p_historial_canjes MODIFY fld_codigo_validacion varchar(255) NOT NULL DEFAULT ''`. Also verify max length of existing codes. |
| **Affected file** | `src/main/java/com/lugares/api/entity/HistorialCanje.java:43` |

---

### 3.7 `fcm_tokens.id_cliente` — int vs bigint

| Field | Detail |
|-------|--------|
| **Severity** | MEDIUM |
| **PROD** | `id_cliente int NOT NULL` (prod schema line 317) |
| **VPS** | `id_cliente bigint NOT NULL` (vps schema line 163) |
| **Entity** | `FcmToken.java:31` — `private Long idCliente` (Long maps to bigint) |
| **Impact** | On prod: MySQL will accept bigint values in int columns only up to `2^31 - 1`. If `id_cliente` ever exceeds ~2.1 billion, inserts will fail. With current data (prod has 7 rows), no immediate risk. On read: no data loss (int is widened to Long in Java). |
| **Required action** | Add Flyway V7 migration to normalize: `ALTER TABLE fcm_tokens MODIFY id_cliente bigint NOT NULL` on prod. Low urgency but should be done before scaling. |
| **Affected file** | `src/main/java/com/lugares/api/entity/FcmToken.java:31` |

---

### 3.8 `fcm_tokens.token` — varchar(255) vs prod varchar(500)

| Field | Detail |
|-------|--------|
| **Severity** | CRITICAL |
| **PROD** | `varchar(500) NOT NULL` (prod schema line 318) |
| **VPS** | `varchar(255) NOT NULL` (vps schema line 165) |
| **Entity** | `FcmToken.java:27-28` — `@Column(nullable = false, unique = true) private String token` — no `length` set, Hibernate defaults to 255 |
| **Impact** | FCM tokens are typically 152–163 characters but can exceed 255 in some environments. Any token > 255 chars stored in prod will cause `Data too long for column 'token'` on INSERT. Existing prod tokens > 255 chars cannot be read back without truncation. With 7 prod rows, check actual token lengths immediately. |
| **Required action** | Add `@Column(nullable = false, unique = true, length = 500)` to `FcmToken.java`. No Flyway migration needed for prod (already varchar(500)). The VPS would need `ALTER TABLE fcm_tokens MODIFY token varchar(500) NOT NULL` but that's dev env only. |
| **Affected file** | `src/main/java/com/lugares/api/entity/FcmToken.java:27` |

---

### 3.9 `fcm_tokens` — missing columns `activo` and `fecha_registro`

| Field | Detail |
|-------|--------|
| **Severity** | HIGH |
| **PROD** | `activo tinyint(1) DEFAULT '1'`, `fecha_registro datetime DEFAULT CURRENT_TIMESTAMP` (prod schema lines 319–320) |
| **VPS** | Neither column exists |
| **Entity** | `FcmToken.java` — neither field mapped |
| **Impact** | Hibernate `ddl-auto=validate` against prod will NOT fail for missing entity fields (validate only checks that what the entity declares exists in the DB — extra DB columns are ignored). BUT: any query joining on `activo` will fail at runtime. More importantly, if any existing prod code or trigger uses `activo` for soft-delete logic, inserting new tokens without setting it will use the default `1` — acceptable. However the `fecha_registro` audit trail is silently lost. |
| **Required action** | Add `activo` (Boolean, defaulting to true) and `fechaRegistro` (LocalDateTime) to `FcmToken.java` with appropriate `@Column(insertable = false, updatable = false)` for `fecha_registro` (server-default). This is a HIGH priority before cutover — the `activo` flag is used in prod to deactivate tokens (prod has index `idx_activo`). |
| **Affected file** | `src/main/java/com/lugares/api/entity/FcmToken.java` |

---

### 3.10 `p_capsulas_culturales.fld_es_visible` — tinyint DEFAULT 1 vs bit(1) nullable

| Field | Detail |
|-------|--------|
| **Severity** | MEDIUM |
| **PROD** | `tinyint DEFAULT '1'` (prod schema line 344) — not null semantics, defaults to visible |
| **VPS** | `bit(1) DEFAULT NULL` (vps schema line 222) |
| **Entity** | `CapsulaCultural.java:41-42` — `private Boolean esVisible` (no nullable constraint) |
| **Impact** | On read: MySQL driver maps both tinyint and bit(1) to Boolean in Java — no runtime error. On write: entity inserts NULL for unset `esVisible`, but prod column defaults to 1 if not specified (server-side default). Net effect: new inserts without setting `esVisible` will store NULL on VPS but 1 on prod — behavioral inconsistency between environments. |
| **Required action** | Add `@Column(columnDefinition = "tinyint default 1")` and set `nullable = false` with a default of `true` in Java. Add Flyway migration to normalize VPS column to `tinyint NOT NULL DEFAULT 1`. LOW urgency — no data loss risk, just behavior divergence. |
| **Affected file** | `src/main/java/com/lugares/api/entity/CapsulaCultural.java:41` |

---

### 3.11 `p_establecimiento.fld_descripcion` — varchar(255) vs prod varchar(510)

| Field | Detail |
|-------|--------|
| **Severity** | CRITICAL |
| **PROD** | `varchar(510)` (prod schema line 426) |
| **VPS** | `varchar(255)` (vps schema line 310) |
| **Entity** | `Establecimiento.java:47-48` — `private String descripcion` — no `length` specified, Hibernate defaults to 255 |
| **Impact** | Any existing prod establishment with `fld_descripcion` > 255 chars will truncate silently on UPDATE, or fail on INSERT from the new API. With 72 rows in prod (`AUTO_INCREMENT=72`), this is immediate data loss risk. |
| **Required action** | Add `@Column(name = "fld_descripcion", length = 510)` to `Establecimiento.java`. No Flyway migration needed on prod (already 510). VPS will need ALTER but that's dev-only. **Check prod data NOW:** `SELECT id_establecimiento, LENGTH(fld_descripcion) FROM p_establecimiento WHERE LENGTH(fld_descripcion) > 255`. |
| **Affected file** | `src/main/java/com/lugares/api/entity/Establecimiento.java:47` |

---

### 3.12 `p_establecimiento` — multiple `text` columns mapped as `varchar(255)` in VPS

| Field | Detail |
|-------|--------|
| **Severity** | HIGH |
| **PROD** | `fld_img_refs TEXT`, `fld_menu TEXT`, `fld_alimentos_bebidas TEXT`, `fld_promo_lunes TEXT` through `fld_promo_domingo TEXT`, `fld_promo_300_lugares TEXT` (prod schema lines 436, 449, 453, 456–463) |
| **VPS** | All mapped as `varchar(255)` |
| **Entity** | `Establecimiento.java` — all these fields are plain `String` with no `@Column` length or `@Lob` |
| **Impact** | Any prod establishment with URLs or text in these columns > 255 chars will truncate on write. IMAGE URL fields (`fld_img_refs`) stored as text can easily exceed 255 chars. |
| **Required action** | Add `@Column(columnDefinition = "TEXT")` (or `@Lob`) to at least `imgRefs`, `menu`, `alimentosBebidas`, `promoLunes` through `promoDomingo`, `promo300Lugares` in `Establecimiento.java`. Verify prod data lengths. |
| **Affected file** | `src/main/java/com/lugares/api/entity/Establecimiento.java` — lines 77, 117, 128, 137–159 |

---

### 3.13 Missing UNIQUE constraints in VPS (not in entity)

| Severity | Table | Column | Prod constraint | Impact |
|----------|-------|--------|----------------|--------|
| HIGH | `p_cliente` | `fld_correo_electronico` | `UNIQUE KEY UQ_Fld_CorreoElectronico` (line 384) | Duplicate emails will be silently accepted on VPS; will fail at DB level on prod. Application must enforce uniqueness or catch constraint violations. |
| HIGH | `p_cliente` | `fld_telefono` | `UNIQUE KEY UQ_Fld_Telefono` (line 385) | Same as above for phone. |
| MEDIUM | `p_etiqueta` | `fld_nombre` | `UNIQUE KEY fld_nombre` (prod line 489) | Duplicate tag names accepted on VPS. |
| MEDIUM | `d_etiqueta_x_cliente` | `(fk_id_cliente, fk_id_etiqueta)` | Composite UNIQUE (prod line 151) | Duplicate tag assignments accepted on VPS. |
| MEDIUM | `d_etiqueta_x_establecimiento` | `(fk_id_establecimiento, fk_id_etiqueta)` | Composite UNIQUE (prod line 170) | Same. |
| MEDIUM | `d_etiqueta_x_tipo_establecimiento` | `(fk_id_tipo_establecimiento, fk_id_etiqueta)` | Composite UNIQUE (prod line 189) | Same. |

**Required action:** The missing UNIQUE constraints on `p_cliente` are HIGH priority — the new API must either add `@Column(unique = true)` to `Cliente.java` for `correoElectronico` and `telefono`, and/or add a `@Table(uniqueConstraints = ...)` annotation. The join table UNIQUEs can be enforced via `@UniqueConstraint` in their respective entities. No Flyway needed for prod (already has them); VPS will get them when `ddl-auto` recreates with entity changes.

---

### 3.14 Most NOT NULL prod constraints are nullable in VPS

| Field | Detail |
|-------|--------|
| **Severity** | MEDIUM (aggregate) |
| **Impact** | Hibernate `validate` against prod will succeed even if the entity has `nullable = true` — `validate` only checks column existence and type, not nullability. However, any INSERT that sets a required prod column to null will fail at runtime with `Column 'x' cannot be null`. |
| **Examples** | `p_establecimiento.fld_nombre NOT NULL` (prod line 425) but VPS nullable. `p_establecimiento.fld_estado NOT NULL` (prod line 427). `p_suscripcion.fld_precio NOT NULL`. Many others. |
| **Required action** | Audit and add `nullable = false` to entity `@Column` annotations for columns marked `NOT NULL` in prod. This is code hygiene that catches bugs at the application layer before they hit the DB. Prioritize: `fld_nombre`, `fld_estado`, `fld_ciudad`, `fld_direccion` on `Establecimiento`; `fld_precio` on `Suscripcion`. |

---

### Action Plan Summary Table

| # | Discrepancy | Severity | newRepo File | Action Required |
|---|------------|----------|-------------|-----------------|
| 3.1 | `fkd_id_establecimiento` typo — entity wrong, prod is correct | **CRITICAL** | `Calificacion.java:35` | Fix `@JoinColumn` name; fix Rule 1 doc |
| 3.8 | `fcm_tokens.token` varchar 255 vs prod 500 | **CRITICAL** | `FcmToken.java:27` | Add `length = 500` to `@Column` |
| 3.11 | `p_establecimiento.fld_descripcion` 255 vs prod 510 | **CRITICAL** | `Establecimiento.java:47` | Add `length = 510`; check prod data |
| 3.3 | `p_promociones.fld_imagen` tinyblob vs prod longblob | **CRITICAL** | `Promocion.java:54` | Flyway V4: `MODIFY fld_imagen longblob` on VPS |
| 3.2 | `p_suscripcion.fld_precio` double vs prod decimal(10,0) | HIGH | `Suscripcion.java:34` | Change to BigDecimal; Flyway V3 |
| 3.4 | `tipo_promocion` varchar vs enum | HIGH | `Promocion.java:65` | Verify prod data; Flyway V5 |
| 3.5 | `dias_disponibles` varchar vs enum | HIGH | `Promocion.java:70` | Verify prod data; Flyway V5/V6 |
| 3.6 | `fld_codigo_validacion` nullable vs NOT NULL | HIGH | `HistorialCanje.java:43` | Check null rows; Flyway V6 |
| 3.9 | `fcm_tokens` missing `activo` and `fecha_registro` | HIGH | `FcmToken.java` | Add fields to entity |
| 3.12 | `p_establecimiento` text cols as varchar(255) | HIGH | `Establecimiento.java` | Add `@Lob` / `columnDefinition` |
| 3.13 | Missing UNIQUE constraints | HIGH (p_cliente) | `Cliente.java` | Add `@Column(unique=true)` + `@Table` constraints |
| 3.7 | `fcm_tokens.id_cliente` int vs bigint | MEDIUM | `FcmToken.java:31` | Flyway V7: MODIFY on prod |
| 3.10 | `fld_es_visible` bit(1) vs tinyint | MEDIUM | `CapsulaCultural.java:41` | Add column definition + default |
| 3.14 | NOT NULL constraints missing in entity | MEDIUM | Multiple entities | Add `nullable = false` annotations |

---

## 4. Pre-Migration Verification Checklist

Run this checklist against a local Docker MySQL populated from the **prod dump** (`prod_schema.sql`), with `ddl-auto=validate`.

- [ ] **P0-1** — Run `SELECT id_establecimiento, LENGTH(fld_descripcion) FROM p_establecimiento WHERE LENGTH(fld_descripcion) > 255` on prod. Record count. Any row > 255 chars means CRITICAL data would be truncated immediately on update.
- [ ] **P0-2** — Run `SELECT COUNT(*) FROM p_historial_canjes WHERE fld_codigo_validacion IS NULL` on prod. If > 0, fix entity or backfill before V6 migration.
- [ ] **P0-3** — Run `SELECT MAX(LENGTH(token)) FROM fcm_tokens` on prod. Verify no existing token exceeds 255 chars. (If any does, insert via new API would fail immediately.)
- [ ] **P0-4** — Run `SELECT DISTINCT tipo_promocion FROM p_promociones` on prod. Verify values are only `NULL`, `'FECHA'`, `'SEMANAL'`. Any other value blocks V5 migration.
- [ ] **P0-5** — Run `SELECT DISTINCT dias_disponibles FROM p_promocion_dias` on prod. Verify values match Java `DayOfWeek` uppercase names.
- [ ] **P0-6** — Fix `Calificacion.java:35` — change `@JoinColumn(name = "fkd_id_establecimiento")` to `@JoinColumn(name = "fk_id_establecimiento")`.
- [ ] **P0-7** — Fix `Establecimiento.java:47` — add `@Column(name = "fld_descripcion", length = 510)`.
- [ ] **P0-8** — Fix `FcmToken.java:27` — add `length = 500` to token `@Column`.
- [ ] **P0-9** — Fix `FcmToken.java` — add `activo` (Boolean) and `fechaRegistro` (LocalDateTime) fields with correct `@Column` annotations.
- [ ] **P1-1** — Write and test Flyway V3: `ALTER TABLE p_suscripcion MODIFY COLUMN fld_precio decimal(10,0) NOT NULL` (VPS alignment; no-op on prod).
- [ ] **P1-2** — Write and test Flyway V4: `ALTER TABLE p_promociones MODIFY COLUMN fld_imagen longblob` (VPS alignment; no-op on prod).
- [ ] **P1-3** — Write and test Flyway V5: ALTER `tipo_promocion` and `dias_disponibles` to enum after P0-4/P0-5 pass.
- [ ] **P1-4** — Write and test Flyway V6: `ALTER TABLE p_historial_canjes MODIFY COLUMN fld_codigo_validacion varchar(255) NOT NULL` — must be guarded with NULL-check from P0-2.
- [ ] **P1-5** — Write and test Flyway V7: `ALTER TABLE fcm_tokens MODIFY COLUMN id_cliente bigint NOT NULL`.
- [ ] **P2-1** — Import prod dump into local Docker MySQL. Start app with `SPRING_PROFILES_ACTIVE=prod` (`ddl-auto=validate`). Confirm zero `SchemaManagementException` in startup logs.
- [ ] **P2-2** — Run full MockMvc test suite (`./mvnw test`) against local Docker running prod dump + all migrations applied. Confirm zero failures.
- [ ] **P2-3** — Test `GET /calificacion` and `POST /calificacion` against local prod dump. Verify `d_estrella_x_cliente` operations resolve without column-not-found errors (validates fix from P0-6).
- [ ] **P2-4** — Test `GET /establecimiento/{id}` against local prod dump. Verify `fld_descripcion` field is not truncated for establishments with long descriptions.
- [ ] **P2-5** — Test `POST /fcm-token` with a real FCM token string against local prod dump. Verify no truncation.
- [ ] **P2-6** — Test `GET /promo` and `POST /promo` against local prod dump. Verify `fld_imagen` is returned correctly for existing blobs.
- [x] **P2-7** — ~~Verify `p_filtroestablecimiento` decision~~ — RESOLVED: table is DEAD; tag-based filtering already implemented via JPQL.
- [ ] **P2-8** — Test `GET /canje` for a HistorialCanje record where `fld_codigo_validacion` was previously null. Confirm the app handles it (nullable entity + migration).
- [ ] **P2-9** — Confirm `mysql-migration-rules.md` Rule 1 is updated to remove the `fkd_id_establecimiento` entry (it is NOT a prod typo).

---

## 5. Migration Day Runbook

**Prerequisites:** All checklist items P0-x and P1-x complete. Test run P2-x passes.

### Step 1 — Final prod backup
```bash
mysqldump -h <prod_host> -u <prod_user> -p \
  --single-transaction --quick --routines --triggers --events \
  --databases db_a559f5_test \
  > backup_premigration_$(date +%Y%m%d_%H%M).sql
```
Store in a location outside the server. Do NOT proceed without this backup.

### Step 2 — Apply Flyway migrations V3–V7 against prod
```bash
# Point Flyway at prod DB
DB_URL=jdbc:mysql://<prod_host>:3306/db_a559f5_test \
DB_USERNAME=<user> DB_PASSWORD=<pass> \
./mvnw flyway:migrate -Dspring.profiles.active=prod
```
Verify `flyway_schema_history` table shows V3 through V7 as `SUCCESS`. Any `FAILED` state stops the runbook — restore from backup.

### Step 3 — Deploy new `newRepo` artifact
Deploy the updated JAR with all entity fixes (Calificacion, Establecimiento, FcmToken, Suscripcion, etc.) to the application server.

### Step 4 — Start with prod DB URL
```bash
DB_URL=jdbc:mysql://<prod_host>:3306/db_a559f5_test \
DB_USERNAME=<user> DB_PASSWORD=<pass> \
SPRING_PROFILES_ACTIVE=prod \
java -jar newRepo.jar
```
Confirm in logs: `HHH000276: Table [<table>] contains physical column name [<col>] referred to as <col> in mapping` does NOT appear. Hibernate validate completes without errors.

### Step 5 — Smoke tests
Run the following checks immediately after boot (≤ 5 minutes):

1. `GET /establecimiento` — returns list without 500 errors
2. `GET /establecimiento/{id}` — returns detail for a known ID; `fld_descripcion` not truncated
3. `GET /calificacion?idEstablecimiento={id}` — returns ratings (was broken before fix 3.1)
4. `GET /promo` — returns promotions; check image field not empty for known promo with image
5. `POST /fcm-token` (dry-run with a test token) — returns 201

### Step 6 — Monitor for 30 minutes
Watch application logs for:
- `Unknown column` errors → column mapping still wrong
- `Data too long for column` → length mapping still wrong
- `Column 'x' cannot be null` → nullable constraint mismatch

### Rollback Plan
If any smoke test fails or critical error appears in step 6:

1. Stop the new API immediately.
2. Verify the old `App300_Lugares` backend is still running and serving traffic (it was never stopped — this is a blue/green switch).
3. Assess whether the Flyway migrations (V3–V7) introduced the failure. If yes, restore from backup: `mysql -h <prod_host> -u <user> -p db_a559f5_test < backup_premigration_YYYYMMDD_HHMM.sql`. WARNING: this wipes any data written since backup. Coordinate with operations to minimize write window.
4. Investigate and fix the issue in a new migration version. Do NOT modify V3–V7 after they have been applied.

---

## 6. Risks & Open Questions

| # | Risk | Probability | Impact | Owner |
|---|------|-------------|--------|-------|
| R1 | ~~`p_filtroestablecimiento` feature gate~~ — RESOLVED: table is DEAD; tag system in `EstablecimientoRepository` already covers the functionality. | — | — | Closed |
| R2 | Prod `tipo_promocion` or `dias_disponibles` columns contain values outside the enum set (e.g. Spanish day names, free-form strings). | MEDIUM | HIGH | Run P0-4 / P0-5 queries |
| R3 | `p_historial_canjes.fld_codigo_validacion` has NULL rows in prod. If yes, V6 migration fails without a backfill step. | MEDIUM | MEDIUM | Run P0-2 query |
| R4 | Stored procedures in prod reference `fk_id_usuario` (pre-V2 rename). They are now broken. If any client still calls the legacy `App300_Lugares` API during the migration window, those SP-based endpoints will return errors. Coordinate migration window with business. | MEDIUM | MEDIUM | Operations |
| R5 | `mysql-migration-rules.md` Rule 1 documents `fkd_id_establecimiento` as an intentional prod typo. This is factually incorrect — prod has `fk_id_establecimiento` (correct). Any developer reading Rule 1 will implement the wrong column name. Rule 1 MUST be corrected before any new contributor touches `Calificacion`. | HIGH | HIGH | Fix immediately |
| R6 | VPS `p_establecimiento` schema missing UNIQUE KEY on `fld_direccion` (prod line 465: `UNIQUE KEY p_establecimiento_direccion_unique`). The newRepo entity has no `@UniqueConstraint` for this. Duplicate direccion inserts will fail on prod but succeed on VPS — divergent behavior in tests. | LOW | MEDIUM | Add to entity/migration |
| R7 | Prod `p_establecimiento` columns `fld_img_refs_2/3/4` are `varchar(500)` (prod lines 437–439) but VPS has `varchar(255)`. Image URLs > 255 chars will truncate on VPS tests but succeed on prod. | MEDIUM | MEDIUM | Add length=500 to entity |
| R8 | Flyway `baseline-on-migrate=true` will mark V1 as BASELINE on prod even though prod already has the real schema. Confirm prod does NOT already have a `flyway_schema_history` table — if it does, Flyway will attempt to apply V2 from a non-baseline start and may conflict. Run `SELECT * FROM flyway_schema_history` on prod before running `flyway:migrate`. | HIGH | HIGH | Pre-migration check |

---

*End of plan. All file:line references are verified against the actual source at time of writing.*
