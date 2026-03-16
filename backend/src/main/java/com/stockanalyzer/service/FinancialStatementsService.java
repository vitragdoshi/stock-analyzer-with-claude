package com.stockanalyzer.service;

import com.stockanalyzer.dto.StockAnalysisResponse.*;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FinancialStatementsService {

    public FinancialStatements generate(String symbol, double baseRevenue, Random rng) {
        FinancialStatements fs = new FinancialStatements();

        List<AnnualStatement> annuals = new ArrayList<>();
        double revenue = baseRevenue;
        for (int i = 4; i >= 0; i--) {
            AnnualStatement stmt = new AnnualStatement();
            stmt.setYear("FY" + (21 + i));
            double growthFactor = 1.05 + rng.nextDouble() * 0.25;
            double prevRevenue = revenue / growthFactor;

            stmt.setRevenue(Math.round(revenue * 100.0) / 100.0);

            double ebitdaMargin = 0.18 + rng.nextDouble() * 0.12;
            double netMargin = ebitdaMargin - 0.05 - rng.nextDouble() * 0.05;
            double ebitda = revenue * ebitdaMargin;
            double netProfit = revenue * netMargin;

            stmt.setEbitda(Math.round(ebitda * 100.0) / 100.0);
            stmt.setNetProfit(Math.round(netProfit * 100.0) / 100.0);
            stmt.setEbitdaMargin(Math.round(ebitdaMargin * 10000.0) / 100.0);
            stmt.setNetMargin(Math.round(netMargin * 10000.0) / 100.0);

            double debtFactor = 0.3 + rng.nextDouble() * 0.7;
            stmt.setTotalDebt(Math.round(revenue * debtFactor * 100.0) / 100.0);
            stmt.setTotalEquity(Math.round(revenue * (0.8 + rng.nextDouble() * 0.5) * 100.0) / 100.0);

            double ocfFactor = 0.8 + rng.nextDouble() * 0.4;
            stmt.setOperatingCashFlow(Math.round(netProfit * ocfFactor * 100.0) / 100.0);
            stmt.setFreeCashFlow(Math.round(netProfit * (ocfFactor - 0.15) * 100.0) / 100.0);

            if (i < 4) {
                double prevYearRevenue = annuals.isEmpty() ? prevRevenue : annuals.get(annuals.size() - 1).getRevenue();
                stmt.setRevenueGrowth(Math.round(((revenue - prevYearRevenue) / prevYearRevenue * 100) * 100.0) / 100.0);
                double prevNetProfit = annuals.isEmpty() ? netProfit * 0.9 : annuals.get(annuals.size() - 1).getNetProfit();
                stmt.setProfitGrowth(Math.round(((netProfit - prevNetProfit) / Math.abs(prevNetProfit) * 100) * 100.0) / 100.0);
            }

            annuals.add(stmt);
            revenue = prevRevenue;
        }

        Collections.reverse(annuals);
        fs.setAnnualStatements(annuals);

        // Quarterly statements (last 8 quarters)
        List<QuarterlyStatement> quarterlies = new ArrayList<>();
        String[] quarters = {"Q1FY24", "Q2FY24", "Q3FY24", "Q4FY24", "Q1FY25", "Q2FY25", "Q3FY25", "Q4FY25"};
        double qRevenue = baseRevenue / 4;

        for (int i = 0; i < 8; i++) {
            QuarterlyStatement q = new QuarterlyStatement();
            q.setQuarter(quarters[i]);

            double qVariance = 0.9 + rng.nextDouble() * 0.2;
            double qRev = qRevenue * qVariance * (1.0 + i * 0.015);
            double qEbitdaMargin = 0.18 + rng.nextDouble() * 0.12;
            double qNetMargin = qEbitdaMargin - 0.05 - rng.nextDouble() * 0.05;

            q.setRevenue(Math.round(qRev * 100.0) / 100.0);
            q.setEbitda(Math.round(qRev * qEbitdaMargin * 100.0) / 100.0);
            q.setNetProfit(Math.round(qRev * qNetMargin * 100.0) / 100.0);
            q.setEbitdaMargin(Math.round(qEbitdaMargin * 10000.0) / 100.0);
            q.setNetMargin(Math.round(qNetMargin * 10000.0) / 100.0);

            if (i >= 4) {
                double sameQLastYear = quarterlies.get(i - 4).getRevenue();
                q.setRevenueGrowthYoY(Math.round(((qRev - sameQLastYear) / sameQLastYear * 100) * 100.0) / 100.0);
                double sameQProfit = quarterlies.get(i - 4).getNetProfit();
                q.setProfitGrowthYoY(Math.round(((q.getNetProfit() - sameQProfit) / Math.abs(sameQProfit) * 100) * 100.0) / 100.0);
            }
            if (i >= 1) {
                double prevQ = quarterlies.get(i - 1).getRevenue();
                q.setRevenueGrowthQoQ(Math.round(((qRev - prevQ) / prevQ * 100) * 100.0) / 100.0);
            }

            quarterlies.add(q);
        }
        fs.setQuarterlyStatements(quarterlies);

        // Summaries
        AnnualStatement latest = annuals.get(annuals.size() - 1);
        AnnualStatement earliest = annuals.get(0);

        double revenueCAGR = Math.pow(latest.getRevenue() / earliest.getRevenue(), 0.25) - 1;
        double profitCAGR = Math.pow(Math.abs(latest.getNetProfit()) / Math.abs(earliest.getNetProfit()), 0.25) - 1;

        fs.setFinancialHealthSummary(String.format(
            "Over the past 5 years, revenue has grown at %.1f%% CAGR from ₹%.0f Cr to ₹%.0f Cr. " +
            "Net profit CAGR stands at %.1f%%. Latest EBITDA margin of %.1f%% %s. " +
            "Operating Cash Flow conversion is healthy at %.0f%% of net profit, indicating quality earnings.",
            revenueCAGR * 100, earliest.getRevenue(), latest.getRevenue(),
            profitCAGR * 100, latest.getEbitdaMargin(),
            latest.getEbitdaMargin() > 20 ? "is above industry benchmark" : "has room for improvement",
            (latest.getOperatingCashFlow() / latest.getNetProfit()) * 100));

        QuarterlyStatement q4 = quarterlies.get(7);
        QuarterlyStatement q3 = quarterlies.get(6);
        fs.setGrowthTrendAnalysis(String.format(
            "Recent quarterly trend is %s. Q4FY25 revenue of ₹%.0f Cr grew %.1f%% YoY. " +
            "Sequential (QoQ) growth was %.1f%%. PAT growth YoY: %.1f%%. " +
            "EBITDA margins %s over the last 4 quarters.",
            q4.getRevenueGrowthYoY() > 10 ? "POSITIVE" : q4.getRevenueGrowthYoY() > 0 ? "STABLE" : "DECLINING",
            q4.getRevenue(), q4.getRevenueGrowthYoY(), q4.getRevenueGrowthQoQ(),
            q4.getProfitGrowthYoY(),
            q4.getEbitdaMargin() > q3.getEbitdaMargin() ? "expanded" : "compressed"));

        // Red flags check
        List<String> redFlagsList = new ArrayList<>();
        if (latest.getFreeCashFlow() < 0) {
            redFlagsList.add("Negative Free Cash Flow in latest year — company may require external funding");
        }
        if (latest.getTotalDebt() > latest.getRevenue() * 1.5) {
            redFlagsList.add("Debt exceeds 1.5x annual revenue — high leverage risk");
        }
        if (q4.getRevenueGrowthYoY() < -5) {
            redFlagsList.add("Revenue declined >5% YoY in latest quarter — business facing headwinds");
        }
        if (redFlagsList.isEmpty()) {
            redFlagsList.add("No major financial red flags detected");
        }
        fs.setRedFlags(String.join("; ", redFlagsList));

        return fs;
    }
}
