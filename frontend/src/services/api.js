import axios from 'axios';

const API_BASE = process.env.REACT_APP_API_URL || '/api';

const api = axios.create({
  baseURL: API_BASE,
  timeout: 30000,
});

export const analyzeStock = (symbol) =>
  api.get(`/stock/analyze/${symbol}`).then(r => r.data);

export const getChartData = (symbol, timeframe = '3M') =>
  api.get(`/stock/chart/${symbol}`, { params: { timeframe } }).then(r => r.data);

export const getAvailableSymbols = () =>
  api.get('/stock/symbols').then(r => r.data);

export const checkHealth = () =>
  api.get('/stock/health').then(r => r.data);

// Inference engine: returns ScoreCard with multi-factor weighted recommendation
export const getInferenceScore = (symbol) =>
  api.get(`/stock/inference/${symbol}`, { timeout: 45000 }).then(r => r.data);

export default api;
