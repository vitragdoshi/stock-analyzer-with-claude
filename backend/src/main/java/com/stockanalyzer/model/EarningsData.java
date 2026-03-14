package com.stockanalyzer.model;

import java.time.Instant;
import java.util.List;

/**
 * Historical and forward earnings estimates from Yahoo Finance earningsTrend module.
 */
public class EarningsData {

    private String symbol;

    // Historical quarterly actuals vs estimates (last 4 quarters)
    private List<QuarterlyEarning> quarterlyHistory;

    // Forward estimates (current quarter, next quarter, current year, next year)
    private List<EarningsTrend> forwardTrends;

    // Summary stats
    private double beatRatePercent;    // % of quarters where EPS beat estimate
    private double averageSurprise;    // average EPS surprise %
    private String earningsMomentum;   // ACCELERATING / DECELERATING / STABLE

    private Instant fetchedAt;
    private boolean isRealData;

    public EarningsData() { this.fetchedAt = Instant.now(); }

    // ── Nested types ──────────────────────────────────────────────────────

    public static class QuarterlyEarning {
        private String quarter;          // e.g. "3Q2024"
        private double epsEstimate;      // analyst consensus estimate (INR)
        private double epsActual;        // reported EPS (INR)
        private double epsSurprise;      // actual - estimate
        private double epsSurprisePercent;
        private boolean beat;

        public String getQuarter() { return quarter; }
        public void setQuarter(String quarter) { this.quarter = quarter; }
        public double getEpsEstimate() { return epsEstimate; }
        public void setEpsEstimate(double epsEstimate) { this.epsEstimate = epsEstimate; }
        public double getEpsActual() { return epsActual; }
        public void setEpsActual(double epsActual) { this.epsActual = epsActual; }
        public double getEpsSurprise() { return epsSurprise; }
        public void setEpsSurprise(double epsSurprise) { this.epsSurprise = epsSurprise; }
        public double getEpsSurprisePercent() { return epsSurprisePercent; }
        public void setEpsSurprisePercent(double epsSurprisePercent) { this.epsSurprisePercent = epsSurprisePercent; }
        public boolean isBeat() { return beat; }
        public void setBeat(boolean beat) { this.beat = beat; }
    }

    public static class EarningsTrend {
        private String period;           // "0q", "+1q", "0y", "+1y"
        private String endDate;
        private double epsEstimateLow;
        private double epsEstimateHigh;
        private double epsEstimateMean;
        private double epsEstimateAvg;
        private double revenueEstimateLow;
        private double revenueEstimateHigh;
        private double revenueEstimateAvg;
        private double epsGrowthRate;

        public String getPeriod() { return period; }
        public void setPeriod(String period) { this.period = period; }
        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }
        public double getEpsEstimateLow() { return epsEstimateLow; }
        public void setEpsEstimateLow(double epsEstimateLow) { this.epsEstimateLow = epsEstimateLow; }
        public double getEpsEstimateHigh() { return epsEstimateHigh; }
        public void setEpsEstimateHigh(double epsEstimateHigh) { this.epsEstimateHigh = epsEstimateHigh; }
        public double getEpsEstimateMean() { return epsEstimateMean; }
        public void setEpsEstimateMean(double epsEstimateMean) { this.epsEstimateMean = epsEstimateMean; }
        public double getEpsEstimateAvg() { return epsEstimateAvg; }
        public void setEpsEstimateAvg(double epsEstimateAvg) { this.epsEstimateAvg = epsEstimateAvg; }
        public double getRevenueEstimateLow() { return revenueEstimateLow; }
        public void setRevenueEstimateLow(double revenueEstimateLow) { this.revenueEstimateLow = revenueEstimateLow; }
        public double getRevenueEstimateHigh() { return revenueEstimateHigh; }
        public void setRevenueEstimateHigh(double revenueEstimateHigh) { this.revenueEstimateHigh = revenueEstimateHigh; }
        public double getRevenueEstimateAvg() { return revenueEstimateAvg; }
        public void setRevenueEstimateAvg(double revenueEstimateAvg) { this.revenueEstimateAvg = revenueEstimateAvg; }
        public double getEpsGrowthRate() { return epsGrowthRate; }
        public void setEpsGrowthRate(double epsGrowthRate) { this.epsGrowthRate = epsGrowthRate; }
    }

    // ── Getters / Setters ──────────────────────────────────────────────────

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public List<QuarterlyEarning> getQuarterlyHistory() { return quarterlyHistory; }
    public void setQuarterlyHistory(List<QuarterlyEarning> quarterlyHistory) { this.quarterlyHistory = quarterlyHistory; }

    public List<EarningsTrend> getForwardTrends() { return forwardTrends; }
    public void setForwardTrends(List<EarningsTrend> forwardTrends) { this.forwardTrends = forwardTrends; }

    public double getBeatRatePercent() { return beatRatePercent; }
    public void setBeatRatePercent(double beatRatePercent) { this.beatRatePercent = beatRatePercent; }

    public double getAverageSurprise() { return averageSurprise; }
    public void setAverageSurprise(double averageSurprise) { this.averageSurprise = averageSurprise; }

    public String getEarningsMomentum() { return earningsMomentum; }
    public void setEarningsMomentum(String earningsMomentum) { this.earningsMomentum = earningsMomentum; }

    public Instant getFetchedAt() { return fetchedAt; }
    public void setFetchedAt(Instant fetchedAt) { this.fetchedAt = fetchedAt; }

    public boolean isRealData() { return isRealData; }
    public void setRealData(boolean realData) { isRealData = realData; }
}
