package com.stockanalyzer.model;

import java.time.Instant;
import java.util.List;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.jackson.Jacksonized;

/**
 * Aggregated analyst recommendation data from Yahoo Finance.
 */
@Data
@Builder
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AnalystConsensus {

    String symbol;

    // Recommendation counts (current month)
    int strongBuyCount;
    int buyCount;
    int holdCount;
    int sellCount;
    int strongSellCount;
    int totalAnalysts;

    // Derived
    double consensusScore;    // 1.0 (strong buy) – 5.0 (strong sell); Yahoo Finance scale
    String consensusLabel;    // STRONG BUY / BUY / HOLD / SELL / STRONG SELL

    // Price targets (INR)
    double targetLow;
    double targetMean;
    double targetHigh;
    double targetMedian;
    double currentPrice;
    double upsidePotentialPercent; // (targetMean - current) / current * 100

    // Recent upgrades / downgrades
    List<UpgradeEvent> recentUpgrades;

    @Builder.Default Instant fetchedAt = Instant.now();
    boolean realData;

    // ── Nested event ──────────────────────────────────────────────────────

    @Getter
    @Builder
    @Jacksonized
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static class UpgradeEvent {
        String firm;
        String action;          // up / down / main / init / reit
        String fromGrade;
        String toGrade;
        Instant epochTime;
    }
}
