package com.stockanalyzer.model;

import java.time.Instant;

/**
 * Fundamental financial metrics from Yahoo Finance quoteSummary API.
 */
public class FundamentalData {

    private String symbol;

    // Valuation
    private double peRatioTTM;          // Trailing P/E
    private double peRatioForward;      // Forward P/E
    private double pbRatio;             // Price-to-Book
    private double psRatio;             // Price-to-Sales
    private double evToEbitda;          // Enterprise Value / EBITDA
    private double pegRatio;            // P/E to Growth

    // Per-share metrics
    private double epsTTM;              // Trailing EPS (INR)
    private double epsForward;          // Forward EPS estimate
    private double bookValuePerShare;   // Book value per share (INR)
    private double dividendYield;       // Annual dividend yield (%)
    private double dividendRate;        // Annual dividend in INR

    // Profitability (as decimals, e.g. 0.22 = 22%)
    private double grossMargins;
    private double operatingMargins;
    private double profitMargins;
    private double returnOnEquity;      // ROE
    private double returnOnAssets;      // ROA

    // Growth (YoY, decimals)
    private double revenueGrowth;       // Revenue YoY growth
    private double earningsGrowth;      // Net profit YoY growth
    private double earningsQuarterlyGrowth; // Most recent quarter YoY

    // Balance sheet (INR crore unless noted)
    private double totalRevenueCr;
    private double grossProfitCr;
    private double ebitdaCr;
    private double totalDebtCr;
    private double totalCashCr;
    private double currentRatio;
    private double debtToEquity;        // D/E ratio

    // Cash flow (INR crore)
    private double operatingCashflowCr;
    private double freeCashflowCr;

    // Shares
    private long sharesOutstanding;
    private double heldByInsidersPercent;
    private double heldByInstitutionsPercent;

    // Market cap
    private double marketCapCr;

    private Instant fetchedAt;
    private boolean isRealData;

    public FundamentalData() { this.fetchedAt = Instant.now(); }

    // ── Getters / Setters ──────────────────────────────────────────────────

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public double getPeRatioTTM() { return peRatioTTM; }
    public void setPeRatioTTM(double peRatioTTM) { this.peRatioTTM = peRatioTTM; }

    public double getPeRatioForward() { return peRatioForward; }
    public void setPeRatioForward(double peRatioForward) { this.peRatioForward = peRatioForward; }

    public double getPbRatio() { return pbRatio; }
    public void setPbRatio(double pbRatio) { this.pbRatio = pbRatio; }

    public double getPsRatio() { return psRatio; }
    public void setPsRatio(double psRatio) { this.psRatio = psRatio; }

    public double getEvToEbitda() { return evToEbitda; }
    public void setEvToEbitda(double evToEbitda) { this.evToEbitda = evToEbitda; }

    public double getPegRatio() { return pegRatio; }
    public void setPegRatio(double pegRatio) { this.pegRatio = pegRatio; }

    public double getEpsTTM() { return epsTTM; }
    public void setEpsTTM(double epsTTM) { this.epsTTM = epsTTM; }

    public double getEpsForward() { return epsForward; }
    public void setEpsForward(double epsForward) { this.epsForward = epsForward; }

    public double getBookValuePerShare() { return bookValuePerShare; }
    public void setBookValuePerShare(double bookValuePerShare) { this.bookValuePerShare = bookValuePerShare; }

    public double getDividendYield() { return dividendYield; }
    public void setDividendYield(double dividendYield) { this.dividendYield = dividendYield; }

    public double getDividendRate() { return dividendRate; }
    public void setDividendRate(double dividendRate) { this.dividendRate = dividendRate; }

    public double getGrossMargins() { return grossMargins; }
    public void setGrossMargins(double grossMargins) { this.grossMargins = grossMargins; }

    public double getOperatingMargins() { return operatingMargins; }
    public void setOperatingMargins(double operatingMargins) { this.operatingMargins = operatingMargins; }

    public double getProfitMargins() { return profitMargins; }
    public void setProfitMargins(double profitMargins) { this.profitMargins = profitMargins; }

    public double getReturnOnEquity() { return returnOnEquity; }
    public void setReturnOnEquity(double returnOnEquity) { this.returnOnEquity = returnOnEquity; }

    public double getReturnOnAssets() { return returnOnAssets; }
    public void setReturnOnAssets(double returnOnAssets) { this.returnOnAssets = returnOnAssets; }

    public double getRevenueGrowth() { return revenueGrowth; }
    public void setRevenueGrowth(double revenueGrowth) { this.revenueGrowth = revenueGrowth; }

    public double getEarningsGrowth() { return earningsGrowth; }
    public void setEarningsGrowth(double earningsGrowth) { this.earningsGrowth = earningsGrowth; }

    public double getEarningsQuarterlyGrowth() { return earningsQuarterlyGrowth; }
    public void setEarningsQuarterlyGrowth(double earningsQuarterlyGrowth) { this.earningsQuarterlyGrowth = earningsQuarterlyGrowth; }

    public double getTotalRevenueCr() { return totalRevenueCr; }
    public void setTotalRevenueCr(double totalRevenueCr) { this.totalRevenueCr = totalRevenueCr; }

    public double getGrossProfitCr() { return grossProfitCr; }
    public void setGrossProfitCr(double grossProfitCr) { this.grossProfitCr = grossProfitCr; }

    public double getEbitdaCr() { return ebitdaCr; }
    public void setEbitdaCr(double ebitdaCr) { this.ebitdaCr = ebitdaCr; }

    public double getTotalDebtCr() { return totalDebtCr; }
    public void setTotalDebtCr(double totalDebtCr) { this.totalDebtCr = totalDebtCr; }

    public double getTotalCashCr() { return totalCashCr; }
    public void setTotalCashCr(double totalCashCr) { this.totalCashCr = totalCashCr; }

    public double getCurrentRatio() { return currentRatio; }
    public void setCurrentRatio(double currentRatio) { this.currentRatio = currentRatio; }

    public double getDebtToEquity() { return debtToEquity; }
    public void setDebtToEquity(double debtToEquity) { this.debtToEquity = debtToEquity; }

    public double getOperatingCashflowCr() { return operatingCashflowCr; }
    public void setOperatingCashflowCr(double operatingCashflowCr) { this.operatingCashflowCr = operatingCashflowCr; }

    public double getFreeCashflowCr() { return freeCashflowCr; }
    public void setFreeCashflowCr(double freeCashflowCr) { this.freeCashflowCr = freeCashflowCr; }

    public long getSharesOutstanding() { return sharesOutstanding; }
    public void setSharesOutstanding(long sharesOutstanding) { this.sharesOutstanding = sharesOutstanding; }

    public double getHeldByInsidersPercent() { return heldByInsidersPercent; }
    public void setHeldByInsidersPercent(double heldByInsidersPercent) { this.heldByInsidersPercent = heldByInsidersPercent; }

    public double getHeldByInstitutionsPercent() { return heldByInstitutionsPercent; }
    public void setHeldByInstitutionsPercent(double heldByInstitutionsPercent) { this.heldByInstitutionsPercent = heldByInstitutionsPercent; }

    public double getMarketCapCr() { return marketCapCr; }
    public void setMarketCapCr(double marketCapCr) { this.marketCapCr = marketCapCr; }

    public Instant getFetchedAt() { return fetchedAt; }
    public void setFetchedAt(Instant fetchedAt) { this.fetchedAt = fetchedAt; }

    public boolean isRealData() { return isRealData; }
    public void setRealData(boolean realData) { isRealData = realData; }
}
