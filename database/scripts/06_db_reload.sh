#!/usr/bin/env bash

set -euo pipefail

# Load variables from .env in root directory
ENV_FILE="${2:-$(dirname "$0")/../../.env}"

if [[ ! -f "$ENV_FILE" ]]; then
  echo "Error: $ENV_FILE not found." >&2
  exit 1
fi

source "$ENV_FILE"

# Validate required variables
if [[ -z "${DATABASE_HOST:-}" || -z "${DATABASE_PORT:-}" || -z "${DATABASE_NAME:-}" || -z "${DATABASE_USER:-}" || -z "${DATABASE_PASSWORD:-}" ]]; then
  echo "Error: All DATABASE_* variables must be set in $ENV_FILE" >&2
  exit 1
fi

dump_file="${1:-}"

# If no dump file provided, find the latest one from database/dumps
if [[ -z "$dump_file" ]]; then
  dumps_dir="$(dirname "$0")/../dumps"
  
  if [[ ! -d "$dumps_dir" ]]; then
    echo "Error: Dumps directory not found: $dumps_dir" >&2
    exit 1
  fi
  
  # Find the latest SQL dump file
  dump_file=$(find "$dumps_dir" -maxdepth 1 -name "*.sql" -type f -printf '%T@ %p\n' | sort -rn | head -1 | cut -d' ' -f2-)
  
  if [[ -z "$dump_file" ]]; then
    echo "Error: No dump files found in $dumps_dir" >&2
    echo "Usage: $0 [dump-file] [.env-file]" >&2
    exit 1
  fi
fi

if [[ ! -f "$dump_file" ]]; then
  echo "Error: Dump file not found: $dump_file" >&2
  exit 1
fi

DB_ADMIN_USER="${DATABASE_ADMIN_USER:-postgres}"

echo "PostgreSQL Database Reload"
echo "=========================="
echo "Host: $DATABASE_HOST"
echo "Port: $DATABASE_PORT"
echo "Database: $DATABASE_NAME"
echo "Dump File: $dump_file"
echo ""
read -p "Are you sure? This will DROP the database and reload from dump. Type 'YES' to confirm: " confirm

if [[ "$confirm" != "YES" ]]; then
  echo "Cancelled."
  exit 0
fi

# Prompt for admin password
read -sp "Enter password for $DB_ADMIN_USER: " DB_ADMIN_PASSWORD
echo ""

# Terminate connections to the database
echo "Terminating connections to database..."
PGPASSWORD="$DB_ADMIN_PASSWORD" psql \
  -h "$DATABASE_HOST" \
  -p "$DATABASE_PORT" \
  -U "$DB_ADMIN_USER" \
  -d "postgres" \
  -c "SELECT pg_terminate_backend(pg_stat_activity.pid) FROM pg_stat_activity WHERE pg_stat_activity.datname = '$DATABASE_NAME' AND pid <> pg_backend_pid();" || true

# Drop database
echo "Dropping database $DATABASE_NAME..."
DB_DROPPED=false
if PGPASSWORD="$DB_ADMIN_PASSWORD" psql \
  -h "$DATABASE_HOST" \
  -p "$DATABASE_PORT" \
  -U "$DB_ADMIN_USER" \
  -d "postgres" \
  -c "DROP DATABASE IF EXISTS $DATABASE_NAME;"; then
  DB_DROPPED=true
else
  echo "  -> Database may not exist or could not be dropped"
fi

# Create database
echo "Creating database $DATABASE_NAME..."
DB_CREATED=false
if PGPASSWORD="$DB_ADMIN_PASSWORD" psql \
  -h "$DATABASE_HOST" \
  -p "$DATABASE_PORT" \
  -U "$DB_ADMIN_USER" \
  -d "postgres" \
  -c "CREATE DATABASE $DATABASE_NAME OWNER $DATABASE_USER;"; then
  DB_CREATED=true
else
  echo "  -> Database could not be created"
  exit 1
fi

# Grant privileges
echo "Granting privileges on schema public..."
PGPASSWORD="$DB_ADMIN_PASSWORD" psql \
  -h "$DATABASE_HOST" \
  -p "$DATABASE_PORT" \
  -U "$DB_ADMIN_USER" \
  -d "$DATABASE_NAME" \
  -c "GRANT ALL PRIVILEGES ON SCHEMA public TO $DATABASE_USER;"

# Load dump file
echo "Loading dump file..."
PGPASSWORD="$DATABASE_PASSWORD" psql \
  -h "$DATABASE_HOST" \
  -p "$DATABASE_PORT" \
  -U "$DATABASE_USER" \
  -d "$DATABASE_NAME" \
  -f "$dump_file"

echo ""
if $DB_DROPPED && $DB_CREATED; then
  echo "Reload completed successfully!"
else
  echo "Reload completed with warnings."
fi
