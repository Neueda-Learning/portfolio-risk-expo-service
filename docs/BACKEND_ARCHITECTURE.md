# Backend Architecture

## Overview

The backend is a Java Spring Boot 4.1.0 application that provides RESTful API endpoints for portfolio risk management. It uses PostgreSQL for data persistence, Hibernate/JPA for ORM, and follows a layered architecture with clean separation of concerns.

To access API documentation in Swagger, visit [`http://localhost:8080/swagger-ui/index.html`](http://localhost:8080/swagger-ui/index.html).

**Technology Stack:**
- **Framework:** Spring Boot 4.1.0
- **Database:** PostgreSQL with Hibernate JPA
- **Build Tool:** Maven 3.8+
- **Java Version:** 21
- **Logging:** SLF4J + Logback

---

## Directory Structure

```
backend/app/
├── pom.xml                              # Maven configuration (dependencies, build)
├── src/
│   ├── main/
│   │   ├── java/com/risk_busters/app/
│   │   │   ├── AppApplication.java      # Spring Boot entry point
│   │   │   ├── controller/              # REST API controllers (6 controllers)
│   │   │   │   ├── PortfolioController.java
│   │   │   │   ├── PositionController.java
│   │   │   │   ├── LimitController.java
│   │   │   │   ├── ExchangeRateController.java
│   │   │   │   ├── HealthController.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   ├── service/                 # Business logic services
│   │   │   │   ├── PortfolioRiskService.java
│   │   │   │   ├── PortfolioSnapshotService.java
│   │   │   │   ├── PositionService.java
│   │   │   │   ├── LimitService.java
│   │   │   │   ├── LimitComparisonService.java
│   │   │   │   ├── LimitBreachPersistenceService.java
│   │   │   │   └── ExchangeRateService.java
│   │   │   ├── repository/              # Spring Data JPA repositories
│   │   │   │   ├── PortfolioRepository.java
│   │   │   │   ├── PositionRepository.java
│   │   │   │   ├── LimitRepository.java
│   │   │   │   ├── LimitBreachRepository.java
│   │   │   │   └── ExchangeRateRepository.java
│   │   │   ├── model/                   # JPA entities
│   │   │   │   ├── Portfolio.java
│   │   │   │   ├── Position.java
│   │   │   │   ├── Instrument.java
│   │   │   │   ├── RiskFactor.java
│   │   │   │   ├── RiskLimit.java
│   │   │   │   ├── LimitBreach.java
│   │   │   │   ├── ExposureSnapshot.java
│   │   │   │   └── [other entities]
│   │   │   ├── dto/                     # Data Transfer Objects (21 DTOs)
│   │   │   │   ├── PortfoliosDTO.java
│   │   │   │   ├── PositionResponseDTO.java
│   │   │   │   ├── ExposureResponseDTO.java
│   │   │   │   ├── VarResponseDTO.java
│   │   │   │   ├── LimitBreachDTO.java
│   │   │   │   ├── ExchangeRateDTO.java
│   │   │   │   └── [other DTOs]
│   │   │   ├── mapper/                  # DTO/Entity mappers
│   │   │   ├── exceptions/              # Custom exceptions
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   └── [other exceptions]
│   │   │   └── clients/                 # External API clients
│   │   │       └── FrankfurterFXClient.java
│   │   └── resources/
│   │       ├── application.properties   # Spring Boot configuration
│   │       └── application-*.properties # Environment-specific configs
│   └── test/
│       └── java/                        # Unit & integration tests
├── target/                              # Compiled output
└── logs/                                # Application logs
```

---

## Layer Architecture

### 1. Controller Layer
**Location:** `com.risk_busters.app.controller`

Handles HTTP requests/responses, request validation, and delegates to service layer.

**Controllers:**
- **PortfolioController** - Portfolio CRUD and risk calculations (7 endpoints)
- **PositionController** - Position retrieval by portfolio (3 endpoints)
- **LimitController** - Limit management and breach tracking (6 endpoints)
- **ExchangeRateController** - FX rate retrieval (3 endpoints)
- **HealthController** - Service health checks (1 endpoint)
- **GlobalExceptionHandler** - Centralized exception handling

**Typical Flow:**
```java
@GetMapping("/{id}/exposure")
public ResponseEntity<ExposureResponseDTO> getPortfolioExposure(@PathVariable Integer id) {
    ExposureResponseDTO exposure = portfolioRiskService.calculateExposure(id);
    return ResponseEntity.ok(exposure);
}
```

---

### 2. Service Layer
**Location:** `com.risk_busters.app.service`

Contains business logic for risk calculations, data transformations, and orchestration.

**Key Services:**

**PortfolioRiskService**
- `getAllPortfolios()` - Fetch all portfolios with DTOs
- `getPortfolioById(Integer id)` - Fetch single portfolio
- `calculateExposure(Integer id)` - Sum positions with FX conversion
- `calculateExposureBySector(Integer id)` - Breakdown by asset class
- `calculateExposureByAsset(Integer id)` - Breakdown by individual asset
- `calculate1DayVar(Integer id, Integer confidence)` - VaR calculation
- `getPortfolioLimits(Integer id)` - Fetch limits and utilization

**PortfolioSnapshotService**
- `storeSnapshot(Integer portfolioId, LocalDate date)` - Create daily snapshot
- `getPortfolioSnapshots(Integer id, LocalDate start, LocalDate end)` - Retrieve historical snapshots

**PositionService**
- `getAllPositionsByIdFromPortfolio(Integer portfolioId)` - List all positions
- `getPositionByIdFromPortfolio(Integer portfolioId, Integer positionId)` - Get single position
- `getInstrumentInPortfolioByPositionId(...)` - Fetch instrument details

**LimitService**
- `getAllLimits()` - Fetch all limits
- `getLimitBreachesByStatus(LimitBreachStatus status)` - Filter breaches
- `acknowledgeLatestLimitBreachInLimit(...)` - Mark breach acknowledged
- `acknowledgeLimitBreachById(...)` - Acknowledge specific breach

**LimitComparisonService**
- `compareAllLimitsInAllPortfolios()` - Run checks across all portfolios
- `compareAllLimitsInPortfolio(Integer portfolioId)` - Run checks for one portfolio

**LimitBreachPersistenceService**
- `persistBreaches(Integer portfolioId, List<LimitCheckResultDTO>)` - Record new breaches

**ExchangeRateService**
- `getAllExchangeRates()` - Fetch all FX rates
- `getExchangeRatesByBaseCurrencyCode(String code)` - Filter by base currency
- `getAllExchangeRatesByDateToday()` - Get today's rates

---

### 3. Repository Layer
**Location:** `com.risk_busters.app.repository`

Spring Data JPA repositories for database access. Provides CRUD operations and custom queries.

**Repositories:**
- `PortfolioRepository extends JpaRepository<Portfolio, Integer>`
- `PositionRepository extends JpaRepository<Position, Integer>`
- `LimitRepository extends JpaRepository<RiskLimit, Integer>`
- `LimitBreachRepository extends JpaRepository<LimitBreach, Integer>`
- `ExchangeRateRepository extends JpaRepository<ExchangeRate, Integer>`
- `InstrumentRepository extends JpaRepository<Instrument, Integer>`
- `ExposureSnapshotRepository extends JpaRepository<ExposureSnapshot, Integer>`
- And others...

**Example Custom Query:**
```java
@Query("SELECT p FROM Position p WHERE p.portfolio.portfolioId = ?1")
List<Position> findAllByPortfolioId(Integer portfolioId);
```

---

### 4. Model Layer (Entities)
**Location:** `com.risk_busters.app.model`

JPA entities mapped to database tables with proper relationships and constraints.

**Core Entities:**
- **Portfolio** - Investment portfolio (20 records)
  - Fields: portfolioId, portfolioCode, portfolioName, portfolioType, baseCurrency, aum, benchmark, riskMandate, manager, isActive
  - Relationships: 1→M with Position, RiskLimit, LimitBreach, ExposureSnapshot, StressResult

- **Position** - Individual position in portfolio (20 records)
  - Fields: positionId, portfolioId, instrumentId, quantity, unitPrice, exposure
  - Relationships: M→1 with Portfolio, M→1 with Instrument, 1→M with RiskMetric

- **Instrument** - Financial instrument (18 records)
  - Fields: instrumentId, instrumentName, isin, assetClassId, currency
  - Relationships: M→1 with AssetClass, 1→M with Position, 1→M with PriceHistory

- **RiskLimit** - Risk threshold for portfolio (10 records)
  - Fields: limitId, portfolioId, limitType, limitValue, currency
  - Relationships: M→1 with Portfolio, 1→M with LimitBreach

- **LimitBreach** - Violation of risk limit (4+ records)
  - Fields: breachId, limitId, portfolioId, breachAmount, status, severity, breachedAt, acknowledgedAt, resolvedAt
  - Relationships: M→1 with RiskLimit, M→1 with Portfolio

- **ExchangeRate** - Currency conversion rate (18 records)
  - Fields: rateId, fromCurrency, toCurrency, rate, rateDate
  - Relationships: M→1 with Currency

- **ExposureSnapshot** - Daily EOD snapshot (60 records)
  - Fields: snapshotId, portfolioId, snapshotDate, totalExposure, var1Day, concentrationRatio
  - Relationships: M→1 with Portfolio

---

### 5. DTO Layer (Data Transfer Objects)
**Location:** `com.risk_busters.app.dto`

Data Transfer Objects for API request/response payloads. Decouples API contract from internal models.

**21 DTOs include:**
- `PortfoliosDTO` - Portfolio info
- `PositionResponseDTO` - Position details
- `ExposureResponseDTO` - Exposure calculation result
- `ExposureSnapshotDTO` - Snapshot details
- `VarResponseDTO` - VaR calculation result
- `LimitDetailDTO` - Limit information
- `LimitBreachDTO` - Breach details
- `AcknowledgeLimitRequestDTO` - Breach acknowledgment request
- `AcknowledgeLimitResponseDTO` - Breach acknowledgment response
- `AssetExposureResponseDTO` - Asset breakdown
- `SectorExposureResponseDTO` - Sector breakdown
- `PortfolioLimitsResponseDTO` - Limits with utilization
- `InstrumentDTO` - Instrument details
- `ExchangeRateDTO` - FX rate data
- And others...

**Key Principle:** DTOs map to Entity models via Mapper classes for clean separation.

---

### 6. Exception Handling
**Location:** `com.risk_busters.app.exceptions`

Custom exceptions and centralized exception handling via `GlobalExceptionHandler`.

**Custom Exceptions:**
- `ResourceNotFoundException` - Resource not found (404)
- `InvalidInputException` - Invalid input (400)
- `DataIntegrityException` - Data constraint violation (400)

**GlobalExceptionHandler Features:**
- Centralized `@RestControllerAdvice` for all exceptions
- Consistent error response format
- Automatic HTTP status code mapping
- Request logging for debugging

---

## Key Design Patterns

1. **Dependency Injection** - Constructor injection for loose coupling
2. **Repository Pattern** - Spring Data JPA abstracts database access
3. **DTO Pattern** - Separates API contracts from database models
4. **Service Layer** - Business logic isolated from controllers
5. **Global Exception Handling** - Centralized error responses

---

## Monitoring & Logging

- **Logging:** SLF4J + Logback (configurable via application.properties)
- **Health Check:** Spring Boot Actuator `/actuator/health`
- **Exception Tracking:** Centralized via GlobalExceptionHandler
