package com.stockanalyzer.inference;

import com.stockanalyzer.model.NewsArticle;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

/**
 * Keyword-based sentiment scoring for news articles.
 *
 * Design:
 *  - Each article is scored from -10 (very bearish) to +10 (very bullish)
 *    based on weighted keyword matches.
 *  - The raw score is then weighted by source credibility (0–1) and a
 *    recency decay factor (exponential, half-life = 3 days).
 *  - Aggregate sentiment is normalised to -1.0 … +1.0.
 */
@Component
public class SentimentAnalyzer {

    // Half-life for recency weighting (in hours)
    private static final double HALF_LIFE_HOURS = 72.0;

    // ── Keyword dictionaries ──────────────────────────────────────────────

    private static final Set<String> STRONG_BULLISH = Set.of(
            "beat estimates", "record profit", "all-time high", "strong buy",
            "upgrade", "outperform", "strong results", "beat expectations",
            "robust growth", "surge", "breakout", "acquisition", "dividend increase",
            "buyback", "share repurchase", "capacity expansion", "order book",
            "new contract", "raises guidance", "raises target", "bullish", "rally"
    );

    private static final Set<String> MILD_BULLISH = Set.of(
            "profit", "growth", "increase", "gain", "positive", "improved",
            "steady", "stable", "recovery", "rebound", "accumulate", "buy",
            "target price", "upside", "outperformed", "expansion", "launch",
            "partnership", "deal", "strong demand", "market share", "earnings beat"
    );

    private static final Set<String> STRONG_BEARISH = Set.of(
            "fraud", "scam", "investigation", "regulatory action", "sebi notice",
            "default", "bankruptcy", "insolvency", "massive loss", "crash", "collapse",
            "strong sell", "downgrade", "major disappointment", "missed estimates",
            "profit warning", "write-off", "impairment", "management exit",
            "promoter selling", "pledged shares sold", "forced selling"
    );

    private static final Set<String> MILD_BEARISH = Set.of(
            "loss", "decline", "fall", "drop", "negative", "miss", "weak",
            "concern", "risk", "pressure", "slowdown", "headwind", "competition",
            "margin compression", "debt", "downside", "underperform", "sell",
            "reduce", "below estimate", "disappointing"
    );

    // ── Public API ────────────────────────────────────────────────────────

    /**
     * Assign sentiment score and label to each article in the list.
     * Modifies articles in-place.
     */
    public void annotate(List<NewsArticle> articles) {
        for (NewsArticle a : articles) {
            double score = scoreText(a.getTitle() + " " + a.getSummary());
            a.setSentimentScore(score);
            a.setSentimentLabel(labelFor(score));
        }
    }

    /**
     * Compute a weighted aggregate sentiment score for the list.
     * @return value in [-1.0, +1.0]
     */
    public double aggregateSentiment(List<NewsArticle> articles) {
        if (articles.isEmpty()) return 0.0;
        double weightedSum = 0;
        double totalWeight = 0;
        Instant now = Instant.now();

        for (NewsArticle a : articles) {
            double raw = a.getSentimentScore(); // already -1..+1
            double credibility = a.getSourceCredibility();
            double recency = recencyWeight(a.getPublishedAt(), now);
            double relevance = a.getRelevanceScore();

            double weight = credibility * recency * (0.4 + 0.6 * relevance);
            weightedSum  += raw * weight;
            totalWeight  += weight;
        }
        return totalWeight > 0 ? clamp(weightedSum / totalWeight, -1.0, 1.0) : 0.0;
    }

    // ── Private helpers ───────────────────────────────────────────────────

    /**
     * Score text in range [-1.0, +1.0].
     * Strong keywords worth ±0.4, mild keywords ±0.15.
     */
    private double scoreText(String text) {
        String lower = text.toLowerCase();
        double score = 0;
        for (String kw : STRONG_BULLISH) if (lower.contains(kw)) score += 0.4;
        for (String kw : MILD_BULLISH)   if (lower.contains(kw)) score += 0.15;
        for (String kw : STRONG_BEARISH) if (lower.contains(kw)) score -= 0.4;
        for (String kw : MILD_BEARISH)   if (lower.contains(kw)) score -= 0.15;
        return clamp(score, -1.0, 1.0);
    }

    private String labelFor(double score) {
        if (score > 0.2)  return "POSITIVE";
        if (score < -0.2) return "NEGATIVE";
        return "NEUTRAL";
    }

    /** Exponential decay; weight = 2^(-hours / halfLife). */
    private double recencyWeight(Instant publishedAt, Instant now) {
        if (publishedAt == null) return 0.3;
        long hours = ChronoUnit.HOURS.between(publishedAt, now);
        hours = Math.max(0, hours);
        return Math.pow(2, -(double) hours / HALF_LIFE_HOURS);
    }

    private double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
