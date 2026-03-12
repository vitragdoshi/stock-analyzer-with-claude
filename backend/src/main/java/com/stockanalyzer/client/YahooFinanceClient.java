package com.stockanalyzer.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockanalyzer.model.*;
import com.stockanalyzer.util.TtlCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Client for Yahoo Finance unofficial APIs.
 *
 * All Indian NSE symbols are suffixed with ".NS" (e.g. RELIANCE → RELIANCE.NS).
 *
 * Endpoints used:
 *   Chart API  – https://query1.finance.yahoo.com/v8/finance/chart/{symbol}
 *   Summary    – https://query1.finance.yahoo.com/v10/finance/quoteSummary/{symbol}?modules=...
 *
 * No API key is required. Rate limit ≈ 2 000 req/h per IP.
 * Results are cached in TtlCache instances to avoid redundant calls.
 */
@Component
public class YahooFinanceClient {

    private static final Logger log = LoggerFactory.getLogger(YahooFinanceClient.class);

    private static final String BASE_CHART   = "https://query1.finance.yahoo.com/v8/finance/chart/";
    private static final String BASE_SUMMARY = "https://query1.finance.yahoo.com/v10/finance/quoteSummary/";

    private static final String MODULES_FULL =
            "summaryDetail,financialData,defaultKeyStatistics," +
            "recommendationTrend,earningsTrend,earningsHistory," +
            "upgradeDowngradeHistory,majorHoldersBreakdown,price";

    // Per-data-type TTL caches (no external library required)
    private final TtlCache<QuoteData>           quoteCache   = new TtlCache<>(TimeUnit.MINUTES.toMillis(5));
    private final TtlCache<List<HistoricalPrice>> histCache   = new TtlCache<>(TimeUnit.HOURS.toMillis(1));
    private final TtlCache<JsonNode>             summaryCache = new TtlCache<>(TimeUnit.HOURS.toMillis(6));
    private final TtlCache<AnalystConsensus>     analystCache = new TtlCache<>(TimeUnit.HOURS.toMillis(12));
    private final TtlCache<EarningsData>         earningsCache = new TtlCache<>(TimeUnit.HOURS.toMillis(6));

    private final RestTemplate http;
    private final ObjectMapper mapper;

    @Autowired
    public YahooFinanceClient(RestTemplate externalRestTemplate) {
        this.http   = externalRestTemplate;
        this.mapper = new ObjectMapper();
    }

    // ── Symbol helpers ────────────────────────────────────────────────────

    public static String toYahooSymbol(String nseSymbol) {
        String s = nseSymbol.toUpperCase().trim();
        if (s.endsWith(".NS") || s.endsWith(".BO")) return s;
        return s + ".NS";
    }

    // ── Quote ─────────────────────────────────────────────────────────────

    public QuoteData getQuote(String symbol) {
        String key = symbol.toUpperCase();
        QuoteData hit = quoteCache.get(key);
        if (hit != null) return hit;

        String ySymbol = toYahooSymbol(symbol);
        String url = BASE_CHART + ySymbol + "?interval=1d&range=1d";
        try {
            String raw = http.getForObject(url, String.class);
            JsonNode root   = mapper.readTree(raw);
            JsonNode result = root.path("chart").path("result").get(0);
            JsonNode meta   = result.path("meta");

            QuoteData q = new QuoteData();
            q.setSymbol(key);
            q.setCompanyName(meta.path("longName").asText(symbol));
            q.setExchange("NSE");
            q.setCurrency(meta.path("currency").asText("INR"));
            q.setCurrentPrice(meta.path("regularMarketPrice").asDouble());
            q.setPreviousClose(meta.path("previousClose").asDouble());
            q.setDayOpen(meta.path("regularMarketOpen").asDouble());
            q.setDayHigh(meta.path("regularMarketDayHigh").asDouble());
            q.setDayLow(meta.path("regularMarketDayLow").asDouble());
            q.setVolume(meta.path("regularMarketVolume").asLong());
            q.setAverageVolume(meta.path("regularMarketVolume").asLong());
            q.setWeek52High(meta.path("fiftyTwoWeekHigh").asDouble());
            q.setWeek52Low(meta.path("fiftyTwoWeekLow").asDouble());
            q.setMarketCap(meta.path("marketCap").asDouble() / 1e7);
            double cp = q.getCurrentPrice();
            double pc = q.getPreviousClose();
            q.setChange(cp - pc);
            q.setChangePercent(pc != 0 ? (cp - pc) / pc * 100 : 0);
            q.setRealData(true);
            quoteCache.put(key, q);
            log.info("YF quote fetched: {} @ {}", ySymbol, cp);
            return q;
        } catch (Exception e) {
            log.warn("YF quote failed for {}: {}", ySymbol, e.getMessage());
            return null;
        }
    }

    // ── Historical OHLCV ──────────────────────────────────────────────────

    public List<HistoricalPrice> getHistoricalPrices(String symbol, String range) {
        String cacheKey = symbol.toUpperCase() + "_" + range;
        List<HistoricalPrice> hit = histCache.get(cacheKey);
        if (hit != null) return hit;

        String ySymbol = toYahooSymbol(symbol);
        String interval = rangeToInterval(range);
        String url = BASE_CHART + ySymbol + "?interval=" + interval + "&range=" + range;
        List<HistoricalPrice> prices = new ArrayList<>();
        try {
            String raw = http.getForObject(url, String.class);
            JsonNode root    = mapper.readTree(raw);
            JsonNode result  = root.path("chart").path("result").get(0);
            JsonNode timestamps  = result.path("timestamp");
            JsonNode ohlcv       = result.path("indicators").path("quote").get(0);
            JsonNode adjCloseArr = result.path("indicators").path("adjclose").get(0).path("adjclose");

            JsonNode opens   = ohlcv.path("open");
            JsonNode highs   = ohlcv.path("high");
            JsonNode lows    = ohlcv.path("low");
            JsonNode closes  = ohlcv.path("close");
            JsonNode volumes = ohlcv.path("volume");

            for (int i = 0; i < timestamps.size(); i++) {
                if (closes.get(i).isNull()) continue;
                long epoch = timestamps.get(i).asLong();
                LocalDate date = Instant.ofEpochSecond(epoch)
                        .atZone(ZoneOffset.ofHoursMinutes(5, 30)).toLocalDate();
                HistoricalPrice hp = new HistoricalPrice(
                        date,
                        opens.get(i).asDouble(), highs.get(i).asDouble(),
                        lows.get(i).asDouble(), closes.get(i).asDouble(),
                        adjCloseArr.size() > i ? adjCloseArr.get(i).asDouble()
                                               : closes.get(i).asDouble(),
                        volumes.get(i).asLong());
                prices.add(hp);
            }
            histCache.put(cacheKey, prices);
            log.info("YF historical: {} bars for {}", prices.size(), ySymbol);
        } catch (Exception e) {
            log.warn("YF historical failed for {}: {}", ySymbol, e.getMessage());
        }
        return prices;
    }

    // ── Fundamentals ──────────────────────────────────────────────────────

    public FundamentalData getFundamentals(String symbol) {
        JsonNode modules = fetchSummaryModules(symbol);
        if (modules == null) return null;

        FundamentalData f = new FundamentalData();
        f.setSymbol(symbol.toUpperCase());

        JsonNode sd = modules.path("summaryDetail");
        JsonNode fd = modules.path("financialData");
        JsonNode ks = modules.path("defaultKeyStatistics");
        JsonNode mh = modules.path("majorHoldersBreakdown");

        f.setPeRatioTTM(doubleVal(sd, "trailingPE"));
        f.setPeRatioForward(doubleVal(sd, "forwardPE"));
        f.setPbRatio(doubleVal(ks, "priceToBook"));
        f.setPsRatio(doubleVal(ks, "priceToSalesTrailing12Months"));
        f.setEvToEbitda(doubleVal(ks, "enterpriseToEbitda"));
        f.setPegRatio(doubleVal(ks, "pegRatio"));
        f.setEpsTTM(doubleVal(ks, "trailingEps"));
        f.setEpsForward(doubleVal(ks, "forwardEps"));
        f.setBookValuePerShare(doubleVal(ks, "bookValue"));
        f.setDividendYield(doubleVal(sd, "dividendYield") * 100);
        f.setDividendRate(doubleVal(sd, "dividendRate"));
        f.setGrossMargins(doubleVal(fd, "grossMargins") * 100);
        f.setOperatingMargins(doubleVal(fd, "operatingMargins") * 100);
        f.setProfitMargins(doubleVal(fd, "profitMargins") * 100);
        f.setReturnOnEquity(doubleVal(fd, "returnOnEquity") * 100);
        f.setReturnOnAssets(doubleVal(fd, "returnOnAssets") * 100);
        f.setRevenueGrowth(doubleVal(fd, "revenueGrowth") * 100);
        f.setEarningsGrowth(doubleVal(fd, "earningsGrowth") * 100);
        f.setEarningsQuarterlyGrowth(doubleVal(ks, "earningsQuarterlyGrowth") * 100);

        double inrCr = 1e7;
        f.setTotalRevenueCr(doubleVal(fd, "totalRevenue") / inrCr);
        f.setGrossProfitCr(doubleVal(fd, "grossProfits") / inrCr);
        f.setEbitdaCr(doubleVal(fd, "ebitda") / inrCr);
        f.setTotalDebtCr(doubleVal(fd, "totalDebt") / inrCr);
        f.setTotalCashCr(doubleVal(fd, "totalCash") / inrCr);
        f.setCurrentRatio(doubleVal(fd, "currentRatio"));
        f.setDebtToEquity(doubleVal(fd, "debtToEquity") / 100);
        f.setOperatingCashflowCr(doubleVal(fd, "operatingCashflow") / inrCr);
        f.setFreeCashflowCr(doubleVal(fd, "freeCashflow") / inrCr);
        f.setSharesOutstanding(longVal(ks, "sharesOutstanding"));
        f.setHeldByInsidersPercent(doubleVal(mh, "insidersPercentHeld") * 100);
        f.setHeldByInstitutionsPercent(doubleVal(mh, "institutionsPercentHeld") * 100);
        f.setMarketCapCr(doubleVal(modules.path("price"), "marketCap") / inrCr);
        f.setRealData(true);
        log.info("YF fundamentals fetched for {}", symbol);
        return f;
    }

    // ── Analyst consensus ─────────────────────────────────────────────────

    public AnalystConsensus getAnalystConsensus(String symbol) {
        String key = symbol.toUpperCase() + "_analyst";
        AnalystConsensus hit = analystCache.get(key);
        if (hit != null) return hit;

        JsonNode modules = fetchSummaryModules(symbol);
        if (modules == null) return null;

        AnalystConsensus ac = new AnalystConsensus();
        ac.setSymbol(symbol.toUpperCase());

        JsonNode rt = modules.path("recommendationTrend").path("trend");
        if (rt.isArray() && rt.size() > 0) {
            JsonNode latest = rt.get(0);
            int sb = latest.path("strongBuy").asInt();
            int buy = latest.path("buy").asInt();
            int hold = latest.path("hold").asInt();
            int sell = latest.path("sell").asInt();
            int ss = latest.path("strongSell").asInt();
            int total = sb + buy + hold + sell + ss;
            ac.setStrongBuyCount(sb); ac.setBuyCount(buy); ac.setHoldCount(hold);
            ac.setSellCount(sell); ac.setStrongSellCount(ss); ac.setTotalAnalysts(total);
            double score = total > 0
                    ? (1.0*sb + 2.0*buy + 3.0*hold + 4.0*sell + 5.0*ss) / total : 3.0;
            ac.setConsensusScore(score);
            ac.setConsensusLabel(scoreToLabel(score));
        }

        JsonNode fd = modules.path("financialData");
        ac.setTargetLow(doubleVal(fd, "targetLowPrice"));
        ac.setTargetMean(doubleVal(fd, "targetMeanPrice"));
        ac.setTargetHigh(doubleVal(fd, "targetHighPrice"));
        ac.setTargetMedian(doubleVal(fd, "targetMedianPrice"));
        double cp = doubleVal(modules.path("price"), "regularMarketPrice");
        ac.setCurrentPrice(cp);
        double tgt = ac.getTargetMean();
        ac.setUpsidePotentialPercent(cp > 0 && tgt > 0 ? (tgt - cp) / cp * 100 : 0);

        JsonNode udh = modules.path("upgradeDowngradeHistory").path("history");
        List<AnalystConsensus.UpgradeEvent> events = new ArrayList<>();
        int limit = Math.min(10, udh.size());
        for (int i = 0; i < limit; i++) {
            JsonNode ev = udh.get(i);
            AnalystConsensus.UpgradeEvent ue = new AnalystConsensus.UpgradeEvent();
            ue.setFirm(ev.path("firm").asText());
            ue.setAction(ev.path("action").asText());
            ue.setFromGrade(ev.path("fromGrade").asText());
            ue.setToGrade(ev.path("toGrade").asText());
            ue.setEpochTime(Instant.ofEpochSecond(ev.path("epochGradeDate").asLong()));
            events.add(ue);
        }
        ac.setRecentUpgrades(events);
        ac.setRealData(true);
        analystCache.put(key, ac);
        log.info("YF analyst consensus fetched for {}", symbol);
        return ac;
    }

    // ── Earnings ──────────────────────────────────────────────────────────

    public EarningsData getEarningsData(String symbol) {
        String key = symbol.toUpperCase() + "_earnings";
        EarningsData hit = earningsCache.get(key);
        if (hit != null) return hit;

        JsonNode modules = fetchSummaryModules(symbol);
        if (modules == null) return null;

        EarningsData ed = new EarningsData();
        ed.setSymbol(symbol.toUpperCase());

        JsonNode hist = modules.path("earningsHistory").path("history");
        List<EarningsData.QuarterlyEarning> qHist = new ArrayList<>();
        double totalSurprise = 0; int beats = 0;
        for (int i = 0; i < hist.size(); i++) {
            JsonNode q = hist.get(i);
            EarningsData.QuarterlyEarning qe = new EarningsData.QuarterlyEarning();
            qe.setQuarter(q.path("quarter").asText());
            double est  = doubleVal(q, "epsEstimate");
            double act  = doubleVal(q, "epsActual");
            double surp = doubleVal(q, "epsDifference");
            double sPct = doubleVal(q, "surprisePercent");
            qe.setEpsEstimate(est); qe.setEpsActual(act);
            qe.setEpsSurprise(surp); qe.setEpsSurprisePercent(sPct);
            qe.setBeat(act > est);
            if (act > est) beats++;
            totalSurprise += sPct;
            qHist.add(qe);
        }
        ed.setQuarterlyHistory(qHist);
        int n = qHist.size();
        ed.setBeatRatePercent(n > 0 ? (double) beats / n * 100 : 50);
        ed.setAverageSurprise(n > 0 ? totalSurprise / n : 0);

        JsonNode trendNode = modules.path("earningsTrend").path("trend");
        List<EarningsData.EarningsTrend> trends = new ArrayList<>();
        for (int i = 0; i < trendNode.size(); i++) {
            JsonNode t = trendNode.get(i);
            EarningsData.EarningsTrend et = new EarningsData.EarningsTrend();
            et.setPeriod(t.path("period").asText());
            et.setEndDate(t.path("endDate").asText());
            JsonNode epsEst = t.path("earningsEstimate");
            et.setEpsEstimateLow(doubleVal(epsEst, "low"));
            et.setEpsEstimateHigh(doubleVal(epsEst, "high"));
            et.setEpsEstimateMean(doubleVal(epsEst, "avg"));
            et.setEpsEstimateAvg(doubleVal(epsEst, "avg"));
            double yearAgo = doubleVal(epsEst, "yearAgoEps");
            et.setEpsGrowthRate(yearAgo != 0
                    ? (et.getEpsEstimateMean() - yearAgo) / Math.abs(yearAgo) * 100 : 0);
            JsonNode revEst = t.path("revenueEstimate");
            et.setRevenueEstimateLow(doubleVal(revEst, "low"));
            et.setRevenueEstimateHigh(doubleVal(revEst, "high"));
            et.setRevenueEstimateAvg(doubleVal(revEst, "avg"));
            trends.add(et);
        }
        ed.setForwardTrends(trends);

        if (n >= 2) {
            double recent = qHist.get(n-1).getEpsSurprisePercent();
            double prior  = qHist.get(n-2).getEpsSurprisePercent();
            ed.setEarningsMomentum(recent > prior ? "ACCELERATING"
                    : recent < prior - 5 ? "DECELERATING" : "STABLE");
        } else {
            ed.setEarningsMomentum("STABLE");
        }

        ed.setRealData(true);
        earningsCache.put(key, ed);
        log.info("YF earnings fetched for {}", symbol);
        return ed;
    }

    // ── Private helpers ───────────────────────────────────────────────────

    /** Fetch all summary modules in one HTTP call; result cached for 6 hours. */
    private JsonNode fetchSummaryModules(String symbol) {
        String key = symbol.toUpperCase() + "_summary";
        JsonNode hit = summaryCache.get(key);
        if (hit != null) return hit;

        String ySymbol = toYahooSymbol(symbol);
        String url = BASE_SUMMARY + ySymbol + "?modules=" + MODULES_FULL;
        try {
            String raw = http.getForObject(url, String.class);
            JsonNode root = mapper.readTree(raw);
            JsonNode result = root.path("quoteSummary").path("result").get(0);
            summaryCache.put(key, result);
            return result;
        } catch (Exception e) {
            log.warn("YF summary failed for {}: {}", ySymbol, e.getMessage());
            return null;
        }
    }

    private double doubleVal(JsonNode node, String field) {
        JsonNode v = node.path(field);
        if (v.isMissingNode() || v.isNull()) return 0.0;
        if (v.isObject()) return v.path("raw").asDouble(0.0);
        return v.asDouble(0.0);
    }

    private long longVal(JsonNode node, String field) {
        JsonNode v = node.path(field);
        if (v.isMissingNode() || v.isNull()) return 0L;
        if (v.isObject()) return v.path("raw").asLong(0L);
        return v.asLong(0L);
    }

    private String rangeToInterval(String range) {
        return switch (range) {
            case "1d"  -> "5m";
            case "5d"  -> "15m";
            case "1mo" -> "1h";
            default    -> "1d";
        };
    }

    private String scoreToLabel(double score) {
        if (score <= 1.5) return "STRONG BUY";
        if (score <= 2.5) return "BUY";
        if (score <= 3.5) return "HOLD";
        if (score <= 4.5) return "SELL";
        return "STRONG SELL";
    }
}
