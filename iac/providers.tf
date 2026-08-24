terraform {
  required_version = ">= 1.5.0"
  required_providers {
    google = {
      source  = "hashicorp/google"
      version = ">= 5.0.0"
    }

  }
  backend "gcs" {
    bucket = "neueda-state-tf-bucket"
  }
}

provider "google" {
  project = var.project_id
  region  = var.region
}