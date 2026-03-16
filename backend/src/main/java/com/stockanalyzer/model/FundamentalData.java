package com.stockanalyzer.model;

import java.time.Instant;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.jackson.Jacksonized;

/**
 * Fundamental financial metrics from Yahoo Finance quoteSummary API.
 */
@Data
@Builder
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FundamentalData {

    String symbol;

    // Valuation
    double peRatioTTM;          // Trailing P/E
    double peRatioForward;      // Forward P/E
    double pbRatio;             // Price-to-Book
    double psRatio;             // Price-to-Sales
    double evToEbitda;          // Enterprise Value / EBITDA
    double pegRatio;            // P/E to Growth

    // Per-share metrics
    double epsTTM;              // Trailing EPS (INR)
    double epsForward;          // Forward EPS estimate
    double bookValuePerShare;   // Book value per share (INR)
    double dividendYield;       // Annual dividend yield (%)
    double dividendRate;        // Annual dividend in INR

    // Profitability (as decimals, e.g. 0.22 = 22%)
    double grossMargins;
    double operatingMargins;
    double profitMargins;
    double returnOnEquity;      // ROE
    double returnOnAssets;      // ROA

    // Growth (YoY, decimals)
    double revenueGrowth;       // Revenue YoY growth
    double earningsGrowth;      // Net profit YoY growth
    double earningsQuarterlyGrowth; // Most recent quarter YoY

    // Balance sheet (INR crore unless noted)
    double totalRevenueCr;
    double grossProfitCr;
    double ebitdaCr;
    double totalDebtCr;
    double totalCashCr;
    double currentRatio;
    double debtToEquity;        // D/E ratio

    // Cash flow (INR crore)
    double operatingCashflowCr;
    double freeCashflowCr;

    // Shares
    long sharesOutstanding;
    double heldByInsidersPercent;
    double heldByInstitutionsPercent;

    // Market cap
    double marketCapCr;

    @Builder.Default Instant fetchedAt = Instant.now();
    boolean realData;
}
