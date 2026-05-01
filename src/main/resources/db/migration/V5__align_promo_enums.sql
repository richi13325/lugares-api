-- V5: Align p_promociones.tipo_promocion and p_promocion_dias.dias_disponibles
--     to enum types matching the Java entity mappings.
--
-- BACKGROUND (plan sections 3.4 and 3.5):
--   VPS has both columns as enum (Hibernate-generated from @Enumerated(EnumType.STRING)).
--   Production has both as varchar — any string was accepted.
--   Pre-flight verification confirmed:
--     - tipo_promocion: only 'SEMANAL' exists in prod (no FECHA, no other values).
--     - dias_disponibles: all values match Java DayOfWeek uppercase names
--       (MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY).
--   Converting varchar → enum with existing values that all fit the enum is safe.
--
-- IDEMPOTENCY STRATEGY:
--   MODIFY COLUMN on a column already declared as the target enum is a no-op in MySQL 8.
--   Against production: converts varchar(10) → enum('FECHA','SEMANAL') — safe, all rows fit.
--   Against VPS: no-op — columns are already enum with these values.
--
-- RISK: If any prod row contains a value outside the enum set, this migration will fail.
--   Pre-flight confirmed zero such rows exist as of 2026-04-27.

ALTER TABLE p_promociones
    MODIFY COLUMN tipo_promocion enum('FECHA','SEMANAL') NOT NULL;

ALTER TABLE p_promocion_dias
    MODIFY COLUMN dias_disponibles enum('MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY') NOT NULL;
