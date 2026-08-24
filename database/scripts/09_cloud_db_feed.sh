#!/bin/bash

# Import all .sql files from ../data in filename order.
DATA_DIR="$(cd "$(dirname "$0")/../data" && pwd)"
BUCKET="gs://app-db-feed-scripts"
INSTANCE="app-postgres-instance"
DATABASE="app_postgres_db"
PROJECT_ID="project-7ff52764-7883-4474-b19"

shopt -s nullglob
FILES=("${DATA_DIR}"/*.sql)

if [ ${#FILES[@]} -eq 0 ]; then
  echo "No .sql files found in ${DATA_DIR}."
  exit 1
fi

for FILE_PATH in "${FILES[@]}"; do
  FILE_NAME="$(basename "${FILE_PATH}")"
  echo "Importing ${FILE_NAME}..."
  gcloud sql import sql "${INSTANCE}" "${BUCKET}/${FILE_NAME}" --database="${DATABASE}" --project="${PROJECT_ID}" --quiet
done

echo "All .sql files imported successfully."