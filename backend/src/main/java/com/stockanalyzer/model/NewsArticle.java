package com.stockanalyzer.model;

import java.time.Instant;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.jackson.Jacksonized;

/**
 * A single news article / headline aggregated from RSS feeds and news APIs.
 */
@Data
@Builder
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewsArticle {

    String title;
    String summary;
    String url;
    String source;          // e.g. "Economic Times", "Moneycontrol"
    Instant publishedAt;

    // Sentiment scoring (set by SentimentAnalyzer)
    double sentimentScore;     // -1.0 (very bearish) to +1.0 (very bullish)
    String sentimentLabel;     // POSITIVE / NEGATIVE / NEUTRAL
    double sourceCredibility;  // 0.0 – 1.0, based on known source quality

    // Relevance to the queried stock (set during filtering)
    double relevanceScore;     // 0.0 – 1.0
    boolean companySpecific; // true = explicitly mentions the stock/company
}
