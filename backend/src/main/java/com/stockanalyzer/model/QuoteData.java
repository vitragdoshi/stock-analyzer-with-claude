package com.stockanalyzer.model;

import java.time.Instant;

/**
 * Live quote data fetched from Yahoo Finance (or NSE fallback).
 * All prices are in INR.
 */
public class QuoteData {

    private String symbol;
    private String companyName;
    private String exchange;       // NSE or BSE

    // Price
    private double currentPrice;
    private double previousClose;
    private double dayOpen;
    private double dayHigh;
    private double dayLow;
    private double change;
    private double changePercent;

    // Volume
    private long volume;
    private long averageVolume;    // 3-month avg
    private long averageVolume10d; // 10-day avg

    // 52-week range
    private double week52High;
    private double week52Low;

    // Market metrics
    private double marketCap;      // in INR crore
    private double beta;
    private String currency;

    // Data freshness
    private Instant fetchedAt;
    private boolean isRealData;    // false = fell back to mock

    public QuoteData() { this.fetchedAt = Instant.now(); }

    // ── Getters / Setters ──────────────────────────────────────────────────

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getExchange() { return exchange; }
    public void setExchange(String exchange) { this.exchange = exchange; }

    public double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }

    public double getPreviousClose() { return previousClose; }
    public void setPreviousClose(double previousClose) { this.previousClose = previousClose; }

    public double getDayOpen() { return dayOpen; }
    public void setDayOpen(double dayOpen) { this.dayOpen = dayOpen; }

    public double getDayHigh() { return dayHigh; }
    public void setDayHigh(double dayHigh) { this.dayHigh = dayHigh; }

    public double getDayLow() { return dayLow; }
    public void setDayLow(double dayLow) { this.dayLow = dayLow; }

    public double getChange() { return change; }
    public void setChange(double change) { this.change = change; }

    public double getChangePercent() { return changePercent; }
    public void setChangePercent(double changePercent) { this.changePercent = changePercent; }

    public long getVolume() { return volume; }
    public void setVolume(long volume) { this.volume = volume; }

    public long getAverageVolume() { return averageVolume; }
    public void setAverageVolume(long averageVolume) { this.averageVolume = averageVolume; }

    public long getAverageVolume10d() { return averageVolume10d; }
    public void setAverageVolume10d(long averageVolume10d) { this.averageVolume10d = averageVolume10d; }

    public double getWeek52High() { return week52High; }
    public void setWeek52High(double week52High) { this.week52High = week52High; }

    public double getWeek52Low() { return week52Low; }
    public void setWeek52Low(double week52Low) { this.week52Low = week52Low; }

    public double getMarketCap() { return marketCap; }
    public void setMarketCap(double marketCap) { this.marketCap = marketCap; }

    public double getBeta() { return beta; }
    public void setBeta(double beta) { this.beta = beta; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public Instant getFetchedAt() { return fetchedAt; }
    public void setFetchedAt(Instant fetchedAt) { this.fetchedAt = fetchedAt; }

    public boolean isRealData() { return isRealData; }
    public void setRealData(boolean realData) { isRealData = realData; }
}
