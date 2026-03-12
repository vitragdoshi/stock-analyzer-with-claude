import React, { useState } from 'react';
import {
  BarChart, Bar, LineChart, Line, XAxis, YAxis, CartesianGrid,
  Tooltip, ResponsiveContainer, Legend
} from 'recharts';

function formatCr(val) {
  if (Math.abs(val) >= 100000) return `₹${(val / 100000).toFixed(1)}L Cr`;
  if (Math.abs(val) >= 1000) return `₹${(val / 1000).toFixed(1)}K Cr`;
  return `₹${val.toFixed(0)} Cr`;
}

function GrowthPill({ value }) {
  if (value === undefined || value === null) return <span style={{ color: '#475569' }}>—</span>;
  const color = value > 0 ? '#22c55e' : '#ef4444';
  return (
    <span style={{ color, fontWeight: 700 }}>
      {value > 0 ? '+' : ''}{value.toFixed(1)}%
    </span>
  );
}

const CustomTooltip = ({ active, payload, label }) => {
  if (!active || !payload?.length) return null;
  return (
    <div style={{ background: '#1e293b', border: '1px solid #334155', borderRadius: 8, padding: '10px 14px', fontSize: 12 }}>
      <div style={{ color: '#94a3b8', marginBottom: 6 }}>{label}</div>
      {payload.map((p, i) => (
        <div key={i} style={{ color: p.color, marginBottom: 2 }}>
          {p.name}: {formatCr(p.value)}
        </div>
      ))}
    </div>
  );
};

export default function FinancialStatements({ data }) {
  const [view, setView] = useState('annual');

  const annual = data.annualStatements || [];
  const quarterly = data.quarterlyStatements || [];
  const items = view === 'annual' ? annual : quarterly;
  const labelKey = view === 'annual' ? 'year' : 'quarter';

  return (
    <div>
      {/* Summary cards */}
      <div className="grid-2" style={{ marginBottom: 20 }}>
        <div className="card">
          <div className="card-title">Financial Health Summary</div>
          <div className="justification-box" style={{ marginTop: 0 }}>
            {data.financialHealthSummary}
          </div>
        </div>
        <div className="card">
          <div className="card-title">Growth Trend Analysis</div>
          <div className="justification-box green" style={{ marginTop: 0 }}>
            {data.growthTrendAnalysis}
          </div>
          {data.redFlags && data.redFlags !== 'No major financial red flags detected' && (
            <div className="justification-box red" style={{ marginTop: 10 }}>
              <strong>⚠ Red Flags: </strong>{data.redFlags}
            </div>
          )}
        </div>
      </div>

      {/* Toggle */}
      <div style={{ display: 'flex', gap: 4, background: '#0f172a', padding: 4, borderRadius: 8, marginBottom: 20, width: 'fit-content' }}>
        {['annual', 'quarterly'].map(v => (
          <button key={v} onClick={() => setView(v)} style={{
            padding: '7px 20px', border: 'none', borderRadius: 6, cursor: 'pointer',
            background: view === v ? '#1e293b' : 'transparent',
            color: view === v ? '#f1f5f9' : '#64748b',
            fontSize: 13, fontWeight: view === v ? 600 : 400, fontFamily: 'inherit',
            transition: 'all 0.15s', textTransform: 'capitalize'
          }}>
            {v === 'annual' ? 'Annual (FY)' : 'Quarterly'}
          </button>
        ))}
      </div>

      {/* Charts */}
      <div className="grid-2" style={{ marginBottom: 20 }}>
        <div className="card">
          <div className="card-title">Revenue & Net Profit (₹ Cr)</div>
          <ResponsiveContainer width="100%" height={200}>
            <BarChart data={items} margin={{ top: 5, right: 5, left: 0, bottom: 0 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" vertical={false} />
              <XAxis dataKey={labelKey} tick={{ fill: '#64748b', fontSize: 10 }} tickLine={false} axisLine={false} />
              <YAxis tick={{ fill: '#64748b', fontSize: 10 }} tickLine={false} axisLine={false}
                tickFormatter={v => formatCr(v)} width={70} />
              <Tooltip content={<CustomTooltip />} />
              <Legend wrapperStyle={{ fontSize: 11, color: '#94a3b8' }} />
              <Bar dataKey="revenue" name="Revenue" fill="#3b82f6" radius={[3, 3, 0, 0]} />
              <Bar dataKey="netProfit" name="Net Profit" fill="#22c55e" radius={[3, 3, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>

        <div className="card">
          <div className="card-title">EBITDA Margin & Net Margin (%)</div>
          <ResponsiveContainer width="100%" height={200}>
            <LineChart data={items} margin={{ top: 5, right: 5, left: 0, bottom: 0 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" vertical={false} />
              <XAxis dataKey={labelKey} tick={{ fill: '#64748b', fontSize: 10 }} tickLine={false} axisLine={false} />
              <YAxis tick={{ fill: '#64748b', fontSize: 10 }} tickLine={false} axisLine={false}
                tickFormatter={v => `${v.toFixed(0)}%`} width={45} />
              <Tooltip
                contentStyle={{ background: '#1e293b', border: '1px solid #334155', borderRadius: 8, fontSize: 12 }}
                labelStyle={{ color: '#94a3b8' }}
                formatter={(v, n) => [`${v.toFixed(1)}%`, n]}
              />
              <Legend wrapperStyle={{ fontSize: 11, color: '#94a3b8' }} />
              <Line type="monotone" dataKey="ebitdaMargin" name="EBITDA Margin" stroke="#f59e0b" strokeWidth={2} dot={{ r: 3 }} />
              <Line type="monotone" dataKey="netMargin" name="Net Margin" stroke="#22c55e" strokeWidth={2} dot={{ r: 3 }} />
            </LineChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Table */}
      <div className="card" style={{ overflowX: 'auto' }}>
        <div className="card-title">{view === 'annual' ? 'Annual Statement Details' : 'Quarterly Statement Details'}</div>
        <table className="data-table">
          <thead>
            <tr>
              <th>{view === 'annual' ? 'Year' : 'Quarter'}</th>
              <th>Revenue</th>
              <th>EBITDA</th>
              <th>Net Profit</th>
              <th>EBITDA Margin</th>
              <th>Net Margin</th>
              {view === 'annual' ? (
                <>
                  <th>Rev Growth</th>
                  <th>PAT Growth</th>
                  <th>Op. Cash Flow</th>
                  <th>Free Cash Flow</th>
                  <th>Total Debt</th>
                </>
              ) : (
                <>
                  <th>Rev Growth YoY</th>
                  <th>Rev Growth QoQ</th>
                  <th>PAT Growth YoY</th>
                </>
              )}
            </tr>
          </thead>
          <tbody>
            {items.map((row, i) => (
              <tr key={i}>
                <td style={{ fontWeight: 600, color: '#f1f5f9' }}>{row[labelKey]}</td>
                <td>{formatCr(row.revenue)}</td>
                <td>{formatCr(row.ebitda)}</td>
                <td style={{ color: row.netProfit >= 0 ? '#22c55e' : '#ef4444', fontWeight: 600 }}>
                  {formatCr(row.netProfit)}
                </td>
                <td>{row.ebitdaMargin?.toFixed(1)}%</td>
                <td>{row.netMargin?.toFixed(1)}%</td>
                {view === 'annual' ? (
                  <>
                    <td><GrowthPill value={row.revenueGrowth} /></td>
                    <td><GrowthPill value={row.profitGrowth} /></td>
                    <td>{formatCr(row.operatingCashFlow || 0)}</td>
                    <td style={{ color: (row.freeCashFlow || 0) >= 0 ? '#22c55e' : '#ef4444' }}>
                      {formatCr(row.freeCashFlow || 0)}
                    </td>
                    <td>{formatCr(row.totalDebt || 0)}</td>
                  </>
                ) : (
                  <>
                    <td><GrowthPill value={row.revenueGrowthYoY} /></td>
                    <td><GrowthPill value={row.revenueGrowthQoQ} /></td>
                    <td><GrowthPill value={row.profitGrowthYoY} /></td>
                  </>
                )}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
