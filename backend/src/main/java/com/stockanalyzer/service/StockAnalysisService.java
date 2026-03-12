package com.stockanalyzer.service;

import com.stockanalyzer.client.YahooFinanceClient;
import com.stockanalyzer.dto.StockAnalysisResponse;
import com.stockanalyzer.dto.StockAnalysisResponse.*;
import com.stockanalyzer.inference.InferenceEngine;
import com.stockanalyzer.inference.ScoreCard;
import com.stockanalyzer.model.QuoteData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * Orchestrates the full stock analysis pipeline.
 *
 * Real-data enrichment strategy:
 *   - Live price/overview → Yahoo Finance (fallback: mock)
 *   - Technical analysis  → computed from real OHLCV via ChartDataService/TechnicalCalculator
 *   - Fundamental analysis→ Yahoo Finance quoteSummary (fallback: mock)
 *   - News               → RSS aggregator (fallback: mock)
 *   - Financial statements→ Yahoo Finance (fallback: mock generator)
 *   - Shareholding        → Yahoo Finance majorHolders (fallback: mock)
 *   - Overall summary     → InferenceEngine (multi-factor weighted scoring on real data)
 */
@Service
public class StockAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(StockAnalysisService.class);

    private final MockDataService            mockDataService;
    private final TechnicalAnalysisService   technicalService;
    private final FundamentalAnalysisService fundamentalService;
    private final NewsService                newsService;
    private final FinancialStatementsService financialService;
    private final ShareholdingService        shareholdingService;
    private final ManipulationDetectionService manipulationService;
    private final YahooFinanceClient         yahooClient;
    private final InferenceEngine            inferenceEngine;

    @Autowired
    public StockAnalysisService(MockDataService mockDataService,
                                TechnicalAnalysisService technicalService,
                                FundamentalAnalysisService fundamentalService,
                                NewsService newsService,
                                FinancialStatementsService financialService,
                                ShareholdingService shareholdingService,
                                ManipulationDetectionService manipulationService,
                                YahooFinanceClient yahooClient,
                                InferenceEngine inferenceEngine) {
        this.mockDataService     = mockDataService;
        this.technicalService    = technicalService;
        this.fundamentalService  = fundamentalService;
        this.newsService         = newsService;
        this.financialService    = financialService;
        this.shareholdingService = shareholdingService;
        this.manipulationService = manipulationService;
        this.yahooClient         = yahooClient;
        this.inferenceEngine     = inferenceEngine;
    }

    public StockAnalysisResponse analyze(String symbol) {
        String upperSymbol = symbol.toUpperCase().trim();
        Random rng = mockDataService.getSeededRandom(upperSymbol);

        String[] meta   = mockDataService.getStockMeta(upperSymbol);
        double[] prices = mockDataService.getStockPrices(upperSymbol);

        // ── Try to get a live quote from Yahoo Finance ────────────────────
        QuoteData liveQuote = null;
        try {
            liveQuote = yahooClient.getQuote(upperSymbol);
        } catch (Exception e) {
            log.warn("Live quote unavailable for {}: {}", upperSymbol, e.getMessage());
        }

        double currentPrice = (liveQuote != null && liveQuote.isRealData())
                ? liveQuote.getCurrentPrice() : prices[0];
        double dayHigh    = (liveQuote != null && liveQuote.isRealData())
                ? liveQuote.getDayHigh()    : prices[1];
        double dayLow     = (liveQuote != null && liveQuote.isRealData())
                ? liveQuote.getDayLow()     : prices[2];
        double week52High = (liveQuote != null && liveQuote.isRealData())
                ? liveQuote.getWeek52High() : prices[3];
        double week52Low  = (liveQuote != null && liveQuote.isRealData())
                ? liveQuote.getWeek52Low()  : prices[4];
        double change     = (liveQuote != null && liveQuote.isRealData())
                ? liveQuote.getChange()     : (rng.nextDouble() - 0.4) * currentPrice * 0.04;
        double changePct  = (liveQuote != null && liveQuote.isRealData())
                ? liveQuote.getChangePercent() : (change / currentPrice) * 100;

        String companyName = (liveQuote != null && liveQuote.getCompanyName() != null
                && !liveQuote.getCompanyName().isEmpty())
                ? liveQuote.getCompanyName() : meta[0];

        // ── Build response ────────────────────────────────────────────────
        StockAnalysisResponse response = new StockAnalysisResponse();
        response.setSymbol(upperSymbol);
        response.setCompanyName(companyName);
        response.setExchange(meta[1]);
        response.setSector(meta[2]);

        // Stock Overview
        StockOverview overview = new StockOverview();
        overview.setCurrentPrice(round2(currentPrice));
        overview.setDayHigh(round2(dayHigh));
        overview.setDayLow(round2(dayLow));
        overview.setWeekHigh52(round2(week52High));
        overview.setWeekLow52(round2(week52Low));

        long volume    = (liveQuote != null) ? liveQuote.getVolume()
                : (long)(1_000_000 + rng.nextDouble() * 50_000_000);
        long avgVolume = (liveQuote != null) ? liveQuote.getAverageVolume()
                : (long)(2_000_000 + rng.nextDouble() * 20_000_000);
        overview.setVolume(volume);
        overview.setAvgVolume(avgVolume);

        double marketCap = (liveQuote != null && liveQuote.getMarketCap() > 0)
                ? liveQuote.getMarketCap()
                : currentPrice * (100_000_000L + (long)(rng.nextDouble() * 10_000_000_000L)) / 10_000_000.0;
        overview.setMarketCap(round2(marketCap));
        overview.setChangeAmount(round2(change));
        overview.setChangePercent(round2(changePct));
        overview.setPriceChangeJustification(String.format(
                "Stock %s ₹%.2f (%.2f%%) today. %s",
                change >= 0 ? "gained" : "fell", Math.abs(change), Math.abs(changePct),
                change >= 0
                    ? "Positive momentum. See inference engine scorecard for detailed drivers."
                    : "Mild selling pressure. Inference engine scorecard available for detailed analysis."));
        response.setOverview(overview);

        // ── Sub-analyses (existing services with real-data enrichment) ────
        response.setTechnicalAnalysis(technicalService.analyze(
                upperSymbol, currentPrice, prices, new Random(rng.nextLong())));

        response.setFundamentalAnalysis(fundamentalService.analyze(
                upperSymbol, currentPrice, meta[2], new Random(rng.nextLong())));

        response.setCompanyNews(newsService.generateCompanyNews(
                upperSymbol, companyName, new Random(rng.nextLong())));
        response.setCompetitorNews(newsService.generateCompetitorNews(
                upperSymbol, mockDataService.getCompetitors(upperSymbol),
                meta[2], new Random(rng.nextLong())));

        double baseRevenue = currentPrice * (10_000 + rng.nextInt(500_000));
        response.setFinancialStatements(financialService.generate(
                upperSymbol, baseRevenue, new Random(rng.nextLong())));

        response.setShareholdingPattern(shareholdingService.analyze(
                upperSymbol, new Random(rng.nextLong())));

        response.setManipulationAnalysis(manipulationService.analyze(
                upperSymbol, currentPrice, prices, volume, avgVolume, new Random(rng.nextLong())));

        // ── Overall summary driven by InferenceEngine ─────────────────────
        buildOverallSummary(response, upperSymbol);

        return response;
    }

    // ── Summary builder (InferenceEngine + legacy fallback) ───────────────

    private void buildOverallSummary(StockAnalysisResponse response, String symbol) {
        ScoreCard card = null;
        try {
            card = inferenceEngine.analyse(symbol);
        } catch (Exception e) {
            log.warn("InferenceEngine failed for {}: {}", symbol, e.getMessage());
        }

        if (card != null) {
            response.setOverallSentiment(card.getSentiment());
            response.setOverallRecommendation(card.getRecommendation());

            StringBuilder sb = new StringBuilder();
            sb.append(String.format(
                    "Inference Engine Score: %.1f / 100 | %s (%s).\n",
                    card.getCompositeScore(), card.getRecommendation(), card.getSentiment()));
            sb.append(String.format("Confidence: %.0f%%", card.getConfidencePercent()));
            if (card.isUsedRealPriceData())    sb.append(" | Live price data");
            if (card.isUsedRealFundamentals()) sb.append(" | Real fundamentals");
            if (card.isUsedRealAnalystData())  sb.append(" | Analyst consensus");
            if (card.isUsedRealNews())         sb.append(" | Live news");
            sb.append(".\n\n");

            sb.append(String.format(
                    "Component Scores → Technical: %.0f | Fundamental: %.0f | " +
                    "Valuation: %.0f | Analyst: %.0f | Earnings: %.0f | " +
                    "Sentiment: %.0f | Momentum: %.0f\n\n",
                    card.getTechnicalScore(), card.getFundamentalScore(),
                    card.getValuationScore(), card.getAnalystScore(),
                    card.getEarningsScore(), card.getSentimentScore(),
                    card.getMomentumScore()));

            if (!card.getBullishFactors().isEmpty()) {
                sb.append("Bullish Factors:\n");
                card.getBullishFactors().forEach(f -> sb.append("  + ").append(f).append("\n"));
                sb.append("\n");
            }
            if (!card.getBearishFactors().isEmpty()) {
                sb.append("Bearish Factors:\n");
                card.getBearishFactors().forEach(f -> sb.append("  - ").append(f).append("\n"));
                sb.append("\n");
            }
            if (!card.getRiskFactors().isEmpty()) {
                sb.append("Risks:\n");
                card.getRiskFactors().forEach(r -> sb.append("  ! ").append(r).append("\n"));
                sb.append("\n");
            }

            if (card.getTargetPriceMid() > 0) {
                sb.append(String.format(
                        "Price Target: ₹%.0f – ₹%.0f (base: ₹%.0f)\n",
                        card.getTargetPriceLow(), card.getTargetPriceHigh(), card.getTargetPriceMid()));
            }

            sb.append("\nImportant: This analysis uses publicly available data. " +
                    "Always consult a SEBI-registered advisor before investing. " +
                    "Past performance does not guarantee future results.");
            response.setAnalysisJustification(sb.toString());

        } else {
            buildLegacyOverallSummary(response);
        }
    }

    private void buildLegacyOverallSummary(StockAnalysisResponse response) {
        TechnicalAnalysis ta = response.getTechnicalAnalysis();
        FundamentalAnalysis fa = response.getFundamentalAnalysis();
        ManipulationAnalysis ma = response.getManipulationAnalysis();

        int bullishPoints = 0;
        int bearishPoints = 0;

        if ("BULLISH".equals(ta.getOverallTechnicalSignal()))   bullishPoints += 2;
        else if ("BEARISH".equals(ta.getOverallTechnicalSignal())) bearishPoints += 2;
        if ("UNDERVALUED".equals(fa.getValuationStatus()))  bullishPoints += 2;
        else if ("OVERVALUED".equals(fa.getValuationStatus())) bearishPoints += 2;
        if (fa.getRoe() > 18) bullishPoints++;
        if (fa.getDebtToEquity() < 0.5) bullishPoints++;
        if (fa.getRevenueGrowthYoY() > 15) bullishPoints++;
        if (fa.getProfitGrowthYoY() > 15) bullishPoints++;
        if ("HIGH".equals(ma.getManipulationRiskLevel())) bearishPoints += 2;
        else if ("MEDIUM".equals(ma.getManipulationRiskLevel())) bearishPoints++;
        ShareholdingPattern sp = response.getShareholdingPattern();
        if ("HIGH".equals(sp.getRiskLevel())) bearishPoints++;

        String sentiment;
        String recommendation;
        if (bullishPoints >= bearishPoints + 3)      { sentiment = "BULLISH";       recommendation = "BUY"; }
        else if (bearishPoints >= bullishPoints + 3) { sentiment = "BEARISH";       recommendation = "SELL / AVOID"; }
        else if (bullishPoints > bearishPoints)      { sentiment = "MILDLY BULLISH"; recommendation = "ACCUMULATE ON DIPS"; }
        else if (bearishPoints > bullishPoints)      { sentiment = "MILDLY BEARISH"; recommendation = "HOLD WITH CAUTION"; }
        else                                         { sentiment = "NEUTRAL";         recommendation = "HOLD / WATCH"; }

        response.setOverallSentiment(sentiment);
        response.setOverallRecommendation(recommendation);
        response.setAnalysisJustification(String.format(
                "Scored %d bullish vs %d bearish points across technical, fundamental, and manipulation dimensions. " +
                "Technical: %s. Valuation: %s. ROE: %.1f%%. " +
                "Always consult a SEBI-registered advisor before investing.",
                bullishPoints, bearishPoints, ta.getOverallTechnicalSignal(),
                fa.getValuationStatus(), fa.getRoe()));
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
