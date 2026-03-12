package com.stockanalyzer.service;

import com.stockanalyzer.dto.StockAnalysisResponse;
import com.stockanalyzer.dto.StockAnalysisResponse.*;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class StockAnalysisService {

    private final MockDataService mockDataService;
    private final TechnicalAnalysisService technicalService;
    private final FundamentalAnalysisService fundamentalService;
    private final NewsService newsService;
    private final FinancialStatementsService financialService;
    private final ShareholdingService shareholdingService;
    private final ManipulationDetectionService manipulationService;

    public StockAnalysisService(MockDataService mockDataService,
                                 TechnicalAnalysisService technicalService,
                                 FundamentalAnalysisService fundamentalService,
                                 NewsService newsService,
                                 FinancialStatementsService financialService,
                                 ShareholdingService shareholdingService,
                                 ManipulationDetectionService manipulationService) {
        this.mockDataService = mockDataService;
        this.technicalService = technicalService;
        this.fundamentalService = fundamentalService;
        this.newsService = newsService;
        this.financialService = financialService;
        this.shareholdingService = shareholdingService;
        this.manipulationService = manipulationService;
    }

    public StockAnalysisResponse analyze(String symbol) {
        String upperSymbol = symbol.toUpperCase().trim();
        Random rng = mockDataService.getSeededRandom(upperSymbol);

        String[] meta = mockDataService.getStockMeta(upperSymbol);
        double[] prices = mockDataService.getStockPrices(upperSymbol);

        double currentPrice = prices[0];
        double dayHigh = prices[1];
        double dayLow = prices[2];
        double weekHigh52 = prices[3];
        double weekLow52 = prices[4];

        StockAnalysisResponse response = new StockAnalysisResponse();
        response.setSymbol(upperSymbol);
        response.setCompanyName(meta[0]);
        response.setExchange(meta[1]);
        response.setSector(meta[2]);

        // Stock Overview
        StockOverview overview = new StockOverview();
        overview.setCurrentPrice(currentPrice);
        overview.setDayHigh(dayHigh);
        overview.setDayLow(dayLow);
        overview.setWeekHigh52(weekHigh52);
        overview.setWeekLow52(weekLow52);

        long volume = (long) (1000000 + rng.nextDouble() * 50000000);
        long avgVolume = (long) (2000000 + rng.nextDouble() * 20000000);
        overview.setVolume(volume);
        overview.setAvgVolume(avgVolume);

        double marketCap = currentPrice * (100000000L + (long)(rng.nextDouble() * 10000000000L));
        overview.setMarketCap(Math.round(marketCap / 10000000.0) / 100.0); // in Cr

        double change = (rng.nextDouble() - 0.4) * currentPrice * 0.04;
        double changePercent = (change / currentPrice) * 100;
        overview.setChangeAmount(Math.round(change * 100.0) / 100.0);
        overview.setChangePercent(Math.round(changePercent * 100.0) / 100.0);

        overview.setPriceChangeJustification(String.format(
            "Stock %s %.2f (%.2f%%) today. %s",
            change >= 0 ? "gained ₹" : "fell ₹", Math.abs(change), Math.abs(changePercent),
            change >= 0 ? "Positive momentum driven by sector tailwinds and recent institutional buying." :
                "Mild selling pressure amid broader market correction. Fundamentals remain intact."));

        response.setOverview(overview);

        // Technical Analysis
        response.setTechnicalAnalysis(technicalService.analyze(upperSymbol, currentPrice, prices, new Random(rng.nextLong())));

        // Fundamental Analysis
        response.setFundamentalAnalysis(fundamentalService.analyze(upperSymbol, currentPrice, meta[2], new Random(rng.nextLong())));

        // News
        response.setCompanyNews(newsService.generateCompanyNews(upperSymbol, meta[0], new Random(rng.nextLong())));
        response.setCompetitorNews(newsService.generateCompetitorNews(upperSymbol,
            mockDataService.getCompetitors(upperSymbol), meta[2], new Random(rng.nextLong())));

        // Financial Statements
        double baseRevenue = currentPrice * (10000 + rng.nextInt(500000));
        response.setFinancialStatements(financialService.generate(upperSymbol, baseRevenue, new Random(rng.nextLong())));

        // Shareholding
        response.setShareholdingPattern(shareholdingService.analyze(upperSymbol, new Random(rng.nextLong())));

        // Manipulation Detection
        response.setManipulationAnalysis(manipulationService.analyze(upperSymbol, currentPrice, prices, volume, avgVolume, new Random(rng.nextLong())));

        // Overall Summary
        buildOverallSummary(response);

        return response;
    }

    private void buildOverallSummary(StockAnalysisResponse response) {
        TechnicalAnalysis ta = response.getTechnicalAnalysis();
        FundamentalAnalysis fa = response.getFundamentalAnalysis();
        ManipulationAnalysis ma = response.getManipulationAnalysis();

        int bullishPoints = 0;
        int bearishPoints = 0;

        // Technical signals
        if (ta.getOverallTechnicalSignal().equals("BULLISH")) bullishPoints += 2;
        else if (ta.getOverallTechnicalSignal().equals("BEARISH")) bearishPoints += 2;

        // Fundamental signals
        if (fa.getValuationStatus().equals("UNDERVALUED")) bullishPoints += 2;
        else if (fa.getValuationStatus().equals("OVERVALUED")) bearishPoints += 2;

        if (fa.getRoe() > 18) bullishPoints++;
        if (fa.getDebtToEquity() < 0.5) bullishPoints++;
        if (fa.getRevenueGrowthYoY() > 15) bullishPoints++;
        if (fa.getProfitGrowthYoY() > 15) bullishPoints++;

        // Manipulation risk
        if (ma.getManipulationRiskLevel().equals("HIGH")) bearishPoints += 2;
        else if (ma.getManipulationRiskLevel().equals("MEDIUM")) bearishPoints++;

        // Shareholding
        ShareholdingPattern sp = response.getShareholdingPattern();
        if (sp.getRiskLevel().equals("HIGH")) bearishPoints++;

        // News sentiment
        long posNews = response.getCompanyNews().stream()
            .filter(n -> n.getSentiment().equals("POSITIVE") && n.isAuthentic()).count();
        long negNews = response.getCompanyNews().stream()
            .filter(n -> n.getSentiment().equals("NEGATIVE")).count();
        if (posNews > negNews) bullishPoints++;
        else if (negNews > posNews) bearishPoints++;

        String sentiment;
        String recommendation;

        if (bullishPoints >= bearishPoints + 3) {
            sentiment = "BULLISH";
            recommendation = "BUY";
        } else if (bearishPoints >= bullishPoints + 3) {
            sentiment = "BEARISH";
            recommendation = "SELL / AVOID";
        } else if (bullishPoints > bearishPoints) {
            sentiment = "MILDLY BULLISH";
            recommendation = "ACCUMULATE ON DIPS";
        } else if (bearishPoints > bullishPoints) {
            sentiment = "MILDLY BEARISH";
            recommendation = "HOLD WITH CAUTION";
        } else {
            sentiment = "NEUTRAL";
            recommendation = "HOLD / WATCH";
        }

        response.setOverallSentiment(sentiment);
        response.setOverallRecommendation(recommendation);

        response.setAnalysisJustification(String.format(
            "Overall assessment: %s | Recommendation: %s. " +
            "Scored %d bullish points vs %d bearish points across technical, fundamental, news, shareholding, and manipulation dimensions. " +
            "Technical analysis is %s. Fundamentals show %s valuation with %.1f%% ROE. " +
            "Manipulation risk is %s. Shareholding pattern risk is %s. " +
            "This analysis is based on mock/simulated data for demonstration purposes. " +
            "Always consult SEBI-registered advisors before making investment decisions. " +
            "Past performance does not guarantee future results. Stock investments are subject to market risks.",
            sentiment, recommendation, bullishPoints, bearishPoints,
            ta.getOverallTechnicalSignal(),
            fa.getValuationStatus(), fa.getRoe(),
            ma.getManipulationRiskLevel(),
            sp.getRiskLevel()));
    }
}
