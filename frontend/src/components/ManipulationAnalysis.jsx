import React from 'react';

function ScoreGauge({ label, value, max = 100 }) {
  const pct = Math.min(100, (value / max) * 100);
  const color = pct < 30 ? '#22c55e' : pct < 60 ? '#f59e0b' : '#ef4444';

  return (
    <div style={{ marginBottom: 16 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 6, fontSize: 13 }}>
        <span style={{ color: '#94a3b8' }}>{label}</span>
        <span style={{ fontWeight: 700, color }}>{value.toFixed(1)}/{max}</span>
      </div>
      <div style={{ height: 8, background: '#334155', borderRadius: 4, overflow: 'hidden' }}>
        <div style={{
          width: `${pct}%`, height: '100%', borderRadius: 4,
          background: `linear-gradient(90deg, #22c55e, ${pct > 50 ? '#f59e0b' : '#22c55e'}, ${pct > 70 ? '#ef4444' : pct > 50 ? '#f59e0b' : '#22c55e'})`
        }}/>
      </div>
    </div>
  );
}

export default function ManipulationAnalysis({ data }) {
  const riskColor = { 'LOW': '#22c55e', 'MEDIUM': '#f59e0b', 'HIGH': '#ef4444' }[data.manipulationRiskLevel] || '#94a3b8';
  const pdColor = { 'LOW': '#22c55e', 'MEDIUM': '#f59e0b', 'HIGH': '#ef4444' }[data.pumpAndDumpRisk] || '#94a3b8';

  return (
    <div>
      {/* Main banner */}
      <div style={{
        background: `${riskColor}15`, border: `1px solid ${riskColor}40`,
        borderRadius: 12, padding: '20px 24px', marginBottom: 20
      }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: 16 }}>
          <div>
            <div style={{ fontSize: 12, color: '#94a3b8', marginBottom: 4, textTransform: 'uppercase', letterSpacing: '0.05em' }}>
              Operator / Manipulation Risk
            </div>
            <div style={{ display: 'flex', gap: 16, alignItems: 'center' }}>
              <div style={{ fontSize: 28, fontWeight: 800, color: riskColor }}>{data.manipulationRiskLevel}</div>
              {data.suspectedManipulation && (
                <div style={{
                  background: 'rgba(239,68,68,0.15)', border: '1px solid rgba(239,68,68,0.4)',
                  borderRadius: 6, padding: '4px 12px', fontSize: 12, fontWeight: 700, color: '#ef4444'
                }}>
                  ⚠ MANIPULATION SUSPECTED
                </div>
              )}
            </div>
            <div style={{ marginTop: 8, display: 'flex', gap: 16, fontSize: 13 }}>
              <span style={{ color: '#64748b' }}>
                Pump & Dump Risk: <span style={{ color: pdColor, fontWeight: 700 }}>{data.pumpAndDumpRisk}</span>
              </span>
              <span style={{ color: '#64748b' }}>
                Circuit Hits (30d): <span style={{ fontWeight: 700, color: data.circuitBreakerHits > 2 ? '#ef4444' : '#94a3b8' }}>
                  {data.circuitBreakerHits}
                </span>
              </span>
            </div>
          </div>
        </div>
      </div>

      <div className="grid-2" style={{ marginBottom: 20 }}>
        {/* Scores */}
        <div className="card">
          <div className="card-title">
            <svg width="14" height="14" fill="none" stroke="#3b82f6" strokeWidth="2" viewBox="0 0 24 24">
              <polyline points="22,12 18,12 15,21 9,3 6,12 2,12"/>
            </svg>
            Manipulation Scores
          </div>
          <ScoreGauge label="Unusual Volume Score" value={data.unusualVolumeScore} />
          <ScoreGauge label="Price Pattern Anomaly Score" value={data.pricePatternScore} />
          <div style={{ marginTop: 16, padding: '12px 14px', background: '#0f172a', borderRadius: 8 }}>
            <div style={{ fontSize: 11, color: '#64748b', marginBottom: 6 }}>OVERALL RISK SCORE</div>
            <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
              <div style={{ fontSize: 32, fontWeight: 800, color: riskColor }}>
                {((data.unusualVolumeScore + data.pricePatternScore) / 2).toFixed(0)}
                <span style={{ fontSize: 16, color: '#64748b' }}>/100</span>
              </div>
              <div style={{ fontSize: 12, color: '#64748b' }}>
                Higher score = more suspicious activity
              </div>
            </div>
          </div>
        </div>

        {/* Operator Analysis */}
        <div className="card">
          <div className="card-title">
            <svg width="14" height="14" fill="none" stroke="#f59e0b" strokeWidth="2" viewBox="0 0 24 24">
              <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><circle cx="12" cy="16" r="0.5" fill="#f59e0b"/>
            </svg>
            Operator Activity Analysis
          </div>
          <div className="justification-box yellow" style={{ marginTop: 0 }}>
            {data.operatorActivityAnalysis}
          </div>
        </div>
      </div>

      {/* Red flags and green flags */}
      <div className="grid-2" style={{ marginBottom: 20 }}>
        <div className="card">
          <div className="card-title" style={{ color: '#ef4444' }}>
            <svg width="14" height="14" fill="#ef4444" viewBox="0 0 24 24">
              <path d="M4 15s1-1 4-1 5 2 8 2 4-1 4-1V3s-1 1-4 1-5-2-8-2-4 1-4 1zM4 22v-7"/>
            </svg>
            Warning Signals
          </div>
          {(data.redFlags || []).map((flag, i) => (
            <div key={i} className="flag-item">
              <span className="flag-icon">
                <svg width="14" height="14" fill="#ef4444" viewBox="0 0 24 24">
                  <path d="M12 9v4m0 4h.01M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/>
                </svg>
              </span>
              <span style={{ color: '#94a3b8', fontSize: 13 }}>{flag}</span>
            </div>
          ))}
          {(!data.redFlags || data.redFlags.length === 0) && (
            <div style={{ color: '#22c55e', fontSize: 13 }}>No warning signals detected</div>
          )}
        </div>

        <div className="card">
          <div className="card-title" style={{ color: '#22c55e' }}>
            <svg width="14" height="14" fill="#22c55e" viewBox="0 0 24 24">
              <path d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
            </svg>
            Clean Indicators
          </div>
          {(data.greenFlags || []).map((flag, i) => (
            <div key={i} className="flag-item">
              <span className="flag-icon">
                <svg width="14" height="14" fill="#22c55e" viewBox="0 0 24 24">
                  <path d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
                </svg>
              </span>
              <span style={{ color: '#94a3b8', fontSize: 13 }}>{flag}</span>
            </div>
          ))}
        </div>
      </div>

      {/* Full justification */}
      <div className="card">
        <div className="card-title">Full Analysis Justification</div>
        <div className="justification-box">
          {data.analysisJustification}
        </div>
        <div style={{
          marginTop: 16, padding: '12px 16px',
          background: 'rgba(59,130,246,0.05)', border: '1px solid rgba(59,130,246,0.15)',
          borderRadius: 8, fontSize: 12, color: '#64748b', lineHeight: 1.7
        }}>
          <strong style={{ color: '#3b82f6' }}>Disclaimer: </strong>
          Manipulation detection uses quantitative pattern analysis on price and volume data.
          This is for informational purposes only. SEBI has dedicated Market Intelligence and Surveillance (MIS) systems
          for official manipulation detection. Always verify through official SEBI/Exchange disclosures before drawing conclusions.
          Under SEBI (Prohibition of Fraudulent and Unfair Trade Practices) Regulations, 2003, market manipulation is a serious offence.
        </div>
      </div>
    </div>
  );
}
