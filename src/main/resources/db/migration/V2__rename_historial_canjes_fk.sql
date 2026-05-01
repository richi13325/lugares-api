-- V2: Rename fk_id_usuario → fk_id_cliente in p_historial_canjes.
--
-- BACKGROUND:
--   The legacy App300_Lugares column was named fk_id_usuario.
--   The new API entity (HistorialCanje.java) already maps fk_id_cliente.
--   This migration makes the DB match the entity mapping.
--
-- IDEMPOTENCY STRATEGY:
--   MySQL 8 does not support IF EXISTS on RENAME COLUMN.
--   We guard with information_schema: if fk_id_usuario does not exist
--   (rename already applied), we skip the block.
--
-- FK HANDLING:
--   MySQL refuses RENAME COLUMN on a column referenced by an active FK.
--   We drop the FK first (using information_schema to find its name),
--   execute the rename, then recreate the FK.
--
-- REQUIREMENTS: MySQL 8.0.4+ (RENAME COLUMN), MySQL 8.0.19+ (DROP FK IF EXISTS).
--   REQ-013 mandates MySQL 8.0+ minimum.

-- Step 1: Drop FK constraint on fk_id_usuario if it exists.
-- We use a prepared statement to handle the dynamic constraint name.
-- IMPORTANT: Flyway executes each statement terminated by ';'. The
-- SET + PREPARE + EXECUTE + DEALLOCATE pattern works fine in JDBC
-- (unlike DELIMITER-based stored procedure syntax which JDBC cannot parse).

SET @fk_name = (
    SELECT CONSTRAINT_NAME
    FROM information_schema.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'p_historial_canjes'
      AND COLUMN_NAME = 'fk_id_usuario'
      AND REFERENCED_TABLE_NAME IS NOT NULL
    LIMIT 1
);

SET @drop_fk_sql = IF(
    @fk_name IS NOT NULL,
    CONCAT('ALTER TABLE p_historial_canjes DROP FOREIGN KEY `', @fk_name, '`'),
    'SELECT 1' -- no-op if FK not found
);

PREPARE stmt_drop_fk FROM @drop_fk_sql;
EXECUTE stmt_drop_fk;
DEALLOCATE PREPARE stmt_drop_fk;

-- Step 2: Rename fk_id_usuario → fk_id_cliente, guarded by existence check.
SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'p_historial_canjes'
      AND COLUMN_NAME = 'fk_id_usuario'
);

SET @rename_sql = IF(
    @col_exists > 0,
    'ALTER TABLE p_historial_canjes RENAME COLUMN fk_id_usuario TO fk_id_cliente',
    'SELECT 1' -- no-op: column already renamed
);

PREPARE stmt_rename FROM @rename_sql;
EXECUTE stmt_rename;
DEALLOCATE PREPARE stmt_rename;

-- Step 3: Recreate the FK constraint pointing at the same parent table.
-- Guarded: only recreate if the table exists, fk_id_cliente column exists, and FK does not already exist.
SET @table_exists = (
    SELECT COUNT(*)
    FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'p_historial_canjes'
);

SET @fk_already_exists = (
    SELECT COUNT(*)
    FROM information_schema.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'p_historial_canjes'
      AND COLUMN_NAME = 'fk_id_cliente'
      AND REFERENCED_TABLE_NAME IS NOT NULL
    LIMIT 1
);

SET @add_fk_sql = IF(
    @table_exists > 0 AND @fk_already_exists = 0,
    'ALTER TABLE p_historial_canjes ADD CONSTRAINT fk_historial_canjes_cliente FOREIGN KEY (fk_id_cliente) REFERENCES p_cliente (id_cliente)',
    'SELECT 1'
);

PREPARE stmt_add_fk FROM @add_fk_sql;
EXECUTE stmt_add_fk;
DEALLOCATE PREPARE stmt_add_fk;
