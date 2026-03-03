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
| Frontend | React 18 + Recharts |
| Charts | Recharts (line, bar, area, radar, pie) |

## Running Locally

### Prerequisites
- Java 17+
- Maven 3.8+
- Node.js 18+

### Backend
```bash
cd backend
mvn spring-boot:run
# API available at http://localhost:8080/api
```

### Frontend
```bash
cd frontend
npm install
npm start
# App available at http://localhost:3000
```

### Docker (Full Stack)
```bash
docker-compose up --build
# App: http://localhost:3000
# API: http://localhost:8080/api
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
