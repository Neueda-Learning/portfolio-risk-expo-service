# SYSTEM PROMPT: Next.js Frontend - Portfolio Risk & Exposure Service

## 1. TECH STACK & CORE RULES
*   **Framework:** Next.js 14+ (App Router).
*   **Language/Styling:** TypeScript, Tailwind CSS.
*   **Icons:** `lucide-react`.
*   **Authentication:** STRICTLY NONE.
*   **Data Fetching:** Next.js Server Components where applicable, Client Components for interactivity. Must query live API (`http://127.0.0.1:8080`) with fallback to local deterministic mock data if fetch fails.

## 2. DESIGN SYSTEM
*   **Primary:** `#2660a6` (Top bar, active states, main buttons).
*   **Backgrounds:** `#f4f4f2` (App background), `#ffffff` (Cards/Containers).
*   **Semantic Accents:** `#db0011` (Breaches/Critical), `#d97706` (Warnings), `#0b6b3a` (Safe).
*   **UI Components:** White cards with `border-[#e5e7eb]` and `shadow-sm`. Alert containers require `border-2 border-[#2660a6]` and `shadow-md`.
*   **Typography:** Sans-serif for UI. `tabular-nums` and right-aligned for all financial table data.

## 3. ROUTING & VIEWS (App Router)

### `app/layout.tsx` (Global UI)
*   **Top Nav:** Solid `#2660a6` background. Logo linking to `/`. Search bar. Notification bell. Static profile.

### `app/page.tsx` (Main Dashboard)
*   **Portfolios Grid:** Fetch and map all portfolios. Each card displays Name, ID, and summary metrics. Must link to `/portfolios/[id]`.
*   **Global Alerts Panel:** Sidebar showing the most recent system-wide OPEN limit breaches.

### `app/portfolios/[id]/page.tsx` (Portfolio Detail)
*   **Header:** Portfolio Name and ID.
*   **Metrics:** 3 Cards: Total Exposure, 1-Day VaR (95%), 1-Day VaR (99%).
*   **Action:** Prominent "Run Limit Checks" button (triggers `POST /api/limits/check/{id}`).
*   **Positions Table:** Columns -> Instrument ID, Name, Asset Class (with icon), Quantity, Avg Cost, Market Value, Currency, Weight (%). Pagination controls.
*   **Contextual Alerts (Sidebar):** Newest active breaches strictly for `[id]`. Include "View All Portfolio Breaches" button linking to `/breaches?portfolioId=[id]`.

### `app/breaches/page.tsx` (All Limit Breaches)
*   **Filters (Top):** Dropdowns for `Status` (OPEN, ACKNOWLEDGED, RESOLVED), `Portfolio ID`, and `Limit Type`.
*   **Data Grid:** Comprehensive table of breaches.
*   **Actions:** Inline buttons per row to `Acknowledge` (Triggers `PATCH`), `Resolve`, or `View Details`.

## 4. API MAPPING (Base: `http://127.0.0.1:8080`)
1.  **Overview:** `GET /api/portfolios/{id}/exposure` -> `totalExposure`, `currency`, `positionCount`
2.  **VaR:** `GET /api/portfolios/{id}/var?confidence=95|99` -> `var1Day`
3.  **Limits:** `GET /api/portfolios/{id}/limits` -> Breakdown & utilization
4.  **Run Check:** `POST /api/limits/check/{portfolioId}` -> Fire manual limit check
5.  **List Breaches:** `GET /api/limits/breaches?status={status}` -> Global or filtered list
6.  **Acknowledge:** `PATCH /api/limits/breaches/{id}/acknowledge` -> Body: `{ "acknowledgedBy": "Jane D.", "resolution": "Reviewed" }`