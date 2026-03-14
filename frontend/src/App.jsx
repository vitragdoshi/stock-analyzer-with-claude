import React, { useState, useCallback } from 'react';
import SearchBar from './components/SearchBar';
import StockOverview from './components/StockOverview';
import PriceChart from './components/PriceChart';
import TechnicalAnalysis from './components/TechnicalAnalysis';
import FundamentalAnalysis from './components/FundamentalAnalysis';
import NewsSection from './components/NewsSection';
import FinancialStatements from './components/FinancialStatements';
import ShareholdingPattern from './components/ShareholdingPattern';
import ManipulationAnalysis from './components/ManipulationAnalysis';
import InferenceScorecard from './components/InferenceScorecard';
import { analyzeStock } from './services/api';

const TABS = [
  { id: 'overview', label: 'Overview' },
  { id: 'chart', label: 'Price Chart' },
  { id: 'technical', label: 'Technical' },
  { id: 'fundamental', label: 'Fundamental' },
  { id: 'financial', label: 'Financials' },
  { id: 'news', label: 'News & Intel' },
  { id: 'shareholding', label: 'Shareholding' },
  { id: 'manipulation', label: 'Manipulation Check' },
  { id: 'inference', label: '🤖 AI Inference' },
];

function RecommendationBanner({ sentiment, recommendation, justification }) {
  const recClass = recommendation?.includes('BUY') ? 'rec-buy'
    : recommendation?.includes('SELL') || recommendation?.includes('AVOID') ? 'rec-sell'
    : recommendation?.includes('HOLD') ? 'rec-hold' : 'rec-neutral';

  const sentColor = sentiment?.includes('BULLISH') ? '#22c55e'
    : sentiment?.includes('BEARISH') ? '#ef4444' : '#f59e0b';

  return (
    <div className={`recommendation-banner ${recClass}`} style={{ marginBottom: 24 }}>
      <div>
        <div style={{ fontSize: 11, color: '#94a3b8', textTransform: 'uppercase', letterSpacing: '0.06em', marginBottom: 4 }}>
          AI Analysis Summary
        </div>
        <div style={{ display: 'flex', gap: 20, alignItems: 'center', flexWrap: 'wrap' }}>
          <div>
            <div style={{ fontSize: 11, color: '#64748b', marginBottom: 2 }}>Sentiment</div>
            <div style={{ fontSize: 20, fontWeight: 800, color: sentColor }}>{sentiment}</div>
          </div>
          <div style={{ width: 1, height: 40, background: 'rgba(255,255,255,0.1)' }} />
          <div>
            <div style={{ fontSize: 11, color: '#64748b', marginBottom: 2 }}>Recommendation</div>
            <div style={{ fontSize: 20, fontWeight: 800, color: sentColor }}>{recommendation}</div>
          </div>
        </div>
      </div>
      <div style={{ fontSize: 12, color: '#94a3b8', maxWidth: 600, lineHeight: 1.7, flex: 1 }}>
        {justification}
      </div>
    </div>
  );
}

function EmptyState() {
  return (
    <div style={{ textAlign: 'center', padding: '80px 20px', maxWidth: 600, margin: '0 auto' }}>
      <div style={{
        width: 80, height: 80, borderRadius: '50%',
        background: 'linear-gradient(135deg, #3b82f6, #6366f1)',
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        margin: '0 auto 24px',
        fontSize: 36
      }}>
        📈
      </div>
      <h2 style={{ fontSize: 24, fontWeight: 700, color: '#f1f5f9', marginBottom: 12 }}>
        Indian Stock Analyzer
      </h2>
      <p style={{ color: '#64748b', fontSize: 15, lineHeight: 1.7, marginBottom: 32 }}>
        Enter any NSE/BSE stock symbol to get a comprehensive analysis covering
        technical indicators, fundamental metrics, financial statements, shareholding patterns,
        news intelligence with authenticity checks, and operator manipulation detection.
      </p>
      <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8, justifyContent: 'center' }}>
        {['RELIANCE', 'TCS', 'INFY', 'HDFCBANK', 'BAJFINANCE', 'ZOMATO'].map(sym => (
          <div key={sym} style={{
            background: '#1e293b', border: '1px solid #334155',
            borderRadius: 8, padding: '8px 16px',
            fontSize: 13, fontWeight: 600, color: '#94a3b8',
            fontFamily: 'monospace'
          }}>{sym}</div>
        ))}
      </div>
      <div style={{ marginTop: 32, padding: '12px 20px', background: 'rgba(245,158,11,0.1)', border: '1px solid rgba(245,158,11,0.3)', borderRadius: 8, fontSize: 12, color: '#94a3b8', lineHeight: 1.6 }}>
        <strong style={{ color: '#f59e0b' }}>Disclaimer: </strong>
        This tool uses mock/simulated data for demonstration. All analysis is AI-generated and for educational purposes only.
        Not to be construed as investment advice. Consult a SEBI-registered investment advisor before making any investment decisions.
        Investing in equities involves risk.
      </div>
    </div>
  );
}

export default function App() {
  const [activeTab, setActiveTab] = useState('overview');
  const [analysisData, setAnalysisData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [currentSymbol, setCurrentSymbol] = useState('');

  const handleSearch = useCallback(async (symbol) => {
    setLoading(true);
    setError(null);
    setActiveTab('overview');
    setCurrentSymbol(symbol);
    try {
      const data = await analyzeStock(symbol);
      setAnalysisData(data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to analyze stock. Please check if the backend is running on port 8080.');
      setAnalysisData(null);
    } finally {
      setLoading(false);
    }
  }, []);

  return (
    <div className="app-container" style={{ minHeight: '100vh', paddingBottom: 60 }}>
      {/* Header */}
      <header style={{
        borderBottom: '1px solid #334155',
        background: 'rgba(15,23,42,0.95)',
        backdropFilter: 'blur(10px)',
        position: 'sticky', top: 0, zIndex: 50,
        marginBottom: 0
      }}>
        <div style={{ padding: '16px 0', display: 'flex', alignItems: 'center', gap: 24, flexWrap: 'wrap' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 10, flexShrink: 0 }}>
            <div style={{
              width: 36, height: 36, borderRadius: 8,
              background: 'linear-gradient(135deg, #3b82f6, #6366f1)',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              fontSize: 18
            }}>📊</div>
            <div>
              <div style={{ fontSize: 15, fontWeight: 800, color: '#f1f5f9', lineHeight: 1.2 }}>
                IndiaStocks.AI
              </div>
              <div style={{ fontSize: 10, color: '#64748b', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                NSE / BSE Analyzer
              </div>
            </div>
          </div>
          <div style={{ flex: 1 }}>
            <SearchBar onSearch={handleSearch} loading={loading} />
          </div>
          {currentSymbol && (
            <div style={{ fontSize: 13, color: '#94a3b8', flexShrink: 0 }}>
              Analyzing: <strong style={{ color: '#3b82f6' }}>{currentSymbol}</strong>
            </div>
          )}
        </div>
      </header>

      {/* Content */}
      <main style={{ paddingTop: 24 }}>
        {loading && (
          <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', padding: '80px 20px', gap: 20 }}>
            <div className="spinner" style={{ width: 50, height: 50, borderWidth: 4 }}/>
            <div style={{ color: '#94a3b8', fontSize: 16 }}>
              Analyzing <strong style={{ color: '#3b82f6' }}>{currentSymbol}</strong>...
            </div>
            <div style={{ color: '#64748b', fontSize: 13 }}>
              Running technical indicators, fundamental analysis, news intelligence, and manipulation checks
            </div>
          </div>
        )}

        {error && (
          <div style={{
            margin: '40px auto', maxWidth: 500, textAlign: 'center',
            background: 'rgba(239,68,68,0.1)', border: '1px solid rgba(239,68,68,0.3)',
            borderRadius: 12, padding: 24
          }}>
            <div style={{ fontSize: 32, marginBottom: 12 }}>⚠</div>
            <div style={{ fontSize: 16, fontWeight: 600, color: '#ef4444', marginBottom: 8 }}>Analysis Failed</div>
            <div style={{ color: '#94a3b8', fontSize: 13, lineHeight: 1.6 }}>{error}</div>
          </div>
        )}

        {!loading && !error && !analysisData && <EmptyState />}

        {!loading && !error && analysisData && (
          <>
            {/* Recommendation Banner */}
            <RecommendationBanner
              sentiment={analysisData.overallSentiment}
              recommendation={analysisData.overallRecommendation}
              justification={analysisData.analysisJustification}
            />

            {/* Tabs */}
            <div className="tabs" style={{ marginBottom: 24 }}>
              {TABS.map(tab => (
                <button
                  key={tab.id}
                  onClick={() => setActiveTab(tab.id)}
                  className={`tab ${activeTab === tab.id ? 'active' : ''}`}
                >
                  {tab.label}
                </button>
              ))}
            </div>

            {/* Tab Content */}
            {activeTab === 'overview' && (
              <StockOverview
                data={analysisData.overview}
                symbol={analysisData.symbol}
                companyName={analysisData.companyName}
                sector={analysisData.sector}
                exchange={analysisData.exchange}
              />
            )}

            {activeTab === 'chart' && (
              <PriceChart symbol={analysisData.symbol} />
            )}

            {activeTab === 'technical' && (
              <TechnicalAnalysis data={analysisData.technicalAnalysis} />
            )}

            {activeTab === 'fundamental' && (
              <FundamentalAnalysis data={analysisData.fundamentalAnalysis} />
            )}

            {activeTab === 'financial' && (
              <FinancialStatements data={analysisData.financialStatements} />
            )}

            {activeTab === 'news' && (
              <NewsSection
                companyNews={analysisData.companyNews}
                competitorNews={analysisData.competitorNews}
              />
            )}

            {activeTab === 'shareholding' && (
              <ShareholdingPattern data={analysisData.shareholdingPattern} />
            )}

            {activeTab === 'manipulation' && (
              <ManipulationAnalysis data={analysisData.manipulationAnalysis} />
            )}

            {activeTab === 'inference' && (
              <InferenceScorecard symbol={analysisData.symbol} />
            )}
          </>
        )}
      </main>

      {/* Footer */}
      <footer style={{
        marginTop: 60, borderTop: '1px solid #1e293b',
        padding: '20px 0', textAlign: 'center',
        fontSize: 11, color: '#334155'
      }}>
        IndiaStocks.AI — For educational & demonstration purposes only.
        Mock data. Not investment advice. SEBI Disclaimer: Investments in securities market are subject to market risks. Read all related documents carefully before investing.
      </footer>
    </div>
  );
}
