import React from 'react';
import { PieChart, Pie, Cell, AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend } from 'recharts';

const COLORS = ['#3b82f6', '#22c55e', '#f59e0b', '#94a3b8'];
const HOLDER_LABELS = ['Promoter', 'FII', 'DII', 'Public'];

const RADIAN = Math.PI / 180;
const renderCustomLabel = ({ cx, cy, midAngle, innerRadius, outerRadius, percent }) => {
  if (percent < 0.05) return null;
  const radius = innerRadius + (outerRadius - innerRadius) * 0.5;
  const x = cx + radius * Math.cos(-midAngle * RADIAN);
  const y = cy + radius * Math.sin(-midAngle * RADIAN);
  return (
    <text x={x} y={y} fill="white" textAnchor="middle" dominantBaseline="central" fontSize={11} fontWeight={600}>
      {`${(percent * 100).toFixed(1)}%`}
    </text>
  );
};

export default function ShareholdingPattern({ data }) {
  const riskColor = { 'LOW': '#22c55e', 'MEDIUM': '#f59e0b', 'HIGH': '#ef4444' }[data.riskLevel] || '#94a3b8';

  const pieData = [
    { name: 'Promoter', value: data.promoterHolding },
    { name: 'FII', value: data.fiiHolding },
    { name: 'DII', value: data.diiHolding },
    { name: 'Public', value: data.publicHolding },
  ].filter(d => d.value > 0);

  const trendData = (data.trend || []).map(t => ({
    quarter: t.quarter,
    Promoter: t.promoter,
    FII: t.fii,
    DII: t.dii,
    Public: t.public_
  }));

  return (
    <div>
      {/* Risk banner */}
      <div style={{
        background: `${riskColor}15`, border: `1px solid ${riskColor}40`,
        borderRadius: 10, padding: '14px 20px', marginBottom: 20,
        display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 12
      }}>
        <div>
          <div style={{ fontSize: 12, color: '#94a3b8', marginBottom: 4 }}>Shareholding Risk Level</div>
          <div style={{ fontSize: 24, fontWeight: 800, color: riskColor }}>{data.riskLevel} RISK</div>
          {data.promoterPledged > 0 && (
            <div style={{ fontSize: 13, color: '#f59e0b', marginTop: 4 }}>
              ⚠ {data.promoterPledged.toFixed(1)}% of promoter holding is pledged
            </div>
          )}
        </div>
        <div style={{ fontSize: 13, color: '#94a3b8', maxWidth: 500, lineHeight: 1.6 }}>
          {data.analysis}
        </div>
      </div>

      <div className="grid-2" style={{ marginBottom: 20 }}>
        {/* Pie chart */}
        <div className="card">
          <div className="card-title">Current Shareholding Distribution</div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 20, flexWrap: 'wrap' }}>
            <PieChart width={180} height={180}>
              <Pie data={pieData} cx={90} cy={90} outerRadius={80}
                dataKey="value" labelLine={false} label={renderCustomLabel}>
                {pieData.map((_, i) => (
                  <Cell key={i} fill={COLORS[i % COLORS.length]} stroke="none" />
                ))}
              </Pie>
            </PieChart>
            <div className="pie-legend">
              {pieData.map((d, i) => (
                <div key={i} className="pie-legend-item">
                  <div className="pie-dot" style={{ background: COLORS[i % COLORS.length] }} />
                  <span style={{ color: '#94a3b8' }}>{d.name}</span>
                  <span style={{ marginLeft: 'auto', fontWeight: 700, color: '#f1f5f9', minWidth: 50, textAlign: 'right' }}>
                    {d.value.toFixed(1)}%
                  </span>
                </div>
              ))}
              {data.promoterPledged > 0 && (
                <div style={{ marginTop: 8, padding: '6px 10px', background: 'rgba(245,158,11,0.1)',
                  border: '1px solid rgba(245,158,11,0.3)', borderRadius: 6, fontSize: 11, color: '#f59e0b' }}>
                  Pledged: {data.promoterPledged.toFixed(1)}% of promoter holding
                </div>
              )}
            </div>
          </div>
        </div>

        {/* Current numbers */}
        <div className="card">
          <div className="card-title">Detailed Breakdown</div>
          <div className="stat-row">
            <span className="stat-label">Promoter + Group</span>
            <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
              <div style={{ width: 60, height: 4, background: '#334155', borderRadius: 2, overflow: 'hidden' }}>
                <div style={{ width: `${data.promoterHolding}%`, height: '100%', background: '#3b82f6' }} />
              </div>
              <span style={{ fontWeight: 700, color: data.promoterHolding > 50 ? '#22c55e' : '#f1f5f9' }}>
                {data.promoterHolding.toFixed(2)}%
              </span>
            </div>
          </div>
          <div className="stat-row">
            <span className="stat-label">Promoter Pledged</span>
            <span style={{ fontWeight: 700, color: data.promoterPledged > 30 ? '#ef4444' : data.promoterPledged > 10 ? '#f59e0b' : '#22c55e' }}>
              {data.promoterPledged.toFixed(2)}%
              {data.promoterPledged > 30 && ' ⚠'}
            </span>
          </div>
          <div className="stat-row">
            <span className="stat-label">FII / FPI</span>
            <span style={{ fontWeight: 700, color: data.fiiHolding > 15 ? '#22c55e' : '#f1f5f9' }}>
              {data.fiiHolding.toFixed(2)}%
            </span>
          </div>
          <div className="stat-row">
            <span className="stat-label">DII (MF + Insurance)</span>
            <span style={{ fontWeight: 700, color: '#f1f5f9' }}>
              {data.diiHolding.toFixed(2)}%
            </span>
          </div>
          <div className="stat-row">
            <span className="stat-label">Public / Retail</span>
            <span className="stat-value">{data.publicHolding.toFixed(2)}%</span>
          </div>
          <div style={{ marginTop: 12, padding: '10px 12px', background: '#0f172a', borderRadius: 8 }}>
            <div style={{ fontSize: 12, color: '#64748b', marginBottom: 4 }}>Institutional Interest (FII + DII)</div>
            <div style={{ fontSize: 20, fontWeight: 800, color: (data.fiiHolding + data.diiHolding) > 25 ? '#22c55e' : '#f1f5f9' }}>
              {(data.fiiHolding + data.diiHolding).toFixed(1)}%
            </div>
          </div>
        </div>
      </div>

      {/* Trend chart */}
      {trendData.length > 0 && (
        <div className="card" style={{ marginBottom: 20 }}>
          <div className="card-title">Shareholding Trend (6 Quarters)</div>
          <ResponsiveContainer width="100%" height={200}>
            <AreaChart data={trendData} margin={{ top: 5, right: 5, left: 0, bottom: 0 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" vertical={false} />
              <XAxis dataKey="quarter" tick={{ fill: '#64748b', fontSize: 10 }} tickLine={false} axisLine={false} />
              <YAxis tick={{ fill: '#64748b', fontSize: 10 }} tickLine={false} axisLine={false}
                tickFormatter={v => `${v.toFixed(0)}%`} width={35} />
              <Tooltip
                contentStyle={{ background: '#1e293b', border: '1px solid #334155', borderRadius: 8, fontSize: 12 }}
                formatter={(v, n) => [`${v.toFixed(2)}%`, n]}
              />
              <Legend wrapperStyle={{ fontSize: 11, color: '#94a3b8' }} />
              <Area type="monotone" dataKey="Promoter" stackId="no" stroke="#3b82f6" fill="rgba(59,130,246,0.1)" strokeWidth={2} />
              <Area type="monotone" dataKey="FII" stackId="no" stroke="#22c55e" fill="rgba(34,197,94,0.1)" strokeWidth={2} />
              <Area type="monotone" dataKey="DII" stackId="no" stroke="#f59e0b" fill="rgba(245,158,11,0.1)" strokeWidth={2} />
            </AreaChart>
          </ResponsiveContainer>
        </div>
      )}

      {/* Red flags */}
      <div className="card">
        <div className="card-title">Shareholding Red Flags & Observations</div>
        {(data.redFlags || []).map((flag, i) => (
          <div key={i} className="flag-item">
            <span className="flag-icon">
              {flag.includes('No significant') || flag.includes('No major')
                ? <svg width="14" height="14" fill="#22c55e" viewBox="0 0 24 24"><path d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>
                : <svg width="14" height="14" fill="#ef4444" viewBox="0 0 24 24"><path d="M12 9v4m0 4h.01M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/></svg>
              }
            </span>
            <span style={{ color: flag.includes('No significant') || flag.includes('No major') ? '#22c55e' : '#94a3b8' }}>
              {flag}
            </span>
          </div>
        ))}
      </div>
    </div>
  );
}
