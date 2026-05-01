# MySQL Migration Rules

Project: `newRepo` (300lugares-api — Spring Boot 3.5 / Java 21)
Counterpart legacy DB: `App300_Lugares` (MySQL 8+)
Created: Wave 0 of `mysql-reconnect` change — MUST be committed before any code change.

---

## The 10 Rules

### Rule 1 — Known column name typos in `fcm_tokens`

Only one column name typo exists in the production DB:

| Column | Table | Typo | Correct spelling (irrelevant) |
|--------|-------|------|-------------------------------|
| `plataform` | `fcm_tokens` | missing final `e` | `platform` |

Any JPA entity mapping this column MUST use the exact typo name in `@Column(name = ...)`:

```java
@Column(name = "plataform")   // FcmToken entity
```

Renaming this column requires a coordinated Flyway migration AND a client-side update — that is out of scope unless explicitly planned.

---

**Historical note — `fkd_id_establecimiento` (CORRECTED 2026-04-27):**

This rule previously listed `fkd_id_establecimiento` in `d_estrella_x_cliente` as a "production typo to preserve". **That was incorrect.** Investigation against the production dump confirmed that production has always had the correct name `fk_id_establecimiento`. The typo `fkd_id_establecimiento` only existed in the VPS schema because Hibernate auto-generated the column name from a wrong entity field in `Calificacion.java` (the Java field used the wrong string literal in `@JoinColumn(name = ...)`).

**Fix applied:** `Calificacion.java` `@JoinColumn` was updated to `fk_id_establecimiento` (matches production). No Flyway migration is needed — production already has the correct column name. The VPS schema will self-correct on next `ddl-auto=update` startup.

Do NOT reintroduce `fkd_id_establecimiento` anywhere in entity code or migrations.

---

### Rule 2 — `fk_id_usuario` has been renamed to `fk_id_cliente` in `p_historial_canjes`

This rename is intentional and was performed via Flyway migration `V2__rename_historial_canjes_fk.sql`.

- **Before V2**: column was `fk_id_usuario` in the legacy `App300_Lugares` DB.
- **After V2**: column is `fk_id_cliente` — this is the name the NEW API entity (`HistorialCanje.java`) already uses.
- **Consequence**: do NOT reference `fk_id_usuario` anywhere in application code, JPQL queries, or native SQL. The column no longer exists after V2 runs.

---

### Rule 3 — All schema changes go through Flyway. No ad-hoc DDL.

**Never** run `ALTER TABLE`, `CREATE TABLE`, `DROP TABLE`, or any DDL directly against the production (or staging) DB by hand. Every schema change must be:

1. Written as a new Flyway migration script: `src/main/resources/db/migration/V<N>__<description>.sql`
2. Reviewed and tested against a local copy first.
3. Committed to version control before being applied to any shared environment.

Once a migration script has been applied (recorded in `flyway_schema_history`), its content is **immutable**. Never edit a committed migration. Create a new version instead.

---

### Rule 4 — `V1__baseline.sql` is intentionally empty

`src/main/resources/db/migration/V1__baseline.sql` contains only a comment. This is by design.

The legacy `App300_Lugares` schema is adopted as-is via `spring.flyway.baseline-on-migrate=true` and `baseline-version=1`. Flyway marks the pre-existing production schema as the `V1` baseline without executing any DDL.

**Do NOT add DDL to `V1__baseline.sql`** — it will never run against the production DB (baseline scripts are not executed). It exists to make the intent explicit and to anchor the migration history.

---

### Rule 5 — Dev profile MUST point at a local MySQL instance. NEVER at the production host.

`application-dev.yml` sets a default datasource URL of `jdbc:mysql://localhost:3306/lugares_dev`. This is intentional.

- **Correct dev setup**: local MySQL 8 Docker container (see `docker/docker-compose.yml`).
- **Wrong**: setting `DB_URL` in your `.env` to the production or staging hostname while running the `dev` profile.

`ddl-auto=update` is active in the `dev` profile. If you point it at the production DB with `ddl-auto=update`, Hibernate will mutate the live schema. This is catastrophic and irreversible without a backup.

If the app fails to start with `Communications link failure` or `Connection refused`, that is correct and expected when no local MySQL is running — it is a deliberate safety mechanism.

---

### Rule 6 — Developer bootstrap MUST use a `mysqldump` of the real DB. Not JPA auto-create.

`V1__baseline.sql` is empty. Flyway will apply it as baseline and then attempt to run `V2__rename_historial_canjes_fk.sql`. **If the DB is empty, V2 will fail** because `p_historial_canjes` does not exist.

The correct bootstrap path for a fresh local environment:

1. Start the local MySQL container: `docker compose -f docker/docker-compose.yml up -d mysql`
2. Wait for MySQL to be ready (see the Dump Import Procedure below).
3. Obtain a `mysqldump` of the real DB from the legacy `App300_Lugares` server.
4. Restore the dump into the local `lugares_dev` database.
5. Start the Spring Boot app — Flyway applies V1 as BASELINE, then V2 as SUCCESS.

See the full step-by-step in the **"Developer DB Bootstrap Procedure"** section (added in Wave 3).

---

### Rule 7 — MySQL server version MUST be 8.0 or higher

`V2__rename_historial_canjes_fk.sql` uses `ALTER TABLE ... RENAME COLUMN` (MySQL 8.0.4+) and `ALTER TABLE ... DROP FOREIGN KEY IF EXISTS` (MySQL 8.0.19+). These statements will fail on MySQL 5.7.

Production server requirements:
- MySQL **8.0.19** or higher (covers both RENAME COLUMN and DROP FK IF EXISTS).
- Collation: `utf8mb4_0900_ai_ci` (MySQL 8 default) — consistent with the legacy DB.

For local development, the `docker/docker-compose.yml` uses `mysql:8.0` and sets this collation explicitly.

---

### Rule 8 — `mysql-connector-j` version is managed by Spring Boot BOM. Do NOT pin it manually.

In `pom.xml`, the `com.mysql:mysql-connector-j` dependency has **no `<version>` tag**. The version is resolved from the Spring Boot 3.5 parent BOM.

```xml
<!-- CORRECT -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- WRONG — do not add a version tag -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>9.x.x</version>   <!-- DO NOT DO THIS -->
    <scope>runtime</scope>
</dependency>
```

The same applies to `flyway-core` and `flyway-mysql`. Manually pinning versions risks drift from the tested BOM matrix.

---

### Rule 9 — No stored procedure calls from the new API

The legacy `App300_Lugares` MySQL DB contains 21 stored procedures. The `newRepo` API MUST NOT call any of them.

Specifically, the following are forbidden anywhere in the codebase:

- `@NamedStoredProcedureQuery` annotations on entities
- `@Procedure` annotations on repository methods
- Native SQL `CALL <sp_name>(...)` statements (in `@Query` or `EntityManager.createNativeQuery`)

The stored procedures are preserved in the DB for reference and possible rollback, but they are orphaned from the new API. Any SPs that reference the old column name `fk_id_usuario` (now renamed to `fk_id_cliente`) are broken and will remain so until a future wave explicitly decides to drop or rewrite them.

---

### Rule 10 — Any FK rename migration MUST drop and recreate the FK constraint

MySQL refuses `RENAME COLUMN` on a column that is referenced by an active FOREIGN KEY constraint. The migration must:

1. Query `information_schema.KEY_COLUMN_USAGE` to find the constraint name dynamically (FK constraint names vary per DB installation).
2. Drop the FK constraint.
3. Execute the `RENAME COLUMN`.
4. Optionally recreate the FK constraint pointing at the same parent table/column.

**Never** assume MySQL will cascade-rename FK references. It does not.

This pattern is demonstrated in full in `V2__rename_historial_canjes_fk.sql` using `PREPARE/EXECUTE/DEALLOCATE` blocks (not `DELIMITER`-based syntax, which JDBC cannot parse).

---

## Procedure: Importing a production MySQL dump

Use this procedure every time you need to seed a fresh local environment from the real
`App300_Lugares` MySQL DB. All steps run locally — the production server is only touched
in step 1 (read-only dump).

---

### Step 1 — Generate the dump (on the legacy server or any host with access)

```bash
mysqldump -h <host> -u <user> -p \
  --single-transaction \
  --quick \
  --routines \
  --triggers \
  --events \
  --add-drop-database \
  --databases <db_name> \
  > dump_YYYYMMDD_HHMM.sql
```

Flag notes:
- `--single-transaction` — consistent snapshot without table locks (InnoDB safe).
- `--routines --triggers --events` — preserves the 21 stored procedures (kept orphaned per Rule 9).
- `--add-drop-database` — makes the dump self-contained (safe for local import).
- Name the file with date-time per Rule 1 convention (`dump_YYYYMMDD_HHMM.sql`).

> **Security**: the dump may contain PII (client emails, phones, etc.).  
> Treat `dump_*.sql` files as sensitive. They are gitignored via `docker/init/*.sql`.

---

### Step 2 — Place the dump in `docker/init/`

```bash
cp dump_YYYYMMDD_HHMM.sql docker/init/
```

MySQL's Docker image auto-imports every `.sql` file in `/docker-entrypoint-initdb.d/`
alphabetically on the **first** container start (i.e., when the data volume is empty).

---

### Step 3 — Start the local MySQL 8 container

```bash
docker compose -f docker/docker-compose.yml up -d
```

First startup auto-imports the dump. Wait for the container to be ready:

```bash
docker compose -f docker/docker-compose.yml logs mysql | grep "ready for connections"
```

This typically takes 10–30 s. The container healthcheck also reflects readiness
(`docker compose -f docker/docker-compose.yml ps` shows `healthy`).

---

### Step 4 — Run Flyway migrate

```bash
./mvnw flyway:migrate -Dspring.profiles.active=dev
```

Expected outcome:
- Flyway baselines at V0 (records the existing schema as `BASELINE` — no DDL executed).
- Applies `V2__rename_historial_canjes_fk.sql` (renames `fk_id_usuario → fk_id_cliente`).
- V1 is a comment-only baseline marker — never executed as DDL.

If V2 fails with a FK constraint error, run `SHOW CREATE TABLE p_historial_canjes` in the local
container and compare the FK parent table/column against Step 3 of the V2 script. Update V2
with the correct parent reference (see Rule 10 and design risk RD-1).

---

### Step 5 — Boot the app with `validate` and observe

```bash
SPRING_PROFILES_ACTIVE=prod ./mvnw spring-boot:run
```

Hibernate runs `ddl-auto=validate` against the migrated schema. Happy path: app reaches
`RUNNING` state with no errors. Any mismatch surfaces as a `SchemaManagementException`
in the log with the exact column/type diff.

**For each validate mismatch**: create a new `V<N>__<desc>.sql` migration (Rule 3).
Never edit `V2__rename_historial_canjes_fk.sql` after it has been applied.

---

### Step 6 — Tear-down (when resetting tests or re-importing a newer dump)

```bash
docker compose -f docker/docker-compose.yml down -v
```

`-v` wipes the `mysql-data` volume. Re-run from Step 2 with the new dump file.

> Do NOT use `-v` against production data. It is irreversible for that volume.
