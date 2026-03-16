package com.stockanalyzer.client.yahoo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

/**
 * POJO mapping for Yahoo Finance Chart API v8:
 * {@code GET https://query1.finance.yahoo.com/v8/finance/chart/{symbol}?interval=...&range=...}
 *
 * Numeric fields in the {@code meta} block are plain doubles/longs (not {@link YfNumber} wrappers).
 * The {@code indicators.quote[0]} arrays may contain {@code null} elements for missing bars.
 */
@Getter
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class YfChartResponse {

    Chart chart;

    // ── chart wrapper ─────────────────────────────────────────────────────

    @Getter
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static class Chart {
        List<ChartResult> result;
    }

    // ── per-symbol result ─────────────────────────────────────────────────

    @Getter
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static class ChartResult {
        Meta         meta;
        List<Long>   timestamp;
        Indicators   indicators;
    }

    // ── quote metadata ────────────────────────────────────────────────────

    @Getter
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static class Meta {
        String currency;
        String symbol;
        String longName;
        Double regularMarketPrice;
        Double previousClose;
        Double regularMarketOpen;
        Double regularMarketDayHigh;
        Double regularMarketDayLow;
        Long   regularMarketVolume;
        Double fiftyTwoWeekHigh;
        Double fiftyTwoWeekLow;
        Long   marketCap;
    }

    // ── OHLCV + adj-close indicators ──────────────────────────────────────

    @Getter
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static class Indicators {
        List<QuoteIndicator>    quote;
        List<AdjCloseIndicator> adjclose;
    }

    @Getter
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static class QuoteIndicator {
        List<Double> open;
        List<Double> high;
        List<Double> low;
        List<Double> close;
        List<Long>   volume;
    }

    @Getter
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static class AdjCloseIndicator {
        List<Double> adjclose;
    }
}
