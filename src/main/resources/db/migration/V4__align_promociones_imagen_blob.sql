-- V4: Align p_promociones.fld_imagen to longblob.
--
-- BACKGROUND (plan section 3.3):
--   VPS schema has fld_imagen as `tinyblob` (max 255 bytes) — Hibernate default
--   for @Lob byte[] on MySQL when the column did not previously exist.
--   Production has always had `longblob` (max 4 GB).
--   Any image stored in prod (up to 4 GB) would silently truncate to 255 bytes
--   on VPS reads or fail with "Data too long" on VPS writes.
--
-- IDEMPOTENCY STRATEGY:
--   MODIFY COLUMN to a larger blob type is safe and idempotent in MySQL 8.
--   Against production: no-op — column is already longblob.
--   Against VPS: upgrades tinyblob → longblob.
--
-- RISK: None — only expands allowed storage, no data loss.

ALTER TABLE p_promociones MODIFY COLUMN fld_imagen longblob;
