# API Structure

## Overview

The Portfolio Risk & Exposure Service exposes a RESTful API built with Spring Boot 4.1.0. The API provides endpoints for portfolio management, risk calculations, position tracking, limit monitoring, and FX rate management.

**Base URL:** `http://localhost:8080/api`

**Content-Type:** `application/json`

---

## API Endpoints

### Health & Readiness

#### GET `/api/health/readiness`
Check service health and database connectivity.

**Response Schema:**
```json
{
  "service": "portfolio-risk-expo-service",
  "status": "UP",
  "timestamp": "2026-03-20T14:30:00Z",
  "checks": {
    "application": "UP",
    "controllers": "UP",
    "database": "UP"
  }
}
```

**Status Codes:**
- `200 OK` - Service is ready
- `503 Service Unavailable` - Service or database is down

---

### Portfolios

#### GET `/api/portfolios`
Retrieve all portfolios.

**Response Schema:**
```json
[
  {
    "portfolioId": 1,
    "portfolioCode": "GLOBAL-001",
    "portfolioName": "Global Balanced Fund",
    "portfolioType": "MUTUAL_FUND",
    "baseCurrency": "USD",
    "aum": "500000000.00",
    "benchmark": "MSCI World",
    "riskMandate": "MODERATE",
    "manager": "John Smith",
    "isActive": true
  }
]
```

---

#### GET `/api/portfolios/{id}`
Retrieve a specific portfolio by ID.

**Path Parameters:**
- `id` (required): Portfolio ID (integer)

**Response Schema:**
```json
{
  "portfolioId": 1,
  "portfolioCode": "GLOBAL-001",
  "portfolioName": "Global Balanced Fund",
  "portfolioType": "MUTUAL_FUND",
  "baseCurrency": "USD",
  "aum": "500000000.00",
  "benchmark": "MSCI World",
  "riskMandate": "MODERATE",
  "manager": "John Smith",
  "isActive": true
}
```

**Status Codes:**
- `200 OK` - Portfolio found
- `404 Not Found` - Portfolio does not exist

---

#### GET `/api/portfolios/{id}/exposure`
Calculate current total exposure for a portfolio (FX-converted to base currency).

**Path Parameters:**
- `id` (required): Portfolio ID

**Response Schema:**
```json
{
  "portfolioId": 1,
  "portfolioName": "Global Balanced Fund",
  "totalExposure": "487325000.50",
  "currency": "USD",
  "positionCount": 20
}
```

---

#### GET `/api/portfolios/{id}/exposure/by-sector`
Calculate exposure breakdown by sector (asset class).

**Response Schema:**
```json
{
  "portfolioId": 1,
  "portfolioName": "Global Balanced Fund",
  "currency": "USD",
  "exposureByAssetClass": [
    {
      "assetClass": "Equities",
      "exposure": "250000000.00",
      "percentage": 51.3
    },
    {
      "assetClass": "Fixed Income",
      "exposure": "150000000.00",
      "percentage": 30.8
    },
    {
      "assetClass": "FX",
      "exposure": "50000000.00",
      "percentage": 10.3
    },
    {
      "assetClass": "Commodities",
      "exposure": "37325000.50",
      "percentage": 7.6
    }
  ]
}
```

---

#### GET `/api/portfolios/{id}/exposure/by-asset`
Calculate exposure breakdown by individual asset class.

**Response Schema:** Same structure as `/exposure/by-sector`

---

#### GET `/api/portfolios/{id}/limits`
Retrieve risk limits and current utilization for a portfolio.

**Response Schema:**
```json
{
  "portfolioId": 1,
  "portfolioName": "Global Balanced Fund",
  "limits": [
    {
      "limitId": 101,
      "limitType": "VAR",
      "limitValue": "50000000.00",
      "currentUtilization": "35000000.00",
      "utilizationPercentage": 70.0,
      "currency": "USD",
      "isBreached": false,
      "breachCount": 0
    },
    {
      "limitId": 102,
      "limitType": "CONCENTRATION",
      "limitValue": "0.15",
      "currentUtilization": "0.12",
      "utilizationPercentage": 80.0,
      "currency": "USD",
      "isBreached": false,
      "breachCount": 0
    }
  ]
}
```

---

#### GET `/api/portfolios/{id}/var?confidence=95`
Calculate 1-day Value at Risk with specified confidence level.

**Query Parameters:**
- `confidence` (optional, default: 95): Confidence level in percent (typically 95 or 99)

**Response Schema:**
```json
{
  "portfolioId": 1,
  "portfolioName": "Global Balanced Fund",
  "var1Day": "12500000.00"
}
```

---

#### POST `/api/portfolios/{id}/snapshots`
Store a daily exposure snapshot with VaR metrics.

**Path Parameters:**
- `id` (required): Portfolio ID

**Request Schema:**
```json
{
  "snapshotDate": "2026-03-20"
}
```
*(If no date provided, defaults to today)*

**Response:**
- `200 OK` - Snapshot stored successfully

---

#### GET `/api/portfolios/{id}/snapshots?startDate=2026-03-01&endDate=2026-03-20`
Retrieve historical snapshots for a portfolio within a date range.

**Query Parameters:**
- `startDate` (required): Start date (ISO format: YYYY-MM-DD)
- `endDate` (optional): End date (if omitted, uses startDate)

**Response Schema:**
```json
{
  "portfolioId": 1,
  "portfolioName": "Global Balanced Fund",
  "snapshots": [
    {
      "snapshotDate": "2026-03-20",
      "totalExposure": "487325000.50",
      "var1Day": "12500000.00",
      "concentrationRatio": 0.18,
      "currency": "USD",
      "recordedAt": "2026-03-20T16:00:00Z"
    }
  ]
}
```

---

### Positions

#### GET `/api/portfolios/{portfolioId}/positions`
Retrieve all positions in a portfolio.

**Path Parameters:**
- `portfolioId` (required): Portfolio ID

**Response Schema:**
```json
[
  {
    "positionId": 1,
    "portfolioId": 1,
    "instrumentId": 10,
    "instrumentName": "Apple Inc.",
    "quantity": "1000.00",
    "unitPrice": "187.50",
    "exposure": "187500.00",
    "currency": "USD"
  }
]
```

---

#### GET `/api/portfolios/{portfolioId}/positions/{positionId}`
Retrieve a specific position.

**Path Parameters:**
- `portfolioId` (required): Portfolio ID
- `positionId` (required): Position ID

**Response Schema:** Same as single position object above

**Status Codes:**
- `200 OK` - Position found
- `404 Not Found` - Position does not exist

---

#### GET `/api/portfolios/{portfolioId}/positions/{positionId}/instrument`
Retrieve the instrument details for a position.

**Response Schema:**
```json
{
  "instrumentId": 10,
  "instrumentName": "Apple Inc.",
  "isin": "US0378331005",
  "assetClass": "Equities",
  "currency": "USD",
  "lastPrice": "187.50",
  "lastPriceDate": "2026-03-20"
}
```

---

### Limits & Breaches

#### GET `/api/limits`
Retrieve all risk limits across all portfolios.

**Response Schema:**
```json
[
  {
    "limitId": 101,
    "portfolioId": 1,
    "portfolioName": "Global Balanced Fund",
    "limitType": "VAR",
    "limitValue": "50000000.00",
    "currency": "USD",
    "status": "ACTIVE"
  }
]
```

---

#### GET `/api/limits/breaches?status=OPEN`
Retrieve limit breaches with optional status filter.

**Query Parameters:**
- `status` (optional, default: OPEN): `OPEN`, `ACKNOWLEDGED`, `RESOLVED`

**Response Schema:**
```json
[
  {
    "breachId": 1001,
    "limitId": 101,
    "portfolioId": 1,
    "portfolioName": "Global Balanced Fund",
    "limitType": "VAR",
    "breachAmount": "5000000.00",
    "breachPercentage": 110.0,
    "status": "OPEN",
    "severity": "HIGH",
    "breachedAt": "2026-03-20T14:30:00Z",
    "acknowledgedAt": null,
    "resolvedAt": null
  }
]
```

---

#### PATCH `/api/limits/{limitId}/breaches/acknowledgeLatest`
Acknowledge the latest breach for a specific limit.

**Path Parameters:**
- `limitId` (required): Limit ID

**Request Schema:**
```json
{
  "acknowledgedBy": "Risk Manager",
  "notes": "Reviewed and approved"
}
```

**Response Schema:**
```json
{
  "breachId": 1001,
  "limitId": 101,
  "status": "ACKNOWLEDGED",
  "acknowledgedAt": "2026-03-20T15:00:00Z",
  "acknowledgedBy": "Risk Manager",
  "notes": "Reviewed and approved"
}
```

---

#### PATCH `/api/limits/breaches/{breachId}/acknowledge`
Acknowledge a specific breach by ID.

**Path Parameters:**
- `breachId` (required): Breach ID

**Request Schema:**
```json
{
  "acknowledgedBy": "Risk Manager",
  "notes": "Approved for action"
}
```

**Response Schema:** Same as above

---

#### POST `/api/limits/check`
Run limit checks against all portfolios and record new breaches.

**Response Schema:**
```json
{
  "message": "Limit check completed",
  "totalPortfolios": 20,
  "totalBreaches": 3,
  "newBreachesRecorded": 2,
  "timestamp": "2026-03-20T15:00:00Z"
}
```

---

#### POST `/api/limits/check/{portfolioId}`
Run limit checks for a specific portfolio.

**Path Parameters:**
- `portfolioId` (required): Portfolio ID

**Response Schema:**
```json
{
  "portfolioId": 1,
  "portfolioName": "Global Balanced Fund",
  "checksRun": 10,
  "breachedCount": 2,
  "warningCount": 3,
  "skippedCount": 5,
  "newBreachesRecorded": 1,
  "timestamp": "2026-03-20T15:00:00Z"
}
```

---

### FX Rates

#### GET `/api/fx-rates`
Retrieve all exchange rates.

**Response Schema:**
```json
[
  {
    "fromCurrency": "USD",
    "toCurrency": "EUR",
    "rate": "0.92",
    "rateDate": "2026-03-20"
  },
  {
    "fromCurrency": "USD",
    "toCurrency": "GBP",
    "rate": "0.79",
    "rateDate": "2026-03-20"
  }
]
```

---

#### GET `/api/fx-rates/{fromCurrencyCode}`
Retrieve exchange rates from a specific base currency.

**Path Parameters:**
- `fromCurrencyCode` (required): 3-letter ISO currency code (e.g., USD, EUR, GBP)

**Response Schema:**
```json
[
  {
    "fromCurrency": "USD",
    "toCurrency": "EUR",
    "rate": "0.92",
    "rateDate": "2026-03-20"
  },
  {
    "fromCurrency": "USD",
    "toCurrency": "GBP",
    "rate": "0.79",
    "rateDate": "2026-03-20"
  }
]
```

---

#### GET `/api/fx-rates/today`
Retrieve all exchange rates for today.

**Response Schema:** Same as `/api/fx-rates`

---

## Error Handling

**HTTP Status Codes:**
- `200 OK` - Successful request
- `400 Bad Request` - Invalid parameters
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error
- `503 Service Unavailable` - Database unavailable

**Error Response:**
```json
{
  "timestamp": "2026-03-20T15:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid portfolio ID",
  "path": "/api/portfolios/invalid"
}
```

---

## Data Types

**Numeric:** BigDecimal (string format for precision), Double for percentages

**Dates:** ISO 8601 format (`YYYY-MM-DD` or `YYYY-MM-DDTHH:mm:ssZ`)

**Enumerations:**
- PortfolioType: `MUTUAL_FUND`, `HEDGE_FUND`, `PENSION_FUND`, `EQUITY_FUND`, `BOND_FUND`
- LimitType: `VAR`, `CONCENTRATION`, `DURATION`, `LEVERAGE`
- LimitBreachStatus: `OPEN`, `ACKNOWLEDGED`, `RESOLVED`

---

## Example Usage

```bash
# Get Portfolio Exposure
curl -X GET "http://localhost:8080/api/portfolios/1/exposure"

# Check Limits
curl -X POST "http://localhost:8080/api/limits/check/1"

# Acknowledge Breach
curl -X PATCH "http://localhost:8080/api/limits/101/breaches/acknowledgeLatest" \
  -d '{"acknowledgedBy": "Manager", "notes": "Approved"}'

# Get VaR at 99% Confidence
curl -X GET "http://localhost:8080/api/portfolios/1/var?confidence=99"
```
