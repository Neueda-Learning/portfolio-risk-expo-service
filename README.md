# Portfolio Risk & Exposure Service

The Portfolio Risk & Exposure Service calculates and monitors market risk across investment portfolios. It
computes  current  exposure,  value  at  risk  (VaR),  concentration  by  sector  and  asset  class,  and  checks
positions against approved risk limits, raising breaches for immediate action.

## SQL Data & Scripts

The `database/` directory contains schema definitions, data seed files, and utility scripts for PostgreSQL setup and management. See [DATABASE_SUMMARY.md](database/DATABASE_SUMMARY.md) for complete schema documentation.


### Database Data Files (`database/data/`)
18 SQL files in dependency order.

### Database Scripts (`database/scripts/`)
8 bash scripts for database management.

### ERD
<img width="3441" height="2995" alt="ERD_rm_database" src="https://github.com/user-attachments/assets/7fb9700c-40ec-404f-8bd7-3193b0701503" />


### Quick Start
```bash
# 1. Setup database and user (requires PostgreSQL admin access)
bash database/scripts/01_db_setup.sh

# 2. Load schema and data (executes all 18 SQL files in order)
bash database/scripts/03_db_load_data.sh

# 3. Connect to database
bash database/scripts/02_db_connect.sh

# 4. Delete database
bash database/scripts/04_db_delete.sh

# 5. Backup database
bash database/scripts/05_db_dump.sh

# 6. Restore database from backup
bash database/scripts/06_db_reload.sh

# 7. Rebuild indexes
bash database/scripts/07_db_rebuild_indexes.sh

# 8. Run views and export to CSV
bash database/scripts/08_db_risk_report.sh
```
