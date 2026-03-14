package com.stockanalyzer.service;

import com.stockanalyzer.client.YahooFinanceClient;
import com.stockanalyzer.dto.ChartDataResponse;
import com.stockanalyzer.dto.ChartDataResponse.*;
import com.stockanalyzer.model.HistoricalPrice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Provides OHLCV candlestick data for the charting UI.
 *
 * Data priority:
 *   1. Yahoo Finance historical API (real market data)
 *   2. Mock random-walk generator (fallback when API is unavailable)
 */
@Service
public class ChartDataService {

    private static final Logger log = LoggerFactory.getLogger(ChartDataService.class);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final MockDataService     mockDataService;
    private final YahooFinanceClient  yahooClient;

    @Autowired
    public ChartDataService(MockDataService mockDataService,
                            YahooFinanceClient yahooClient) {
        this.mockDataService = mockDataService;
        this.yahooClient     = yahooClient;
    }

    public ChartDataResponse getChartData(String symbol, String timeframe) {
        String sym = symbol.toUpperCase().trim();

        ChartDataResponse response = new ChartDataResponse();
        response.setSymbol(sym);
        response.setTimeframe(timeframe);

        // Map UI timeframe → Yahoo Finance range
        String yfRange = toYfRange(timeframe);

        List<HistoricalPrice> real = null;
        try {
            real = yahooClient.getHistoricalPrices(sym, yfRange);
        } catch (Exception e) {
            log.warn("Chart data fetch failed for {}: {}", sym, e.getMessage());
        }

        if (real != null && !real.isEmpty()) {
            buildFromReal(response, real);
        } else {
            log.info("Using mock chart data for {}", sym);
            buildFromMock(response, sym, timeframe);
        }

        return response;
    }

    // ── Real data path ────────────────────────────────────────────────────

    private void buildFromReal(ChartDataResponse response, List<HistoricalPrice> bars) {
        List<CandleData> candles = new ArrayList<>();
        List<VolumeData> volumes = new ArrayList<>();

        long totalVol = 0;
        for (HistoricalPrice hp : bars) totalVol += hp.getVolume();
        long avgVol = bars.isEmpty() ? 5_000_000L : totalVol / bars.size();

        for (HistoricalPrice hp : bars) {
            CandleData c = new CandleData();
            c.setDate(hp.getDate().format(FMT));
            c.setOpen(round2(hp.getOpen()));
            c.setHigh(round2(hp.getHigh()));
            c.setLow(round2(hp.getLow()));
            c.setClose(round2(hp.getClose()));
            c.setVolume(hp.getVolume());
            candles.add(c);

            VolumeData v = new VolumeData();
            v.setDate(hp.getDate().format(FMT));
            v.setVolume(hp.getVolume());
            v.setAboveAverage(hp.getVolume() > avgVol);
            volumes.add(v);
        }

        response.setCandles(candles);
        response.setVolumes(volumes);
    }

    // ── Mock fallback ─────────────────────────────────────────────────────

    private void buildFromMock(ChartDataResponse response, String symbol, String timeframe) {
        double[] prices  = mockDataService.getStockPrices(symbol);
        double basePrice = prices[0];
        int days = switch (timeframe.toUpperCase()) {
            case "1W" -> 7;
            case "1M" -> 30;
            case "3M" -> 90;
            case "6M" -> 180;
            case "1Y" -> 365;
            case "3Y" -> 1095;
            case "5Y" -> 1825;
            default   -> 90;
        };

        Random rng = new Random((long) symbol.hashCode() * 31 + timeframe.hashCode());
        double price     = basePrice * 0.7;
        long avgVol      = 5_000_000L + (long)(rng.nextDouble() * 15_000_000L);
        LocalDate start  = LocalDate.now().minusDays(days);
        double trend     = Math.pow(basePrice / price, 1.0 / days);

        List<CandleData> candles = new ArrayList<>();
        List<VolumeData> volumes = new ArrayList<>();

        for (int i = 0; i < days; i++) {
            LocalDate date = start.plusDays(i);
            if (date.getDayOfWeek().getValue() >= 6) continue;

            double dailyReturn = rng.nextGaussian() * 0.015 + Math.log(trend);
            double open = price;
            price = price * Math.exp(dailyReturn);
            double intraVol = price * 0.01 * (0.5 + rng.nextDouble());
            double high  = Math.max(open, price) + intraVol * rng.nextDouble();
            double low   = Math.min(open, price) - intraVol * rng.nextDouble();

            CandleData c = new CandleData();
            c.setDate(date.format(FMT));
            c.setOpen(round2(open));
            c.setHigh(round2(high));
            c.setLow(round2(Math.max(0.01, low)));
            c.setClose(round2(price));
            long vol = (long)(avgVol * (0.5 + rng.nextDouble() * 1.5));
            c.setVolume(vol);
            candles.add(c);

            VolumeData v = new VolumeData();
            v.setDate(date.format(FMT));
            v.setVolume(vol);
            v.setAboveAverage(vol > avgVol);
            volumes.add(v);
        }

        response.setCandles(candles);
        response.setVolumes(volumes);
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private String toYfRange(String uiTimeframe) {
        return switch (uiTimeframe.toUpperCase()) {
            case "1W"  -> "5d";
            case "1M"  -> "1mo";
            case "3M"  -> "3mo";
            case "6M"  -> "6mo";
            case "1Y"  -> "1y";
            case "3Y"  -> "5y";  // YF doesn't have 3y; use 5y
            case "5Y"  -> "5y";
            default    -> "3mo";
        };
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
