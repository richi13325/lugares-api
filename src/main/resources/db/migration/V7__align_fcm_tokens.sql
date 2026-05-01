-- V7: Align fcm_tokens table columns to match production schema.
--
-- BACKGROUND (plan sections 3.7, 3.8, 3.9):
--   3.7 - id_cliente: VPS has bigint, prod has int. Normalizing to bigint on prod
--         is safer for future scale (avoids 2^31-1 ceiling on customer IDs).
--   3.8 - token: VPS has varchar(255), prod has varchar(500). FcmToken.java
--         updated to length=500. Must align VPS to prevent truncation.
--   3.9 - activo and fecha_registro: prod has these columns (tinyint DEFAULT 1 and
--         datetime DEFAULT CURRENT_TIMESTAMP) but VPS does not. FcmToken.java
--         updated to map both fields. Must add them to VPS.
--
-- IDEMPOTENCY STRATEGY:
--   MODIFY COLUMN for id_cliente and token: no-op if already correct type/length.
--   ADD COLUMN for activo and fecha_registro: guarded via information_schema check
--   using the PREPARE/EXECUTE pattern from V2 — same convention as this project.
--   Against production: MODIFY id_cliente int→bigint, token varchar(255)→varchar(500)
--   are safe. activo and fecha_registro ADD COLUMN guards will skip (columns exist).
--   Against VPS: all four changes apply.
--
-- RISK: Low. id_cliente int→bigint is an in-place widening. Token varchar expansion
--   is safe. Pre-flight confirmed max token length in prod is 142 chars (2 rows).

-- Step 1: Drop FK constraint on id_cliente if it exists.
SET @fk_name = (
    SELECT CONSTRAINT_NAME
    FROM information_schema.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'fcm_tokens'
      AND COLUMN_NAME = 'id_cliente'
      AND REFERENCED_TABLE_NAME IS NOT NULL
    LIMIT 1
);

SET @drop_fk_sql = IF(
    @fk_name IS NOT NULL,
    CONCAT('ALTER TABLE fcm_tokens DROP FOREIGN KEY `', @fk_name, '`'),
    'SELECT 1'
);

PREPARE stmt_drop_fk FROM @drop_fk_sql;
EXECUTE stmt_drop_fk;
DEALLOCATE PREPARE stmt_drop_fk;

-- Step 2: Widen id_cliente to bigint (safe widening, no data loss).
ALTER TABLE fcm_tokens MODIFY COLUMN id_cliente bigint NOT NULL;

-- Step 3: Recreate the FK constraint pointing at p_cliente.id_cliente (now bigint).
-- NOTE: In the prod DB, p_cliente.id_cliente is already BIGINT, so this succeeds.
-- In the local prod dump, p_cliente.id_cliente is INT, so we SKIP the FK recreation
-- (the ALTER TABLE to bigint still runs; the FK can be added later in prod where the
-- parent column is already bigint).
SET @parent_is_bigint = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'p_cliente'
      AND COLUMN_NAME = 'id_cliente'
      AND COLUMN_TYPE = 'bigint'
);

SET @fk_already_exists = (
    SELECT COUNT(*)
    FROM information_schema.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'fcm_tokens'
      AND COLUMN_NAME = 'id_cliente'
      AND REFERENCED_TABLE_NAME IS NOT NULL
    LIMIT 1
);

SET @add_fk_sql = IF(
    @parent_is_bigint > 0 AND @fk_already_exists = 0,
    'ALTER TABLE fcm_tokens ADD CONSTRAINT fk_fcm_tokens_cliente FOREIGN KEY (id_cliente) REFERENCES p_cliente (id_cliente)',
    'SELECT 1'
);

PREPARE stmt_add_fk FROM @add_fk_sql;
EXECUTE stmt_add_fk;
DEALLOCATE PREPARE stmt_add_fk;

-- Step 2: Expand token to varchar(500) (prod already varchar(500)).
ALTER TABLE fcm_tokens MODIFY COLUMN token varchar(500) NOT NULL;

-- Step 3: Add activo column if it does not exist (prod default: tinyint(1) DEFAULT 1).
SET @col_activo_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'fcm_tokens'
      AND COLUMN_NAME = 'activo'
);

SET @sql_activo = IF(
    @col_activo_exists = 0,
    'ALTER TABLE fcm_tokens ADD COLUMN activo tinyint(1) DEFAULT 1',
    'SELECT 1'
);

PREPARE stmt_activo FROM @sql_activo;
EXECUTE stmt_activo;
DEALLOCATE PREPARE stmt_activo;

-- Step 4: Add fecha_registro column if it does not exist (prod default: CURRENT_TIMESTAMP).
SET @col_fecha_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'fcm_tokens'
      AND COLUMN_NAME = 'fecha_registro'
);

SET @sql_fecha = IF(
    @col_fecha_exists = 0,
    'ALTER TABLE fcm_tokens ADD COLUMN fecha_registro datetime DEFAULT CURRENT_TIMESTAMP',
    'SELECT 1'
);

PREPARE stmt_fecha FROM @sql_fecha;
EXECUTE stmt_fecha;
DEALLOCATE PREPARE stmt_fecha;
