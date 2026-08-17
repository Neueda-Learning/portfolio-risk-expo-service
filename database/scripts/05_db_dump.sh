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

timestamp="$(date +"%Y%m%d_%H%M%S")"
output_file="${1:-$(dirname "$0")/../dumps/${DATABASE_NAME}_${timestamp}.sql}"

output_dir="$(dirname "$output_file")"
mkdir -p "$output_dir"

echo "PostgreSQL Database Dump"
echo "========================"
echo "Host: $DATABASE_HOST"
echo "Port: $DATABASE_PORT"
echo "Database: $DATABASE_NAME"
echo "Output: $output_file"
echo ""

echo "Creating database dump..."
PGPASSWORD="$DATABASE_PASSWORD" pg_dump \
  --format=plain \
  --no-owner \
  --no-privileges \
  -h "$DATABASE_HOST" \
  -p "$DATABASE_PORT" \
  -U "$DATABASE_USER" \
  -d "$DATABASE_NAME" \
  --file="$output_file"

if [[ -f "$output_file" ]]; then
  file_size=$(du -h "$output_file" | cut -f1)
  echo "Dump completed successfully! ($file_size)"
else
  echo "Error: Dump file was not created." >&2
  exit 1
fi
