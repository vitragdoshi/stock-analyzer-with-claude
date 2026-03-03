import React from 'react';

function MetricCard({ label, value, subvalue, color }) {
  return (
    <div className="card" style={{ padding: '16px 20px' }}>
      <div style={{ fontSize: 12, color: '#64748b', marginBottom: 6, textTransform: 'uppercase', letterSpacing: '0.05em' }}>
        {label}
      </div>
      <div style={{ fontSize: 22, fontWeight: 700, color: color || '#f1f5f9' }}>
        {value}
      </div>
      {subvalue && (
        <div style={{ fontSize: 12, color: '#94a3b8', marginTop: 4 }}>{subvalue}</div>
      )}
    </div>
  );
}

function RangeBar({ low, high, current, label }) {
  const pct = ((current - low) / (high - low)) * 100;
  return (
    <div style={{ marginBottom: 12 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 12, color: '#94a3b8', marginBottom: 6 }}>
        <span>{label}</span>
        <span style={{ color: '#f1f5f9', fontWeight: 600 }}>₹{current.toLocaleString('en-IN', { minimumFractionDigits: 2 })}</span>
      </div>
      <div style={{ position: 'relative', height: 6, background: '#334155', borderRadius: 3 }}>
        <div style={{
          position: 'absolute', left: 0, height: '100%', borderRadius: 3,
          width: `${Math.min(100, Math.max(0, pct))}%`,
          background: 'linear-gradient(90deg, #ef4444, #f59e0b, #22c55e)'
        }}/>
        <div style={{
          position: 'absolute',
          left: `${Math.min(98, Math.max(2, pct))}%`,
          top: -3, width: 12, height: 12,
          background: '#fff', borderRadius: '50%',
          border: '2px solid #1e293b',
          transform: 'translateX(-50%)',
          boxShadow: '0 0 6px rgba(255,255,255,0.3)'
        }}/>
      </div>
      <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 11, color: '#64748b', marginTop: 4 }}>
        <span>₹{low.toLocaleString('en-IN', { minimumFractionDigits: 2 })}</span>
        <span>₹{high.toLocaleString('en-IN', { minimumFractionDigits: 2 })}</span>
      </div>
    </div>
  );
}

export default function StockOverview({ data, symbol, companyName, sector, exchange }) {
  const isPositive = data.changeAmount >= 0;
  const changeColor = isPositive ? '#22c55e' : '#ef4444';

  return (
    <div>
      {/* Hero price section */}
      <div className="card" style={{ marginBottom: 16, padding: '24px 28px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: 16 }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 8 }}>
              <div style={{
                width: 48, height: 48, borderRadius: 10,
                background: 'linear-gradient(135deg, #3b82f6, #6366f1)',
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                fontSize: 18, fontWeight: 800, color: '#fff', letterSpacing: '-0.02em'
              }}>
                {symbol.substring(0, 2)}
              </div>
              <div>
                <div style={{ fontSize: 22, fontWeight: 800, color: '#f1f5f9' }}>{companyName}</div>
                <div style={{ display: 'flex', gap: 8, marginTop: 4, alignItems: 'center' }}>
                  <span className="badge badge-blue">{exchange}</span>
                  <span style={{ fontSize: 12, color: '#94a3b8' }}>{sector}</span>
                </div>
              </div>
            </div>
          </div>
          <div style={{ textAlign: 'right' }}>
            <div style={{ fontSize: 36, fontWeight: 800, color: '#f1f5f9', letterSpacing: '-0.02em' }}>
              ₹{data.currentPrice.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
            </div>
            <div style={{ fontSize: 16, fontWeight: 600, color: changeColor, marginTop: 4 }}>
              {isPositive ? '+' : ''}₹{data.changeAmount.toFixed(2)} ({isPositive ? '+' : ''}{data.changePercent.toFixed(2)}%)
            </div>
          </div>
        </div>

        <div style={{ marginTop: 20 }}>
          <RangeBar low={data.dayLow} high={data.dayHigh} current={data.currentPrice} label="Day Range" />
          <RangeBar low={data.weekLow52} high={data.weekHigh52} current={data.currentPrice} label="52 Week Range" />
        </div>

        {data.priceChangeJustification && (
          <div className="justification-box" style={{ marginTop: 12 }}>
            <strong style={{ color: changeColor }}>Price Movement: </strong>
            {data.priceChangeJustification}
          </div>
        )}
      </div>

      {/* Stats grid */}
      <div className="grid-4">
        <MetricCard
          label="Market Cap"
          value={`₹${data.marketCap.toFixed(0)} Cr`}
          subvalue={data.marketCap > 100000 ? 'Large Cap' : data.marketCap > 10000 ? 'Mid Cap' : 'Small Cap'}
        />
        <MetricCard
          label="Volume"
          value={data.volume.toLocaleString('en-IN')}
          subvalue={`Avg: ${data.avgVolume.toLocaleString('en-IN')}`}
          color={data.volume > data.avgVolume ? '#22c55e' : '#f1f5f9'}
        />
        <MetricCard
          label="Day High"
          value={`₹${data.dayHigh.toLocaleString('en-IN', { minimumFractionDigits: 2 })}`}
        />
        <MetricCard
          label="Day Low"
          value={`₹${data.dayLow.toLocaleString('en-IN', { minimumFractionDigits: 2 })}`}
        />
      </div>
    </div>
  );
}
