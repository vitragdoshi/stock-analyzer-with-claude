package com.stockanalyzer.model;

import java.time.Instant;

/**
 * A single news article / headline aggregated from RSS feeds and news APIs.
 */
public class NewsArticle {

    private String title;
    private String summary;
    private String url;
    private String source;          // e.g. "Economic Times", "Moneycontrol"
    private Instant publishedAt;

    // Sentiment scoring (set by SentimentAnalyzer)
    private double sentimentScore;     // -1.0 (very bearish) to +1.0 (very bullish)
    private String sentimentLabel;     // POSITIVE / NEGATIVE / NEUTRAL
    private double sourceCredibility;  // 0.0 – 1.0, based on known source quality

    // Relevance to the queried stock (set during filtering)
    private double relevanceScore;     // 0.0 – 1.0
    private boolean isCompanySpecific; // true = explicitly mentions the stock/company

    public NewsArticle() {}

    // ── Getters / Setters ──────────────────────────────────────────────────

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public Instant getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Instant publishedAt) { this.publishedAt = publishedAt; }

    public double getSentimentScore() { return sentimentScore; }
    public void setSentimentScore(double sentimentScore) { this.sentimentScore = sentimentScore; }

    public String getSentimentLabel() { return sentimentLabel; }
    public void setSentimentLabel(String sentimentLabel) { this.sentimentLabel = sentimentLabel; }

    public double getSourceCredibility() { return sourceCredibility; }
    public void setSourceCredibility(double sourceCredibility) { this.sourceCredibility = sourceCredibility; }

    public double getRelevanceScore() { return relevanceScore; }
    public void setRelevanceScore(double relevanceScore) { this.relevanceScore = relevanceScore; }

    public boolean isCompanySpecific() { return isCompanySpecific; }
    public void setCompanySpecific(boolean companySpecific) { isCompanySpecific = companySpecific; }
}
