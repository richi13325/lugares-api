-- V6: Align p_historial_canjes.fld_codigo_validacion to varchar(255) NOT NULL.
--
-- BACKGROUND (plan section 3.6):
--   Production has fld_codigo_validacion as `varchar(50) DEFAULT NULL` — nullable, 50 chars.
--   VPS has `varchar(255) NOT NULL` — Hibernate-generated from the entity annotation.
--   HistorialCanje.java declares `@Column(name = "fld_codigo_validacion", nullable = false)`.
--   Pre-flight confirmed: 0 NULL rows exist in p_historial_canjes.fld_codigo_validacion.
--   Adding DEFAULT '' and NOT NULL is therefore safe — no backfill needed.
--
-- IDEMPOTENCY STRATEGY:
--   MODIFY COLUMN on a column that already matches varchar(255) NOT NULL is a no-op.
--   Against production: expands varchar(50) → varchar(255), removes NULL, adds DEFAULT ''.
--   Against VPS: no-op — column is already varchar(255) NOT NULL.
--
-- RISK: Low. Zero NULL rows confirmed. Max existing code length unknown but codes are
--   typically short validation strings well under 50 chars. Column only expands capacity.

ALTER TABLE p_historial_canjes
    MODIFY COLUMN fld_codigo_validacion varchar(255) NOT NULL DEFAULT '';
