import React from 'react';
import { RadarChart, Radar, PolarGrid, PolarAngleAxis, ResponsiveContainer } from 'recharts';

function MetricCard({ label, value, benchmark, status, justification }) {
  const statusColor = { 'GOOD': '#22c55e', 'CAUTION': '#f59e0b', 'POOR': '#ef4444', 'NEUTRAL': '#94a3b8' }[status] || '#94a3b8';
  return (
    <div style={{
      background: '#0f172a', border: '1px solid #334155', borderRadius: 8,
      padding: '14px 16px', transition: 'border-color 0.2s'
    }}>
      <div style={{ fontSize: 11, color: '#64748b', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: 6 }}>{label}</div>
      <div style={{ fontSize: 22, fontWeight: 800, color: statusColor, marginBottom: 4 }}>{value}</div>
      {benchmark && <div style={{ fontSize: 11, color: '#475569' }}>Industry: {benchmark}</div>}
      {justification && <div style={{ fontSize: 11, color: '#64748b', marginTop: 6, lineHeight: 1.5 }}>{justification}</div>}
    </div>
  );
}

function getStatus(value, good, caution) {
  if (good(value)) return 'GOOD';
  if (caution(value)) return 'CAUTION';
  return 'POOR';
}

export default function FundamentalAnalysis({ data }) {
  const valColor = { 'UNDERVALUED': '#22c55e', 'OVERVALUED': '#ef4444', 'FAIRLY VALUED': '#f59e0b' }[data.valuationStatus] || '#94a3b8';

  const radarData = [
    { subject: 'ROE', A: Math.min(100, (data.roe / 30) * 100), fullMark: 100 },
    { subject: 'ROCE', A: Math.min(100, (data.roce / 30) * 100), fullMark: 100 },
    { subject: 'Growth', A: Math.min(100, Math.max(0, (data.revenueGrowthYoY + 10) / 50 * 100)), fullMark: 100 },
    { subject: 'Low Debt', A: Math.min(100, Math.max(0, (2 - data.debtToEquity) / 2 * 100)), fullMark: 100 },
    { subject: 'Liquidity', A: Math.min(100, (data.currentRatio / 3) * 100), fullMark: 100 },
    { subject: 'Promoter', A: Math.min(100, (data.promoterHolding / 75) * 100), fullMark: 100 },
  ];

  return (
    <div>
      {/* Valuation banner */}
      <div style={{
        background: `${valColor}15`, border: `1px solid ${valColor}40`,
        borderRadius: 10, padding: '16px 20px', marginBottom: 20,
        display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 12
      }}>
        <div>
          <div style={{ fontSize: 12, color: '#94a3b8', marginBottom: 4 }}>Valuation Status</div>
          <div style={{ fontSize: 24, fontWeight: 800, color: valColor }}>{data.valuationStatus}</div>
          <div style={{ fontSize: 13, color: '#64748b', marginTop: 4 }}>
            Intrinsic Value Est: <strong style={{ color: '#f1f5f9' }}>₹{data.estimatedIntrinsicValue.toLocaleString('en-IN', { minimumFractionDigits: 2 })}</strong>
            <span style={{ marginLeft: 12 }}>P/IV Ratio: <strong style={{ color: valColor }}>{data.priceToIntrinsicRatio.toFixed(2)}x</strong></span>
          </div>
        </div>
        <div style={{ fontSize: 13, color: '#94a3b8', maxWidth: 440, lineHeight: 1.6 }}>
          {data.fundamentalSummary}
        </div>
      </div>

      <div className="grid-2" style={{ marginBottom: 16 }}>
        <div>
          {/* Key Metrics Grid */}
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10, marginBottom: 16 }}>
            <MetricCard
              label="P/E Ratio"
              value={`${data.pe.toFixed(1)}x`}
              benchmark={`${data.industryPe.toFixed(1)}x`}
              status={getStatus(data.pe, v => v < data.industryPe * 0.9, v => v < data.industryPe * 1.2)}
              justification={data.peJustification?.substring(0, 100) + '...'}
            />
            <MetricCard label="P/B Ratio" value={`${data.pb.toFixed(1)}x`}
              status={getStatus(data.pb, v => v < 3, v => v < 6)} />
            <MetricCard label="ROE" value={`${data.roe.toFixed(1)}%`}
              benchmark="15%+"
              status={getStatus(data.roe, v => v > 18, v => v > 12)} />
            <MetricCard label="ROCE" value={`${data.roce.toFixed(1)}%`}
              benchmark="12%+"
              status={getStatus(data.roce, v => v > 15, v => v > 10)} />
            <MetricCard label="EPS" value={`₹${data.eps.toFixed(2)}`}
              status="NEUTRAL" />
            <MetricCard label="D/E Ratio" value={`${data.debtToEquity.toFixed(2)}x`}
              benchmark="<1.0"
              status={getStatus(data.debtToEquity, v => v < 0.5, v => v < 1.0)} />
            <MetricCard label="Current Ratio" value={`${data.currentRatio.toFixed(2)}x`}
              benchmark=">1.5"
              status={getStatus(data.currentRatio, v => v > 2, v => v > 1.2)} />
            <MetricCard label="Dividend Yield" value={`${data.dividendYield.toFixed(2)}%`}
              status="NEUTRAL" />
          </div>

          {/* Growth metrics */}
          <div className="card">
            <div className="card-title">Growth Metrics</div>
            <div className="stat-row">
              <span className="stat-label">Revenue Growth YoY</span>
              <span style={{ fontWeight: 700, color: data.revenueGrowthYoY > 0 ? '#22c55e' : '#ef4444' }}>
                {data.revenueGrowthYoY > 0 ? '+' : ''}{data.revenueGrowthYoY.toFixed(1)}%
              </span>
            </div>
            <div className="stat-row">
              <span className="stat-label">Profit Growth YoY</span>
              <span style={{ fontWeight: 700, color: data.profitGrowthYoY > 0 ? '#22c55e' : '#ef4444' }}>
                {data.profitGrowthYoY > 0 ? '+' : ''}{data.profitGrowthYoY.toFixed(1)}%
              </span>
            </div>
            <div className="stat-row">
              <span className="stat-label">Promoter Holding</span>
              <span style={{ fontWeight: 700, color: data.promoterHolding > 50 ? '#22c55e' : '#f59e0b' }}>
                {data.promoterHolding.toFixed(1)}%
              </span>
            </div>
            <div className="stat-row">
              <span className="stat-label">FII Holding</span>
              <span className="stat-value">{data.fiiHolding.toFixed(1)}%</span>
            </div>
            <div className="stat-row">
              <span className="stat-label">DII Holding</span>
              <span className="stat-value">{data.diiHolding.toFixed(1)}%</span>
            </div>
          </div>
        </div>

        {/* Radar chart + Intrinsic value */}
        <div>
          <div className="card" style={{ marginBottom: 16 }}>
            <div className="card-title">Financial Health Radar</div>
            <ResponsiveContainer width="100%" height={220}>
              <RadarChart data={radarData}>
                <PolarGrid stroke="#334155" />
                <PolarAngleAxis dataKey="subject" tick={{ fill: '#94a3b8', fontSize: 11 }} />
                <Radar name="Score" dataKey="A" stroke="#3b82f6" fill="#3b82f6" fillOpacity={0.2} strokeWidth={2} />
              </RadarChart>
            </ResponsiveContainer>
          </div>

          <div className="card">
            <div className="card-title">Intrinsic Value Analysis</div>
            <div className="justification-box" style={{
              background: `${valColor}08`, borderColor: `${valColor}30`, marginTop: 0
            }}>
              {data.intrinsicValueJustification}
            </div>
            <div style={{ marginTop: 12 }}>
              <div className="stat-row">
                <span className="stat-label">P/E vs Industry</span>
                <span style={{
                  fontWeight: 700,
                  color: data.pe < data.industryPe ? '#22c55e' : data.pe > data.industryPe * 1.3 ? '#ef4444' : '#f59e0b'
                }}>
                  {data.pe < data.industryPe ? 'DISCOUNT' : data.pe > data.industryPe * 1.3 ? 'PREMIUM' : 'IN-LINE'}
                  {' '}({((data.pe - data.industryPe) / data.industryPe * 100).toFixed(1)}%)
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* P/E Justification */}
      <div className="card">
        <div className="card-title">P/E Analysis Justification</div>
        <div className="justification-box">
          {data.peJustification}
        </div>
      </div>
    </div>
  );
}
