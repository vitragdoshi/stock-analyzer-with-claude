package com.stockanalyzer.client.yahoo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

/**
 * POJO mapping for Yahoo Finance quoteSummary API v10:
 * {@code GET https://query2.finance.yahoo.com/v10/finance/quoteSummary/{symbol}?modules=...&crumb=...}
 *
 * Most numeric fields use Yahoo Finance's {@code {"raw": 1.23, "fmt": "1.23"}} format,
 * handled transparently by {@link YfNumber}.
 *
 * Modules requested: summaryDetail, financialData, defaultKeyStatistics,
 *   recommendationTrend, earningsTrend, earningsHistory,
 *   upgradeDowngradeHistory, majorHoldersBreakdown, price
 */
@Getter
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class YfQuoteSummaryResponse {

    @JsonProperty("quoteSummary")
    QuoteSummary quoteSummary;

    // ── top-level wrapper ─────────────────────────────────────────────────

    @Getter
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static class QuoteSummary {
        List<QuoteSummaryResult> result;
    }

    // ── merged result object (all requested modules) ──────────────────────

    @Getter
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static class QuoteSummaryResult {
        SummaryDetail              summaryDetail;
        FinancialData              financialData;
        DefaultKeyStatistics       defaultKeyStatistics;
        RecommendationTrend        recommendationTrend;
        EarningsTrend              earningsTrend;
        EarningsHistory            earningsHistory;
        UpgradeDowngradeHistory    upgradeDowngradeHistory;
        MajorHoldersBreakdown      majorHoldersBreakdown;
        Price                      price;
    }

    // ── summaryDetail ─────────────────────────────────────────────────────

    @Getter
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static class SummaryDetail {
        YfNumber trailingPE;
        YfNumber forwardPE;
        YfNumber dividendYield;
        YfNumber dividendRate;
        YfNumber beta;
    }

    // ── financialData ─────────────────────────────────────────────────────

    @Getter
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static class FinancialData {
        YfNumber grossMargins;
        YfNumber operatingMargins;
        YfNumber profitMargins;
        YfNumber returnOnEquity;
        YfNumber returnOnAssets;
        YfNumber revenueGrowth;
        YfNumber earningsGrowth;
        YfNumber totalRevenue;
        YfNumber grossProfits;
        YfNumber ebitda;
        YfNumber totalDebt;
        YfNumber totalCash;
        YfNumber currentRatio;
        YfNumber debtToEquity;
        YfNumber operatingCashflow;
        YfNumber freeCashflow;
        YfNumber targetLowPrice;
        YfNumber targetMeanPrice;
        YfNumber targetHighPrice;
        YfNumber targetMedianPrice;
    }

    // ── defaultKeyStatistics ──────────────────────────────────────────────

    @Getter
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static class DefaultKeyStatistics {
        YfNumber priceToBook;
        YfNumber priceToSalesTrailing12Months;
        YfNumber enterpriseToEbitda;
        YfNumber pegRatio;
        YfNumber trailingEps;
        YfNumber forwardEps;
        YfNumber bookValue;
        YfNumber earningsQuarterlyGrowth;
        YfNumber sharesOutstanding;
    }

    // ── recommendationTrend ───────────────────────────────────────────────

    @Getter
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static class RecommendationTrend {
        List<RecommendationPeriod> trend;
    }

    @Getter
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static class RecommendationPeriod {
        String period;
        int    strongBuy;
        int    buy;
        int    hold;
        int    sell;
        int    strongSell;
    }

    // ── earningsTrend ─────────────────────────────────────────────────────

    @Getter
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static class EarningsTrend {
        List<EarningsTrendPeriod> trend;
    }

    @Getter
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static class EarningsTrendPeriod {
        String          period;
        String          endDate;
        EarningsEstimate earningsEstimate;
        RevenueEstimate  revenueEstimate;
    }

    @Getter
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static class EarningsEstimate {
        YfNumber avg;
        YfNumber low;
        YfNumber high;
        YfNumber yearAgoEps;
    }

    @Getter
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static class RevenueEstimate {
        YfNumber avg;
        YfNumber low;
        YfNumber high;
    }

    // ── earningsHistory ───────────────────────────────────────────────────

    @Getter
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static class EarningsHistory {
        List<EarningsHistoryEntry> history;
    }

    @Getter
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static class EarningsHistoryEntry {
        String   quarter;
        YfNumber epsEstimate;
        YfNumber epsActual;
        YfNumber epsDifference;
        YfNumber surprisePercent;
    }

    // ── upgradeDowngradeHistory ───────────────────────────────────────────

    @Getter
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static class UpgradeDowngradeHistory {
        List<UpgradeDowngradeEvent> history;
    }

    @Getter
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static class UpgradeDowngradeEvent {
        String firm;
        String action;
        String fromGrade;
        String toGrade;
        Long   epochGradeDate;
    }

    // ── majorHoldersBreakdown ─────────────────────────────────────────────

    @Getter
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static class MajorHoldersBreakdown {
        YfNumber insidersPercentHeld;
        YfNumber institutionsPercentHeld;
    }

    // ── price ─────────────────────────────────────────────────────────────

    @Getter
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static class Price {
        YfNumber marketCap;
        YfNumber regularMarketPrice;
    }
}
