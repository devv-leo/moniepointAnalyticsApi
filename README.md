# Moniepoint Analytics API

This project provides a REST API for analyzing merchant behavior across Moniepoint's product ecosystem. It processes a year's worth of activity logs from CSV files, ingests them into a database, and exposes key business insights through endpoints.

## Author 
Lekan Olaoye 

## Tech Stack
- **Language**: Java 17
- **Framework**: Spring Boot 3.2.2
- **Database**: H2 (In-memory/File-based for high portability) / PostgreSQL compatible
- **Library**: OpenCSV for fast parsing

## Features
- **Automatic Ingestion**: Files in the `data/` directory are automatically processed on application startup.
- **Batch Processing**: Efficiently handles large datasets using JPA batch inserts.
- **Resilient Parsing**: Gracefully handles malformed data or missing timestamps.

## Prerequisites
- Java 17 or higher
- Maven (optional, if you want to rebuild)

## Database Setup

### Option 1: H2 (Default - Quick Start)
No setup required. The application uses H2 file-based database by default, which runs out of the box.

### Option 2: PostgreSQL (Recommended)
If you want to use PostgreSQL instead:

1. **Install PostgreSQL** (version 14+)
2. **Create database:**
```sql
CREATE DATABASE moniepoint_analytics;
```
3. **Run application with PostgreSQL profile:**
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=postgresql"
```

**Default PostgreSQL credentials (update in `application-postgresql.properties` if different):**
- Host: localhost:5432
- Username: postgres
- Password: postgres
- Database: moniepoint_analytics

## Getting Started

### 1. Data Setup
Ensure the CSV files are located in a `data/` folder at the root of the project:
```
moniepointAnalyticsApi/
├── data/
│   ├── activities_20240101.csv
│   └── ...
```

### 2. Run the Application
If you have Maven installed, you can run:

**Using H2 (default):**
```bash
mvn spring-boot:run
```

**Using PostgreSQL:**
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=postgresql"
```

If you don't have Maven, you can compile and run using your IDE by running the `AnalyticsApiApplication` class.

**The API will be available on port 8080.**

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/analytics/top-merchant` | Merchant with the highest successful transaction volume |
| GET | `/analytics/monthly-active-merchants` | Count of unique merchants with successful events per month |
| GET | `/analytics/product-adoption` | Unique merchant count per product (highest first) |
| GET | `/analytics/kyc-funnel` | KYC conversion funnel stages |
| GET | `/analytics/failure-rates` | Failure rate (%) per product |

## Implementation Notes & Assumptions
- **Database Choice**: The application supports both H2 and PostgreSQL databases using Spring profiles. Use H2 for quick local testing, PostgreSQL for production deployments. All queries are written in standard JPQL for maximum portability.
- **Monthly Active Merchants**: Depending on the database, the schema uses H2's `FORMATDATETIME('yyyy-MM')` for H2 or `TO_CHAR(field, 'YYYY-MM')` for PostgreSQL. The query is dynamically executed based on the active profile.
- **Malformed Data**: A robust `ActivityImporter` gracefully handles malformed data by skipping rows with empty IDs and logging warnings for unparseable amounts or timestamps rather than crashing the entire ingestion process.
- **KYC Funnel**: The funnel stages are defined by specific `event_type` values (`DOCUMENT_SUBMITTED`, `VERIFICATION_COMPLETED`, `TIER_UPGRADE`) under the `KYC` product with `SUCCESS` status.

