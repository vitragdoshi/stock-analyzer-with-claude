import React, { useState } from 'react';

function AuthBadge({ score, isAuthentic }) {
  if (!isAuthentic) {
    return (
      <span className="auth-badge suspicious">
        <svg width="10" height="10" viewBox="0 0 24 24" fill="currentColor">
          <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
        </svg>
        UNVERIFIED
      </span>
    );
  }
  if (score === 'HIGH') {
    return (
      <span className="auth-badge verified">
        <svg width="10" height="10" viewBox="0 0 24 24" fill="currentColor">
          <path d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
        </svg>
        VERIFIED
      </span>
    );
  }
  if (score === 'MEDIUM') {
    return (
      <span className="auth-badge medium">
        <svg width="10" height="10" viewBox="0 0 24 24" fill="currentColor">
          <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
        </svg>
        PARTIALLY VERIFIED
      </span>
    );
  }
  return <span className="auth-badge medium">{score}</span>;
}

function SentimentChip({ sentiment }) {
  const colors = {
    'POSITIVE': { bg: 'rgba(34,197,94,0.15)', color: '#22c55e' },
    'NEGATIVE': { bg: 'rgba(239,68,68,0.15)', color: '#ef4444' },
    'NEUTRAL': { bg: 'rgba(100,116,139,0.15)', color: '#94a3b8' },
  };
  const c = colors[sentiment] || colors['NEUTRAL'];
  return (
    <span style={{
      background: c.bg, color: c.color,
      fontSize: 10, fontWeight: 700, padding: '2px 7px',
      borderRadius: 4, letterSpacing: '0.05em'
    }}>
      {sentiment}
    </span>
  );
}

function ImpactChip({ impact }) {
  const colors = {
    'BULLISH': '#22c55e', 'BEARISH': '#ef4444',
    'NEUTRAL': '#94a3b8', 'SUSPECT': '#f59e0b'
  };
  const c = colors[impact] || '#94a3b8';
  return (
    <span style={{
      background: `${c}20`, color: c, border: `1px solid ${c}40`,
      fontSize: 10, fontWeight: 700, padding: '2px 7px',
      borderRadius: 4, letterSpacing: '0.05em'
    }}>
      Impact: {impact}
    </span>
  );
}

function NewsCard({ item }) {
  const [expanded, setExpanded] = useState(false);

  return (
    <div className={`news-card ${!item.authentic ? 'suspicious-news' : ''}`}
      style={{ borderColor: !item.authentic ? 'rgba(239,68,68,0.4)' : undefined }}>
      {!item.authentic && (
        <div style={{
          background: 'rgba(239,68,68,0.1)', border: '1px solid rgba(239,68,68,0.3)',
          borderRadius: 6, padding: '6px 10px', marginBottom: 10,
          fontSize: 11, color: '#ef4444', fontWeight: 600
        }}>
          ⚠ AUTHENTICITY ALERT: This news article could not be fully verified. Treat with caution.
        </div>
      )}
      <div className="news-headline">{item.headline}</div>
      <div className="news-meta">
        <span className="news-source">{item.source}</span>
        <span className="news-date">{item.publishedDate}</span>
        <SentimentChip sentiment={item.sentiment} />
        <ImpactChip impact={item.impactOnStock} />
        <AuthBadge score={item.authenticityScore} isAuthentic={item.authentic} />
      </div>
      <div className="news-summary">{item.summary}</div>

      <button
        onClick={() => setExpanded(!expanded)}
        style={{
          background: 'transparent', border: '1px solid #334155',
          color: '#64748b', fontSize: 12, padding: '5px 12px',
          borderRadius: 6, cursor: 'pointer', fontFamily: 'inherit',
          display: 'flex', alignItems: 'center', gap: 5
        }}
      >
        <svg width="12" height="12" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
          {expanded
            ? <path d="M9 18l6-6-6-6"/>
            : <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
          }
        </svg>
        {expanded ? 'Hide' : 'Authenticity Analysis'}
      </button>

      {expanded && (
        <div style={{
          marginTop: 10, padding: '10px 14px',
          background: item.authentic ? 'rgba(34,197,94,0.05)' : 'rgba(239,68,68,0.05)',
          border: `1px solid ${item.authentic ? 'rgba(34,197,94,0.2)' : 'rgba(239,68,68,0.2)'}`,
          borderRadius: 6, fontSize: 12, color: '#94a3b8', lineHeight: 1.7
        }}>
          <strong style={{ color: item.authentic ? '#22c55e' : '#ef4444' }}>
            Authenticity Assessment ({item.authenticityScore} confidence):
          </strong>{' '}
          {item.authenticityJustification}
        </div>
      )}
    </div>
  );
}

export default function NewsSection({ companyNews, competitorNews }) {
  const [tab, setTab] = useState('company');

  const items = tab === 'company' ? companyNews : competitorNews;
  const verifiedCount = items.filter(n => n.authentic).length;
  const unverifiedCount = items.filter(n => !n.authentic).length;
  const positiveCount = items.filter(n => n.sentiment === 'POSITIVE').length;
  const negativeCount = items.filter(n => n.sentiment === 'NEGATIVE').length;

  return (
    <div>
      <div style={{ display: 'flex', gap: 10, marginBottom: 20, flexWrap: 'wrap', alignItems: 'center' }}>
        <div style={{ display: 'flex', gap: 4, background: '#0f172a', padding: 4, borderRadius: 8 }}>
          {['company', 'competitor'].map(t => (
            <button key={t} onClick={() => setTab(t)} style={{
              padding: '7px 16px', border: 'none', borderRadius: 6, cursor: 'pointer',
              background: tab === t ? '#1e293b' : 'transparent',
              color: tab === t ? '#f1f5f9' : '#64748b',
              fontSize: 13, fontWeight: tab === t ? 600 : 400, fontFamily: 'inherit',
              transition: 'all 0.15s'
            }}>
              {t === 'company' ? 'Company News' : 'Competitor & Sector News'}
            </button>
          ))}
        </div>
        <div style={{ display: 'flex', gap: 8 }}>
          <span className="badge badge-green">{verifiedCount} Verified</span>
          {unverifiedCount > 0 && <span className="badge badge-red">{unverifiedCount} Unverified</span>}
          <span className="badge badge-green">+{positiveCount} Positive</span>
          <span className="badge badge-red">-{negativeCount} Negative</span>
        </div>
      </div>

      {items.length === 0 ? (
        <div style={{ textAlign: 'center', color: '#64748b', padding: 40 }}>No news available</div>
      ) : (
        items.map((item, i) => <NewsCard key={i} item={item} />)
      )}

      {tab === 'competitor' && (
        <div className="justification-box" style={{ marginTop: 8 }}>
          <strong style={{ color: '#3b82f6' }}>Sector Intelligence Note: </strong>
          Competitor news is analyzed to identify risks and opportunities that may indirectly affect the target stock.
          Strong competitor performance can indicate healthy sector demand (positive) or intensified competition (negative).
          Always consider the source and cross-reference with official disclosures.
        </div>
      )}
    </div>
  );
}
