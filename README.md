# Portfolio Risk & Exposure Service

The Portfolio Risk & Exposure Service calculates and monitors market risk across investment portfolios. It
computes  current  exposure,  value  at  risk  (VaR),  concentration  by  sector  and  asset  class,  and  checks
positions against approved risk limits, raising breaches for immediate action.

## Table of Contents
- [Technology Stack](#technology-stack)
- [SQL Data & Scripts](#sql-data--scripts)
- [API Structure](#api-structure)
- [Backend Architecture](#backend-architecture)
- [Frontend Architecture](#frontend-architecture)
- [Infrastructure as Code](#infrastructure-as-code)
- [Quick Start](#quick-start)
- [Documentation](#documentation)

---

## Technology Stack

### Backend
- **Runtime:** Java 25
- **Framework:** Spring Boot 4.1.0
- **Data Access:** Spring Data JPA with Hibernate
- **Database Driver:** PostgreSQL JDBC
- **Build Tool:** Maven 3
- **Utilities:** Lombok (annotation processing)
- **Testing:** Spring Boot Test Suite
- **Validation:** Spring Validation (Bean Validation)

### Frontend
- **Framework:** Next.js 16.3.1
- **UI Library:** React 18
- **Language:** TypeScript 5
- **Styling:** Tailwind CSS 3.4.1
- **Icons:** Lucide React 0.438.0
- **CSS Processing:** PostCSS 8, Autoprefixer 10
- **Linting:** ESLint 8
- **Package Manager:** npm

### Database
- **Primary Database:** PostgreSQL 16
- **Schema Management:** Custom SQL scripts (18 data files, 9 utility scripts)
- **Connection Pooling:** Spring Boot embedded (HikariCP)

### Infrastructure & DevOps
- **Containerization:** Docker & Docker Compose
- **Container Orchestration:** Docker Compose (local), Google Cloud Run (production)
- **Infrastructure as Code:** Terraform 1.x
- **Cloud Platform:** Google Cloud Platform (GCP)
    - Cloud Run (serverless compute)
    - Cloud SQL (managed PostgreSQL)
    - Cloud Storage (seed scripts)
    - Google Secret Manager (credentials)
    - VPC & Private Networking
- **CI/CD:** GitHub Actions
- **Authentication:** OIDC-based Workload Identity Federation (keyless auth)

### Development Environment
- **Local Development:** Docker Compose with multi-container setup
- **Version Control:** Git + GitHub
- **API Design:** RESTful with OpenAPI-compatible structure

---

## SQL Data & Scripts

The `database/` directory contains schema definitions, data seed files, and utility scripts for PostgreSQL setup and management.

### Database Data Files (`database/data/`)
18 SQL files in dependency order.

### Database Scripts (`database/scripts/`)
9 bash scripts for database management.

### E/R Diagram
<img width="800" alt="ERD_rm_database" src="https://github.com/user-attachments/assets/7fb9700c-40ec-404f-8bd7-3193b0701503" />


**Full Database Documentation:** See [DATABASE_SUMMARY.md](docs/DATABASE_SUMMARY.md) for complete schema and script details.

---

## API Structure

The project provides a comprehensive RESTful API for portfolio management, risk calculations, and limit monitoring.

### Key Endpoints

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/api/portfolios` | GET | List all portfolios |
| `/api/portfolios/{id}` | GET | Get portfolio details |
| `/api/portfolios/{id}/exposure` | GET | Calculate total exposure (FX-converted) |
| `/api/portfolios/{id}/exposure/by-asset` | GET | Exposure breakdown by asset class |
| `/api/portfolios/{id}/var?confidence=95` | GET | Calculate 1-day Value at Risk |
| `/api/portfolios/{id}/limits` | GET | Get portfolio limits & utilization |
| `/api/portfolios/{id}/snapshots` | GET/POST | Store/retrieve daily risk snapshots |
| `/api/portfolios/{id}/positions` | GET | List portfolio positions |
| `/api/limits` | GET | Get all limits |
| `/api/limits/breaches` | GET | Get limit breaches (filterable by status) |
| `/api/limits/check/{portfolioId}` | POST | Run limit checks & record breaches |
| `/api/limits/{limitId}/breaches/acknowledgeLatest` | PATCH | Acknowledge latest breach |
| `/api/fx-rates` | GET | Get FX rates |
| `/api/health/readiness` | GET | Service health check |

### Response Schema Example

**GET `/api/portfolios/1/exposure`:**
```json
{
  "portfolioId": 1,
  "portfolioName": "Global Balanced Fund",
  "totalExposure": "487325000.50",
  "currency": "USD",
  "positionCount": 20
}
```

**Full API Documentation:** See [docs/API_STRUCTURE.md](docs/API_STRUCTURE.md) for complete endpoint reference with all request/response schemas, parameters, and error codes.

---

## Backend Architecture

Java Spring Boot microservice with layered architecture:
- **Controller Layer** - 6 REST controllers handling 20+ endpoints
- **Service Layer** - Business logic for risk calculations and data orchestration
- **Repository Layer** - Spring Data JPA abstractions over PostgreSQL
- **Model Layer** - 15 JPA entities with proper relationships
- **DTO Layer** - 21 Data Transfer Objects for API contracts

**Key Features:**
- Real-time exposure calculations with FX conversion
- Automated limit monitoring and breach detection
- Daily risk snapshots with VaR metrics
- Centralized exception handling

**Project Structure:**
```
backend/app/
├── src/main/java/com/risk_busters/app/
│   ├── controller/          # 6 REST controllers
│   ├── service/             # Business logic services
│   ├── repository/          # Spring Data JPA repositories
│   ├── model/               # JPA entities
│   ├── dto/                 # 21 Data Transfer Objects
│   ├── mapper/              # Entity ↔ DTO mappers
│   ├── exceptions/          # Custom exception handling
│   └── clients/             # External API clients
└── pom.xml                  # Maven configuration
```

**Full Backend Documentation:** See [docs/BACKEND_ARCHITECTURE.md](docs/BACKEND_ARCHITECTURE.md) for detailed layer descriptions, design patterns, and development guide.

---

## Frontend Architecture

**Pages:**
- **Dashboard** - Portfolio overview and key metrics
- **Portfolios** - List all portfolios with sorting/filtering
- **Portfolio Detail** - Deep dive into portfolio risk metrics, positions, and limits
- **Limit Breaches** - Monitor and acknowledge limit violations

**Key Components:**
- PortfolioCard - Portfolio summary display
- ExposureChart - Exposure visualization (pie/bar/line)
- LimitIndicator - Limit utilization progress bar
- BreachAlert - Breach notifications with actions

**Custom Hooks:**
- `usePortfolio` - Fetch and manage portfolio data
- `useLimits` - Fetch and manage limit breach data

**Project Structure:**
```
frontend/
├── app/(route)/
│   ├── page.tsx                 # Dashboard
│   ├── portfolios/              # Portfolios list/detail
│   └── breaches/                # Limit breaches
├── components/                  # Reusable React components
├── lib/
│   ├── api.ts                   # Centralized API client
│   ├── hooks/                   # Custom React hooks
│   └── utils.ts                 # Helper utilities
├── types/                       # TypeScript definitions
└── package.json                 # npm dependencies
```

**Full Frontend Documentation:** See [docs/FRONTEND_ARCHITECTURE.md](docs/FRONTEND_ARCHITECTURE.md) for component hierarchy, API integration patterns, styling strategy, and deployment guide.

---

## Infrastructure as Code

Terraform-based infrastructure provisioning on Google Cloud Platform (GCP).

**Environments:**
- **Local Development:** Docker Compose with PostgreSQL, Spring Boot backend, and Next.js frontend
- **Cloud Production:** GCP infrastructure with Cloud Run, Cloud SQL, and managed secrets

**Infrastructure Components:**

| Component | Local Dev | Cloud Prod |
|-----------|-----------|-----------|
| Database | Docker PostgreSQL 16 | Cloud SQL PostgreSQL 16 |
| Backend | Docker container | Cloud Run service + Service Account |
| Frontend | Docker container | Cloud Run service + Service Account |
| Network | Docker bridge network | VPC + Cloud SQL private endpoint |
| Secrets | Environment file | Google Secret Manager |
| CI/CD | Manual | GitHub Actions + Workload Identity |

**Key Features:**
- **Local-to-Cloud Parity:** Docker Compose mirrors production GCP setup
- **Keyless Authentication:** GitHub Actions authenticates via OIDC (no long-lived credentials)
- **Automatic Provisioning:** Single `terraform apply` creates all cloud infrastructure
- **Secret Management:** Database password encrypted in Google Secret Manager
- **Database Initialization:** SQL seed scripts automatically uploaded to Cloud Storage

**Project Structure:**
```
iac/
├── providers.tf              # Terraform provider config + GCS backend
├── backend.tf                # Cloud Run backend + service account + IAM
├── frontend.tf               # Cloud Run frontend + service account
├── database.tf               # Cloud SQL + Storage bucket for SQL scripts
├── secrets.tf                # Google Secret Manager for password
├── gh_actions.tf             # GitHub Actions Workload Identity + IAM
└── variables.tf              # Input variables (project_id, region, etc.)

docker-compose.yml            # Local development orchestration
```

**Full Documentation:** See [docs/IaC_ARCHITECTURE.md](docs/IaC_ARCHITECTURE.md) for detailed Terraform configuration, deployment workflows, and best practices.

---

## Quick Start

### Prerequisites
- Docker & Docker Compose installed

### Local Development Setup
1. Copy [`.env.example`](.env.example) to `.env` and set environment variables
2. Run `docker-compose up --build` to start the backend, frontend, and database
3. Access the frontend at [`http://localhost:3000`](http://localhost:3000) and backend API documentation at [`http://localhost:8080/swagger-ui/index.html`](http://localhost:8080/swagger-ui/index.html)
4. Run `docker-compose down` to stop the services

### Production Deployment
1. Configure GCP project and enable required APIs (Cloud Run, Cloud SQL, Secret Manager)
2. Set up GitHub Actions secrets for OIDC authentication
3. Run `terraform init`, `terraform plan`, and `terraform apply` to provision cloud infrastructure
4. Push code to GitHub to trigger CI/CD deployment to Cloud Run

---

## Documentation

For detailed architecture, API reference, and development guides, refer to the following documentation files:
- [DATABASE_SUMMARY.md](docs/DATABASE_SUMMARY.md)
- [API_STRUCTURE.md](docs/API_STRUCTURE.md)
- [BACKEND_ARCHITECTURE.md](docs/BACKEND_ARCHITECTURE.md)
- [FRONTEND_ARCHITECTURE.md](docs/FRONTEND_ARCHITECTURE.md)
- [IaC_ARCHITECTURE.md](docs/IaC_ARCHITECTURE.md)

