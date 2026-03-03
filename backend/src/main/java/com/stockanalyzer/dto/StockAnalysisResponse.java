package com.stockanalyzer.dto;

import java.util.List;

public class StockAnalysisResponse {
    private String symbol;
    private String companyName;
    private String exchange;
    private String sector;
    private StockOverview overview;
    private TechnicalAnalysis technicalAnalysis;
    private FundamentalAnalysis fundamentalAnalysis;
    private List<NewsItem> companyNews;
    private List<NewsItem> competitorNews;
    private FinancialStatements financialStatements;
    private ShareholdingPattern shareholdingPattern;
    private ManipulationAnalysis manipulationAnalysis;
    private String overallSentiment;
    private String overallRecommendation;
    private String analysisJustification;

    // Getters and Setters
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getExchange() { return exchange; }
    public void setExchange(String exchange) { this.exchange = exchange; }

    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }

    public StockOverview getOverview() { return overview; }
    public void setOverview(StockOverview overview) { this.overview = overview; }

    public TechnicalAnalysis getTechnicalAnalysis() { return technicalAnalysis; }
    public void setTechnicalAnalysis(TechnicalAnalysis technicalAnalysis) { this.technicalAnalysis = technicalAnalysis; }

    public FundamentalAnalysis getFundamentalAnalysis() { return fundamentalAnalysis; }
    public void setFundamentalAnalysis(FundamentalAnalysis fundamentalAnalysis) { this.fundamentalAnalysis = fundamentalAnalysis; }

    public List<NewsItem> getCompanyNews() { return companyNews; }
    public void setCompanyNews(List<NewsItem> companyNews) { this.companyNews = companyNews; }

    public List<NewsItem> getCompetitorNews() { return competitorNews; }
    public void setCompetitorNews(List<NewsItem> competitorNews) { this.competitorNews = competitorNews; }

    public FinancialStatements getFinancialStatements() { return financialStatements; }
    public void setFinancialStatements(FinancialStatements financialStatements) { this.financialStatements = financialStatements; }

    public ShareholdingPattern getShareholdingPattern() { return shareholdingPattern; }
    public void setShareholdingPattern(ShareholdingPattern shareholdingPattern) { this.shareholdingPattern = shareholdingPattern; }

    public ManipulationAnalysis getManipulationAnalysis() { return manipulationAnalysis; }
    public void setManipulationAnalysis(ManipulationAnalysis manipulationAnalysis) { this.manipulationAnalysis = manipulationAnalysis; }

    public String getOverallSentiment() { return overallSentiment; }
    public void setOverallSentiment(String overallSentiment) { this.overallSentiment = overallSentiment; }

    public String getOverallRecommendation() { return overallRecommendation; }
    public void setOverallRecommendation(String overallRecommendation) { this.overallRecommendation = overallRecommendation; }

    public String getAnalysisJustification() { return analysisJustification; }
    public void setAnalysisJustification(String analysisJustification) { this.analysisJustification = analysisJustification; }

    // Nested classes
    public static class StockOverview {
        private double currentPrice;
        private double dayHigh;
        private double dayLow;
        private double weekHigh52;
        private double weekLow52;
        private long volume;
        private long avgVolume;
        private double marketCap;
        private double changePercent;
        private double changeAmount;
        private String priceChangeJustification;

        public double getCurrentPrice() { return currentPrice; }
        public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }

        public double getDayHigh() { return dayHigh; }
        public void setDayHigh(double dayHigh) { this.dayHigh = dayHigh; }

        public double getDayLow() { return dayLow; }
        public void setDayLow(double dayLow) { this.dayLow = dayLow; }

        public double getWeekHigh52() { return weekHigh52; }
        public void setWeekHigh52(double weekHigh52) { this.weekHigh52 = weekHigh52; }

        public double getWeekLow52() { return weekLow52; }
        public void setWeekLow52(double weekLow52) { this.weekLow52 = weekLow52; }

        public long getVolume() { return volume; }
        public void setVolume(long volume) { this.volume = volume; }

        public long getAvgVolume() { return avgVolume; }
        public void setAvgVolume(long avgVolume) { this.avgVolume = avgVolume; }

        public double getMarketCap() { return marketCap; }
        public void setMarketCap(double marketCap) { this.marketCap = marketCap; }

        public double getChangePercent() { return changePercent; }
        public void setChangePercent(double changePercent) { this.changePercent = changePercent; }

        public double getChangeAmount() { return changeAmount; }
        public void setChangeAmount(double changeAmount) { this.changeAmount = changeAmount; }

        public String getPriceChangeJustification() { return priceChangeJustification; }
        public void setPriceChangeJustification(String priceChangeJustification) { this.priceChangeJustification = priceChangeJustification; }
    }

    public static class TechnicalAnalysis {
        private double rsi;
        private String rsiSignal;
        private String rsiJustification;
        private double macd;
        private double macdSignal;
        private double macdHistogram;
        private String macdTrend;
        private String macdJustification;
        private double sma20;
        private double sma50;
        private double sma200;
        private String movingAvgSignal;
        private String movingAvgJustification;
        private double bollingerUpper;
        private double bollingerMiddle;
        private double bollingerLower;
        private String bollingerSignal;
        private double atr;
        private double obv;
        private String obvTrend;
        private double stochasticK;
        private double stochasticD;
        private String stochasticSignal;
        private String overallTechnicalSignal;
        private String technicalSummary;

        public double getRsi() { return rsi; }
        public void setRsi(double rsi) { this.rsi = rsi; }

        public String getRsiSignal() { return rsiSignal; }
        public void setRsiSignal(String rsiSignal) { this.rsiSignal = rsiSignal; }

        public String getRsiJustification() { return rsiJustification; }
        public void setRsiJustification(String rsiJustification) { this.rsiJustification = rsiJustification; }

        public double getMacd() { return macd; }
        public void setMacd(double macd) { this.macd = macd; }

        public double getMacdSignal() { return macdSignal; }
        public void setMacdSignal(double macdSignal) { this.macdSignal = macdSignal; }

        public double getMacdHistogram() { return macdHistogram; }
        public void setMacdHistogram(double macdHistogram) { this.macdHistogram = macdHistogram; }

        public String getMacdTrend() { return macdTrend; }
        public void setMacdTrend(String macdTrend) { this.macdTrend = macdTrend; }

        public String getMacdJustification() { return macdJustification; }
        public void setMacdJustification(String macdJustification) { this.macdJustification = macdJustification; }

        public double getSma20() { return sma20; }
        public void setSma20(double sma20) { this.sma20 = sma20; }

        public double getSma50() { return sma50; }
        public void setSma50(double sma50) { this.sma50 = sma50; }

        public double getSma200() { return sma200; }
        public void setSma200(double sma200) { this.sma200 = sma200; }

        public String getMovingAvgSignal() { return movingAvgSignal; }
        public void setMovingAvgSignal(String movingAvgSignal) { this.movingAvgSignal = movingAvgSignal; }

        public String getMovingAvgJustification() { return movingAvgJustification; }
        public void setMovingAvgJustification(String movingAvgJustification) { this.movingAvgJustification = movingAvgJustification; }

        public double getBollingerUpper() { return bollingerUpper; }
        public void setBollingerUpper(double bollingerUpper) { this.bollingerUpper = bollingerUpper; }

        public double getBollingerMiddle() { return bollingerMiddle; }
        public void setBollingerMiddle(double bollingerMiddle) { this.bollingerMiddle = bollingerMiddle; }

        public double getBollingerLower() { return bollingerLower; }
        public void setBollingerLower(double bollingerLower) { this.bollingerLower = bollingerLower; }

        public String getBollingerSignal() { return bollingerSignal; }
        public void setBollingerSignal(String bollingerSignal) { this.bollingerSignal = bollingerSignal; }

        public double getAtr() { return atr; }
        public void setAtr(double atr) { this.atr = atr; }

        public double getObv() { return obv; }
        public void setObv(double obv) { this.obv = obv; }

        public String getObvTrend() { return obvTrend; }
        public void setObvTrend(String obvTrend) { this.obvTrend = obvTrend; }

        public double getStochasticK() { return stochasticK; }
        public void setStochasticK(double stochasticK) { this.stochasticK = stochasticK; }

        public double getStochasticD() { return stochasticD; }
        public void setStochasticD(double stochasticD) { this.stochasticD = stochasticD; }

        public String getStochasticSignal() { return stochasticSignal; }
        public void setStochasticSignal(String stochasticSignal) { this.stochasticSignal = stochasticSignal; }

        public String getOverallTechnicalSignal() { return overallTechnicalSignal; }
        public void setOverallTechnicalSignal(String overallTechnicalSignal) { this.overallTechnicalSignal = overallTechnicalSignal; }

        public String getTechnicalSummary() { return technicalSummary; }
        public void setTechnicalSummary(String technicalSummary) { this.technicalSummary = technicalSummary; }
    }

    public static class FundamentalAnalysis {
        private double pe;
        private double industryPe;
        private String peJustification;
        private double pb;
        private double roe;
        private double roce;
        private double eps;
        private double debtToEquity;
        private double currentRatio;
        private double dividendYield;
        private double revenueGrowthYoY;
        private double profitGrowthYoY;
        private double promoterHolding;
        private double fiiHolding;
        private double diiHolding;
        private String valuationStatus;
        private String fundamentalSummary;
        private String intrinsicValueJustification;
        private double estimatedIntrinsicValue;
        private double priceToIntrinsicRatio;

        public double getPe() { return pe; }
        public void setPe(double pe) { this.pe = pe; }

        public double getIndustryPe() { return industryPe; }
        public void setIndustryPe(double industryPe) { this.industryPe = industryPe; }

        public String getPeJustification() { return peJustification; }
        public void setPeJustification(String peJustification) { this.peJustification = peJustification; }

        public double getPb() { return pb; }
        public void setPb(double pb) { this.pb = pb; }

        public double getRoe() { return roe; }
        public void setRoe(double roe) { this.roe = roe; }

        public double getRoce() { return roce; }
        public void setRoce(double roce) { this.roce = roce; }

        public double getEps() { return eps; }
        public void setEps(double eps) { this.eps = eps; }

        public double getDebtToEquity() { return debtToEquity; }
        public void setDebtToEquity(double debtToEquity) { this.debtToEquity = debtToEquity; }

        public double getCurrentRatio() { return currentRatio; }
        public void setCurrentRatio(double currentRatio) { this.currentRatio = currentRatio; }

        public double getDividendYield() { return dividendYield; }
        public void setDividendYield(double dividendYield) { this.dividendYield = dividendYield; }

        public double getRevenueGrowthYoY() { return revenueGrowthYoY; }
        public void setRevenueGrowthYoY(double revenueGrowthYoY) { this.revenueGrowthYoY = revenueGrowthYoY; }

        public double getProfitGrowthYoY() { return profitGrowthYoY; }
        public void setProfitGrowthYoY(double profitGrowthYoY) { this.profitGrowthYoY = profitGrowthYoY; }

        public double getPromoterHolding() { return promoterHolding; }
        public void setPromoterHolding(double promoterHolding) { this.promoterHolding = promoterHolding; }

        public double getFiiHolding() { return fiiHolding; }
        public void setFiiHolding(double fiiHolding) { this.fiiHolding = fiiHolding; }

        public double getDiiHolding() { return diiHolding; }
        public void setDiiHolding(double diiHolding) { this.diiHolding = diiHolding; }

        public String getValuationStatus() { return valuationStatus; }
        public void setValuationStatus(String valuationStatus) { this.valuationStatus = valuationStatus; }

        public String getFundamentalSummary() { return fundamentalSummary; }
        public void setFundamentalSummary(String fundamentalSummary) { this.fundamentalSummary = fundamentalSummary; }

        public String getIntrinsicValueJustification() { return intrinsicValueJustification; }
        public void setIntrinsicValueJustification(String intrinsicValueJustification) { this.intrinsicValueJustification = intrinsicValueJustification; }

        public double getEstimatedIntrinsicValue() { return estimatedIntrinsicValue; }
        public void setEstimatedIntrinsicValue(double estimatedIntrinsicValue) { this.estimatedIntrinsicValue = estimatedIntrinsicValue; }

        public double getPriceToIntrinsicRatio() { return priceToIntrinsicRatio; }
        public void setPriceToIntrinsicRatio(double priceToIntrinsicRatio) { this.priceToIntrinsicRatio = priceToIntrinsicRatio; }
    }

    public static class NewsItem {
        private String headline;
        private String source;
        private String publishedDate;
        private String summary;
        private String sentiment;
        private String authenticityScore;
        private String authenticityJustification;
        private String impactOnStock;
        private boolean isAuthentic;
        private String url;

        public String getHeadline() { return headline; }
        public void setHeadline(String headline) { this.headline = headline; }

        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }

        public String getPublishedDate() { return publishedDate; }
        public void setPublishedDate(String publishedDate) { this.publishedDate = publishedDate; }

        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }

        public String getSentiment() { return sentiment; }
        public void setSentiment(String sentiment) { this.sentiment = sentiment; }

        public String getAuthenticityScore() { return authenticityScore; }
        public void setAuthenticityScore(String authenticityScore) { this.authenticityScore = authenticityScore; }

        public String getAuthenticityJustification() { return authenticityJustification; }
        public void setAuthenticityJustification(String authenticityJustification) { this.authenticityJustification = authenticityJustification; }

        public String getImpactOnStock() { return impactOnStock; }
        public void setImpactOnStock(String impactOnStock) { this.impactOnStock = impactOnStock; }

        public boolean isAuthentic() { return isAuthentic; }
        public void setAuthentic(boolean authentic) { isAuthentic = authentic; }

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
    }

    public static class FinancialStatements {
        private List<AnnualStatement> annualStatements;
        private List<QuarterlyStatement> quarterlyStatements;
        private String financialHealthSummary;
        private String growthTrendAnalysis;
        private String redFlags;

        public List<AnnualStatement> getAnnualStatements() { return annualStatements; }
        public void setAnnualStatements(List<AnnualStatement> annualStatements) { this.annualStatements = annualStatements; }

        public List<QuarterlyStatement> getQuarterlyStatements() { return quarterlyStatements; }
        public void setQuarterlyStatements(List<QuarterlyStatement> quarterlyStatements) { this.quarterlyStatements = quarterlyStatements; }

        public String getFinancialHealthSummary() { return financialHealthSummary; }
        public void setFinancialHealthSummary(String financialHealthSummary) { this.financialHealthSummary = financialHealthSummary; }

        public String getGrowthTrendAnalysis() { return growthTrendAnalysis; }
        public void setGrowthTrendAnalysis(String growthTrendAnalysis) { this.growthTrendAnalysis = growthTrendAnalysis; }

        public String getRedFlags() { return redFlags; }
        public void setRedFlags(String redFlags) { this.redFlags = redFlags; }
    }

    public static class AnnualStatement {
        private String year;
        private double revenue;
        private double netProfit;
        private double ebitda;
        private double totalDebt;
        private double totalEquity;
        private double operatingCashFlow;
        private double freeCashFlow;
        private double revenueGrowth;
        private double profitGrowth;
        private double ebitdaMargin;
        private double netMargin;

        public String getYear() { return year; }
        public void setYear(String year) { this.year = year; }

        public double getRevenue() { return revenue; }
        public void setRevenue(double revenue) { this.revenue = revenue; }

        public double getNetProfit() { return netProfit; }
        public void setNetProfit(double netProfit) { this.netProfit = netProfit; }

        public double getEbitda() { return ebitda; }
        public void setEbitda(double ebitda) { this.ebitda = ebitda; }

        public double getTotalDebt() { return totalDebt; }
        public void setTotalDebt(double totalDebt) { this.totalDebt = totalDebt; }

        public double getTotalEquity() { return totalEquity; }
        public void setTotalEquity(double totalEquity) { this.totalEquity = totalEquity; }

        public double getOperatingCashFlow() { return operatingCashFlow; }
        public void setOperatingCashFlow(double operatingCashFlow) { this.operatingCashFlow = operatingCashFlow; }

        public double getFreeCashFlow() { return freeCashFlow; }
        public void setFreeCashFlow(double freeCashFlow) { this.freeCashFlow = freeCashFlow; }

        public double getRevenueGrowth() { return revenueGrowth; }
        public void setRevenueGrowth(double revenueGrowth) { this.revenueGrowth = revenueGrowth; }

        public double getProfitGrowth() { return profitGrowth; }
        public void setProfitGrowth(double profitGrowth) { this.profitGrowth = profitGrowth; }

        public double getEbitdaMargin() { return ebitdaMargin; }
        public void setEbitdaMargin(double ebitdaMargin) { this.ebitdaMargin = ebitdaMargin; }

        public double getNetMargin() { return netMargin; }
        public void setNetMargin(double netMargin) { this.netMargin = netMargin; }
    }

    public static class QuarterlyStatement {
        private String quarter;
        private double revenue;
        private double netProfit;
        private double ebitda;
        private double revenueGrowthYoY;
        private double profitGrowthYoY;
        private double revenueGrowthQoQ;
        private double ebitdaMargin;
        private double netMargin;

        public String getQuarter() { return quarter; }
        public void setQuarter(String quarter) { this.quarter = quarter; }

        public double getRevenue() { return revenue; }
        public void setRevenue(double revenue) { this.revenue = revenue; }

        public double getNetProfit() { return netProfit; }
        public void setNetProfit(double netProfit) { this.netProfit = netProfit; }

        public double getEbitda() { return ebitda; }
        public void setEbitda(double ebitda) { this.ebitda = ebitda; }

        public double getRevenueGrowthYoY() { return revenueGrowthYoY; }
        public void setRevenueGrowthYoY(double revenueGrowthYoY) { this.revenueGrowthYoY = revenueGrowthYoY; }

        public double getProfitGrowthYoY() { return profitGrowthYoY; }
        public void setProfitGrowthYoY(double profitGrowthYoY) { this.profitGrowthYoY = profitGrowthYoY; }

        public double getRevenueGrowthQoQ() { return revenueGrowthQoQ; }
        public void setRevenueGrowthQoQ(double revenueGrowthQoQ) { this.revenueGrowthQoQ = revenueGrowthQoQ; }

        public double getEbitdaMargin() { return ebitdaMargin; }
        public void setEbitdaMargin(double ebitdaMargin) { this.ebitdaMargin = ebitdaMargin; }

        public double getNetMargin() { return netMargin; }
        public void setNetMargin(double netMargin) { this.netMargin = netMargin; }
    }

    public static class ShareholdingPattern {
        private double promoterHolding;
        private double promoterPledged;
        private double fiiHolding;
        private double diiHolding;
        private double publicHolding;
        private List<ShareholdingTrend> trend;
        private List<String> redFlags;
        private String analysis;
        private String riskLevel;

        public double getPromoterHolding() { return promoterHolding; }
        public void setPromoterHolding(double promoterHolding) { this.promoterHolding = promoterHolding; }

        public double getPromoterPledged() { return promoterPledged; }
        public void setPromoterPledged(double promoterPledged) { this.promoterPledged = promoterPledged; }

        public double getFiiHolding() { return fiiHolding; }
        public void setFiiHolding(double fiiHolding) { this.fiiHolding = fiiHolding; }

        public double getDiiHolding() { return diiHolding; }
        public void setDiiHolding(double diiHolding) { this.diiHolding = diiHolding; }

        public double getPublicHolding() { return publicHolding; }
        public void setPublicHolding(double publicHolding) { this.publicHolding = publicHolding; }

        public List<ShareholdingTrend> getTrend() { return trend; }
        public void setTrend(List<ShareholdingTrend> trend) { this.trend = trend; }

        public List<String> getRedFlags() { return redFlags; }
        public void setRedFlags(List<String> redFlags) { this.redFlags = redFlags; }

        public String getAnalysis() { return analysis; }
        public void setAnalysis(String analysis) { this.analysis = analysis; }

        public String getRiskLevel() { return riskLevel; }
        public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    }

    public static class ShareholdingTrend {
        private String quarter;
        private double promoter;
        private double fii;
        private double dii;
        private double public_;

        public String getQuarter() { return quarter; }
        public void setQuarter(String quarter) { this.quarter = quarter; }

        public double getPromoter() { return promoter; }
        public void setPromoter(double promoter) { this.promoter = promoter; }

        public double getFii() { return fii; }
        public void setFii(double fii) { this.fii = fii; }

        public double getDii() { return dii; }
        public void setDii(double dii) { this.dii = dii; }

        public double getPublic_() { return public_; }
        public void setPublic_(double public_) { this.public_ = public_; }
    }

    public static class ManipulationAnalysis {
        private boolean suspectedManipulation;
        private String manipulationRiskLevel;
        private List<String> redFlags;
        private List<String> greenFlags;
        private double unusualVolumeScore;
        private double pricePatternScore;
        private double circuitBreakerHits;
        private String operatorActivityAnalysis;
        private String pumpAndDumpRisk;
        private String analysisJustification;

        public boolean isSuspectedManipulation() { return suspectedManipulation; }
        public void setSuspectedManipulation(boolean suspectedManipulation) { this.suspectedManipulation = suspectedManipulation; }

        public String getManipulationRiskLevel() { return manipulationRiskLevel; }
        public void setManipulationRiskLevel(String manipulationRiskLevel) { this.manipulationRiskLevel = manipulationRiskLevel; }

        public List<String> getRedFlags() { return redFlags; }
        public void setRedFlags(List<String> redFlags) { this.redFlags = redFlags; }

        public List<String> getGreenFlags() { return greenFlags; }
        public void setGreenFlags(List<String> greenFlags) { this.greenFlags = greenFlags; }

        public double getUnusualVolumeScore() { return unusualVolumeScore; }
        public void setUnusualVolumeScore(double unusualVolumeScore) { this.unusualVolumeScore = unusualVolumeScore; }

        public double getPricePatternScore() { return pricePatternScore; }
        public void setPricePatternScore(double pricePatternScore) { this.pricePatternScore = pricePatternScore; }

        public double getCircuitBreakerHits() { return circuitBreakerHits; }
        public void setCircuitBreakerHits(double circuitBreakerHits) { this.circuitBreakerHits = circuitBreakerHits; }

        public String getOperatorActivityAnalysis() { return operatorActivityAnalysis; }
        public void setOperatorActivityAnalysis(String operatorActivityAnalysis) { this.operatorActivityAnalysis = operatorActivityAnalysis; }

        public String getPumpAndDumpRisk() { return pumpAndDumpRisk; }
        public void setPumpAndDumpRisk(String pumpAndDumpRisk) { this.pumpAndDumpRisk = pumpAndDumpRisk; }

        public String getAnalysisJustification() { return analysisJustification; }
        public void setAnalysisJustification(String analysisJustification) { this.analysisJustification = analysisJustification; }
    }
}
