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

timestamp="$(date +"%Y%m%d_%H%M%S")"
output_dir="$(dirname "$0")/../views_csv"
mkdir -p "$output_dir"

limit_breaches_csv="$output_dir/limit_breaches_vw_${timestamp}.csv"
exposure_by_asset_class_csv="$output_dir/exposure_by_asset_class_vw_${timestamp}.csv"

echo "PostgreSQL Risk Report Export"
echo "============================="
echo "Host: $DATABASE_HOST"
echo "Port: $DATABASE_PORT"
echo "Database: $DATABASE_NAME"
echo "Output Directory: $output_dir"
echo ""

echo "Exporting limit_breaches_vw..."
PGPASSWORD="$DATABASE_PASSWORD" psql \
  -X \
  -h "$DATABASE_HOST" \
  -p "$DATABASE_PORT" \
  -U "$DATABASE_USER" \
  -d "$DATABASE_NAME" \
  -v ON_ERROR_STOP=1 \
  -c "\\copy (SELECT * FROM limit_breaches_vw) TO '$limit_breaches_csv' CSV HEADER"

echo "Exporting exposure_by_asset_class_vw..."
PGPASSWORD="$DATABASE_PASSWORD" psql \
  -X \
  -h "$DATABASE_HOST" \
  -p "$DATABASE_PORT" \
  -U "$DATABASE_USER" \
  -d "$DATABASE_NAME" \
  -v ON_ERROR_STOP=1 \
  -c "\\copy (SELECT * FROM exposure_by_asset_class_vw) TO '$exposure_by_asset_class_csv' CSV HEADER"

echo ""
echo "Risk report export complete!"
echo "  - $limit_breaches_csv"
echo "  - $exposure_by_asset_class_csv"