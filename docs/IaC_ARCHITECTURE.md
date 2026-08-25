# Infrastructure as Code (IaC) Architecture

## Overview

The Portfolio Risk & Exposure Service uses Terraform to provision and manage cloud infrastructure on Google Cloud Platform (GCP). The IaC approach enables reproducible, version-controlled infrastructure deployments with clear separation between local development (Docker Compose) and cloud production environments.

**Technology Stack:**
- **IaC Tool:** Terraform 1.5.0+
- **Cloud Provider:** Google Cloud Platform (GCP)
- **Backend Storage:** Google Cloud Storage (GCS) for Terraform state
- **CI/CD Integration:** GitHub Actions with Workload Identity Federation
- **Container Orchestration:** Google Cloud Run (serverless)
- **Database:** Google Cloud SQL (PostgreSQL 16)

---

## Architecture Overview

### Local Development Environment
```
Docker Compose (docker-compose.yml)
├── PostgreSQL 16 Container
├── Spring Boot Backend Container
├── Next.js Frontend Container
└── Internal Bridge Network

Volumes:
├── postgres_data (persistent database storage)
└── ./database/data (SQL initialization scripts)
```

### Cloud Production Environment (GCP)
```
Terraform (iac/ directory)
├── Cloud SQL Instance (PostgreSQL 16)
│   ├── Database: app_postgres_db
│   ├── User: app_db_user
│   └── Secret: postgres-db-password (Secrets Manager)
├── Cloud Run Services
│   ├── Backend Service (Spring Boot 4.1.0)
│   │   ├── Service Account: backend-sa
│   │   ├── IAM Roles: cloudsql.client
│   │   └── Environment: DB credentials via Secrets Manager
│   └── Frontend Service (Next.js)
│       ├── Service Account: frontend-sa
│       └── Environment: Backend URL injection
├── Storage Bucket (db-feed-scripts)
│   └── SQL seed files for database initialization
├── Secrets Manager
│   └── Encrypted database password
└── GitHub Actions Integration
    ├── Workload Identity Pool
    ├── Service Account: github-actions-sa
    └── IAM Roles: run.admin, artifactregistry.writer
```

---

## Directory Structure

```
iac/
├── providers.tf                 # Terraform provider configuration (Google 5.0.0+)
├── backend.tf                   # Cloud Run backend service + service account + IAM
├── frontend.tf                  # Cloud Run frontend service + service account
├── database.tf                  # Cloud SQL instance, database, user + Storage bucket
├── secrets.tf                   # Secret Manager for database password
├── gh_actions.tf                # GitHub Actions Workload Identity + IAM roles
├── variables.tf                 # Input variables (project_id, region, db_password, etc.)
├── .terraform.lock.hcl          # Dependency lock file (committed to version control)
└── terraform.tfvars             # Terraform variables file (NOT committed, local only)
```

---

## Terraform Configuration Files

### 1. providers.tf
**Purpose:** Configure Terraform backend and Google Cloud provider.

- **Remote State Storage:** Google Cloud Storage bucket
- **State Locking:** Prevents concurrent modifications
- **Google Provider:** Version 5.0.0+ for latest GCP features
- **Multi-region Support:** Region configurable via variable

---

### 2. variables.tf
**Purpose:** Define input variables for infrastructure configuration.

**Variables:**

| Variable | Type | Default                       | Sensitive | Purpose |
|----------|------|-------------------------------|-----------|---------|
| `project_id` | string | `your-project-id`             | No | GCP Project ID |
| `region` | string | `europe-west1`                | No | GCP deployment region |
| `db_password` | string | -                             | Yes | Database password (from environment) |
| `gh_org` | string | -                             | Yes | GitHub organization |
| `gh_repo` | string | `portfolio-risk-expo-service` | Yes | GitHub repository name |
| `gcp_user_email` | string | -                             | No | User email for storage bucket access |

**Usage:**
```bash
terraform apply -var="db_password=secure_password" -var="gcp_user_email=user@example.com"
```

---

### 3. database.tf
**Purpose:** Create PostgreSQL database infrastructure and seed data storage.

**Resources Created:**
- **Cloud SQL Instance** - PostgreSQL 16, performance-optimized tier, auto backups
- **Database User** - `app_db_user` with password from Secrets Manager
- **Database** - `app_postgres_db` for application data
- **Storage Bucket** - `app-db-feed-scripts` stores 18 SQL seed files from `database/data/`
- **IAM Permissions** - User can upload scripts; Cloud SQL can read for initialization

**Key Features:**
- Automatic replication of SQL scripts to Cloud Storage
- Cloud SQL service account can read scripts for import
- Deletion protection disabled (set to true in production)

---

### 4. backend.tf
**Purpose:** Deploy Spring Boot backend to Cloud Run with database connectivity.

**Resources Created:**
- **Service Account** - `backend-sa` with Cloud SQL client role
- **Cloud Run Service** - Exposes Spring Boot API on public URL
- **Environment Variables** - DB connection, username, password (from Secrets Manager)
- **IAM Binding** - Public access via `roles/run.invoker` for all users

**Key Features:**
- Serverless auto-scaling (0 to N instances)
- Cloud SQL Socket Factory for secure database connections
- Password retrieved from Google Secret Manager (encrypted)
- Image placeholder updated by GitHub Actions CI/CD
- Ignore image changes during `terraform apply` (CI/CD manages updates)

---

### 5. frontend.tf
**Purpose:** Deploy Next.js frontend to Cloud Run with backend URL injection.

**Resources Created:**
- **Service Account** - `frontend-sa` (minimal permissions)
- **Cloud Run Service** - Exposes Next.js app on public URL
- **Backend URL Injection** - `NEXT_PUBLIC_API_URL` set to backend Cloud Run URL
- **IAM Binding** - Public access for all users

**Key Features:**
- Frontend automatically routes API calls to deployed backend
- Backend URL injected at deploy time (auto-updated if backend URL changes)
- Independent scaling from backend service
- Ignore image changes during terraform apply (CI/CD manages updates)

---

### 6. secrets.tf
**Purpose:** Manage sensitive database password using Google Secret Manager.

**Resources Created:**
- **Secret** - `postgres-db-password` with auto-replication across regions
- **Secret Version** - Encrypted password stored in Secret Manager
- **IAM Binding** - Only backend service account can access password

**Features:**
- Encryption at rest and in transit
- Audit logging for all secret access
- Automatic replication across regions
- Least privilege access (only backend can read)

---

### 7. gh_actions.tf
**Purpose:** Configure GitHub Actions CI/CD with Workload Identity Federation for keyless authentication.

**Resources Created:**
- **Workload Identity Pool** - OIDC provider for GitHub Actions
- **Pool Provider** - Restricts to `main` branch of specific repo
- **Service Account** - `github-actions-sa` for deployment
- **IAM Roles** - `run.admin`, `iam.serviceAccountUser`, `artifactregistry.writer`

**Security Features:**
- Keyless authentication via OIDC (no secrets in GitHub)
- Repository-level access control
- Branch protection (main only)
- Full audit trail for deployments

---

## Local Development Environment

### Docker Compose Configuration
**File:** `docker-compose.yml`

**Services Overview:**

- **PostgreSQL** - Alpine image, auto-initialization from SQL scripts, health checks
- **Spring Boot Backend** - Depends on database health, port 8080, Spring Actuator health checks
- **Next.js Frontend** - Depends on backend health, port 3000, backend URL injection
- **Network** - Bridge network for inter-service communication
- **Volumes** - `postgres_data` for persistent storage

---

## Environment Configuration

**Local Development ([.env.example](../.env.example) --> .env):**
```
DATABASE_NAME=your_database_name
DATABASE_USER=your_database_user
DATABASE_PASSWORD=your_database_password
DATABASE_HOST=localhost
DATABASE_PORT=5432
NEXT_PUBLIC_API_URL=http://localhost:8080
HIBERNATE_DDL_AUTO=update
SPRING_PROFILES_ACTIVE=prod
```

**Cloud Production (terraform.tfvars, local only):**
```
project_id = "your-project-id"
region = "europe-west1"
db_password = "secure_password"
gcp_user_email = "your@email.com"
```

---

## Deployment Workflows

### Local Development

```bash
# 1. Start services
docker-compose up -d

# 2. Verify services
curl http://localhost:8080/api/health/readiness
open http://localhost:3000

Swagger: http://localhost:8080/swagger-ui/index.html

# 3. View logs
docker-compose logs -f backend
```

### Cloud Production (GCP)

```bash
cd iac/

# 1. Authenticate and initialize
gcloud auth application-default login
terraform init

# 2. Plan and apply
terraform plan \
  -var="db_password=secure_password" \
  -var="gcp_user_email=your@email.com"

terraform apply \
  -var="db_password=secure_password" \
  -var="gcp_user_email=your@email.com"
```

**Creates:** Cloud SQL, Cloud Run backend/frontend, Secret Manager, GitHub Actions setup

