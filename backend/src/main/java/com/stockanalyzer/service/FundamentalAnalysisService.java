package com.stockanalyzer.service;

import com.stockanalyzer.dto.StockAnalysisResponse.FundamentalAnalysis;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class FundamentalAnalysisService {

    public FundamentalAnalysis analyze(String symbol, double currentPrice, String sector, Random rng) {
        FundamentalAnalysis fa = new FundamentalAnalysis();

        // Sector-specific P/E benchmarks
        double industryPe = getIndustryPe(sector);
        double peVariance = (rng.nextDouble() - 0.3) * 10;
        double pe = industryPe + peVariance;

        fa.setPe(Math.round(pe * 100.0) / 100.0);
        fa.setIndustryPe(industryPe);

        if (pe < industryPe * 0.85) {
            fa.setPeJustification(String.format(
                "P/E of %.1fx is %.0f%% below the industry average of %.1fx. " +
                "This suggests the stock may be undervalued relative to peers. " +
                "Possible reasons: market skepticism about growth prospects, short-term headwinds, or genuine value opportunity. " +
                "Further investigation into earnings quality and growth trajectory is warranted.",
                pe, ((industryPe - pe) / industryPe) * 100, industryPe));
        } else if (pe > industryPe * 1.25) {
            fa.setPeJustification(String.format(
                "P/E of %.1fx is %.0f%% above the industry average of %.1fx. " +
                "Market is pricing in premium growth expectations. " +
                "Justification requires strong revenue growth (>20%% YoY), high ROCE, and a strong competitive moat. " +
                "At current valuations, any earnings disappointment could trigger a sharp correction.",
                pe, ((pe - industryPe) / industryPe) * 100, industryPe));
        } else {
            fa.setPeJustification(String.format(
                "P/E of %.1fx is roughly in line with the industry average of %.1fx. " +
                "The stock is fairly valued from an earnings perspective. " +
                "Focus on earnings growth trajectory to determine if current valuation is justified.",
                pe, industryPe));
        }

        // Price-to-Book
        double pb = 1.5 + rng.nextDouble() * 8;
        fa.setPb(Math.round(pb * 100.0) / 100.0);

        // ROE and ROCE
        double roe = 10 + rng.nextDouble() * 30;
        double roce = roe - 2 + rng.nextDouble() * 5;
        fa.setRoe(Math.round(roe * 100.0) / 100.0);
        fa.setRoce(Math.round(roce * 100.0) / 100.0);

        // EPS (mock)
        double eps = currentPrice / pe;
        fa.setEps(Math.round(eps * 100.0) / 100.0);

        // Debt metrics
        double debtToEquity = rng.nextDouble() * 1.5;
        double currentRatio = 1.0 + rng.nextDouble() * 2.5;
        fa.setDebtToEquity(Math.round(debtToEquity * 100.0) / 100.0);
        fa.setCurrentRatio(Math.round(currentRatio * 100.0) / 100.0);

        // Dividend Yield
        double dividendYield = rng.nextDouble() * 3;
        fa.setDividendYield(Math.round(dividendYield * 100.0) / 100.0);

        // Growth metrics
        double revenueGrowth = -5 + rng.nextDouble() * 40;
        double profitGrowth = -10 + rng.nextDouble() * 50;
        fa.setRevenueGrowthYoY(Math.round(revenueGrowth * 100.0) / 100.0);
        fa.setProfitGrowthYoY(Math.round(profitGrowth * 100.0) / 100.0);

        // Holding pattern
        double promoter = 40 + rng.nextDouble() * 35;
        double fii = 5 + rng.nextDouble() * 25;
        double dii = 5 + rng.nextDouble() * 20;
        fa.setPromoterHolding(Math.round(promoter * 100.0) / 100.0);
        fa.setFiiHolding(Math.round(fii * 100.0) / 100.0);
        fa.setDiiHolding(Math.round(dii * 100.0) / 100.0);

        // Intrinsic Value estimation (simplified DCF approach)
        double growthRate = Math.max(0.05, revenueGrowth / 100.0 * 0.8);
        double terminalGrowth = 0.04;
        double discountRate = 0.12;
        double intrinsicValue = eps * (1 + growthRate) / (discountRate - terminalGrowth);
        intrinsicValue = Math.max(intrinsicValue, currentPrice * 0.5);
        fa.setEstimatedIntrinsicValue(Math.round(intrinsicValue * 100.0) / 100.0);

        double priceToIntrinsic = currentPrice / intrinsicValue;
        fa.setPriceToIntrinsicRatio(Math.round(priceToIntrinsic * 100.0) / 100.0);

        fa.setIntrinsicValueJustification(String.format(
            "Estimated intrinsic value of ₹%.2f computed using a simplified DCF model: " +
            "EPS of ₹%.2f grown at %.1f%% CAGR, discounted at 12%% with 4%% terminal growth. " +
            "Current price (₹%.2f) trades at %.1fx of estimated intrinsic value. " +
            "%s",
            intrinsicValue, eps, growthRate * 100, currentPrice, priceToIntrinsic,
            priceToIntrinsic < 0.9 ? "Stock appears undervalued — potential margin of safety exists." :
            priceToIntrinsic > 1.3 ? "Stock appears overvalued relative to intrinsic estimate — elevated downside risk." :
            "Stock is trading near fair value — limited margin of safety at current price."));

        // Valuation Status
        if (pe < industryPe * 0.85 && priceToIntrinsic < 0.95) {
            fa.setValuationStatus("UNDERVALUED");
        } else if (pe > industryPe * 1.2 && priceToIntrinsic > 1.2) {
            fa.setValuationStatus("OVERVALUED");
        } else {
            fa.setValuationStatus("FAIRLY VALUED");
        }

        // Fundamental Summary
        fa.setFundamentalSummary(buildFundamentalSummary(fa, sector));

        return fa;
    }

    private double getIndustryPe(String sector) {
        return switch (sector.toUpperCase()) {
            case "INFORMATION TECHNOLOGY" -> 28.5;
            case "BANKING & FINANCE", "BANKING" -> 18.0;
            case "NBFC" -> 22.0;
            case "OIL & GAS", "OIL & GAS / CONGLOMERATE" -> 12.5;
            case "AUTOMOBILE" -> 20.0;
            case "PHARMACEUTICALS" -> 30.0;
            case "CONSUMER GOODS" -> 45.0;
            case "INTERNET & E-COMMERCE" -> 80.0;
            case "FINTECH" -> 60.0;
            case "POWER & ENERGY" -> 15.0;
            case "INFRASTRUCTURE" -> 25.0;
            case "CONGLOMERATE" -> 35.0;
            default -> 25.0;
        };
    }

    private String buildFundamentalSummary(FundamentalAnalysis fa, String sector) {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("Valuation: %s (P/E %.1fx vs Industry %.1fx). ",
            fa.getValuationStatus(), fa.getPe(), fa.getIndustryPe()));

        if (fa.getRoe() > 20) {
            sb.append(String.format("Strong capital efficiency with ROE of %.1f%%, significantly above the 15%% threshold. ", fa.getRoe()));
        } else if (fa.getRoe() > 12) {
            sb.append(String.format("Adequate ROE of %.1f%%, meeting minimum profitability standards. ", fa.getRoe()));
        } else {
            sb.append(String.format("Weak ROE of %.1f%%, indicating poor capital utilization. ", fa.getRoe()));
        }

        if (fa.getDebtToEquity() < 0.5) {
            sb.append(String.format("Low D/E ratio of %.2fx indicates a financially conservative balance sheet. ", fa.getDebtToEquity()));
        } else if (fa.getDebtToEquity() < 1.0) {
            sb.append(String.format("Moderate D/E ratio of %.2fx is manageable. Monitor debt repayment schedule. ", fa.getDebtToEquity()));
        } else {
            sb.append(String.format("High D/E ratio of %.2fx poses financial risk, especially in a rising interest rate environment. ", fa.getDebtToEquity()));
        }

        if (fa.getRevenueGrowthYoY() > 15) {
            sb.append(String.format("Impressive revenue growth of %.1f%% YoY demonstrates strong business momentum. ", fa.getRevenueGrowthYoY()));
        } else if (fa.getRevenueGrowthYoY() > 0) {
            sb.append(String.format("Modest revenue growth of %.1f%% YoY. ", fa.getRevenueGrowthYoY()));
        } else {
            sb.append(String.format("Revenue declined %.1f%% YoY — a concern that needs monitoring over next 2 quarters. ", Math.abs(fa.getRevenueGrowthYoY())));
        }

        sb.append(String.format("Promoter holding at %.1f%% %s. ",
            fa.getPromoterHolding(),
            fa.getPromoterHolding() > 55 ? "shows strong conviction by founders/management" :
            fa.getPromoterHolding() > 40 ? "is at healthy levels" : "is on the lower side — monitor for further reduction"));

        return sb.toString();
    }
}
