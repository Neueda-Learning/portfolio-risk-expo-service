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

resource "google_storage_bucket" "db_feed_scripts"{
  name = "app-db-feed-scripts"
  location = "EU"
  public_access_prevention = "enforced"
  uniform_bucket_level_access = true
}
resource "google_storage_bucket_iam_member" "user_object_admin" {
  bucket = google_storage_bucket.db_feed_scripts.name
  role   = "roles/storage.objectAdmin"
  member = "user:${var.gcp_user_email}"
}

resource "google_storage_bucket_iam_member" "cloudsql_object_viewer" {
  bucket = google_storage_bucket.db_feed_scripts.name
  role   = "roles/storage.objectViewer"
  member = "serviceAccount:${google_sql_database_instance.postgres.service_account_email_address}"
}
resource "google_storage_bucket_object" "sql_imports" {
  for_each = fileset("../${path.root}/database/data", "*.sql")
  name = each.value
  bucket = google_storage_bucket.db_feed_scripts.name
  source = "../${path.root}/database/data/${each.value}"
}