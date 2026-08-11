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

DATA_DIR="$(dirname "$0")/../data"

if [[ ! -d "$DATA_DIR" ]]; then
  echo "Error: $DATA_DIR directory not found." >&2
  exit 1
fi

echo "Populating PostgreSQL Database"
echo "=============================="
echo "Host: $DATABASE_HOST"
echo "Port: $DATABASE_PORT"
echo "Database: $DATABASE_NAME"
echo "User: $DATABASE_USER"
echo ""

# Load SQL files in order
for i in {0..8}; do
  FILE=$(printf "%s/%02d_*.sql" "$DATA_DIR" "$i")
  
  # Check if file exists (handle glob expansion)
  if ! ls $FILE 1> /dev/null 2>&1; then
    echo "Skipping: No file matching $FILE"
    continue
  fi
  
  # Get actual filename
  ACTUAL_FILE=$(ls $FILE | head -n1)
  FILENAME=$(basename "$ACTUAL_FILE")
  
  echo "Loading $FILENAME..."
  
  PGPASSWORD="$DATABASE_PASSWORD" psql \
    -h "$DATABASE_HOST" \
    -p "$DATABASE_PORT" \
    -U "$DATABASE_USER" \
    -d "$DATABASE_NAME" \
    -f "$ACTUAL_FILE" || \
    { echo "Failed to load $FILENAME"; exit 1; }
  
  echo "Done"
done

echo ""
echo "Database population complete!"
