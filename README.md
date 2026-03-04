# Indian Stock Analyzer

A comprehensive full-stack application for analyzing Indian stocks listed on NSE/BSE.

## Features

- **Technical Analysis** — RSI, MACD, Bollinger Bands, Moving Averages (SMA20/50/200), Stochastic Oscillator, ATR, OBV — all with justification text
- **Fundamental Analysis** — P/E, P/B, ROE, ROCE, D/E, Intrinsic Value (DCF-based), Valuation Status
- **Price Charts** — Interactive candlestick/line charts with volume, multiple timeframes (1W to 5Y)
- **News Intelligence** — Company & competitor news with authenticity scoring and verification analysis
- **Financial Statements** — Annual (FY) and quarterly P&L with revenue/profit growth charts
- **Shareholding Pattern** — Promoter, FII, DII, public breakdown with pledge analysis and trend charts
- **Operator Manipulation Detection** — Volume anomaly detection, pump-and-dump risk scoring, SEBI surveillance flags

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 17 + Spring Boot 3.2 |
| Frontend | React 18 + Recharts (bundled into the JAR) |
| Build | frontend-maven-plugin (Node/npm managed by Maven) |

## Bundled Mode — Single Process

The React app is compiled by Maven and embedded in the Spring Boot JAR.
**No separate Node.js runtime is needed at runtime.**

### Prerequisites
- Java 17+
- Maven 3.8+ (Maven downloads Node 18 automatically via the plugin)

### Run locally
```bash
cd backend
mvn spring-boot:run
# UI + API both at http://localhost:8080
```

### Build fat JAR
```bash
cd backend
mvn clean package -DskipTests
java -jar target/indian-stock-analyzer-1.0.0.jar
# UI + API both at http://localhost:8080
```

### Docker
```bash
docker-compose up --build
# UI + API at http://localhost:8080
```

### Frontend hot-reload dev mode (optional)
If you want React hot-reload while iterating on the UI, run both:
```bash
# Terminal 1 — Spring Boot (skip frontend build for speed)
cd backend && mvn spring-boot:run -Dmaven.frontend.skip=true

# Terminal 2 — React dev server (proxies /api to :8080 via package.json proxy)
cd frontend && npm start
# UI at http://localhost:3000, API proxied to http://localhost:8080
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/stock/analyze/{symbol}` | Full analysis for a stock symbol |
| GET | `/api/stock/chart/{symbol}?timeframe=3M` | Time-series price/volume data |
| GET | `/api/stock/symbols` | List of pre-loaded symbols |
| GET | `/api/stock/health` | Health check |

### Supported Timeframes
`1W`, `1M`, `3M`, `6M`, `1Y`, `3Y`, `5Y`

## Pre-loaded Symbols
`RELIANCE`, `TCS`, `INFY`, `HDFCBANK`, `WIPRO`, `ICICIBANK`, `BAJFINANCE`, `HCLTECH`, `MARUTI`, `TATAMOTORS`, `SUNPHARMA`, `TITAN`, `ZOMATO`, `PAYTM`, `ONGC`, `NTPC`, `SBIN`, `ADANIENT`, `ADANIPORTS`

Any other symbol will generate generic mock data.

## Disclaimer

> This application uses **mock/simulated data** for demonstration purposes only.
> All analysis is generated algorithmically and does not constitute investment advice.
> Consult a SEBI-registered investment advisor before making any investment decisions.
> Investments in securities market are subject to market risks.
