package com.stockanalyzer.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockanalyzer.client.yahoo.*;
import com.stockanalyzer.model.*;
import com.stockanalyzer.util.TtlCache;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
 *   Chart API   – https://query1.finance.yahoo.com/v8/finance/chart/{symbol}
 *                 (no auth required; returns 200)
 *   Summary API – https://query2.finance.yahoo.com/v10/finance/quoteSummary/{symbol}?modules=...&crumb=...
 *                 (requires crumb token + session cookie from YahooFinanceCrumbProvider)
 *
 * No API key is required. Rate limit ≈ 2 000 req/h per IP.
 * Results are cached in TtlCache instances to avoid redundant calls.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class YahooFinanceClient {

    // Chart API does NOT require crumb – use query1
    static final String BASE_CHART   = "https://query1.finance.yahoo.com/v8/finance/chart/";
    // Summary API DOES require crumb – must use query2
    static final String BASE_SUMMARY = "https://query2.finance.yahoo.com/v10/finance/quoteSummary/";

    static final String UA =
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    static final String MODULES_FULL =
            "summaryDetail,financialData,defaultKeyStatistics," +
            "recommendationTrend,earningsTrend,earningsHistory," +
            "upgradeDowngradeHistory,majorHoldersBreakdown,price";

    // Per-data-type TTL caches
    TtlCache<QuoteData>                   quoteCache    = new TtlCache<>(TimeUnit.MINUTES.toMillis(5));
    TtlCache<List<HistoricalPrice>>       histCache     = new TtlCache<>(TimeUnit.HOURS.toMillis(1));
    TtlCache<YfQuoteSummaryResponse.QuoteSummaryResult> summaryCache =
            new TtlCache<>(TimeUnit.HOURS.toMillis(6));
    TtlCache<AnalystConsensus>            analystCache  = new TtlCache<>(TimeUnit.HOURS.toMillis(12));
    TtlCache<EarningsData>                earningsCache = new TtlCache<>(TimeUnit.HOURS.toMillis(6));

    @NonNull RestTemplate              http;
    @NonNull ObjectMapper              mapper;
    @NonNull YahooFinanceCrumbProvider crumbProvider;

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
            YfChartResponse resp   = mapper.readValue(raw, YfChartResponse.class);
            YfChartResponse.ChartResult result = firstResult(resp);
            if (result == null) return null;

            YfChartResponse.Meta meta = result.getMeta();
            double cp = orZero(meta.getRegularMarketPrice());
            double pc = orZero(meta.getPreviousClose());

            QuoteData q = QuoteData.builder()
                    .symbol(key)
                    .companyName(meta.getLongName() != null ? meta.getLongName() : symbol)
                    .exchange("NSE")
                    .currency(meta.getCurrency() != null ? meta.getCurrency() : "INR")
                    .currentPrice(cp)
                    .previousClose(pc)
                    .dayOpen(orZero(meta.getRegularMarketOpen()))
                    .dayHigh(orZero(meta.getRegularMarketDayHigh()))
                    .dayLow(orZero(meta.getRegularMarketDayLow()))
                    .volume(orZero(meta.getRegularMarketVolume()))
                    .averageVolume(orZero(meta.getRegularMarketVolume()))
                    .week52High(orZero(meta.getFiftyTwoWeekHigh()))
                    .week52Low(orZero(meta.getFiftyTwoWeekLow()))
                    .marketCap(orZero(meta.getMarketCap()) / 1e7)
                    .change(cp - pc)
                    .changePercent(pc != 0 ? (cp - pc) / pc * 100 : 0)
                    .realData(true)
                    .build();

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
            YfChartResponse resp   = mapper.readValue(raw, YfChartResponse.class);
            YfChartResponse.ChartResult result = firstResult(resp);
            if (result == null || result.getTimestamp() == null) return prices;

            YfChartResponse.QuoteIndicator ohlcv = firstQuote(result);
            List<Double> adjCloseArr = adjClose(result);
            List<Long>   timestamps  = result.getTimestamp();

            for (int i = 0; i < timestamps.size(); i++) {
                Double close = safeGet(ohlcv.getClose(), i);
                if (close == null) continue;

                LocalDate date = Instant.ofEpochSecond(timestamps.get(i))
                        .atZone(ZoneOffset.ofHoursMinutes(5, 30)).toLocalDate();

                prices.add(HistoricalPrice.builder()
                        .date(date)
                        .open(safeDouble(ohlcv.getOpen(), i))
                        .high(safeDouble(ohlcv.getHigh(), i))
                        .low(safeDouble(ohlcv.getLow(), i))
                        .close(close)
                        .adjClose(adjCloseArr.size() > i && adjCloseArr.get(i) != null
                                ? adjCloseArr.get(i) : close)
                        .volume(safeLong(ohlcv.getVolume(), i))
                        .build());
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
        YfQuoteSummaryResponse.QuoteSummaryResult modules = fetchSummaryModules(symbol);
        if (modules == null) return null;

        YfQuoteSummaryResponse.SummaryDetail        sd = modules.getSummaryDetail();
        YfQuoteSummaryResponse.FinancialData         fd = modules.getFinancialData();
        YfQuoteSummaryResponse.DefaultKeyStatistics  ks = modules.getDefaultKeyStatistics();
        YfQuoteSummaryResponse.MajorHoldersBreakdown mh = modules.getMajorHoldersBreakdown();
        YfQuoteSummaryResponse.Price                 pr = modules.getPrice();

        double inrCr = 1e7;

        FundamentalData f = FundamentalData.builder()
                .symbol(symbol.toUpperCase())
                .peRatioTTM(sd != null ? n(sd.getTrailingPE()) : 0)
                .peRatioForward(sd != null ? n(sd.getForwardPE()) : 0)
                .dividendYield(sd != null ? n(sd.getDividendYield()) * 100 : 0)
                .dividendRate(sd != null ? n(sd.getDividendRate()) : 0)
                .pbRatio(ks != null ? n(ks.getPriceToBook()) : 0)
                .psRatio(ks != null ? n(ks.getPriceToSalesTrailing12Months()) : 0)
                .evToEbitda(ks != null ? n(ks.getEnterpriseToEbitda()) : 0)
                .pegRatio(ks != null ? n(ks.getPegRatio()) : 0)
                .epsTTM(ks != null ? n(ks.getTrailingEps()) : 0)
                .epsForward(ks != null ? n(ks.getForwardEps()) : 0)
                .bookValuePerShare(ks != null ? n(ks.getBookValue()) : 0)
                .earningsQuarterlyGrowth(ks != null ? n(ks.getEarningsQuarterlyGrowth()) * 100 : 0)
                .sharesOutstanding(ks != null && ks.getSharesOutstanding() != null
                        ? ks.getSharesOutstanding().asLong() : 0L)
                .grossMargins(fd != null ? n(fd.getGrossMargins()) * 100 : 0)
                .operatingMargins(fd != null ? n(fd.getOperatingMargins()) * 100 : 0)
                .profitMargins(fd != null ? n(fd.getProfitMargins()) * 100 : 0)
                .returnOnEquity(fd != null ? n(fd.getReturnOnEquity()) * 100 : 0)
                .returnOnAssets(fd != null ? n(fd.getReturnOnAssets()) * 100 : 0)
                .revenueGrowth(fd != null ? n(fd.getRevenueGrowth()) * 100 : 0)
                .earningsGrowth(fd != null ? n(fd.getEarningsGrowth()) * 100 : 0)
                .totalRevenueCr(fd != null ? n(fd.getTotalRevenue()) / inrCr : 0)
                .grossProfitCr(fd != null ? n(fd.getGrossProfits()) / inrCr : 0)
                .ebitdaCr(fd != null ? n(fd.getEbitda()) / inrCr : 0)
                .totalDebtCr(fd != null ? n(fd.getTotalDebt()) / inrCr : 0)
                .totalCashCr(fd != null ? n(fd.getTotalCash()) / inrCr : 0)
                .currentRatio(fd != null ? n(fd.getCurrentRatio()) : 0)
                .debtToEquity(fd != null ? n(fd.getDebtToEquity()) / 100 : 0)
                .operatingCashflowCr(fd != null ? n(fd.getOperatingCashflow()) / inrCr : 0)
                .freeCashflowCr(fd != null ? n(fd.getFreeCashflow()) / inrCr : 0)
                .heldByInsidersPercent(mh != null ? n(mh.getInsidersPercentHeld()) * 100 : 0)
                .heldByInstitutionsPercent(mh != null ? n(mh.getInstitutionsPercentHeld()) * 100 : 0)
                .marketCapCr(pr != null ? n(pr.getMarketCap()) / inrCr : 0)
                .realData(true)
                .build();

        log.info("YF fundamentals fetched for {}", symbol);
        return f;
    }

    // ── Analyst consensus ─────────────────────────────────────────────────

    public AnalystConsensus getAnalystConsensus(String symbol) {
        String key = symbol.toUpperCase() + "_analyst";
        AnalystConsensus hit = analystCache.get(key);
        if (hit != null) return hit;

        YfQuoteSummaryResponse.QuoteSummaryResult modules = fetchSummaryModules(symbol);
        if (modules == null) return null;

        YfQuoteSummaryResponse.RecommendationTrend rt = modules.getRecommendationTrend();
        YfQuoteSummaryResponse.FinancialData        fd = modules.getFinancialData();
        YfQuoteSummaryResponse.Price               pr = modules.getPrice();

        AnalystConsensus.AnalystConsensusBuilder builder = AnalystConsensus.builder()
                .symbol(symbol.toUpperCase());

        List<YfQuoteSummaryResponse.RecommendationPeriod> trend =
                (rt != null && rt.getTrend() != null) ? rt.getTrend() : List.of();
        if (!trend.isEmpty()) {
            YfQuoteSummaryResponse.RecommendationPeriod latest = trend.get(0);
            int sb = latest.getStrongBuy(), buy = latest.getBuy(),
                hold = latest.getHold(), sell = latest.getSell(), ss = latest.getStrongSell();
            int total = sb + buy + hold + sell + ss;
            double score = total > 0
                    ? (1.0 * sb + 2.0 * buy + 3.0 * hold + 4.0 * sell + 5.0 * ss) / total : 3.0;
            builder.strongBuyCount(sb).buyCount(buy).holdCount(hold)
                   .sellCount(sell).strongSellCount(ss).totalAnalysts(total)
                   .consensusScore(score).consensusLabel(scoreToLabel(score));
        }

        double cp  = pr != null ? n(pr.getRegularMarketPrice()) : 0;
        double tgt = fd != null ? n(fd.getTargetMeanPrice()) : 0;
        builder.targetLow(fd != null ? n(fd.getTargetLowPrice()) : 0)
               .targetMean(tgt)
               .targetHigh(fd != null ? n(fd.getTargetHighPrice()) : 0)
               .targetMedian(fd != null ? n(fd.getTargetMedianPrice()) : 0)
               .currentPrice(cp)
               .upsidePotentialPercent(cp > 0 && tgt > 0 ? (tgt - cp) / cp * 100 : 0);

        List<AnalystConsensus.UpgradeEvent> events = new ArrayList<>();
        YfQuoteSummaryResponse.UpgradeDowngradeHistory udh = modules.getUpgradeDowngradeHistory();
        if (udh != null && udh.getHistory() != null) {
            int limit = Math.min(10, udh.getHistory().size());
            for (int i = 0; i < limit; i++) {
                YfQuoteSummaryResponse.UpgradeDowngradeEvent ev = udh.getHistory().get(i);
                events.add(AnalystConsensus.UpgradeEvent.builder()
                        .firm(ev.getFirm())
                        .action(ev.getAction())
                        .fromGrade(ev.getFromGrade())
                        .toGrade(ev.getToGrade())
                        .epochTime(ev.getEpochGradeDate() != null
                                ? Instant.ofEpochSecond(ev.getEpochGradeDate()) : Instant.EPOCH)
                        .build());
            }
        }

        AnalystConsensus ac = builder.recentUpgrades(events).realData(true).build();
        analystCache.put(key, ac);
        log.info("YF analyst consensus fetched for {}", symbol);
        return ac;
    }

    // ── Earnings ──────────────────────────────────────────────────────────

    public EarningsData getEarningsData(String symbol) {
        String key = symbol.toUpperCase() + "_earnings";
        EarningsData hit = earningsCache.get(key);
        if (hit != null) return hit;

        YfQuoteSummaryResponse.QuoteSummaryResult modules = fetchSummaryModules(symbol);
        if (modules == null) return null;

        YfQuoteSummaryResponse.EarningsHistory earningsHistoryModule = modules.getEarningsHistory();
        YfQuoteSummaryResponse.EarningsTrend   earningsTrendModule   = modules.getEarningsTrend();

        List<EarningsData.QuarterlyEarning> qHist = new ArrayList<>();
        double totalSurprise = 0;
        int beats = 0;

        if (earningsHistoryModule != null && earningsHistoryModule.getHistory() != null) {
            for (YfQuoteSummaryResponse.EarningsHistoryEntry q : earningsHistoryModule.getHistory()) {
                double est  = n(q.getEpsEstimate());
                double act  = n(q.getEpsActual());
                double surp = n(q.getEpsDifference());
                double sPct = n(q.getSurprisePercent());
                boolean beat = act > est;
                if (beat) beats++;
                totalSurprise += sPct;
                qHist.add(EarningsData.QuarterlyEarning.builder()
                        .quarter(q.getQuarter())
                        .epsEstimate(est).epsActual(act)
                        .epsSurprise(surp).epsSurprisePercent(sPct)
                        .beat(beat)
                        .build());
            }
        }

        int n = qHist.size();

        List<EarningsData.EarningsTrend> trends = new ArrayList<>();
        if (earningsTrendModule != null && earningsTrendModule.getTrend() != null) {
            for (YfQuoteSummaryResponse.EarningsTrendPeriod t : earningsTrendModule.getTrend()) {
                YfQuoteSummaryResponse.EarningsEstimate epsEst = t.getEarningsEstimate();
                YfQuoteSummaryResponse.RevenueEstimate  revEst = t.getRevenueEstimate();
                double mean    = epsEst != null ? n(epsEst.getAvg()) : 0;
                double yearAgo = epsEst != null ? n(epsEst.getYearAgoEps()) : 0;
                double growth  = yearAgo != 0 ? (mean - yearAgo) / Math.abs(yearAgo) * 100 : 0;
                trends.add(EarningsData.EarningsTrend.builder()
                        .period(t.getPeriod())
                        .endDate(t.getEndDate())
                        .epsEstimateLow(epsEst != null ? n(epsEst.getLow()) : 0)
                        .epsEstimateHigh(epsEst != null ? n(epsEst.getHigh()) : 0)
                        .epsEstimateMean(mean)
                        .epsEstimateAvg(mean)
                        .revenueEstimateLow(revEst != null ? n(revEst.getLow()) : 0)
                        .revenueEstimateHigh(revEst != null ? n(revEst.getHigh()) : 0)
                        .revenueEstimateAvg(revEst != null ? n(revEst.getAvg()) : 0)
                        .epsGrowthRate(growth)
                        .build());
            }
        }

        String momentum = "STABLE";
        if (n >= 2) {
            double recent = qHist.get(n - 1).getEpsSurprisePercent();
            double prior  = qHist.get(n - 2).getEpsSurprisePercent();
            momentum = recent > prior ? "ACCELERATING" : recent < prior - 5 ? "DECELERATING" : "STABLE";
        }

        EarningsData ed = EarningsData.builder()
                .symbol(symbol.toUpperCase())
                .quarterlyHistory(qHist)
                .forwardTrends(trends)
                .beatRatePercent(n > 0 ? (double) beats / n * 100 : 50)
                .averageSurprise(n > 0 ? totalSurprise / n : 0)
                .earningsMomentum(momentum)
                .realData(true)
                .build();

        earningsCache.put(key, ed);
        log.info("YF earnings fetched for {}", symbol);
        return ed;
    }

    // ── Private: fetch quoteSummary ───────────────────────────────────────

    /**
     * Fetch all quoteSummary modules in one call using crumb authentication.
     * Result is cached for 6 hours.
     */
    private YfQuoteSummaryResponse.QuoteSummaryResult fetchSummaryModules(String symbol) {
        String key = symbol.toUpperCase() + "_summary";
        YfQuoteSummaryResponse.QuoteSummaryResult hit = summaryCache.get(key);
        if (hit != null) return hit;

        String ySymbol = toYahooSymbol(symbol);
        String baseUrl = BASE_SUMMARY + ySymbol + "?modules=" + MODULES_FULL;
        String url     = crumbProvider.buildSummaryUrl(baseUrl);

        try {
            HttpResponse<String> resp = sendGet(url, ySymbol);

            if (resp.statusCode() == 401) {
                log.warn("YF quoteSummary 401 for {} – retrying with fresh crumb", ySymbol);
                url  = crumbProvider.buildSummaryUrl(baseUrl);
                resp = sendGet(url, ySymbol);
            }

            if (resp.statusCode() != 200) {
                log.warn("YF quoteSummary HTTP {} for {}", resp.statusCode(), ySymbol);
                return null;
            }

            YfQuoteSummaryResponse parsed = mapper.readValue(resp.body(), YfQuoteSummaryResponse.class);
            if (parsed.getQuoteSummary() == null
                    || parsed.getQuoteSummary().getResult() == null
                    || parsed.getQuoteSummary().getResult().isEmpty()) {
                return null;
            }

            YfQuoteSummaryResponse.QuoteSummaryResult result =
                    parsed.getQuoteSummary().getResult().get(0);
            summaryCache.put(key, result);
            log.info("YF quoteSummary OK for {}", ySymbol);
            return result;

        } catch (Exception e) {
            log.warn("YF summary failed for {}: {}", ySymbol, e.getMessage());
            return null;
        }
    }

    private HttpResponse<String> sendGet(String url, String ySymbol) throws Exception {
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .GET()
                .header("User-Agent", UA)
                .header("Accept", "application/json, */*")
                .header("Referer", "https://finance.yahoo.com/quote/" + ySymbol + "/")
                .header("Accept-Language", "en-US,en;q=0.9")
                .build();
        return crumbProvider.getHttpClient().send(req, HttpResponse.BodyHandlers.ofString());
    }

    // ── Null-safe helpers ─────────────────────────────────────────────────

    private YfChartResponse.ChartResult firstResult(YfChartResponse resp) {
        if (resp == null || resp.getChart() == null
                || resp.getChart().getResult() == null
                || resp.getChart().getResult().isEmpty()) return null;
        return resp.getChart().getResult().get(0);
    }

    private YfChartResponse.QuoteIndicator firstQuote(YfChartResponse.ChartResult r) {
        if (r.getIndicators() == null || r.getIndicators().getQuote() == null
                || r.getIndicators().getQuote().isEmpty()) {
            return YfChartResponse.QuoteIndicator.builder().build();
        }
        return r.getIndicators().getQuote().get(0);
    }

    private List<Double> adjClose(YfChartResponse.ChartResult r) {
        if (r.getIndicators() == null || r.getIndicators().getAdjclose() == null
                || r.getIndicators().getAdjclose().isEmpty()) return List.of();
        YfChartResponse.AdjCloseIndicator ac = r.getIndicators().getAdjclose().get(0);
        return ac.getAdjclose() != null ? ac.getAdjclose() : List.of();
    }

    /** Unwrap a nullable YfNumber to a primitive double. */
    private double n(YfNumber v) { return v != null ? v.asDouble() : 0.0; }

    private double orZero(Double v) { return v != null ? v : 0.0; }
    private long   orZero(Long v)   { return v != null ? v : 0L; }

    private <T> T safeGet(List<T> list, int i) {
        return (list != null && i < list.size()) ? list.get(i) : null;
    }

    private double safeDouble(List<Double> list, int i) {
        Double v = safeGet(list, i); return v != null ? v : 0.0;
    }

    private long safeLong(List<Long> list, int i) {
        Long v = safeGet(list, i); return v != null ? v : 0L;
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
