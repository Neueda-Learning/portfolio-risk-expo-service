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
if [[ -z "${DATABASE_HOST:-}" || -z "${DATABASE_PORT:-}" || -z "${DATABASE_NAME:-}" || -z "${DATABASE_USER:-}" || -z "${DATABASE_PASSWORD:-}" ]]; then
  echo "Error: All DATABASE_* variables must be set in $ENV_FILE" >&2
  exit 1
fi

DB_ADMIN_USER="${DATABASE_ADMIN_USER:-postgres}"

echo "PostgreSQL Database Setup"
echo "========================="
echo "Host: $DATABASE_HOST"
echo "Port: $DATABASE_PORT"
echo "Admin User: $DB_ADMIN_USER"
echo "New User: $DATABASE_USER"
echo "New Database: $DATABASE_NAME"
echo ""

# Prompt for admin password
read -sp "Enter password for $DB_ADMIN_USER: " DB_ADMIN_PASSWORD
echo ""

# Create user
echo "Creating user $DATABASE_USER..."
USER_CREATED=false
if PGPASSWORD="$DB_ADMIN_PASSWORD" psql \
  -h "$DATABASE_HOST" \
  -p "$DATABASE_PORT" \
  -U "$DB_ADMIN_USER" \
  -d "postgres" \
  -c "CREATE USER $DATABASE_USER WITH PASSWORD '$DATABASE_PASSWORD';" | grep -q "CREATE ROLE"; then
  USER_CREATED=true
else
  echo "  -> User may already exist or could not be created"
fi

# Create database
echo "Creating database $DATABASE_NAME..."
DB_CREATED=false
if PGPASSWORD="$DB_ADMIN_PASSWORD" psql \
  -h "$DATABASE_HOST" \
  -p "$DATABASE_PORT" \
  -U "$DB_ADMIN_USER" \
  -d "postgres" \
  -c "CREATE DATABASE $DATABASE_NAME OWNER $DATABASE_USER;" | grep -q "CREATE DATABASE"; then
  DB_CREATED=true
else
  echo "  -> Database may already exist or could not be created"
fi

# Grant privileges
echo "Granting privileges on schema public..."
PGPASSWORD="$DB_ADMIN_PASSWORD" psql \
  -h "$DATABASE_HOST" \
  -p "$DATABASE_PORT" \
  -U "$DB_ADMIN_USER" \
  -d "$DATABASE_NAME" \
  -c "GRANT ALL PRIVILEGES ON SCHEMA public TO $DATABASE_USER;"

echo ""
if $USER_CREATED && $DB_CREATED; then
  echo "Setup complete!"
elif $USER_CREATED; then
  echo "Setup complete! (Database was not created)"
elif $DB_CREATED; then
  echo "Setup complete! (User was not created)"
else
  echo "Setup complete! (Note: User and/or database may have already existed)"
fi
