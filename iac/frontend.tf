resource "google_service_account" "frontend_sa" {
  account_id   = "frontend-sa"
  display_name = "Cloud Run Frontend Service Account"
}

resource "google_cloud_run_v2_service_iam_member" "frontend_public" {
  project  = var.project_id
  location = google_cloud_run_v2_service.frontend.location
  name     = google_cloud_run_v2_service.frontend.name
  role     = "roles/run.invoker"
  member   = "allUsers"
}

resource "google_cloud_run_v2_service" "frontend" {
  name                = "nextjs-frontend"
  location            = var.region
  deletion_protection = false
  ingress             = "INGRESS_TRAFFIC_ALL"


  template {
    service_account = google_service_account.frontend_sa.email
    containers {
      image = "us-docker.pkg.dev/cloudrun/container/hello"

      env {
        name  = "NEXT_PUBLIC_API_URL"
        value = google_cloud_run_v2_service.backend.uri
      }

      env {
        name  = "BACKEND_URL"
        value = google_cloud_run_v2_service.backend.uri
      }
    }
  }
  lifecycle {
    ignore_changes = [
      template[0].containers[0].image
    ]
  }
}