import React from 'react';

function IndicatorRow({ label, value, signal, justification }) {
  const signalColor = {
    'BULLISH': '#22c55e', 'STRONG UPTREND': '#22c55e', 'BULLISH CROSSOVER': '#22c55e',
    'OVERSOLD': '#22c55e',
    'BEARISH': '#ef4444', 'BEARISH CROSSOVER': '#ef4444',
    'OVERBOUGHT': '#ef4444',
    'NEUTRAL': '#94a3b8', 'MIXED': '#94a3b8',
  }[signal] || '#f59e0b';

  return (
    <div style={{ marginBottom: 16, paddingBottom: 16, borderBottom: '1px solid rgba(51,65,85,0.5)' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 6 }}>
        <span style={{ fontSize: 13, fontWeight: 600, color: '#94a3b8' }}>{label}</span>
        <div style={{ display: 'flex', gap: 10, alignItems: 'center' }}>
          <span style={{ fontSize: 15, fontWeight: 700, color: '#f1f5f9' }}>{value}</span>
          <span style={{
            fontSize: 11, fontWeight: 700, padding: '2px 8px', borderRadius: 4,
            background: `${signalColor}20`, color: signalColor, letterSpacing: '0.04em'
          }}>
            {signal}
          </span>
        </div>
      </div>
      {justification && (
        <div style={{ fontSize: 12, color: '#64748b', lineHeight: 1.6, paddingLeft: 0 }}>
          {justification}
        </div>
      )}
    </div>
  );
}

function GaugeBar({ label, value, min, max, thresholds }) {
  const pct = Math.min(100, Math.max(0, ((value - min) / (max - min)) * 100));
  const getColor = (v) => {
    if (thresholds) {
      if (v < thresholds.low) return '#22c55e';
      if (v > thresholds.high) return '#ef4444';
      return '#f59e0b';
    }
    return '#3b82f6';
  };

  return (
    <div style={{ marginBottom: 14 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 12, marginBottom: 5 }}>
        <span style={{ color: '#94a3b8' }}>{label}</span>
        <span style={{ fontWeight: 700, color: getColor(value) }}>{value.toFixed(1)}</span>
      </div>
      <div style={{ height: 6, background: '#334155', borderRadius: 3, overflow: 'hidden' }}>
        {thresholds && (
          <>
            <div style={{
              position: 'absolute', left: `${(thresholds.low - min) / (max - min) * 100}%`,
              width: 1, height: '100%', background: '#475569'
            }}/>
            <div style={{
              position: 'absolute', left: `${(thresholds.high - min) / (max - min) * 100}%`,
              width: 1, height: '100%', background: '#475569'
            }}/>
          </>
        )}
        <div style={{ width: `${pct}%`, height: '100%', background: getColor(value), borderRadius: 3 }}/>
      </div>
      <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 10, color: '#475569', marginTop: 3 }}>
        <span>{min}</span>
        {thresholds && <span style={{ color: '#22c55e' }}>Oversold:{thresholds.low}</span>}
        {thresholds && <span style={{ color: '#ef4444' }}>Overbought:{thresholds.high}</span>}
        <span>{max}</span>
      </div>
    </div>
  );
}

export default function TechnicalAnalysis({ data }) {
  const signalColor = {
    'BULLISH': '#22c55e', 'BEARISH': '#ef4444', 'NEUTRAL': '#94a3b8'
  }[data.overallTechnicalSignal] || '#f59e0b';

  return (
    <div>
      {/* Overall Signal Banner */}
      <div style={{
        background: `${signalColor}15`,
        border: `1px solid ${signalColor}40`,
        borderRadius: 10, padding: '14px 18px',
        marginBottom: 20,
        display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 10
      }}>
        <div>
          <div style={{ fontSize: 12, color: '#94a3b8', marginBottom: 4 }}>Overall Technical Signal</div>
          <div style={{ fontSize: 22, fontWeight: 800, color: signalColor }}>{data.overallTechnicalSignal}</div>
        </div>
        <div style={{ fontSize: 13, color: '#94a3b8', maxWidth: 500, lineHeight: 1.6 }}>
          {data.technicalSummary}
        </div>
      </div>

      <div className="grid-2">
        {/* Left column */}
        <div>
          <div className="card" style={{ marginBottom: 16 }}>
            <div className="card-title">
              <svg width="14" height="14" fill="#3b82f6" viewBox="0 0 24 24">
                <circle cx="12" cy="12" r="10"/><polyline points="12,6 12,12 16,14" stroke="#fff" strokeWidth="2" fill="none"/>
              </svg>
              Momentum Indicators
            </div>
            <GaugeBar label="RSI (14)" value={data.rsi} min={0} max={100}
              thresholds={{ low: 30, high: 70 }} />
            <IndicatorRow
              label="RSI Signal"
              value={data.rsi.toFixed(1)}
              signal={data.rsiSignal}
              justification={data.rsiJustification}
            />
            <GaugeBar label="Stochastic %K" value={data.stochasticK} min={0} max={100}
              thresholds={{ low: 20, high: 80 }} />
            <IndicatorRow
              label="Stochastic"
              value={`%K: ${data.stochasticK.toFixed(1)} / %D: ${data.stochasticD.toFixed(1)}`}
              signal={data.stochasticSignal}
            />
          </div>

          <div className="card">
            <div className="card-title">
              <svg width="14" height="14" fill="none" stroke="#a855f7" strokeWidth="2" viewBox="0 0 24 24">
                <polyline points="22,12 18,12 15,21 9,3 6,12 2,12"/>
              </svg>
              MACD
            </div>
            <IndicatorRow
              label="MACD"
              value={`${data.macd.toFixed(2)} / Signal: ${data.macdSignal.toFixed(2)}`}
              signal={data.macdTrend}
              justification={data.macdJustification}
            />
            <div className="stat-row">
              <span className="stat-label">Histogram</span>
              <span style={{ fontWeight: 600, color: data.macdHistogram >= 0 ? '#22c55e' : '#ef4444' }}>
                {data.macdHistogram.toFixed(2)}
              </span>
            </div>
          </div>
        </div>

        {/* Right column */}
        <div>
          <div className="card" style={{ marginBottom: 16 }}>
            <div className="card-title">
              <svg width="14" height="14" fill="none" stroke="#f59e0b" strokeWidth="2" viewBox="0 0 24 24">
                <line x1="22" y1="12" x2="2" y2="12"/><line x1="22" y1="6" x2="2" y2="6"/><line x1="22" y1="18" x2="2" y2="18"/>
              </svg>
              Moving Averages
            </div>
            <IndicatorRow
              label="SMA Alignment"
              value={`SMA20: ₹${data.sma20.toFixed(0)}`}
              signal={data.movingAvgSignal}
              justification={data.movingAvgJustification}
            />
            <div className="stat-row">
              <span className="stat-label">SMA 20</span>
              <span className="stat-value">₹{data.sma20.toLocaleString('en-IN', { minimumFractionDigits: 2 })}</span>
            </div>
            <div className="stat-row">
              <span className="stat-label">SMA 50</span>
              <span className="stat-value">₹{data.sma50.toLocaleString('en-IN', { minimumFractionDigits: 2 })}</span>
            </div>
            <div className="stat-row">
              <span className="stat-label">SMA 200</span>
              <span className="stat-value">₹{data.sma200.toLocaleString('en-IN', { minimumFractionDigits: 2 })}</span>
            </div>
          </div>

          <div className="card">
            <div className="card-title">
              <svg width="14" height="14" fill="none" stroke="#06b6d4" strokeWidth="2" viewBox="0 0 24 24">
                <path d="M22 12c0-5.523-4.477-10-10-10S2 6.477 2 12s4.477 10 10 10 10-4.477 10-10z"/>
              </svg>
              Bollinger Bands & Volatility
            </div>
            <div className="stat-row">
              <span className="stat-label">Upper Band</span>
              <span style={{ color: '#ef4444', fontWeight: 600 }}>₹{data.bollingerUpper.toLocaleString('en-IN', { minimumFractionDigits: 2 })}</span>
            </div>
            <div className="stat-row">
              <span className="stat-label">Middle Band</span>
              <span style={{ color: '#f59e0b', fontWeight: 600 }}>₹{data.bollingerMiddle.toLocaleString('en-IN', { minimumFractionDigits: 2 })}</span>
            </div>
            <div className="stat-row">
              <span className="stat-label">Lower Band</span>
              <span style={{ color: '#22c55e', fontWeight: 600 }}>₹{data.bollingerLower.toLocaleString('en-IN', { minimumFractionDigits: 2 })}</span>
            </div>
            <div className="stat-row">
              <span className="stat-label">BB Signal</span>
              <span style={{ fontSize: 12, color: '#94a3b8' }}>{data.bollingerSignal}</span>
            </div>
            <div className="stat-row">
              <span className="stat-label">ATR (Volatility)</span>
              <span className="stat-value">₹{data.atr.toFixed(2)}</span>
            </div>
            <div className="stat-row">
              <span className="stat-label">OBV Trend</span>
              <span style={{ fontSize: 12, color: data.obvTrend?.includes('RISING') ? '#22c55e' : '#ef4444' }}>{data.obvTrend}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
