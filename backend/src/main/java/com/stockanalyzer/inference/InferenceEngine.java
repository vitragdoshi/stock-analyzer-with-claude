package com.stockanalyzer.inference;

import com.stockanalyzer.client.NewsAggregatorClient;
import com.stockanalyzer.client.YahooFinanceClient;
import com.stockanalyzer.model.*;
import com.stockanalyzer.service.MockDataService;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Core inference engine: pulls real market data from multiple public sources,
 * applies a multi-factor weighted scoring model, and produces actionable
 * stock recommendations with explicit reasoning.
 *
 * ──────────────────────────────────────────────────────
 * Scoring weights (sum to 1.0):
 *   Technical   0.20  – RSI, MACD, MA crossovers, Bollinger, ATR, OBV
 *   Fundamental 0.25  – P/E, ROE, D/E, margins, growth, FCF
 *   Valuation   0.15  – price vs analyst targets, intrinsic value
 *   Analyst     0.15  – buy/hold/sell consensus + upgrades/downgrades
 *   Earnings    0.10  – beat rate, surprise %, forward estimates
 *   Sentiment   0.10  – weighted news sentiment (credibility × recency)
 *   Momentum    0.05  – 1M / 3M / 6M price return
 * ──────────────────────────────────────────────────────
 *
 * Recommendation bands (composite 0–100):
 *   85–100  STRONG BUY
 *   70–84   BUY
 *   55–69   ACCUMULATE
 *   45–54   HOLD
 *   30–44   REDUCE
 *   0–29    SELL
 */
@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InferenceEngine {

    // ── Weights ───────────────────────────────────────────────────────────
    static final double W_TECHNICAL   = 0.20;
    static final double W_FUNDAMENTAL = 0.25;
    static final double W_VALUATION   = 0.15;
    static final double W_ANALYST     = 0.15;
    static final double W_EARNINGS    = 0.10;
    static final double W_SENTIMENT   = 0.10;
    static final double W_MOMENTUM    = 0.05;

    // Industry P/E benchmarks for valuation scoring
    static final Map<String, Double> INDUSTRY_PE = Map.of(
            "IT",       28.5,
            "Banking",  18.0,
            "NBFC",     28.0,
            "Pharma",   30.0,
            "Consumer", 45.0,
            "Auto",     20.0,
            "Energy",   14.0,
            "Telecom",  35.0
    );

    @NonNull YahooFinanceClient   yahooClient;
    @NonNull NewsAggregatorClient newsClient;
    @NonNull TechnicalCalculator  techCalc;
    @NonNull SentimentAnalyzer    sentimentAnalyzer;
    @NonNull MockDataService      mockDataService;

    // ── Main entry point ──────────────────────────────────────────────────

    public ScoreCard analyse(String symbol) {
        String sym = symbol.toUpperCase().trim();
        String companyName = mockDataService.getCompanyName(sym);
        String sector      = mockDataService.getSector(sym);

        ScoreCard card = new ScoreCard();
        int dataSourcesUsed = 0;

        // ── 1. Live price data ────────────────────────────────────────────
        QuoteData quote = null;
        try { quote = yahooClient.getQuote(sym); } catch (Exception e) {
            log.warn("Quote fetch failed: {}", e.getMessage());
        }
        double currentPrice = (quote != null && quote.isRealData())
                ? quote.getCurrentPrice() : mockDataService.getPrice(sym);
        card.setUsedRealPriceData(quote != null && quote.isRealData());

        // ── 2. Historical OHLCV ───────────────────────────────────────────
        List<HistoricalPrice> history = null;
        try { history = yahooClient.getHistoricalPrices(sym, "1y"); } catch (Exception e) {
            log.warn("Historical fetch failed: {}", e.getMessage());
        }
        if (history == null) history = List.of();

        // ── 3. Fundamentals ───────────────────────────────────────────────
        FundamentalData fundamentals = null;
        try { fundamentals = yahooClient.getFundamentals(sym); } catch (Exception e) {
            log.warn("Fundamentals fetch failed: {}", e.getMessage());
        }
        card.setUsedRealFundamentals(fundamentals != null && fundamentals.isRealData());
        if (fundamentals != null) dataSourcesUsed++;

        // ── 4. Analyst consensus ──────────────────────────────────────────
        AnalystConsensus analyst = null;
        try { analyst = yahooClient.getAnalystConsensus(sym); } catch (Exception e) {
            log.warn("Analyst fetch failed: {}", e.getMessage());
        }
        card.setUsedRealAnalystData(analyst != null && analyst.isRealData());
        if (analyst != null) dataSourcesUsed++;

        // ── 5. Earnings data ──────────────────────────────────────────────
        EarningsData earnings = null;
        try { earnings = yahooClient.getEarningsData(sym); } catch (Exception e) {
            log.warn("Earnings fetch failed: {}", e.getMessage());
        }
        if (earnings != null) dataSourcesUsed++;

        // ── 6. News & sentiment ───────────────────────────────────────────
        List<NewsArticle> news = new ArrayList<>();
        try {
            news = newsClient.fetchNews(sym, companyName, 25);
            sentimentAnalyzer.annotate(news);
            card.setUsedRealNews(!news.isEmpty());
        } catch (Exception e) {
            log.warn("News fetch failed: {}", e.getMessage());
        }
        if (!news.isEmpty()) dataSourcesUsed++;

        // ── Score each dimension ──────────────────────────────────────────
        double techScore     = scoreTechnical(history, quote, card);
        double fundScore     = scoreFundamental(fundamentals, sector, currentPrice, card);
        double valuationScore = scoreValuation(analyst, fundamentals, currentPrice, card);
        double analystScr    = scoreAnalyst(analyst, card);
        double earningsScr   = scoreEarnings(earnings, card);
        double sentimentScr  = scoreSentiment(news, card);
        double momentumScr   = scoreMomentum(history, card);

        card.setTechnicalScore(techScore);
        card.setFundamentalScore(fundScore);
        card.setValuationScore(valuationScore);
        card.setAnalystScore(analystScr);
        card.setEarningsScore(earningsScr);
        card.setSentimentScore(sentimentScr);
        card.setMomentumScore(momentumScr);

        // ── Weighted composite ────────────────────────────────────────────
        double composite = techScore     * W_TECHNICAL
                + fundScore     * W_FUNDAMENTAL
                + valuationScore * W_VALUATION
                + analystScr    * W_ANALYST
                + earningsScr   * W_EARNINGS
                + sentimentScr  * W_SENTIMENT
                + momentumScr   * W_MOMENTUM;
        composite = clamp(composite, 0, 100);
        card.setCompositeScore(composite);

        // ── Recommendation ────────────────────────────────────────────────
        card.setRecommendation(toRecommendation(composite));
        card.setSentiment(toSentiment(composite));

        // ── Confidence = f(data completeness) ────────────────────────────
        double confidence = 40 + (dataSourcesUsed * 12.5); // 40 base + 12.5 per source (max 4)
        if (!history.isEmpty()) confidence += 10;
        card.setConfidencePercent(clamp(confidence, 40, 95));

        // ── Target prices ─────────────────────────────────────────────────
        setTargetPrices(card, analyst, currentPrice, composite);

        log.info("Inference complete for {} | composite={:.1f} | rec={}",
                sym, composite, card.getRecommendation());
        return card;
    }

    // ── Technical scoring (0–100) ─────────────────────────────────────────

    private double scoreTechnical(List<HistoricalPrice> bars, QuoteData quote, ScoreCard card) {
        if (bars.isEmpty()) {
            card.getBearishFactors().add("Insufficient price history for technical analysis.");
            return 50; // neutral
        }

        double score = 50; // start neutral

        // RSI
        double rsi = techCalc.rsi(bars, 14);
        if (!Double.isNaN(rsi)) {
            if (rsi < 30) {
                score += 10;
                card.getBullishFactors().add(String.format("RSI oversold at %.1f – potential reversal zone.", rsi));
            } else if (rsi < 50) {
                score += 5;
            } else if (rsi < 70) {
                score += 12;
                card.getBullishFactors().add(String.format("RSI bullish at %.1f – healthy momentum.", rsi));
            } else {
                score -= 8;
                card.getBearishFactors().add(String.format("RSI overbought at %.1f – short-term caution.", rsi));
            }
        }

        // MACD
        double[] macdArr = techCalc.macd(bars);
        double macdLine = macdArr[0], signal = macdArr[1], hist = macdArr[2];
        if (macdLine > signal) {
            score += 12;
            card.getBullishFactors().add(String.format("MACD bullish crossover (MACD %.2f > Signal %.2f).", macdLine, signal));
        } else {
            score -= 10;
            card.getBearishFactors().add(String.format("MACD bearish (MACD %.2f < Signal %.2f).", macdLine, signal));
        }

        // Moving averages
        double sma50  = techCalc.sma(bars, 50);
        double sma200 = techCalc.sma(bars, 200);
        double cp     = bars.get(bars.size() - 1).getClose();
        if (!Double.isNaN(sma200)) {
            if (cp > sma200) {
                score += 10;
                card.getBullishFactors().add(String.format("Price above 200-DMA (₹%.0f > ₹%.0f) – long-term uptrend.", cp, sma200));
            } else {
                score -= 10;
                card.getBearishFactors().add(String.format("Price below 200-DMA (₹%.0f < ₹%.0f) – bearish structure.", cp, sma200));
            }
        }
        if (!Double.isNaN(sma50) && !Double.isNaN(sma200)) {
            if (sma50 > sma200) {
                score += 8;
                card.getBullishFactors().add("Golden cross: 50-DMA above 200-DMA.");
            } else {
                score -= 6;
                card.getBearishFactors().add("Death cross: 50-DMA below 200-DMA.");
            }
        }

        // Bollinger Bands
        double[] bb = techCalc.bollingerBands(bars, 20, 2.0);
        if (!Double.isNaN(bb[0])) {
            if (cp < bb[2]) {
                score += 8;
                card.getBullishFactors().add(String.format("Price near lower Bollinger Band (₹%.0f) – potential bounce.", bb[2]));
            } else if (cp > bb[0]) {
                score -= 6;
                card.getBearishFactors().add(String.format("Price near upper Bollinger Band (₹%.0f) – stretched.", bb[0]));
            }
        }

        // Volume
        double volRatio = techCalc.volumeRatio(bars);
        if (volRatio > 1.5 && cp > bars.get(bars.size() - 2).getClose()) {
            score += 5;
            card.getBullishFactors().add(String.format("High volume breakout (%.1fx average) with price gain.", volRatio));
        } else if (volRatio > 2.0 && cp < bars.get(bars.size() - 2).getClose()) {
            score -= 5;
            card.getBearishFactors().add(String.format("High volume sell-off (%.1fx average).", volRatio));
        }

        return clamp(score, 0, 100);
    }

    // ── Fundamental scoring (0–100) ───────────────────────────────────────

    private double scoreFundamental(FundamentalData fd, String sector,
                                    double price, ScoreCard card) {
        if (fd == null || !fd.isRealData()) {
            return 50; // neutral when no real data
        }
        double score = 50;

        // P/E vs industry
        double industryPE = INDUSTRY_PE.getOrDefault(sector, 25.0);
        double pe = fd.getPeRatioTTM();
        if (pe > 0) {
            double ratio = pe / industryPE;
            if (ratio < 0.8) {
                score += 15;
                card.getBullishFactors().add(String.format(
                        "P/E %.1fx is %.0f%% below industry avg %.1fx – potentially undervalued.", pe, (1-ratio)*100, industryPE));
            } else if (ratio < 1.1) {
                score += 8;
            } else if (ratio > 1.5) {
                score -= 12;
                card.getBearishFactors().add(String.format(
                        "P/E %.1fx is %.0f%% above industry avg %.1fx – rich valuation.", pe, (ratio-1)*100, industryPE));
            }
        }

        // ROE
        double roe = fd.getReturnOnEquity();
        if (roe > 25) {
            score += 12;
            card.getBullishFactors().add(String.format("Strong ROE of %.1f%% – exceptional capital efficiency.", roe));
        } else if (roe > 15) {
            score += 7;
            card.getBullishFactors().add(String.format("Healthy ROE of %.1f%%.", roe));
        } else if (roe < 8) {
            score -= 8;
            card.getBearishFactors().add(String.format("Weak ROE of %.1f%% – poor capital deployment.", roe));
        }

        // Debt / Equity
        double de = fd.getDebtToEquity();
        if (de < 0.5) {
            score += 10;
            card.getBullishFactors().add(String.format("Low D/E ratio of %.2fx – conservatively leveraged.", de));
        } else if (de > 1.5) {
            score -= 10;
            card.getBearishFactors().add(String.format("High D/E ratio of %.2fx – elevated financial risk.", de));
            card.getRiskFactors().add("High leverage may amplify downside in a rate-rising environment.");
        }

        // Revenue growth
        double revGrowth = fd.getRevenueGrowth();
        if (revGrowth > 20) {
            score += 12;
            card.getBullishFactors().add(String.format("Strong revenue growth of %.1f%% YoY.", revGrowth));
        } else if (revGrowth > 10) {
            score += 7;
        } else if (revGrowth < 0) {
            score -= 12;
            card.getBearishFactors().add(String.format("Revenue contracting %.1f%% YoY.", revGrowth));
        }

        // Profit margins
        double pm = fd.getProfitMargins();
        if (pm > 20) { score += 8; card.getBullishFactors().add(String.format("High profit margin of %.1f%%.", pm)); }
        else if (pm < 5) { score -= 6; card.getBearishFactors().add(String.format("Thin profit margin of %.1f%%.", pm)); }

        // Free cash flow
        if (fd.getFreeCashflowCr() > 0) {
            score += 8;
            card.getBullishFactors().add(String.format("Positive FCF of ₹%.0f Cr – strong cash generation.", fd.getFreeCashflowCr()));
        } else if (fd.getFreeCashflowCr() < 0) {
            score -= 8;
            card.getBearishFactors().add("Negative free cash flow – company burning cash.");
        }

        // EPS growth
        double epsGrowth = fd.getEarningsQuarterlyGrowth();
        if (epsGrowth > 20) {
            score += 8;
            card.getBullishFactors().add(String.format("Quarterly EPS grew %.1f%% YoY.", epsGrowth));
        } else if (epsGrowth < -10) {
            score -= 8;
            card.getBearishFactors().add(String.format("EPS declined %.1f%% YoY.", Math.abs(epsGrowth)));
        }

        return clamp(score, 0, 100);
    }

    // ── Valuation scoring (0–100) ─────────────────────────────────────────

    private double scoreValuation(AnalystConsensus analyst, FundamentalData fd,
                                  double price, ScoreCard card) {
        double score = 50;
        if (analyst != null && analyst.getTargetMean() > 0 && price > 0) {
            double upside = analyst.getUpsidePotentialPercent();
            if (upside > 30) {
                score += 25;
                card.getBullishFactors().add(String.format(
                        "Analyst mean target ₹%.0f implies %.1f%% upside from current ₹%.0f.",
                        analyst.getTargetMean(), upside, price));
            } else if (upside > 15) {
                score += 15;
                card.getBullishFactors().add(String.format(
                        "Analyst target ₹%.0f implies %.1f%% upside.", analyst.getTargetMean(), upside));
            } else if (upside > 5) {
                score += 8;
            } else if (upside < -10) {
                score -= 15;
                card.getBearishFactors().add(String.format(
                        "Price ₹%.0f already %.1f%% above analyst mean target ₹%.0f.",
                        price, -upside, analyst.getTargetMean()));
            }
        }

        // P/B ratio
        if (fd != null && fd.getPbRatio() > 0) {
            double pb = fd.getPbRatio();
            if (pb < 1.0) {
                score += 12;
                card.getBullishFactors().add(String.format("P/B ratio %.2fx < 1 – trading below book value.", pb));
            } else if (pb > 5.0) {
                score -= 8;
                card.getBearishFactors().add(String.format("Premium P/B ratio of %.2fx – price heavily premium to assets.", pb));
            }
        }

        // Forward P/E vs trailing P/E (earnings acceleration)
        if (fd != null && fd.getPeRatioForward() > 0 && fd.getPeRatioTTM() > 0) {
            if (fd.getPeRatioForward() < fd.getPeRatioTTM() * 0.85) {
                score += 8;
                card.getBullishFactors().add(String.format(
                        "Forward P/E %.1fx meaningfully lower than trailing %.1fx – earnings expansion expected.",
                        fd.getPeRatioForward(), fd.getPeRatioTTM()));
            }
        }

        return clamp(score, 0, 100);
    }

    // ── Analyst scoring (0–100) ───────────────────────────────────────────

    private double scoreAnalyst(AnalystConsensus ac, ScoreCard card) {
        if (ac == null || ac.getTotalAnalysts() == 0) return 50;
        // Yahoo Finance scale: 1=strong buy, 5=strong sell → invert to 0–100
        double raw = ac.getConsensusScore(); // 1.0 – 5.0
        double score = (5.0 - raw) / 4.0 * 100; // maps 1→100, 3→50, 5→0

        card.getBullishFactors().add(String.format(
                "Analyst consensus: %d Strong Buy, %d Buy, %d Hold, %d Sell across %d analysts.",
                ac.getStrongBuyCount(), ac.getBuyCount(), ac.getHoldCount(),
                ac.getSellCount() + ac.getStrongSellCount(), ac.getTotalAnalysts()));

        // Recent upgrades/downgrades
        if (ac.getRecentUpgrades() != null) {
            long upgrades   = ac.getRecentUpgrades().stream().filter(e -> "up".equals(e.getAction())).count();
            long downgrades = ac.getRecentUpgrades().stream().filter(e -> "down".equals(e.getAction())).count();
            if (upgrades > downgrades) {
                score += 8;
                card.getBullishFactors().add(String.format("%d analyst upgrade(s) vs %d downgrade(s) recently.", upgrades, downgrades));
            } else if (downgrades > upgrades) {
                score -= 8;
                card.getBearishFactors().add(String.format("%d analyst downgrade(s) recently – institutional caution.", downgrades));
            }
        }
        return clamp(score, 0, 100);
    }

    // ── Earnings scoring (0–100) ──────────────────────────────────────────

    private double scoreEarnings(EarningsData ed, ScoreCard card) {
        if (ed == null) return 50;
        double score = 50;

        // Beat rate
        double beatRate = ed.getBeatRatePercent();
        if (beatRate >= 75) {
            score += 20;
            card.getBullishFactors().add(String.format("Strong earnings track record: beat estimates %.0f%% of quarters.", beatRate));
        } else if (beatRate >= 50) {
            score += 8;
        } else {
            score -= 10;
            card.getBearishFactors().add(String.format("Frequently misses earnings estimates (beat rate %.0f%%).", beatRate));
        }

        // Average surprise
        double avgSurp = ed.getAverageSurprise();
        if (avgSurp > 10) {
            score += 12;
            card.getBullishFactors().add(String.format("Average EPS surprise of +%.1f%% – consistently beating guidance.", avgSurp));
        } else if (avgSurp < -5) {
            score -= 10;
            card.getBearishFactors().add(String.format("Persistent EPS disappointment (avg surprise %.1f%%).", avgSurp));
        }

        // Momentum
        if ("ACCELERATING".equals(ed.getEarningsMomentum())) {
            score += 8;
            card.getBullishFactors().add("Earnings momentum accelerating – surprises improving quarter-over-quarter.");
        } else if ("DECELERATING".equals(ed.getEarningsMomentum())) {
            score -= 8;
            card.getBearishFactors().add("Earnings momentum decelerating – watch for guidance cut.");
        }

        // Forward EPS growth
        if (ed.getForwardTrends() != null && !ed.getForwardTrends().isEmpty()) {
            double fwdGrowth = ed.getForwardTrends().get(0).getEpsGrowthRate();
            if (fwdGrowth > 20) {
                score += 10;
                card.getBullishFactors().add(String.format("Forward EPS growth of %.1f%% – strong analyst expectations.", fwdGrowth));
            } else if (fwdGrowth < 0) {
                score -= 10;
                card.getBearishFactors().add(String.format("Forward EPS expected to contract %.1f%%.", Math.abs(fwdGrowth)));
            }
        }

        return clamp(score, 0, 100);
    }

    // ── Sentiment scoring (0–100) ─────────────────────────────────────────

    private double scoreSentiment(List<NewsArticle> news, ScoreCard card) {
        if (news.isEmpty()) return 50;
        double agg = sentimentAnalyzer.aggregateSentiment(news); // -1 to +1
        double score = (agg + 1.0) / 2.0 * 100;                 // map to 0–100
        long positiveCount = news.stream().filter(a -> "POSITIVE".equals(a.getSentimentLabel())).count();
        long negativeCount = news.stream().filter(a -> "NEGATIVE".equals(a.getSentimentLabel())).count();
        card.getBullishFactors().add(String.format(
                "News sentiment: %d positive vs %d negative articles out of %d scanned.",
                positiveCount, negativeCount, news.size()));
        if (agg < -0.3) {
            card.getBearishFactors().add("Strong negative news flow – market narrative is unfavourable.");
        }
        return clamp(score, 0, 100);
    }

    // ── Momentum scoring (0–100) ──────────────────────────────────────────

    private double scoreMomentum(List<HistoricalPrice> bars, ScoreCard card) {
        if (bars.size() < 22) return 50;
        double m1  = techCalc.momentum(bars, 21);   // 1 month
        double m3  = techCalc.momentum(bars, Math.min(63, bars.size() - 1));
        double m6  = techCalc.momentum(bars, Math.min(126, bars.size() - 1));

        double score = 50;
        score += clamp(m1 * 100, -15, 15);
        score += clamp(m3 * 60,  -15, 15);
        score += clamp(m6 * 30,  -10, 10);

        if (m1 > 0.05 && m3 > 0.10) {
            card.getBullishFactors().add(String.format(
                    "Positive price momentum: +%.1f%% (1M), +%.1f%% (3M).", m1*100, m3*100));
        } else if (m1 < -0.05 && m3 < -0.10) {
            card.getBearishFactors().add(String.format(
                    "Negative price momentum: %.1f%% (1M), %.1f%% (3M).", m1*100, m3*100));
        }
        return clamp(score, 0, 100);
    }

    // ── Target price ──────────────────────────────────────────────────────

    private void setTargetPrices(ScoreCard card, AnalystConsensus ac,
                                  double currentPrice, double composite) {
        if (ac != null && ac.getTargetMean() > 0) {
            card.setTargetPriceLow(ac.getTargetLow() > 0 ? ac.getTargetLow()
                    : currentPrice * 0.90);
            card.setTargetPriceMid(ac.getTargetMean());
            card.setTargetPriceHigh(ac.getTargetHigh() > 0 ? ac.getTargetHigh()
                    : currentPrice * 1.25);
        } else {
            // Estimate from composite score
            double upside = (composite - 50) / 100.0; // -0.5 to +0.5
            card.setTargetPriceLow(currentPrice * (1 + upside * 0.5));
            card.setTargetPriceMid(currentPrice * (1 + upside));
            card.setTargetPriceHigh(currentPrice * (1 + upside * 1.5));
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private String toRecommendation(double score) {
        if (score >= 85) return "STRONG BUY";
        if (score >= 70) return "BUY";
        if (score >= 55) return "ACCUMULATE";
        if (score >= 45) return "HOLD";
        if (score >= 30) return "REDUCE";
        return "SELL";
    }

    private String toSentiment(double score) {
        if (score >= 70) return "BULLISH";
        if (score >= 58) return "MILDLY BULLISH";
        if (score >= 42) return "NEUTRAL";
        if (score >= 30) return "MILDLY BEARISH";
        return "BEARISH";
    }

    private double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
