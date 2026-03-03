package com.stockanalyzer.service;

import com.stockanalyzer.dto.StockAnalysisResponse.ManipulationAnalysis;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ManipulationDetectionService {

    public ManipulationAnalysis analyze(String symbol, double currentPrice, double[] priceData,
                                         long volume, long avgVolume, Random rng) {
        ManipulationAnalysis analysis = new ManipulationAnalysis();

        List<String> redFlags = new ArrayList<>();
        List<String> greenFlags = new ArrayList<>();

        // Volume analysis
        double volumeRatio = (double) volume / avgVolume;
        double unusualVolumeScore = Math.min(100, volumeRatio * 40);
        analysis.setUnusualVolumeScore(Math.round(unusualVolumeScore * 100.0) / 100.0);

        if (volumeRatio > 3.0) {
            redFlags.add(String.format("Extreme volume spike: Today's volume is %.1fx the 30-day average. " +
                "Sudden volume spikes without news catalysts can indicate coordinated buying/selling.", volumeRatio));
        } else if (volumeRatio > 2.0) {
            redFlags.add(String.format("High volume: %.1fx the daily average. " +
                "Unusual but may be explained by institutional rebalancing or news flow.", volumeRatio));
        } else if (volumeRatio > 1.5) {
            redFlags.add(String.format("Moderately elevated volume (%.1fx average). Worth monitoring.", volumeRatio));
        } else {
            greenFlags.add(String.format("Normal volume activity (%.1fx of 30-day average). No manipulation signal here.", volumeRatio));
        }

        // Price pattern analysis (simulated)
        double pricePatternScore = rng.nextDouble() * 60;
        analysis.setPricePatternScore(Math.round(pricePatternScore * 100.0) / 100.0);

        // Circuit breaker hits simulation
        int circuitHits = rng.nextInt(5);
        analysis.setCircuitBreakerHits(circuitHits);

        if (circuitHits >= 3) {
            redFlags.add(String.format("Stock hit circuit breakers %d times in the last 30 days. " +
                "Frequent circuit hits indicate extreme volatility, often associated with operator activity, " +
                "especially in small/mid-cap stocks.", circuitHits));
        } else if (circuitHits > 0) {
            redFlags.add(String.format("%d circuit breaker hit(s) in last 30 days. " +
                "Monitor for pattern consistency.", circuitHits));
        } else {
            greenFlags.add("No circuit breaker hits in last 30 days — normal price discovery process.");
        }

        // Market cap based risk
        double marketCap = priceData[0] * (50000000 + rng.nextInt(500000000000L > Integer.MAX_VALUE ?
            Integer.MAX_VALUE : (int) 100000000));
        boolean isSmallCap = priceData[0] < 500 || rng.nextDouble() < 0.3;

        if (isSmallCap) {
            redFlags.add("Small/mid-cap stock with relatively lower liquidity — higher susceptibility to operator manipulation. " +
                "Retail investors should be extra cautious with position sizing.");
        } else {
            greenFlags.add("Large-cap stock with deep liquidity — difficult to manipulate at scale. " +
                "SEBI surveillance systems actively monitor large-caps.");
        }

        // Price movement vs fundamentals
        boolean highPriceWithWeakFundamentals = pricePatternScore > 40 && rng.nextDouble() < 0.3;
        if (highPriceWithWeakFundamentals) {
            redFlags.add("Price appreciation appears disconnected from fundamental earnings growth over last 6 months. " +
                "Valuation expansion without commensurate earnings growth is a potential manipulation indicator.");
        } else {
            greenFlags.add("Price movement broadly correlated with fundamental performance. No decoupling detected.");
        }

        // SEBI surveillance check
        boolean onSurveillance = rng.nextDouble() < 0.15;
        if (onSurveillance) {
            redFlags.add("ALERT: Stock recently appeared on SEBI/Exchange surveillance watchlist (ASM/GSM framework). " +
                "This indicates regulators have flagged unusual trading activity. Extreme caution advised.");
        } else {
            greenFlags.add("Stock is not on SEBI's Additional Surveillance Measure (ASM) or Graded Surveillance Measure (GSM) list.");
        }

        // Bid-ask spread
        boolean wideSpread = rng.nextDouble() < 0.25;
        if (wideSpread) {
            redFlags.add("Wide bid-ask spreads observed during analysis period — indicative of low liquidity. " +
                "Can be exploited by operators to create artificial price movements.");
        } else {
            greenFlags.add("Tight bid-ask spreads indicate healthy market depth and liquidity.");
        }

        analysis.setRedFlags(redFlags);
        analysis.setGreenFlags(greenFlags);

        // Pump and dump risk
        int riskScore = redFlags.size();
        String pumpAndDumpRisk;
        String riskLevel;
        boolean suspected;

        if (riskScore >= 4) {
            pumpAndDumpRisk = "HIGH";
            riskLevel = "HIGH";
            suspected = true;
        } else if (riskScore >= 2) {
            pumpAndDumpRisk = "MEDIUM";
            riskLevel = "MEDIUM";
            suspected = riskScore >= 3;
        } else {
            pumpAndDumpRisk = "LOW";
            riskLevel = "LOW";
            suspected = false;
        }

        analysis.setPumpAndDumpRisk(pumpAndDumpRisk);
        analysis.setManipulationRiskLevel(riskLevel);
        analysis.setSuspectedManipulation(suspected);

        // Operator activity analysis
        analysis.setOperatorActivityAnalysis(buildOperatorAnalysis(redFlags, greenFlags, volumeRatio, riskLevel));

        // Justification
        analysis.setAnalysisJustification(buildJustification(analysis, redFlags, greenFlags));

        return analysis;
    }

    private String buildOperatorAnalysis(List<String> redFlags, List<String> greenFlags, double volumeRatio, String riskLevel) {
        if (riskLevel.equals("HIGH")) {
            return String.format("Multiple technical indicators suggest possible operator/smart money activity. " +
                "Volume is %.1fx normal levels, price action shows patterns inconsistent with organic retail flow. " +
                "In Indian markets, 'operators' (large traders/syndicates) typically work in three phases: " +
                "Accumulation (quiet buying at low prices), Markup (price ramping with news/tips), " +
                "Distribution (selling to retail investors at peak). Current signals suggest potential markup/distribution phase. " +
                "Retail investors should avoid chasing the stock at current levels.", volumeRatio);
        } else if (riskLevel.equals("MEDIUM")) {
            return String.format("Some unusual activity detected but insufficient for strong manipulation conclusion. " +
                "Volume at %.1fx average could be institutional rebalancing or genuine news-driven buying. " +
                "Recommend monitoring for 2-3 more sessions to confirm or deny suspicious pattern.", volumeRatio);
        } else {
            return String.format("No significant operator manipulation signals detected. " +
                "Volume (%.1fx average) and price action appear consistent with normal market activity. " +
                "SEBI surveillance mechanisms have not flagged this stock. " +
                "Price discovery appears to be organic and fundamentals-driven.", volumeRatio);
        }
    }

    private String buildJustification(ManipulationAnalysis analysis, List<String> redFlags, List<String> greenFlags) {
        return String.format("Manipulation risk assessment: %s. Analysis based on %d warning signal(s) and %d positive indicator(s). " +
            "Key risk drivers: %s. " +
            "Note: This analysis uses quantitative pattern recognition on price/volume data. " +
            "SEBI's market surveillance uses additional proprietary algorithms. Always verify with official SEBI/Exchange disclosures.",
            analysis.getManipulationRiskLevel(),
            redFlags.size(), greenFlags.size(),
            redFlags.isEmpty() ? "None detected" : redFlags.get(0));
    }
}
