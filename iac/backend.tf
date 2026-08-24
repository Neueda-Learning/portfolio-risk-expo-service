resource "google_service_account" "backend_sa" {
  account_id   = "backend-sa"
  display_name = "Cloud Run Backend Service Account"
}

resource "google_project_iam_member" "backend_sql_client" {
  project = var.project_id
  role    = "roles/cloudsql.client"
  member  = "serviceAccount:${google_service_account.backend_sa.email}"
}

resource "google_cloud_run_v2_service_iam_member" "backend_public" {
  project  = var.project_id
  location = google_cloud_run_v2_service.backend.location
  name     = google_cloud_run_v2_service.backend.name
  role     = "roles/run.invoker"
  member   = "allUsers"
}

resource "google_cloud_run_v2_service" "backend" {
  name                = "cloud-run-spring-boot-backend"
  location            = var.region
  deletion_protection = false
  ingress             = "INGRESS_TRAFFIC_ALL"


  template {
    service_account = google_service_account.backend_sa.email

    containers {
      image = "us-docker.pkg.dev/cloudrun/container/hello" #a placeholder image for cold start, always replaced by github actions 

      env {
        name  = "SPRING_DATASOURCE_URL"
        value = "jdbc:postgresql:///${google_sql_database.database.name}?cloudSqlInstance=${var.project_id}:${var.region}:${google_sql_database_instance.postgres.name}&socketFactory=com.google.cloud.sql.postgres.SocketFactory"
      }
      env {
        name  = "SPRING_DATASOURCE_USERNAME"
        value = google_sql_user.db_user.name
      }

      env {
        name = "SPRING_DATASOURCE_PASSWORD"
        value_source {
          secret_key_ref {
            secret  = google_secret_manager_secret.db_password.secret_id
            version = "latest"
          }
        }
      }
    }
  }
  lifecycle {
    ignore_changes = [
      template[0].containers[0].image
    ]
  }
}