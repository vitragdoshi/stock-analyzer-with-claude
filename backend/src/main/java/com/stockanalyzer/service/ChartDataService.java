package com.stockanalyzer.service;

import com.stockanalyzer.client.YahooFinanceClient;
import com.stockanalyzer.dto.ChartDataResponse;
import com.stockanalyzer.dto.ChartDataResponse.*;
import com.stockanalyzer.model.HistoricalPrice;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChartDataService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @NonNull MockDataService     mockDataService;
    @NonNull YahooFinanceClient  yahooClient;

    public ChartDataResponse getChartData(String symbol, String timeframe) {
        String sym = symbol.toUpperCase().trim();

        // Map UI timeframe → Yahoo Finance range
        String yfRange = toYfRange(timeframe);

        List<HistoricalPrice> real = null;
        try {
            real = yahooClient.getHistoricalPrices(sym, yfRange);
        } catch (Exception e) {
            log.warn("Chart data fetch failed for {}: {}", sym, e.getMessage());
        }

        List<CandleData> candles;
        List<VolumeData> volumes;

        if (real != null && !real.isEmpty()) {
            candles = new ArrayList<>();
            volumes = new ArrayList<>();
            buildFromReal(candles, volumes, real);
        } else {
            log.info("Using mock chart data for {}", sym);
            candles = new ArrayList<>();
            volumes = new ArrayList<>();
            buildFromMock(candles, volumes, sym, timeframe);
        }

        return ChartDataResponse.builder()
                .symbol(sym)
                .timeframe(timeframe)
                .candles(candles)
                .volumes(volumes)
                .build();
    }

    // ── Real data path ────────────────────────────────────────────────────

    private void buildFromReal(List<CandleData> candles, List<VolumeData> volumes,
                               List<HistoricalPrice> bars) {
        long totalVol = 0;
        for (HistoricalPrice hp : bars) totalVol += hp.getVolume();
        long avgVol = bars.isEmpty() ? 5_000_000L : totalVol / bars.size();

        for (HistoricalPrice hp : bars) {
            candles.add(CandleData.builder()
                    .date(hp.getDate().format(FMT))
                    .open(round2(hp.getOpen()))
                    .high(round2(hp.getHigh()))
                    .low(round2(hp.getLow()))
                    .close(round2(hp.getClose()))
                    .volume(hp.getVolume())
                    .build());

            volumes.add(VolumeData.builder()
                    .date(hp.getDate().format(FMT))
                    .volume(hp.getVolume())
                    .aboveAverage(hp.getVolume() > avgVol)
                    .build());
        }
    }

    // ── Mock fallback ─────────────────────────────────────────────────────

    private void buildFromMock(List<CandleData> candles, List<VolumeData> volumes,
                               String symbol, String timeframe) {
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

        for (int i = 0; i < days; i++) {
            LocalDate date = start.plusDays(i);
            if (date.getDayOfWeek().getValue() >= 6) continue;

            double dailyReturn = rng.nextGaussian() * 0.015 + Math.log(trend);
            double open = price;
            price = price * Math.exp(dailyReturn);
            double intraVol = price * 0.01 * (0.5 + rng.nextDouble());
            double high  = Math.max(open, price) + intraVol * rng.nextDouble();
            double low   = Math.min(open, price) - intraVol * rng.nextDouble();
            long vol = (long)(avgVol * (0.5 + rng.nextDouble() * 1.5));

            candles.add(CandleData.builder()
                    .date(date.format(FMT))
                    .open(round2(open))
                    .high(round2(high))
                    .low(round2(Math.max(0.01, low)))
                    .close(round2(price))
                    .volume(vol)
                    .build());

            volumes.add(VolumeData.builder()
                    .date(date.format(FMT))
                    .volume(vol)
                    .aboveAverage(vol > avgVol)
                    .build());
        }
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
