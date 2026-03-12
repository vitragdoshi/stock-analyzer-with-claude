package com.stockanalyzer.service;

import com.stockanalyzer.dto.StockAnalysisResponse.TechnicalAnalysis;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class TechnicalAnalysisService {

    public TechnicalAnalysis analyze(String symbol, double currentPrice, double[] prices, Random rng) {
        TechnicalAnalysis ta = new TechnicalAnalysis();

        // RSI calculation (mock)
        double rsi = 40 + rng.nextDouble() * 40;  // 40-80 range
        ta.setRsi(Math.round(rsi * 100.0) / 100.0);

        if (rsi > 70) {
            ta.setRsiSignal("OVERBOUGHT");
            ta.setRsiJustification(String.format(
                "RSI of %.1f is above 70, indicating the stock is in overbought territory. " +
                "This suggests the recent rally may be overextended and a correction or consolidation is possible. " +
                "Caution advised for new entries at current levels.", rsi));
        } else if (rsi < 30) {
            ta.setRsiSignal("OVERSOLD");
            ta.setRsiJustification(String.format(
                "RSI of %.1f is below 30, indicating oversold conditions. " +
                "This may represent a buying opportunity as the stock could be due for a bounce. " +
                "However, confirm with other indicators before entering.", rsi));
        } else if (rsi > 55) {
            ta.setRsiSignal("BULLISH");
            ta.setRsiJustification(String.format(
                "RSI of %.1f is in bullish territory (50-70 range). " +
                "Momentum is positive without being overextended. " +
                "The stock has room to run before reaching overbought levels.", rsi));
        } else {
            ta.setRsiSignal("NEUTRAL");
            ta.setRsiJustification(String.format(
                "RSI of %.1f indicates neutral momentum (40-55 range). " +
                "No strong directional bias from this indicator alone. " +
                "Watch for a breakout above 60 for bullish confirmation.", rsi));
        }

        // MACD (mock values based on price)
        double macd = (rng.nextDouble() - 0.45) * currentPrice * 0.02;
        double macdSignal = macd - (rng.nextDouble() - 0.45) * currentPrice * 0.005;
        double macdHistogram = macd - macdSignal;

        ta.setMacd(Math.round(macd * 100.0) / 100.0);
        ta.setMacdSignal(Math.round(macdSignal * 100.0) / 100.0);
        ta.setMacdHistogram(Math.round(macdHistogram * 100.0) / 100.0);

        if (macd > macdSignal && macdHistogram > 0) {
            ta.setMacdTrend("BULLISH CROSSOVER");
            ta.setMacdJustification("MACD line is above the signal line with a positive histogram, " +
                "confirming bullish momentum. The crossover suggests increasing buying pressure. " +
                "This is a classic buy signal in trending markets.");
        } else if (macd < macdSignal && macdHistogram < 0) {
            ta.setMacdTrend("BEARISH CROSSOVER");
            ta.setMacdJustification("MACD line has crossed below the signal line with a negative histogram, " +
                "indicating bearish momentum. Selling pressure is increasing. " +
                "Consider reducing exposure or setting stop-losses.");
        } else {
            ta.setMacdTrend("NEUTRAL");
            ta.setMacdJustification("MACD is near the signal line with minimal histogram divergence. " +
                "No strong directional signal currently. Monitor for a decisive crossover.");
        }

        // Moving Averages
        double sma20 = currentPrice * (0.97 + rng.nextDouble() * 0.06);
        double sma50 = currentPrice * (0.93 + rng.nextDouble() * 0.08);
        double sma200 = currentPrice * (0.85 + rng.nextDouble() * 0.2);

        ta.setSma20(Math.round(sma20 * 100.0) / 100.0);
        ta.setSma50(Math.round(sma50 * 100.0) / 100.0);
        ta.setSma200(Math.round(sma200 * 100.0) / 100.0);

        if (currentPrice > sma20 && sma20 > sma50 && sma50 > sma200) {
            ta.setMovingAvgSignal("STRONG UPTREND");
            ta.setMovingAvgJustification(String.format(
                "Price (₹%.2f) is above SMA20 (₹%.2f) > SMA50 (₹%.2f) > SMA200 (₹%.2f). " +
                "This perfect bullish alignment across all timeframes indicates a strong uptrend. " +
                "The stock is in a healthy trending phase with institutional support.",
                currentPrice, sma20, sma50, sma200));
        } else if (currentPrice > sma50 && currentPrice > sma200) {
            ta.setMovingAvgSignal("BULLISH");
            ta.setMovingAvgJustification(String.format(
                "Price (₹%.2f) is above both SMA50 (₹%.2f) and SMA200 (₹%.2f), confirming a bullish bias. " +
                "The stock is in an uptrend on medium and long-term timeframes.",
                currentPrice, sma50, sma200));
        } else if (currentPrice < sma20 && currentPrice < sma50) {
            ta.setMovingAvgSignal("BEARISH");
            ta.setMovingAvgJustification(String.format(
                "Price (₹%.2f) is below SMA20 (₹%.2f) and SMA50 (₹%.2f). " +
                "Short-term trend is bearish. Watch the SMA200 (₹%.2f) as key support.",
                currentPrice, sma20, sma50, sma200));
        } else {
            ta.setMovingAvgSignal("MIXED");
            ta.setMovingAvgJustification("Moving averages are in a mixed configuration. " +
                "The stock may be in a consolidation or transition phase. " +
                "Wait for clearer alignment before taking directional bets.");
        }

        // Bollinger Bands
        double stdDev = currentPrice * 0.025;
        double bbMiddle = sma20;
        double bbUpper = bbMiddle + 2 * stdDev;
        double bbLower = bbMiddle - 2 * stdDev;

        ta.setBollingerUpper(Math.round(bbUpper * 100.0) / 100.0);
        ta.setBollingerMiddle(Math.round(bbMiddle * 100.0) / 100.0);
        ta.setBollingerLower(Math.round(bbLower * 100.0) / 100.0);

        if (currentPrice > bbUpper) {
            ta.setBollingerSignal("PRICE ABOVE UPPER BAND - Potential reversal or strong breakout");
        } else if (currentPrice < bbLower) {
            ta.setBollingerSignal("PRICE BELOW LOWER BAND - Oversold, potential bounce");
        } else if (currentPrice > bbMiddle) {
            ta.setBollingerSignal("PRICE IN UPPER HALF - Bullish bias within bands");
        } else {
            ta.setBollingerSignal("PRICE IN LOWER HALF - Bearish bias within bands");
        }

        // ATR (Average True Range) - volatility measure
        double atr = currentPrice * (0.015 + rng.nextDouble() * 0.025);
        ta.setAtr(Math.round(atr * 100.0) / 100.0);

        // OBV trend
        double obv = rng.nextDouble() * 1000000000;
        ta.setObv(Math.round(obv));
        ta.setObvTrend(rng.nextBoolean() ? "RISING - Accumulation in progress" : "DECLINING - Distribution phase");

        // Stochastic
        double stochK = 20 + rng.nextDouble() * 60;
        double stochD = stochK - (rng.nextDouble() - 0.5) * 10;

        ta.setStochasticK(Math.round(stochK * 100.0) / 100.0);
        ta.setStochasticD(Math.round(stochD * 100.0) / 100.0);

        if (stochK > 80) {
            ta.setStochasticSignal("OVERBOUGHT");
        } else if (stochK < 20) {
            ta.setStochasticSignal("OVERSOLD");
        } else if (stochK > stochD) {
            ta.setStochasticSignal("BULLISH");
        } else {
            ta.setStochasticSignal("BEARISH");
        }

        // Overall Technical Signal
        int bullishCount = 0;
        int bearishCount = 0;

        if (ta.getRsiSignal().contains("BULLISH") || ta.getRsiSignal().equals("OVERSOLD")) bullishCount++;
        if (ta.getRsiSignal().contains("BEARISH") || ta.getRsiSignal().equals("OVERBOUGHT")) bearishCount++;
        if (ta.getMacdTrend().contains("BULLISH")) bullishCount++;
        if (ta.getMacdTrend().contains("BEARISH")) bearishCount++;
        if (ta.getMovingAvgSignal().contains("BULLISH") || ta.getMovingAvgSignal().contains("UPTREND")) bullishCount++;
        if (ta.getMovingAvgSignal().contains("BEARISH")) bearishCount++;
        if (ta.getStochasticSignal().equals("BULLISH") || ta.getStochasticSignal().equals("OVERSOLD")) bullishCount++;
        if (ta.getStochasticSignal().equals("BEARISH") || ta.getStochasticSignal().equals("OVERBOUGHT")) bearishCount++;

        if (bullishCount >= 3) {
            ta.setOverallTechnicalSignal("BULLISH");
            ta.setTechnicalSummary(String.format(
                "Technical indicators are predominantly bullish (%d bullish vs %d bearish signals). " +
                "The stock is showing positive momentum with RSI, MACD, and moving averages mostly aligned to the upside. " +
                "Key resistance: ₹%.2f (Upper BB). Key support: ₹%.2f (SMA50).",
                bullishCount, bearishCount, bbUpper, sma50));
        } else if (bearishCount >= 3) {
            ta.setOverallTechnicalSignal("BEARISH");
            ta.setTechnicalSummary(String.format(
                "Technical indicators are predominantly bearish (%d bearish vs %d bullish signals). " +
                "Momentum indicators suggest selling pressure. " +
                "Key support to watch: ₹%.2f (SMA200). Resistance: ₹%.2f (SMA50).",
                bearishCount, bullishCount, sma200, sma50));
        } else {
            ta.setOverallTechnicalSignal("NEUTRAL");
            ta.setTechnicalSummary(String.format(
                "Mixed technical signals (%d bullish, %d bearish). " +
                "The stock is in a consolidation phase. " +
                "Wait for a decisive move above ₹%.2f (SMA20) or below ₹%.2f (Lower BB) for direction.",
                bullishCount, bearishCount, sma20, bbLower));
        }

        return ta;
    }
}
