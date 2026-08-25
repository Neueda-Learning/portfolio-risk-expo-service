# Frontend Architecture

## Overview

The frontend is a Next.js 16.3.1 application with React 18, TypeScript, and Tailwind CSS. It provides a risk management dashboard for viewing portfolios, monitoring limits, and tracking breaches.

**Technology Stack:**
- **Framework:** Next.js 16.3.1 (React 18)
- **Language:** TypeScript
- **Styling:** Tailwind CSS 3.4.1
- **UI Components:** Lucide React icons
- **Build Tool:** Next.js built-in (Webpack)
- **Linting:** ESLint 8

---

## Directory Structure

```
frontend/
├── package.json                         # Dependencies and scripts
├── next.config.ts                       # Next.js configuration
├── tsconfig.json                        # TypeScript configuration
├── tailwind.config.ts                   # Tailwind CSS configuration
├── postcss.config.mjs                   # PostCSS configuration
├── .next/                               # Build output
├── node_modules/                        # Installed dependencies
├── app/                                 # Next.js App Router (pages)
│   ├── (route)/
│   │   ├── page.tsx                     # Home/Dashboard page
│   │   ├── layout.tsx                   # Root layout
│   │   ├── error.tsx                    # Error boundary
│   │   ├── portfolios/
│   │   │   ├── page.tsx                 # Portfolios list
│   │   │   ├── [id]/
│   │   │   │   └── page.tsx             # Portfolio detail
│   │   │   └── layout.tsx               # Portfolio layout
│   │   └── breaches/
│   │       ├── page.tsx                 # Limit breaches page
│   │       └── layout.tsx               # Breaches layout
│   ├── globals.css                      # Global styles
│   └── [middleware routes]
├── components/                          # Reusable React components
│   ├── PortfolioCard.tsx                # Portfolio display card
│   ├── ExposureChart.tsx                # Exposure visualization
│   ├── LimitIndicator.tsx               # Limit utilization indicator
│   ├── BreachAlert.tsx                  # Breach notification
│   ├── Navigation.tsx                   # Header/nav component
│   └── [other components]
├── lib/                                 # Utility functions
│   ├── api.ts                           # API client functions
│   ├── utils.ts                         # Helper utilities
│   └── hooks/                           # Custom React hooks
│       ├── usePortfolio.ts              # Portfolio data hook
│       ├── useLimits.ts                 # Limits data hook
│       └── [other hooks]
├── types/                               # TypeScript type definitions
│   ├── api.ts                           # API response types
│   ├── portfolio.ts                     # Portfolio domain types
│   ├── limit.ts                         # Limit domain types
│   └── [other types]
└── public/                              # Static assets
    ├── logo.svg
    └── [other assets]
```

---

## Pages Structure (Next.js App Router)

### 1. Home / Dashboard
**File:** `app/(route)/page.tsx`

**Purpose:** Main dashboard with portfolio overview and key metrics.

**Components Used:**
- Portfolio summary cards
- Top 3 portfolios by AUM
- Recent limit breaches
- System health status
- Quick action buttons

**Data Fetched:**
- GET `/api/portfolios` (all portfolios)
- GET `/api/limits/breaches?status=OPEN` (open breaches)
- GET `/api/health/readiness` (service status)

---

### 2. Portfolios List
**File:** `app/(route)/portfolios/page.tsx`

**Purpose:** Display all portfolios with key metrics in a table or card view.

**Features:**
- Sortable table (by name, AUM, manager)
- Filterable by status (active/inactive)
- Search by portfolio name or code
- Link to portfolio detail pages
- Quick action buttons (view, edit, monitor)

**Data Fetched:**
- GET `/api/portfolios`

---

### 3. Portfolio Detail
**File:** `app/(route)/portfolios/[id]/page.tsx`

**Purpose:** Deep dive into a single portfolio's risk metrics and positions.

**Sections:**

#### Portfolio Header
- Portfolio name, code, manager
- AUM and benchmark
- Risk mandate
- Base currency

#### Exposure Summary
- Total exposure
- Position count
- Exposure by asset class (pie chart)
- Exposure by sector (bar chart)

#### Risk Metrics
- 1-Day VaR (95% and 99% confidence)
- Concentration ratio
- Top 5 risk factors
- Historical exposure trend (line chart)

#### Positions Table
- Instrument name
- Quantity, unit price, exposure
- Currency
- Asset class
- Link to position detail

#### Limits & Breaches
- Current limits utilization (progress bars)
- Alert status (green/yellow/red)
- Recent breaches for this portfolio
- Acknowledge breach button

**Data Fetched:**
- GET `/api/portfolios/{id}`
- GET `/api/portfolios/{id}/exposure`
- GET `/api/portfolios/{id}/exposure/by-asset`
- GET `/api/portfolios/{id}/exposure/by-sector`
- GET `/api/portfolios/{id}/var?confidence=95`
- GET `/api/portfolios/{id}/var?confidence=99`
- GET `/api/portfolios/{id}/limits`
- GET `/api/portfolios/{id}/positions`
- GET `/api/portfolios/{id}/snapshots?startDate=X&endDate=Y`

---

### 4. Limit Breaches
**File:** `app/(route)/breaches/page.tsx`

**Purpose:** Monitor all limit breaches across portfolios.

**Features:**
- Table with breach details
- Status filter (OPEN, ACKNOWLEDGED, RESOLVED)
- Severity filter (HIGH, MEDIUM, LOW)
- Sort by date, severity, breach amount
- Bulk acknowledge action
- Individual breach action (acknowledge, resolve)

**Columns:**
- Portfolio name
- Limit type
- Breach amount
- Breach %
- Status
- Severity
- Breached date
- Actions

**Data Fetched:**
- GET `/api/limits/breaches?status=OPEN` (or other status)
- GET `/api/limits/breaches` (all statuses)

---

## Component Architecture

### Data Flow Pattern

```
Page Component (e.g., page.tsx)
    ↓
Custom Hook (e.g., usePortfolio)
    ↓
API Client Function (lib/api.ts)
    ↓
HTTP Request to Backend
    ↓
Response Data
    ↓
UI Components (Card, Chart, Table, etc.)
    ↓
Rendered HTML
```

---

## Key Components

### PortfolioCard
**Location:** `components/PortfolioCard.tsx`

Displays portfolio summary in card format.

**Props:**
```typescript
interface PortfolioCardProps {
  portfolio: PortfoliosDTO;
  onClick?: () => void;
  showActions?: boolean;
}
```

**Features:**
- Portfolio name and code
- Manager name
- AUM formatted
- Risk mandate badge
- Status indicator
- Action buttons (view, monitor)

---

### ExposureChart
**Location:** `components/ExposureChart.tsx`

Visualizes exposure breakdown using chart library.

**Props:**
```typescript
interface ExposureChartProps {
  data: Array<{
    label: string;
    value: number;
    percentage: number;
  }>;
  type: 'pie' | 'bar' | 'line';
}
```

**Features:**
- Responsive chart
- Color-coded categories
- Hover tooltips
- Legend

---

### LimitIndicator
**Location:** `components/LimitIndicator.tsx`

Shows limit utilization with progress indicator and status.

**Props:**
```typescript
interface LimitIndicatorProps {
  limit: LimitDetailDTO;
  showLabel?: boolean;
}
```

**Features:**
- Progress bar
- Percentage display
- Status badge (OK, WARNING, BREACHED)
- Color coding (green/yellow/red)
- Tooltip with details

---

### BreachAlert
**Location:** `components/BreachAlert.tsx`

Displays breach notification with action button.

**Props:**
```typescript
interface BreachAlertProps {
  breach: LimitBreachDTO;
  onAcknowledge: (breachId: number) => Promise<void>;
  onResolve?: (breachId: number) => Promise<void>;
}
```

**Features:**
- Severity indicator (HIGH, MEDIUM, LOW)
- Breach details
- Status badge
- Action buttons
- Loading state during API call
- Error handling

---

## Custom Hooks

**usePortfolio** - Fetches portfolio data with error handling and loading state

**useLimits** - Fetches and manages limit breach data with acknowledge action

---

## API Client

**Location:** `lib/api.ts` - Centralized API functions for backend communication

```typescript
// Portfolio
getPortfolios()
getPortfolio(id)
getPortfolioExposure(id)
getPortfolioLimits(id)
getPortfolioVAR(id, confidence)

// Positions
getPositions(portfolioId)
getPosition(portfolioId, positionId)

// Limits
getLimits()
getLimitBreaches(status?)
acknowledgeBreach(breachId, data)

// FX Rates
getFXRates()
getFXRatesByBase(currencyCode)
```

---

## Styling

**Tailwind CSS** with custom color scheme:
- `primary: #1e3a8a` - Main color
- `breach: #dc2626` - Red for breaches
- `warning: #f59e0b` - Amber for warnings
- `safe: #10b981` - Green for safe status

