import React, { useState, useEffect, useCallback } from 'react';
import {
  RadarChart, Radar, PolarGrid, PolarAngleAxis,
  Tooltip, ResponsiveContainer
} from 'recharts';
import { getInferenceScore } from '../services/api';

/* ─────────────────────────────────────────────────────────────────────────
   Colour helpers
───────────────────────────────────────────────────────────────────────── */
const recColour = (rec) => {
  if (!rec) return '#94a3b8';
  const r = rec.toUpperCase();
  if (r.includes('STRONG BUY'))  return '#16a34a';
  if (r.includes('BUY'))         return '#22c55e';
  if (r.includes('ACCUMULATE'))  return '#84cc16';
  if (r.includes('HOLD'))        return '#f59e0b';
  if (r.includes('REDUCE'))      return '#f97316';
  if (r.includes('SELL'))        return '#ef4444';
  return '#94a3b8';
};

const sentColour = (sent) => {
  if (!sent) return '#94a3b8';
  const s = sent.toUpperCase();
  if (s.includes('BULLISH'))  return '#22c55e';
  if (s.includes('BEARISH'))  return '#ef4444';
  return '#f59e0b';
};

const scoreColour = (score) => {
  if (score >= 75) return '#22c55e';
  if (score >= 55) return '#84cc16';
  if (score >= 45) return '#f59e0b';
  if (score >= 30) return '#f97316';
  return '#ef4444';
};

/* ─────────────────────────────────────────────────────────────────────────
   Sub-components
───────────────────────────────────────────────────────────────────────── */
function DataSourceBadge({ label, active }) {
  return (
    <span style={{
      display: 'inline-flex', alignItems: 'center', gap: 5,
      padding: '3px 10px', borderRadius: 20, fontSize: 11, fontWeight: 600,
      background: active ? 'rgba(34,197,94,0.15)' : 'rgba(100,116,139,0.15)',
      border: `1px solid ${active ? 'rgba(34,197,94,0.4)' : 'rgba(100,116,139,0.3)'}`,
      color: active ? '#22c55e' : '#64748b',
    }}>
      <span style={{ fontSize: 8 }}>{active ? '●' : '○'}</span>
      {label}
    </span>
  );
}

function ScoreRing({ score, size = 100 }) {
  const r = (size - 14) / 2;
  const circ = 2 * Math.PI * r;
  const dash  = (score / 100) * circ;
  const color = scoreColour(score);
  return (
    <svg width={size} height={size} style={{ display: 'block' }}>
      <circle cx={size/2} cy={size/2} r={r} fill="none"
        stroke="#1e293b" strokeWidth={10} />
      <circle cx={size/2} cy={size/2} r={r} fill="none"
        stroke={color} strokeWidth={10}
        strokeDasharray={`${dash} ${circ - dash}`}
        strokeLinecap="round"
        transform={`rotate(-90 ${size/2} ${size/2})`} />
      <text x={size/2} y={size/2 - 4} textAnchor="middle"
        fill={color} fontSize={size * 0.22} fontWeight={800}
        dominantBaseline="middle" style={{ fontFamily: 'monospace' }}>
        {Math.round(score)}
      </text>
      <text x={size/2} y={size/2 + 14} textAnchor="middle"
        fill="#64748b" fontSize={size * 0.10} dominantBaseline="middle">
        /100
      </text>
    </svg>
  );
}

function ComponentBar({ label, score, weight }) {
  const color = scoreColour(score);
  return (
    <div style={{ marginBottom: 14 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 5, alignItems: 'center' }}>
        <span style={{ fontSize: 12, color: '#94a3b8', fontWeight: 500 }}>{label}</span>
        <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
          <span style={{ fontSize: 10, color: '#475569' }}>{weight} wt</span>
          <span style={{ fontSize: 13, fontWeight: 700, color, minWidth: 32, textAlign: 'right' }}>
            {Math.round(score)}
          </span>
        </div>
      </div>
      <div style={{ height: 6, borderRadius: 3, background: '#1e293b', overflow: 'hidden' }}>
        <div style={{
          width: `${score}%`, height: '100%', borderRadius: 3,
          background: `linear-gradient(90deg, ${color}99, ${color})`,
          transition: 'width 0.8s ease'
        }} />
      </div>
    </div>
  );
}

function FactorList({ title, items, color, icon }) {
  if (!items || items.length === 0) return null;
  return (
    <div style={{
      background: '#1e293b', border: `1px solid ${color}33`,
      borderRadius: 10, padding: 16, flex: 1, minWidth: 260
    }}>
      <div style={{ fontSize: 12, fontWeight: 700, color, marginBottom: 12,
        textTransform: 'uppercase', letterSpacing: '0.06em', display: 'flex', gap: 6 }}>
        <span>{icon}</span>{title}
      </div>
      <ul style={{ listStyle: 'none', padding: 0, margin: 0 }}>
        {items.map((f, i) => (
          <li key={i} style={{
            fontSize: 12, color: '#94a3b8', lineHeight: 1.6, paddingBottom: 8,
            borderBottom: i < items.length - 1 ? '1px solid #0f172a' : 'none',
            marginBottom: 8, paddingLeft: 12, position: 'relative'
          }}>
            <span style={{ position: 'absolute', left: 0, color }}>{icon === '↑' ? '▲' : icon === '↓' ? '▼' : '!'}</span>
            {f}
          </li>
        ))}
      </ul>
    </div>
  );
}

function PriceTargetBar({ low, mid, high, current }) {
  if (!low || !mid || !high || !current) return null;
  const min = Math.min(low, current) * 0.96;
  const max = Math.max(high, current) * 1.04;
  const pct = (v) => ((v - min) / (max - min)) * 100;

  return (
    <div style={{ marginTop: 8 }}>
      <div style={{ position: 'relative', height: 32, marginBottom: 20 }}>
        {/* Track */}
        <div style={{
          position: 'absolute', top: 14, left: 0, right: 0,
          height: 4, borderRadius: 2, background: '#1e293b'
        }} />
        {/* Target range bar */}
        <div style={{
          position: 'absolute', top: 14,
          left: `${pct(low)}%`, width: `${pct(high) - pct(low)}%`,
          height: 4, borderRadius: 2, background: 'rgba(59,130,246,0.4)'
        }} />
        {[
          { v: low,     label: `₹${Math.round(low)}`,     color: '#64748b', sub: 'Low' },
          { v: mid,     label: `₹${Math.round(mid)}`,     color: '#3b82f6', sub: 'Target' },
          { v: high,    label: `₹${Math.round(high)}`,    color: '#64748b', sub: 'High' },
          { v: current, label: `₹${Math.round(current)}`, color: '#f59e0b', sub: 'Now' },
        ].map(({ v, label, color, sub }) => (
          <div key={sub} style={{ position: 'absolute', left: `${pct(v)}%`, transform: 'translateX(-50%)' }}>
            <div style={{ width: 2, height: 12, background: color, margin: '8px auto 0' }} />
            <div style={{ fontSize: 10, color, whiteSpace: 'nowrap', textAlign: 'center', marginTop: 2 }}>{label}</div>
            <div style={{ fontSize: 9, color: '#475569', textAlign: 'center' }}>{sub}</div>
          </div>
        ))}
      </div>
    </div>
  );
}

/* ─────────────────────────────────────────────────────────────────────────
   Main component
───────────────────────────────────────────────────────────────────────── */
export default function InferenceScorecard({ symbol }) {
  const [data, setData]     = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError]   = useState(null);

  const load = useCallback(async () => {
    if (!symbol) return;
    setLoading(true);
    setError(null);
    setData(null);
    try {
      const score = await getInferenceScore(symbol);
      setData(score);
    } catch (err) {
      setError(err?.response?.data?.message || 'Inference engine unavailable.');
    } finally {
      setLoading(false);
    }
  }, [symbol]);

  useEffect(() => { load(); }, [load]);

  /* ── Radar chart data ── */
  const radarData = data ? [
    { subject: 'Technical',   score: data.technicalScore   ?? 50, fullMark: 100 },
    { subject: 'Fundamental', score: data.fundamentalScore ?? 50, fullMark: 100 },
    { subject: 'Valuation',   score: data.valuationScore   ?? 50, fullMark: 100 },
    { subject: 'Analyst',     score: data.analystScore     ?? 50, fullMark: 100 },
    { subject: 'Earnings',    score: data.earningsScore    ?? 50, fullMark: 100 },
    { subject: 'Sentiment',   score: data.sentimentScore   ?? 50, fullMark: 100 },
    { subject: 'Momentum',    score: data.momentumScore    ?? 50, fullMark: 100 },
  ] : [];

  /* ── Render states ── */
  if (loading) return (
    <div style={{ textAlign: 'center', padding: '60px 0' }}>
      <div className="spinner" style={{ margin: '0 auto 16px' }} />
      <div style={{ color: '#94a3b8', fontSize: 14 }}>
        Running inference engine for <strong style={{ color: '#3b82f6' }}>{symbol}</strong>…
      </div>
      <div style={{ color: '#475569', fontSize: 12, marginTop: 8 }}>
        Fetching live prices, fundamentals, analyst data, earnings &amp; news
      </div>
    </div>
  );

  if (error) return (
    <div style={{
      maxWidth: 500, margin: '40px auto', textAlign: 'center',
      background: 'rgba(239,68,68,0.1)', border: '1px solid rgba(239,68,68,0.3)',
      borderRadius: 12, padding: 24
    }}>
      <div style={{ fontSize: 28, marginBottom: 12 }}>⚠</div>
      <div style={{ color: '#ef4444', fontWeight: 600, marginBottom: 8 }}>Inference Failed</div>
      <div style={{ color: '#94a3b8', fontSize: 13 }}>{error}</div>
      <button onClick={load} style={{
        marginTop: 16, padding: '8px 20px', borderRadius: 8,
        background: '#1e293b', border: '1px solid #334155',
        color: '#94a3b8', cursor: 'pointer', fontSize: 13
      }}>Retry</button>
    </div>
  );

  if (!data) return null;

  const comp   = data.compositeScore   ?? 50;
  const rec    = data.recommendation   ?? 'HOLD';
  const sent   = data.sentiment        ?? 'NEUTRAL';
  const conf   = data.confidencePercent ?? 60;
  const rColor = recColour(rec);
  const sColor = sentColour(sent);

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>

      {/* ── Header: composite score + recommendation ── */}
      <div className="card" style={{ display: 'flex', gap: 32, flexWrap: 'wrap', alignItems: 'center' }}>

        {/* Score ring */}
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 8 }}>
          <ScoreRing score={comp} size={120} />
          <div style={{ fontSize: 10, color: '#475569', textTransform: 'uppercase', letterSpacing: '0.06em' }}>
            Composite
          </div>
        </div>

        {/* Recommendation */}
        <div style={{ flex: 1, minWidth: 200 }}>
          <div style={{ fontSize: 10, color: '#64748b', textTransform: 'uppercase', letterSpacing: '0.06em', marginBottom: 4 }}>
            Recommendation
          </div>
          <div style={{ fontSize: 32, fontWeight: 900, color: rColor, lineHeight: 1.1, marginBottom: 8 }}>
            {rec}
          </div>
          <div style={{ display: 'flex', gap: 8, alignItems: 'center', marginBottom: 12 }}>
            <span style={{ fontSize: 13, color: sColor, fontWeight: 700 }}>{sent}</span>
            <span style={{ fontSize: 11, color: '#334155' }}>·</span>
            <span style={{ fontSize: 12, color: '#64748b' }}>Confidence {Math.round(conf)}%</span>
          </div>
          {/* Confidence bar */}
          <div style={{ height: 4, background: '#0f172a', borderRadius: 2, overflow: 'hidden', maxWidth: 300 }}>
            <div style={{ width: `${conf}%`, height: '100%', background: '#3b82f6', borderRadius: 2 }} />
          </div>
        </div>

        {/* Data source badges */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
          <div style={{ fontSize: 10, color: '#475569', textTransform: 'uppercase', letterSpacing: '0.06em', marginBottom: 4 }}>
            Data Sources
          </div>
          <DataSourceBadge label="Live Price Data"   active={data.usedRealPriceData} />
          <DataSourceBadge label="Real Fundamentals" active={data.usedRealFundamentals} />
          <DataSourceBadge label="Analyst Consensus" active={data.usedRealAnalystData} />
          <DataSourceBadge label="Live News"         active={data.usedRealNews} />
        </div>
      </div>

      {/* ── Radar + component bars ── */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 20 }}>

        {/* Radar chart */}
        <div className="card">
          <div style={{ fontSize: 12, fontWeight: 700, color: '#94a3b8',
            textTransform: 'uppercase', letterSpacing: '0.06em', marginBottom: 16 }}>
            Factor Breakdown
          </div>
          <ResponsiveContainer width="100%" height={260}>
            <RadarChart data={radarData} margin={{ top: 0, right: 30, bottom: 0, left: 30 }}>
              <PolarGrid stroke="#1e293b" />
              <PolarAngleAxis
                dataKey="subject"
                tick={{ fill: '#64748b', fontSize: 11 }}
              />
              <Radar
                name="Score"
                dataKey="score"
                stroke="#3b82f6"
                fill="#3b82f6"
                fillOpacity={0.2}
                strokeWidth={2}
              />
              <Tooltip
                contentStyle={{ background: '#1e293b', border: '1px solid #334155', borderRadius: 8 }}
                formatter={(v) => [`${Math.round(v)} / 100`, 'Score']}
              />
            </RadarChart>
          </ResponsiveContainer>
        </div>

        {/* Component score bars */}
        <div className="card">
          <div style={{ fontSize: 12, fontWeight: 700, color: '#94a3b8',
            textTransform: 'uppercase', letterSpacing: '0.06em', marginBottom: 16 }}>
            Component Scores
          </div>
          <ComponentBar label="Fundamental Analysis" score={data.fundamentalScore ?? 50} weight="25%" />
          <ComponentBar label="Technical Analysis"   score={data.technicalScore   ?? 50} weight="20%" />
          <ComponentBar label="Valuation vs Targets" score={data.valuationScore   ?? 50} weight="15%" />
          <ComponentBar label="Analyst Consensus"    score={data.analystScore     ?? 50} weight="15%" />
          <ComponentBar label="Earnings Quality"     score={data.earningsScore    ?? 50} weight="10%" />
          <ComponentBar label="News Sentiment"       score={data.sentimentScore   ?? 50} weight="10%" />
          <ComponentBar label="Price Momentum"       score={data.momentumScore    ?? 50} weight="5%"  />
        </div>
      </div>

      {/* ── Price target ── */}
      {data.targetPriceMid > 0 && (
        <div className="card">
          <div style={{ fontSize: 12, fontWeight: 700, color: '#94a3b8',
            textTransform: 'uppercase', letterSpacing: '0.06em', marginBottom: 8 }}>
            Price Target Range
          </div>
          <PriceTargetBar
            low={data.targetPriceLow}
            mid={data.targetPriceMid}
            high={data.targetPriceHigh}
            current={data.targetPriceMid * (1 - data.upsidePotentialPercent / 100 || 0)}
          />
          <div style={{ display: 'flex', gap: 20, flexWrap: 'wrap', marginTop: 16 }}>
            {[
              { label: 'Bear Case', value: data.targetPriceLow,  color: '#ef4444' },
              { label: 'Base Case', value: data.targetPriceMid,  color: '#3b82f6' },
              { label: 'Bull Case', value: data.targetPriceHigh, color: '#22c55e' },
            ].map(({ label, value, color }) => (
              <div key={label} style={{ textAlign: 'center' }}>
                <div style={{ fontSize: 10, color: '#64748b', marginBottom: 2 }}>{label}</div>
                <div style={{ fontSize: 20, fontWeight: 800, color, fontFamily: 'monospace' }}>
                  ₹{Math.round(value).toLocaleString('en-IN')}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* ── Factor lists ── */}
      <div style={{ display: 'flex', gap: 16, flexWrap: 'wrap' }}>
        <FactorList
          title="Bullish Factors"
          items={data.bullishFactors}
          color="#22c55e"
          icon="↑"
        />
        <FactorList
          title="Bearish Factors"
          items={data.bearishFactors}
          color="#ef4444"
          icon="↓"
        />
        {data.riskFactors?.length > 0 && (
          <FactorList
            title="Risk Factors"
            items={data.riskFactors}
            color="#f59e0b"
            icon="!"
          />
        )}
      </div>

      {/* ── Methodology note ── */}
      <div style={{
        padding: '12px 16px', background: 'rgba(59,130,246,0.05)',
        border: '1px solid rgba(59,130,246,0.15)', borderRadius: 8, fontSize: 11, color: '#475569', lineHeight: 1.7
      }}>
        <strong style={{ color: '#3b82f6' }}>Methodology:</strong>{' '}
        Scores are computed by the on-server inference engine using live public data —
        Yahoo Finance (prices, fundamentals, analyst ratings, earnings history) and
        Google News / Moneycontrol RSS (news sentiment).
        Weights: Fundamental 25% · Technical 20% · Valuation 15% · Analyst 15% · Earnings 10% · Sentiment 10% · Momentum 5%.
        Mock fallback is used for any dimension where live data is unavailable.
        <strong style={{ color: '#f59e0b', marginLeft: 6 }}>
          Not investment advice. Consult a SEBI-registered advisor.
        </strong>
      </div>
    </div>
  );
}
