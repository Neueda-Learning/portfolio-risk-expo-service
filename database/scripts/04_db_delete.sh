#!/usr/bin/env bash

set -euo pipefail

# Load variables from .env in root directory
ENV_FILE="${1:-$(dirname "$0")/../../.env}"

if [[ ! -f "$ENV_FILE" ]]; then
  echo "Error: $ENV_FILE not found." >&2
  exit 1
fi

source "$ENV_FILE"

# Validate required variables
if [[ -z "${DATABASE_HOST:-}" || -z "${DATABASE_PORT:-}" || -z "${DATABASE_NAME:-}" || -z "${DATABASE_USER:-}" ]]; then
  echo "Error: All DATABASE_* variables must be set in $ENV_FILE" >&2
  exit 1
fi

DB_ADMIN_USER="${DATABASE_ADMIN_USER:-postgres}"

echo "WARNING: This will delete the database and user!"
echo "=================================================="
echo "Host: $DATABASE_HOST"
echo "Port: $DATABASE_PORT"
echo "Database: $DATABASE_NAME"
echo "User: $DATABASE_USER"
echo ""
read -p "Are you sure? Type 'YES' to confirm: " confirm

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
DB_DELETED=false
if PGPASSWORD="$DB_ADMIN_PASSWORD" psql \
  -h "$DATABASE_HOST" \
  -p "$DATABASE_PORT" \
  -U "$DB_ADMIN_USER" \
  -d "postgres" \
  -c "DROP DATABASE IF EXISTS $DATABASE_NAME;" | grep -q "DROP DATABASE"; then
  DB_DELETED=true
else
  echo "  -> Database may not exist or could not be dropped"
fi

# Drop user
echo "Dropping user $DATABASE_USER..."
USER_DELETED=false
if PGPASSWORD="$DB_ADMIN_PASSWORD" psql \
  -h "$DATABASE_HOST" \
  -p "$DATABASE_PORT" \
  -U "$DB_ADMIN_USER" \
  -d "postgres" \
  -c "DROP USER IF EXISTS $DATABASE_USER;" | grep -q "DROP ROLE"; then
  USER_DELETED=true
else
  echo "  -> User may not exist or could not be dropped"
fi

echo ""
if $DB_DELETED && $USER_DELETED; then
  echo "Database and user deleted!"
elif $DB_DELETED; then
  echo "Database deleted! (User was not deleted)"
elif $USER_DELETED; then
  echo "User deleted! (Database was not deleted)"
else
  echo "Neither database nor user were deleted."
fi
