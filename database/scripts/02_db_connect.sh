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

echo "Connecting to PostgreSQL"
echo "========================"
echo "Host: $DATABASE_HOST"
echo "Port: $DATABASE_PORT"
echo "User: $DATABASE_USER"
echo "Database: $DATABASE_NAME"
echo ""

# Connect to the database
PGPASSWORD="$DATABASE_PASSWORD" psql \
  -h "$DATABASE_HOST" \
  -p "$DATABASE_PORT" \
  -U "$DATABASE_USER" \
  -d "$DATABASE_NAME"
