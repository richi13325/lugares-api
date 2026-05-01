# Docker — Local MySQL 8 for Development

This directory contains the Docker Compose setup for running a local MySQL 8 instance
used during Wave 3 schema validation and Wave 4 smoke tests of the `mysql-reconnect` change.

> NOT for production. Local development only.

---

## Prerequisites

- Docker Desktop running on the host.
- A copy of `docker/.env.example` saved as `docker/.env` with your local credentials filled in.

```
cp docker/.env.example docker/.env
# Edit docker/.env — set MYSQL_ROOT_PASSWORD, MYSQL_USER, MYSQL_PASSWORD
```

The `.env` file lives inside the `docker/` directory, NOT the project root. This keeps dev secrets
isolated and out of the Spring Boot application context.

---

## Start / Stop

```bash
# Start in background (detached)
docker compose -f docker/docker-compose.yml up -d

# View logs (wait for "ready for connections" before connecting)
docker compose -f docker/docker-compose.yml logs -f mysql

# Stop (keeps the volume / data)
docker compose -f docker/docker-compose.yml down

# Stop AND wipe the volume (full reset — re-import required)
docker compose -f docker/docker-compose.yml down -v
```

---

## Importing a Production Dump

SQL files placed in `docker/init/` are auto-imported **on the first container start** (alphabetically).

1. Copy your dump file:
   ```bash
   cp /path/to/dump_20260422_1800.sql docker/init/
   ```

2. Start the container:
   ```bash
   docker compose -f docker/docker-compose.yml up -d
   ```

3. Wait for MySQL to be ready (10–30 s):
   ```bash
   docker compose -f docker/docker-compose.yml logs mysql | grep "ready for connections"
   ```

**To replace the dump with a newer one:**

```bash
docker compose -f docker/docker-compose.yml down -v   # wipes volume
rm docker/init/old_dump.sql
cp /path/to/new_dump.sql docker/init/
docker compose -f docker/docker-compose.yml up -d
```

Note: `-v` wipes `mysql-data`. Do NOT run this against production data.

---

## Connecting

```bash
# Via mysql CLI
mysql -h 127.0.0.1 -P 3306 -u <MYSQL_USER> -p<MYSQL_PASSWORD> lugares_dev

# Or as root
mysql -h 127.0.0.1 -P 3306 -u root -p<MYSQL_ROOT_PASSWORD> lugares_dev
```

---

## Credentials

Credentials come from `docker/.env` (NOT the project root `.env`).

| Variable             | Description               | Example value |
|----------------------|---------------------------|---------------|
| `MYSQL_ROOT_PASSWORD`| MySQL root password        | `changeme`    |
| `MYSQL_DATABASE`     | Database created on boot   | `lugares_dev` |
| `MYSQL_USER`         | App user                   | `lugares`     |
| `MYSQL_PASSWORD`     | App user password          | `changeme`    |

`docker/.env` is gitignored. Never commit real passwords. See `docker/.env.example` for the template.

---

## Gitignore Notes

- `docker/.env` — gitignored (real secrets)
- `docker/init/*.sql` — gitignored (dump files may contain PII)
- `docker/init/.gitkeep` — tracked (keeps the directory in git)
