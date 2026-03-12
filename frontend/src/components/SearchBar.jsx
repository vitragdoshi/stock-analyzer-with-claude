import React, { useState } from 'react';

const POPULAR_SYMBOLS = [
  'RELIANCE', 'TCS', 'INFY', 'HDFCBANK', 'WIPRO',
  'ICICIBANK', 'BAJFINANCE', 'MARUTI', 'TATAMOTORS', 'SBIN'
];

export default function SearchBar({ onSearch, loading }) {
  const [input, setInput] = useState('');
  const [focused, setFocused] = useState(false);

  const handleSubmit = (e) => {
    e.preventDefault();
    if (input.trim()) onSearch(input.trim().toUpperCase());
  };

  const handleSuggestion = (sym) => {
    setInput(sym);
    onSearch(sym);
    setFocused(false);
  };

  return (
    <div style={{ width: '100%', maxWidth: 680, position: 'relative' }}>
      <form onSubmit={handleSubmit}>
        <div style={{
          display: 'flex',
          gap: 10,
          background: '#1e293b',
          border: `2px solid ${focused ? '#3b82f6' : '#334155'}`,
          borderRadius: 12,
          padding: '6px 6px 6px 16px',
          transition: 'border-color 0.2s',
          alignItems: 'center'
        }}>
          <svg width="18" height="18" fill="none" stroke="#64748b" strokeWidth="2" viewBox="0 0 24 24">
            <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
          </svg>
          <input
            type="text"
            value={input}
            onChange={e => setInput(e.target.value.toUpperCase())}
            onFocus={() => setFocused(true)}
            onBlur={() => setTimeout(() => setFocused(false), 200)}
            placeholder="Enter NSE/BSE symbol (e.g. RELIANCE, TCS, INFY)"
            style={{
              flex: 1,
              background: 'transparent',
              border: 'none',
              outline: 'none',
              color: '#f1f5f9',
              fontSize: 15,
              fontFamily: 'inherit',
              letterSpacing: '0.03em'
            }}
          />
          <button
            type="submit"
            disabled={loading || !input.trim()}
            style={{
              background: loading ? '#334155' : '#3b82f6',
              color: '#fff',
              border: 'none',
              borderRadius: 8,
              padding: '10px 20px',
              cursor: loading ? 'not-allowed' : 'pointer',
              fontWeight: 600,
              fontSize: 14,
              fontFamily: 'inherit',
              transition: 'all 0.2s',
              display: 'flex',
              alignItems: 'center',
              gap: 8,
              whiteSpace: 'nowrap'
            }}
          >
            {loading ? (
              <>
                <div style={{
                  width: 14, height: 14, border: '2px solid rgba(255,255,255,0.3)',
                  borderTopColor: '#fff', borderRadius: '50%',
                  animation: 'spin 0.7s linear infinite'
                }}/>
                Analyzing...
              </>
            ) : 'Analyze Stock'}
          </button>
        </div>
      </form>

      {/* Popular symbols */}
      {focused && (
        <div style={{
          position: 'absolute',
          top: '100%',
          left: 0,
          right: 0,
          background: '#1e293b',
          border: '1px solid #334155',
          borderRadius: 10,
          marginTop: 6,
          overflow: 'hidden',
          zIndex: 100,
          boxShadow: '0 10px 25px rgba(0,0,0,0.5)'
        }}>
          <div style={{ padding: '8px 12px', fontSize: 11, color: '#64748b',
            textTransform: 'uppercase', letterSpacing: '0.05em', borderBottom: '1px solid #334155' }}>
            Popular Symbols
          </div>
          <div style={{ display: 'flex', flexWrap: 'wrap', padding: 8, gap: 6 }}>
            {POPULAR_SYMBOLS.map(sym => (
              <button
                key={sym}
                onMouseDown={() => handleSuggestion(sym)}
                style={{
                  background: '#263348',
                  border: '1px solid #334155',
                  borderRadius: 6,
                  color: '#94a3b8',
                  padding: '5px 10px',
                  fontSize: 12,
                  cursor: 'pointer',
                  fontFamily: 'monospace',
                  fontWeight: 600,
                  transition: 'all 0.15s'
                }}
                onMouseEnter={e => {
                  e.target.style.background = '#3b82f6';
                  e.target.style.color = '#fff';
                }}
                onMouseLeave={e => {
                  e.target.style.background = '#263348';
                  e.target.style.color = '#94a3b8';
                }}
              >
                {sym}
              </button>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
