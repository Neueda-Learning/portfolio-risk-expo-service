resource "google_sql_database_instance" "postgres" {
  name             = "app-postgres-instance"
  database_version = "POSTGRES_16"
  region           = var.region

  settings {
    tier = "db-perf-optimized-N-2"
  }

  deletion_protection = false # Set to true in production!
}

resource "google_sql_user" "db_user" {
  name     = "app_db_user"
  instance = google_sql_database_instance.postgres.name
  password = var.db_password
}

resource "google_sql_database" "database" {
  name     = "app_postgres_db"
  instance = google_sql_database_instance.postgres.name
}