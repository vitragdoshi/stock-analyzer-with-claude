package com.stockanalyzer.inference;

import java.util.ArrayList;
import java.util.List;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.jackson.Jacksonized;

/**
 * Aggregated multi-factor score that drives the inference engine's final
 * recommendation.  All component scores are on 0–100 scale.
 */
@Data
@Builder
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScoreCard {

    // Component scores (0–100 each)
    double technicalScore;
    double fundamentalScore;
    double sentimentScore;     // from news
    double analystScore;       // from analyst consensus
    double earningsScore;      // beat/miss history + forward estimates
    double momentumScore;      // price momentum across timeframes
    double valuationScore;     // price vs intrinsic value / targets

    // Weighted composite score (0–100)
    double compositeScore;

    // Recommendation derived from composite score
    String recommendation;     // STRONG BUY / BUY / ACCUMULATE / HOLD / REDUCE / SELL
    String sentiment;          // BULLISH / MILDLY BULLISH / NEUTRAL / MILDLY BEARISH / BEARISH
    double confidencePercent;  // 0–100; based on data completeness

    // Qualitative reasoning
    @Builder.Default List<String> bullishFactors  = new ArrayList<>();
    @Builder.Default List<String> bearishFactors  = new ArrayList<>();
    @Builder.Default List<String> riskFactors     = new ArrayList<>();

    // Target price range (INR)
    double targetPriceLow;
    double targetPriceMid;
    double targetPriceHigh;

    // Data quality flags
    boolean usedRealPriceData;
    boolean usedRealFundamentals;
    boolean usedRealNews;
    boolean usedRealAnalystData;
}
