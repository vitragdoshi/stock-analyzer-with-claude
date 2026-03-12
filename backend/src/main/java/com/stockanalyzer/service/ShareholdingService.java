package com.stockanalyzer.service;

import com.stockanalyzer.dto.StockAnalysisResponse.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ShareholdingService {

    public ShareholdingPattern analyze(String symbol, Random rng) {
        ShareholdingPattern pattern = new ShareholdingPattern();

        // Current holdings
        double promoter = 40 + rng.nextDouble() * 35;
        double fii = 10 + rng.nextDouble() * 25;
        double dii = 8 + rng.nextDouble() * 20;
        double pledged = rng.nextDouble() * 25;

        // Ensure sum doesn't exceed 100
        double total = promoter + fii + dii;
        if (total > 95) {
            double scale = 90.0 / total;
            promoter *= scale;
            fii *= scale;
            dii *= scale;
        }
        double publicHolding = 100.0 - promoter - fii - dii;

        pattern.setPromoterHolding(Math.round(promoter * 100.0) / 100.0);
        pattern.setFiiHolding(Math.round(fii * 100.0) / 100.0);
        pattern.setDiiHolding(Math.round(dii * 100.0) / 100.0);
        pattern.setPublicHolding(Math.round(publicHolding * 100.0) / 100.0);
        pattern.setPromoterPledged(Math.round(pledged * 100.0) / 100.0);

        // Trend over 6 quarters
        List<ShareholdingTrend> trend = new ArrayList<>();
        String[] quarters = {"Q2FY24", "Q3FY24", "Q4FY24", "Q1FY25", "Q2FY25", "Q3FY25"};
        double tPromoter = promoter - (rng.nextDouble() * 4 - 1);
        double tFii = fii - (rng.nextDouble() * 5 - 1);
        double tDii = dii - (rng.nextDouble() * 3 - 1);

        for (int i = 0; i < 6; i++) {
            ShareholdingTrend t = new ShareholdingTrend();
            t.setQuarter(quarters[i]);

            // Gradual change toward current
            double progress = (double) i / 5;
            t.setPromoter(Math.round((tPromoter + (promoter - tPromoter) * progress + (rng.nextDouble() - 0.5) * 0.8) * 100.0) / 100.0);
            t.setFii(Math.round((tFii + (fii - tFii) * progress + (rng.nextDouble() - 0.5) * 1.2) * 100.0) / 100.0);
            t.setDii(Math.round((tDii + (dii - tDii) * progress + (rng.nextDouble() - 0.5) * 0.8) * 100.0) / 100.0);
            t.setPublic_(Math.round((100 - t.getPromoter() - t.getFii() - t.getDii()) * 100.0) / 100.0);

            trend.add(t);
        }
        pattern.setTrend(trend);

        // Detect red flags
        List<String> redFlags = new ArrayList<>();

        if (pledged > 50) {
            redFlags.add(String.format("HIGH ALERT: %.1f%% of promoter holding is pledged — severe financial distress risk. " +
                "If stock falls, margin calls could trigger forced selling.", pledged));
        } else if (pledged > 20) {
            redFlags.add(String.format("WARNING: %.1f%% promoter pledge is elevated. Monitor for changes each quarter.", pledged));
        }

        if (promoter < 35) {
            redFlags.add(String.format("Low promoter holding (%.1f%%) below 35%% threshold — " +
                "reduced skin in the game may indicate lack of confidence or gradual exit.", promoter));
        }

        // Check for promoter reduction trend
        double firstPromoter = trend.get(0).getPromoter();
        double lastPromoter = trend.get(5).getPromoter();
        if (firstPromoter - lastPromoter > 3) {
            redFlags.add(String.format("Promoter stake reduced by %.1f%% over 6 quarters (%.1f%% → %.1f%%). " +
                "Steady promoter reduction is often a bearish signal.", firstPromoter - lastPromoter, firstPromoter, lastPromoter));
        }

        // Check FII trend
        double firstFii = trend.get(0).getFii();
        double lastFii = trend.get(5).getFii();
        if (lastFii - firstFii > 3) {
            // Green flag - FII buying
        } else if (firstFii - lastFii > 3) {
            redFlags.add(String.format("FIIs have been net sellers, reducing stake by %.1f%% over 6 quarters. " +
                "Foreign institutional exit may signal concern about growth trajectory or governance.", firstFii - lastFii));
        }

        // Check for unusual public holding increase (could indicate operator activity)
        double firstPublic = trend.get(0).getPublic_();
        double lastPublic = trend.get(5).getPublic_();
        if (lastPublic - firstPublic > 5) {
            redFlags.add(String.format("Public holding increased sharply by %.1f%% while institutional holding fell. " +
                "Retail investor accumulation without institutional support can signal speculative activity.", lastPublic - firstPublic));
        }

        if (redFlags.isEmpty()) {
            redFlags.add("No significant red flags in shareholding pattern");
        }
        pattern.setRedFlags(redFlags);

        // Risk level
        String riskLevel;
        if (pledged > 50 || promoter < 30 || (firstPromoter - lastPromoter > 5)) {
            riskLevel = "HIGH";
        } else if (pledged > 20 || promoter < 40 || (firstPromoter - lastPromoter > 2)) {
            riskLevel = "MEDIUM";
        } else {
            riskLevel = "LOW";
        }
        pattern.setRiskLevel(riskLevel);

        // Analysis narrative
        pattern.setAnalysis(buildAnalysis(pattern, trend, firstPromoter, lastPromoter, firstFii, lastFii));

        return pattern;
    }

    private String buildAnalysis(ShareholdingPattern p, List<ShareholdingTrend> trend,
                                   double firstPromoter, double lastPromoter,
                                   double firstFii, double lastFii) {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("Promoter holding stands at %.1f%% (%.1f%% pledged). ", p.getPromoterHolding(), p.getPromoterPledged()));

        if (p.getPromoterPledged() < 10) {
            sb.append("Negligible pledge is a positive signal — promoters are not financially stressed. ");
        } else if (p.getPromoterPledged() < 25) {
            sb.append("Moderate pledge levels — monitor closely for any increase. ");
        } else {
            sb.append("High pledge levels are a CONCERN — forced selling risk in case of stock price decline. ");
        }

        if (lastPromoter > firstPromoter) {
            sb.append(String.format("Promoters increased their stake by %.1f%% over last 6 quarters — strong confidence signal. ",
                lastPromoter - firstPromoter));
        } else if (firstPromoter - lastPromoter > 2) {
            sb.append(String.format("Promoter stake declined %.1f%% over last 6 quarters — warrants monitoring. ",
                firstPromoter - lastPromoter));
        }

        if (lastFii > firstFii) {
            sb.append(String.format("FIIs have been NET BUYERS (+%.1f%% over 6 quarters), indicating positive foreign institutional sentiment. ",
                lastFii - firstFii));
        } else if (firstFii - lastFii > 2) {
            sb.append(String.format("FIIs have been NET SELLERS (-%.1f%% over 6 quarters). ", firstFii - lastFii));
        }

        sb.append(String.format("DII holding at %.1f%% %s. ",
            p.getDiiHolding(),
            p.getDiiHolding() > 15 ? "shows strong domestic institutional support" : "indicates limited domestic institutional interest"));

        sb.append(String.format("Overall shareholding risk is assessed as %s.", p.getRiskLevel()));

        return sb.toString();
    }
}
