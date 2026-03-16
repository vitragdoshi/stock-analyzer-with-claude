package com.stockanalyzer.model;

import java.time.Instant;
import java.util.List;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.jackson.Jacksonized;

/**
 * Historical and forward earnings estimates from Yahoo Finance earningsTrend module.
 */
@Data
@Builder
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EarningsData {

    String symbol;

    // Historical quarterly actuals vs estimates (last 4 quarters)
    List<QuarterlyEarning> quarterlyHistory;

    // Forward estimates (current quarter, next quarter, current year, next year)
    List<EarningsTrend> forwardTrends;

    // Summary stats
    double beatRatePercent;    // % of quarters where EPS beat estimate
    double averageSurprise;    // average EPS surprise %
    String earningsMomentum;   // ACCELERATING / DECELERATING / STABLE

    @Builder.Default Instant fetchedAt = Instant.now();
    boolean realData;

    // ── Nested types ──────────────────────────────────────────────────────

    @Getter
    @Builder
    @Jacksonized
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static class QuarterlyEarning {
        String quarter;          // e.g. "3Q2024"
        double epsEstimate;      // analyst consensus estimate (INR)
        double epsActual;        // reported EPS (INR)
        double epsSurprise;      // actual - estimate
        double epsSurprisePercent;
        boolean beat;
    }

    @Getter
    @Builder
    @Jacksonized
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static class EarningsTrend {
        String period;           // "0q", "+1q", "0y", "+1y"
        String endDate;
        double epsEstimateLow;
        double epsEstimateHigh;
        double epsEstimateMean;
        double epsEstimateAvg;
        double revenueEstimateLow;
        double revenueEstimateHigh;
        double revenueEstimateAvg;
        double epsGrowthRate;
    }
}
