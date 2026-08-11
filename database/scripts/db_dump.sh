#!/usr/bin/env bash

set -euo pipefail

usage() {
  cat <<'EOF'
Usage: db_dump.sh [output-file]

Creates a full PostgreSQL dump with schema and data.

Environment:
  DATABASE_URL  Full PostgreSQL connection string (recommended)
  PGHOST        Database host
  PGPORT        Database port
  PGUSER        Database user
  PGPASSWORD    Database password
  PGDATABASE    Database name

If no output file is provided, the script writes to:
  ./database/dumps/<database>_<timestamp>.sql
EOF
}

if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
  usage
  exit 0
fi

if [[ -n "${DATABASE_URL:-}" ]]; then
  if [[ -n "${PGDATABASE:-}" ]]; then
    db_name="$PGDATABASE"
  else
    db_name="postgres"
  fi
else
  if [[ -z "${PGDATABASE:-}" ]]; then
    echo "Error: PGDATABASE must be set unless DATABASE_URL is provided." >&2
    exit 1
  fi
  db_name="$PGDATABASE"
fi

timestamp="$(date +"%Y%m%d_%H%M%S")"
output_file="${1:-database/dumps/${db_name}_${timestamp}.sql}"

output_dir="$(dirname "$output_file")"
mkdir -p "$output_dir"

echo "Writing PostgreSQL dump to: $output_file"

if [[ -n "${DATABASE_URL:-}" ]]; then
  pg_dump \
    --format=plain \
    --no-owner \
    --no-privileges \
    --file="$output_file" \
    "$DATABASE_URL"
else
  pg_dump \
    --format=plain \
    --no-owner \
    --no-privileges \
    --file="$output_file"
fi

echo "Dump completed."
