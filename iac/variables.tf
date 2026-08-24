variable "project_id" {
  type    = string
  default = "project-7ff52764-7883-4474-b19"
}

variable "region" {
  type    = string
  default = "europe-west1"
}

variable "db_password" {
  type      = string
  sensitive = true
}

variable "gh_org" {
  type      = string
  sensitive = true
  default   = "Neueda-Learning"
}
variable "gcp_user_email" {
  type = string
}

variable "gh_repo" {
  type      = string
  sensitive = true
  default   = "portfolio-risk-expo-service"
}