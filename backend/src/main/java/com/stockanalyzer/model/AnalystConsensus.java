package com.stockanalyzer.model;

import java.time.Instant;
import java.util.List;

/**
 * Aggregated analyst recommendation data from Yahoo Finance.
 */
public class AnalystConsensus {

    private String symbol;

    // Recommendation counts (current month)
    private int strongBuyCount;
    private int buyCount;
    private int holdCount;
    private int sellCount;
    private int strongSellCount;
    private int totalAnalysts;

    // Derived
    private double consensusScore;    // 1.0 (strong buy) – 5.0 (strong sell); Yahoo Finance scale
    private String consensusLabel;    // STRONG BUY / BUY / HOLD / SELL / STRONG SELL

    // Price targets (INR)
    private double targetLow;
    private double targetMean;
    private double targetHigh;
    private double targetMedian;
    private double currentPrice;
    private double upsidePotentialPercent; // (targetMean - current) / current * 100

    // Recent upgrades / downgrades
    private List<UpgradeEvent> recentUpgrades;

    private Instant fetchedAt;
    private boolean isRealData;

    public AnalystConsensus() { this.fetchedAt = Instant.now(); }

    // ── Nested event ──────────────────────────────────────────────────────

    public static class UpgradeEvent {
        private String firm;
        private String action;          // up / down / main / init / reit
        private String fromGrade;
        private String toGrade;
        private Instant epochTime;

        public String getFirm() { return firm; }
        public void setFirm(String firm) { this.firm = firm; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public String getFromGrade() { return fromGrade; }
        public void setFromGrade(String fromGrade) { this.fromGrade = fromGrade; }
        public String getToGrade() { return toGrade; }
        public void setToGrade(String toGrade) { this.toGrade = toGrade; }
        public Instant getEpochTime() { return epochTime; }
        public void setEpochTime(Instant epochTime) { this.epochTime = epochTime; }
    }

    // ── Getters / Setters ──────────────────────────────────────────────────

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public int getStrongBuyCount() { return strongBuyCount; }
    public void setStrongBuyCount(int strongBuyCount) { this.strongBuyCount = strongBuyCount; }

    public int getBuyCount() { return buyCount; }
    public void setBuyCount(int buyCount) { this.buyCount = buyCount; }

    public int getHoldCount() { return holdCount; }
    public void setHoldCount(int holdCount) { this.holdCount = holdCount; }

    public int getSellCount() { return sellCount; }
    public void setSellCount(int sellCount) { this.sellCount = sellCount; }

    public int getStrongSellCount() { return strongSellCount; }
    public void setStrongSellCount(int strongSellCount) { this.strongSellCount = strongSellCount; }

    public int getTotalAnalysts() { return totalAnalysts; }
    public void setTotalAnalysts(int totalAnalysts) { this.totalAnalysts = totalAnalysts; }

    public double getConsensusScore() { return consensusScore; }
    public void setConsensusScore(double consensusScore) { this.consensusScore = consensusScore; }

    public String getConsensusLabel() { return consensusLabel; }
    public void setConsensusLabel(String consensusLabel) { this.consensusLabel = consensusLabel; }

    public double getTargetLow() { return targetLow; }
    public void setTargetLow(double targetLow) { this.targetLow = targetLow; }

    public double getTargetMean() { return targetMean; }
    public void setTargetMean(double targetMean) { this.targetMean = targetMean; }

    public double getTargetHigh() { return targetHigh; }
    public void setTargetHigh(double targetHigh) { this.targetHigh = targetHigh; }

    public double getTargetMedian() { return targetMedian; }
    public void setTargetMedian(double targetMedian) { this.targetMedian = targetMedian; }

    public double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }

    public double getUpsidePotentialPercent() { return upsidePotentialPercent; }
    public void setUpsidePotentialPercent(double upsidePotentialPercent) { this.upsidePotentialPercent = upsidePotentialPercent; }

    public List<UpgradeEvent> getRecentUpgrades() { return recentUpgrades; }
    public void setRecentUpgrades(List<UpgradeEvent> recentUpgrades) { this.recentUpgrades = recentUpgrades; }

    public Instant getFetchedAt() { return fetchedAt; }
    public void setFetchedAt(Instant fetchedAt) { this.fetchedAt = fetchedAt; }

    public boolean isRealData() { return isRealData; }
    public void setRealData(boolean realData) { isRealData = realData; }
}
