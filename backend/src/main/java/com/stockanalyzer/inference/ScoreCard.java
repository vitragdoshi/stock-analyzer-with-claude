package com.stockanalyzer.inference;

import java.util.ArrayList;
import java.util.List;

/**
 * Aggregated multi-factor score that drives the inference engine's final
 * recommendation.  All component scores are on 0–100 scale.
 */
public class ScoreCard {

    // Component scores (0–100 each)
    private double technicalScore;
    private double fundamentalScore;
    private double sentimentScore;     // from news
    private double analystScore;       // from analyst consensus
    private double earningsScore;      // beat/miss history + forward estimates
    private double momentumScore;      // price momentum across timeframes
    private double valuationScore;     // price vs intrinsic value / targets

    // Weighted composite score (0–100)
    private double compositeScore;

    // Recommendation derived from composite score
    private String recommendation;     // STRONG BUY / BUY / ACCUMULATE / HOLD / REDUCE / SELL
    private String sentiment;          // BULLISH / MILDLY BULLISH / NEUTRAL / MILDLY BEARISH / BEARISH
    private double confidencePercent;  // 0–100; based on data completeness

    // Qualitative reasoning
    private List<String> bullishFactors  = new ArrayList<>();
    private List<String> bearishFactors  = new ArrayList<>();
    private List<String> riskFactors     = new ArrayList<>();

    // Target price range (INR)
    private double targetPriceLow;
    private double targetPriceMid;
    private double targetPriceHigh;

    // Data quality flags
    private boolean usedRealPriceData;
    private boolean usedRealFundamentals;
    private boolean usedRealNews;
    private boolean usedRealAnalystData;

    // ── Getters / Setters ──────────────────────────────────────────────────

    public double getTechnicalScore() { return technicalScore; }
    public void setTechnicalScore(double technicalScore) { this.technicalScore = technicalScore; }

    public double getFundamentalScore() { return fundamentalScore; }
    public void setFundamentalScore(double fundamentalScore) { this.fundamentalScore = fundamentalScore; }

    public double getSentimentScore() { return sentimentScore; }
    public void setSentimentScore(double sentimentScore) { this.sentimentScore = sentimentScore; }

    public double getAnalystScore() { return analystScore; }
    public void setAnalystScore(double analystScore) { this.analystScore = analystScore; }

    public double getEarningsScore() { return earningsScore; }
    public void setEarningsScore(double earningsScore) { this.earningsScore = earningsScore; }

    public double getMomentumScore() { return momentumScore; }
    public void setMomentumScore(double momentumScore) { this.momentumScore = momentumScore; }

    public double getValuationScore() { return valuationScore; }
    public void setValuationScore(double valuationScore) { this.valuationScore = valuationScore; }

    public double getCompositeScore() { return compositeScore; }
    public void setCompositeScore(double compositeScore) { this.compositeScore = compositeScore; }

    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }

    public String getSentiment() { return sentiment; }
    public void setSentiment(String sentiment) { this.sentiment = sentiment; }

    public double getConfidencePercent() { return confidencePercent; }
    public void setConfidencePercent(double confidencePercent) { this.confidencePercent = confidencePercent; }

    public List<String> getBullishFactors() { return bullishFactors; }
    public void setBullishFactors(List<String> bullishFactors) { this.bullishFactors = bullishFactors; }

    public List<String> getBearishFactors() { return bearishFactors; }
    public void setBearishFactors(List<String> bearishFactors) { this.bearishFactors = bearishFactors; }

    public List<String> getRiskFactors() { return riskFactors; }
    public void setRiskFactors(List<String> riskFactors) { this.riskFactors = riskFactors; }

    public double getTargetPriceLow() { return targetPriceLow; }
    public void setTargetPriceLow(double targetPriceLow) { this.targetPriceLow = targetPriceLow; }

    public double getTargetPriceMid() { return targetPriceMid; }
    public void setTargetPriceMid(double targetPriceMid) { this.targetPriceMid = targetPriceMid; }

    public double getTargetPriceHigh() { return targetPriceHigh; }
    public void setTargetPriceHigh(double targetPriceHigh) { this.targetPriceHigh = targetPriceHigh; }

    public boolean isUsedRealPriceData() { return usedRealPriceData; }
    public void setUsedRealPriceData(boolean usedRealPriceData) { this.usedRealPriceData = usedRealPriceData; }

    public boolean isUsedRealFundamentals() { return usedRealFundamentals; }
    public void setUsedRealFundamentals(boolean usedRealFundamentals) { this.usedRealFundamentals = usedRealFundamentals; }

    public boolean isUsedRealNews() { return usedRealNews; }
    public void setUsedRealNews(boolean usedRealNews) { this.usedRealNews = usedRealNews; }

    public boolean isUsedRealAnalystData() { return usedRealAnalystData; }
    public void setUsedRealAnalystData(boolean usedRealAnalystData) { this.usedRealAnalystData = usedRealAnalystData; }
}
