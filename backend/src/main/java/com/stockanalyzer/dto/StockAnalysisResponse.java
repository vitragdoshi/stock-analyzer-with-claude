package com.stockanalyzer.dto;

import java.util.List;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockAnalysisResponse {
    String symbol;
    String companyName;
    String exchange;
    String sector;
    StockOverview overview;
    TechnicalAnalysis technicalAnalysis;
    FundamentalAnalysis fundamentalAnalysis;
    List<NewsItem> companyNews;
    List<NewsItem> competitorNews;
    FinancialStatements financialStatements;
    ShareholdingPattern shareholdingPattern;
    ManipulationAnalysis manipulationAnalysis;
    String overallSentiment;
    String overallRecommendation;
    String analysisJustification;

    @Data
    @Builder
    @Jacksonized
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class StockOverview {
        double currentPrice;
        double dayHigh;
        double dayLow;
        double weekHigh52;
        double weekLow52;
        long volume;
        long avgVolume;
        double marketCap;
        double changePercent;
        double changeAmount;
        String priceChangeJustification;
    }

    @Data
    @Builder
    @Jacksonized
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class TechnicalAnalysis {
        double rsi;
        String rsiSignal;
        String rsiJustification;
        double macd;
        double macdSignal;
        double macdHistogram;
        String macdTrend;
        String macdJustification;
        double sma20;
        double sma50;
        double sma200;
        String movingAvgSignal;
        String movingAvgJustification;
        double bollingerUpper;
        double bollingerMiddle;
        double bollingerLower;
        String bollingerSignal;
        double atr;
        double obv;
        String obvTrend;
        double stochasticK;
        double stochasticD;
        String stochasticSignal;
        String overallTechnicalSignal;
        String technicalSummary;
    }

    @Data
    @Builder
    @Jacksonized
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class FundamentalAnalysis {
        double pe;
        double industryPe;
        String peJustification;
        double pb;
        double roe;
        double roce;
        double eps;
        double debtToEquity;
        double currentRatio;
        double dividendYield;
        double revenueGrowthYoY;
        double profitGrowthYoY;
        double promoterHolding;
        double fiiHolding;
        double diiHolding;
        String valuationStatus;
        String fundamentalSummary;
        String intrinsicValueJustification;
        double estimatedIntrinsicValue;
        double priceToIntrinsicRatio;
    }

    @Data
    @Builder
    @Jacksonized
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class NewsItem {
        String headline;
        String source;
        String publishedDate;
        String summary;
        String sentiment;
        String authenticityScore;
        String authenticityJustification;
        String impactOnStock;
        boolean authentic;
        String url;
    }

    @Data
    @Builder
    @Jacksonized
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class FinancialStatements {
        List<AnnualStatement> annualStatements;
        List<QuarterlyStatement> quarterlyStatements;
        String financialHealthSummary;
        String growthTrendAnalysis;
        String redFlags;
    }

    @Data
    @Builder
    @Jacksonized
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class AnnualStatement {
        String year;
        double revenue;
        double netProfit;
        double ebitda;
        double totalDebt;
        double totalEquity;
        double operatingCashFlow;
        double freeCashFlow;
        double revenueGrowth;
        double profitGrowth;
        double ebitdaMargin;
        double netMargin;
    }

    @Data
    @Builder
    @Jacksonized
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class QuarterlyStatement {
        String quarter;
        double revenue;
        double netProfit;
        double ebitda;
        double revenueGrowthYoY;
        double profitGrowthYoY;
        double revenueGrowthQoQ;
        double ebitdaMargin;
        double netMargin;
    }

    @Data
    @Builder
    @Jacksonized
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ShareholdingPattern {
        double promoterHolding;
        double promoterPledged;
        double fiiHolding;
        double diiHolding;
        double publicHolding;
        List<ShareholdingTrend> trend;
        List<String> redFlags;
        String analysis;
        String riskLevel;
    }

    @Data
    @Builder
    @Jacksonized
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ShareholdingTrend {
        String quarter;
        double promoter;
        double fii;
        double dii;
        double public_;
    }

    @Data
    @Builder
    @Jacksonized
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ManipulationAnalysis {
        boolean suspectedManipulation;
        String manipulationRiskLevel;
        List<String> redFlags;
        List<String> greenFlags;
        double unusualVolumeScore;
        double pricePatternScore;
        double circuitBreakerHits;
        String operatorActivityAnalysis;
        String pumpAndDumpRisk;
        String analysisJustification;
    }
}
