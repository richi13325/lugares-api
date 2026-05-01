-- V3: Align p_suscripcion.fld_precio to decimal(10,0) NOT NULL.
--
-- BACKGROUND (plan section 3.2):
--   VPS schema has fld_precio as `double` because Hibernate generated it from
--   Suscripcion.java which previously declared `private Double precio`.
--   Production has always had `decimal(10,0) NOT NULL` — integer-precision decimal.
--   Suscripcion.java has been updated to BigDecimal with precision=10 scale=0.
--
-- IDEMPOTENCY STRATEGY:
--   MODIFY COLUMN on a column that already matches the target type is a no-op in
--   MySQL 8 (it rewrites metadata but does not move data). Safe to run twice.
--   Against production: no-op — column is already decimal(10,0) NOT NULL.
--   Against VPS: converts double → decimal(10,0) NOT NULL.
--
-- RISK: None — current prod data contains only integer prices (e.g. 200, 500).

ALTER TABLE p_suscripcion MODIFY COLUMN fld_precio decimal(10,0) NOT NULL;
