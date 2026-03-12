import React, { useState, useEffect } from 'react';
import { ComposedChart, Bar, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, ReferenceLine } from 'recharts';
import { getChartData } from '../services/api';

const TIMEFRAMES = ['1W', '1M', '3M', '6M', '1Y', '3Y', '5Y'];

const CustomTooltip = ({ active, payload, label }) => {
  if (!active || !payload || !payload.length) return null;
  const d = payload[0]?.payload;
  if (!d) return null;
  const isGreen = d.close >= d.open;

  return (
    <div style={{
      background: '#1e293b', border: '1px solid #334155',
      borderRadius: 8, padding: '10px 14px', fontSize: 12
    }}>
      <div style={{ color: '#94a3b8', marginBottom: 6 }}>{label}</div>
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2px 16px' }}>
        <span style={{ color: '#64748b' }}>Open</span>
        <span style={{ color: '#f1f5f9', textAlign: 'right' }}>₹{d.open?.toFixed(2)}</span>
        <span style={{ color: '#64748b' }}>High</span>
        <span style={{ color: '#22c55e', textAlign: 'right' }}>₹{d.high?.toFixed(2)}</span>
        <span style={{ color: '#64748b' }}>Low</span>
        <span style={{ color: '#ef4444', textAlign: 'right' }}>₹{d.low?.toFixed(2)}</span>
        <span style={{ color: '#64748b' }}>Close</span>
        <span style={{ color: isGreen ? '#22c55e' : '#ef4444', textAlign: 'right', fontWeight: 600 }}>₹{d.close?.toFixed(2)}</span>
        <span style={{ color: '#64748b' }}>Volume</span>
        <span style={{ color: '#94a3b8', textAlign: 'right' }}>{(d.volume / 1000000).toFixed(1)}M</span>
      </div>
    </div>
  );
};

export default function PriceChart({ symbol }) {
  const [timeframe, setTimeframe] = useState('3M');
  const [chartData, setChartData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [displayData, setDisplayData] = useState([]);

  useEffect(() => {
    if (!symbol) return;
    setLoading(true);
    getChartData(symbol, timeframe)
      .then(data => {
        setChartData(data);
        const candles = data.candles || [];
        // Thin out data for display (show max 120 points)
        const step = Math.max(1, Math.floor(candles.length / 120));
        const thinned = candles.filter((_, i) => i % step === 0 || i === candles.length - 1);
        setDisplayData(thinned.map(c => ({
          ...c,
          dateLabel: formatDate(c.date, timeframe),
          change: c.close - c.open,
          color: c.close >= c.open ? '#22c55e' : '#ef4444'
        })));
      })
      .catch(() => setDisplayData([]))
      .finally(() => setLoading(false));
  }, [symbol, timeframe]);

  function formatDate(dateStr, tf) {
    const d = new Date(dateStr);
    if (tf === '1W' || tf === '1M') return d.toLocaleDateString('en-IN', { day: '2-digit', month: 'short' });
    if (tf === '3M' || tf === '6M') return d.toLocaleDateString('en-IN', { day: '2-digit', month: 'short' });
    return d.toLocaleDateString('en-IN', { month: 'short', year: '2-digit' });
  }

  const prices = displayData.map(d => d.close).filter(Boolean);
  const minPrice = prices.length ? Math.min(...prices) * 0.995 : 0;
  const maxPrice = prices.length ? Math.max(...prices) * 1.005 : 100;
  const maxVol = Math.max(...displayData.map(d => d.volume || 0));

  return (
    <div className="card">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20, flexWrap: 'wrap', gap: 12 }}>
        <div style={{ fontSize: 16, fontWeight: 700, color: '#f1f5f9', display: 'flex', alignItems: 'center', gap: 8 }}>
          <svg width="18" height="18" fill="none" stroke="#3b82f6" strokeWidth="2" viewBox="0 0 24 24">
            <polyline points="22,12 18,12 15,21 9,3 6,12 2,12"/>
          </svg>
          Price Chart — {symbol}
        </div>
        <div style={{ display: 'flex', gap: 4, background: '#0f172a', borderRadius: 8, padding: 3 }}>
          {TIMEFRAMES.map(tf => (
            <button
              key={tf}
              onClick={() => setTimeframe(tf)}
              style={{
                padding: '5px 12px', border: 'none', borderRadius: 5, cursor: 'pointer',
                background: timeframe === tf ? '#3b82f6' : 'transparent',
                color: timeframe === tf ? '#fff' : '#64748b',
                fontSize: 12, fontWeight: 600, fontFamily: 'inherit',
                transition: 'all 0.15s'
              }}
            >
              {tf}
            </button>
          ))}
        </div>
      </div>

      {loading ? (
        <div style={{ height: 350, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <div className="spinner"/>
        </div>
      ) : displayData.length === 0 ? (
        <div style={{ height: 350, display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#64748b' }}>
          No chart data available
        </div>
      ) : (
        <>
          {/* Close price line chart */}
          <div style={{ marginBottom: 4 }}>
            <ResponsiveContainer width="100%" height={260}>
              <ComposedChart data={displayData} margin={{ top: 5, right: 5, left: 0, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" vertical={false} />
                <XAxis
                  dataKey="dateLabel"
                  tick={{ fill: '#64748b', fontSize: 10 }}
                  tickLine={false}
                  axisLine={{ stroke: '#334155' }}
                  interval="preserveStartEnd"
                />
                <YAxis
                  domain={[minPrice, maxPrice]}
                  tick={{ fill: '#64748b', fontSize: 10 }}
                  tickLine={false}
                  axisLine={false}
                  width={65}
                  tickFormatter={v => `₹${v.toLocaleString('en-IN', { maximumFractionDigits: 0 })}`}
                />
                <Tooltip content={<CustomTooltip />} />
                <Line
                  type="monotone"
                  dataKey="close"
                  stroke="#3b82f6"
                  strokeWidth={2}
                  dot={false}
                  activeDot={{ r: 4, fill: '#3b82f6', stroke: '#1e293b', strokeWidth: 2 }}
                />
              </ComposedChart>
            </ResponsiveContainer>
          </div>

          {/* Volume bars */}
          <ResponsiveContainer width="100%" height={60}>
            <ComposedChart data={displayData} margin={{ top: 0, right: 5, left: 0, bottom: 0 }}>
              <XAxis dataKey="dateLabel" hide />
              <YAxis hide domain={[0, maxVol * 1.5]} />
              <Bar
                dataKey="volume"
                fill="#334155"
                radius={[1, 1, 0, 0]}
                // color based on close vs open
                cell={displayData.map((d, i) => (
                  <rect key={i} fill={d.close >= d.open ? 'rgba(34,197,94,0.4)' : 'rgba(239,68,68,0.4)'} />
                ))}
              />
            </ComposedChart>
          </ResponsiveContainer>

          <div style={{ fontSize: 11, color: '#475569', textAlign: 'center', marginTop: 4 }}>
            Volume — Green: Up day | Red: Down day
          </div>
        </>
      )}
    </div>
  );
}
