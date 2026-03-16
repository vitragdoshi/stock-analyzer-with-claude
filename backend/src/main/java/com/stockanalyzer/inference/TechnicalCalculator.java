package com.stockanalyzer.inference;

import com.stockanalyzer.model.HistoricalPrice;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Computes standard technical indicators from a list of OHLCV bars.
 *
 * All inputs assume bars are chronological (oldest first).
 * Indicators that require more bars than are available return NaN.
 */
@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TechnicalCalculator {

    // ── RSI ───────────────────────────────────────────────────────────────

    /** Wilder-smoothed RSI (default 14-period). */
    public double rsi(List<HistoricalPrice> bars, int period) {
        if (bars.size() < period + 1) return Double.NaN;
        double gains = 0, losses = 0;
        for (int i = bars.size() - period; i < bars.size(); i++) {
            double delta = bars.get(i).getClose() - bars.get(i - 1).getClose();
            if (delta > 0) gains  += delta;
            else           losses -= delta;
        }
        double avgGain = gains / period;
        double avgLoss = losses / period;
        if (avgLoss == 0) return 100;
        double rs = avgGain / avgLoss;
        return 100 - (100 / (1 + rs));
    }

    // ── EMA / SMA ─────────────────────────────────────────────────────────

    public double sma(List<HistoricalPrice> bars, int period) {
        if (bars.size() < period) return Double.NaN;
        double sum = 0;
        int start = bars.size() - period;
        for (int i = start; i < bars.size(); i++) sum += bars.get(i).getClose();
        return sum / period;
    }

    /** Exponential moving average (close prices). */
    public double ema(List<HistoricalPrice> bars, int period) {
        if (bars.size() < period) return Double.NaN;
        double k = 2.0 / (period + 1);
        // Seed with SMA
        double ema = 0;
        int start  = bars.size() - bars.size(); // 0
        int seedEnd = Math.min(period, bars.size());
        for (int i = 0; i < seedEnd; i++) ema += bars.get(i).getClose();
        ema /= seedEnd;
        for (int i = seedEnd; i < bars.size(); i++) {
            ema = bars.get(i).getClose() * k + ema * (1 - k);
        }
        return ema;
    }

    // ── MACD ──────────────────────────────────────────────────────────────

    public double[] macd(List<HistoricalPrice> bars) {
        // Returns [macdLine, signalLine, histogram]
        double fast   = ema(bars, 12);
        double slow   = ema(bars, 26);
        double macdLine = fast - slow;
        // Signal = 9-period EMA of MACD; approximate here as 9-bar EMA of (fast-slow)
        if (bars.size() < 35) return new double[]{macdLine, 0, macdLine};
        List<HistoricalPrice> sub = bars.subList(bars.size() - 35, bars.size());
        double[] prevMacd = new double[9];
        for (int i = 0; i < 9; i++) {
            List<HistoricalPrice> s2 = bars.subList(bars.size() - 35 + i, bars.size() - 26 + i);
            prevMacd[i] = ema(s2, 12) - ema(s2, 26);
        }
        double signal = 0;
        double k = 2.0 / 10;
        signal = prevMacd[0];
        for (int i = 1; i < prevMacd.length; i++) signal = prevMacd[i] * k + signal * (1 - k);
        return new double[]{macdLine, signal, macdLine - signal};
    }

    // ── Bollinger Bands ───────────────────────────────────────────────────

    /** Returns [upper, middle, lower] for 20-period, 2-sigma bands. */
    public double[] bollingerBands(List<HistoricalPrice> bars, int period, double sigma) {
        if (bars.size() < period) return new double[]{Double.NaN, Double.NaN, Double.NaN};
        double middle = sma(bars, period);
        double variance = 0;
        int start = bars.size() - period;
        for (int i = start; i < bars.size(); i++) {
            double d = bars.get(i).getClose() - middle;
            variance += d * d;
        }
        double stdDev = Math.sqrt(variance / period);
        return new double[]{middle + sigma * stdDev, middle, middle - sigma * stdDev};
    }

    // ── ATR ───────────────────────────────────────────────────────────────

    public double atr(List<HistoricalPrice> bars, int period) {
        if (bars.size() < period + 1) return Double.NaN;
        double atr = 0;
        int start = bars.size() - period;
        for (int i = start; i < bars.size(); i++) {
            double hl  = bars.get(i).getHigh()  - bars.get(i).getLow();
            double hc  = Math.abs(bars.get(i).getHigh()  - bars.get(i - 1).getClose());
            double lc  = Math.abs(bars.get(i).getLow()   - bars.get(i - 1).getClose());
            atr += Math.max(hl, Math.max(hc, lc));
        }
        return atr / period;
    }

    // ── OBV ───────────────────────────────────────────────────────────────

    /** On-Balance Volume over the supplied window. */
    public double obvTrend(List<HistoricalPrice> bars, int window) {
        if (bars.size() < window + 1) return 0;
        double obv = 0;
        int start = bars.size() - window;
        for (int i = start; i < bars.size(); i++) {
            double delta = bars.get(i).getClose() - bars.get(i - 1).getClose();
            if (delta > 0)      obv += bars.get(i).getVolume();
            else if (delta < 0) obv -= bars.get(i).getVolume();
        }
        return obv; // positive = accumulation, negative = distribution
    }

    // ── Stochastic ────────────────────────────────────────────────────────

    /** Returns [%K, %D] for the standard 14-3 stochastic oscillator. */
    public double[] stochastic(List<HistoricalPrice> bars, int kPeriod, int dPeriod) {
        if (bars.size() < kPeriod + dPeriod) return new double[]{50, 50};
        // %K values for the last dPeriod bars
        double[] kVals = new double[dPeriod];
        for (int j = 0; j < dPeriod; j++) {
            int end   = bars.size() - j;
            int start = end - kPeriod;
            double high = Double.MIN_VALUE, low = Double.MAX_VALUE;
            for (int i = start; i < end; i++) {
                high = Math.max(high, bars.get(i).getHigh());
                low  = Math.min(low,  bars.get(i).getLow());
            }
            double close = bars.get(end - 1).getClose();
            kVals[j] = (high != low) ? (close - low) / (high - low) * 100 : 50;
        }
        double k = kVals[0];
        double d = 0;
        for (double v : kVals) d += v;
        d /= dPeriod;
        return new double[]{k, d};
    }

    // ── Volume analysis ───────────────────────────────────────────────────

    /** Ratio of today's volume to the 20-day average. */
    public double volumeRatio(List<HistoricalPrice> bars) {
        if (bars.size() < 21) return 1.0;
        double avgVol = 0;
        int start = bars.size() - 20;
        for (int i = start; i < bars.size() - 1; i++) avgVol += bars.get(i).getVolume();
        avgVol /= 20;
        return avgVol > 0 ? bars.get(bars.size() - 1).getVolume() / avgVol : 1.0;
    }

    // ── Price momentum ────────────────────────────────────────────────────

    /** Return of close prices over last N days (as decimal, e.g. 0.12 = 12%). */
    public double momentum(List<HistoricalPrice> bars, int days) {
        if (bars.size() < days + 1) return 0;
        double then = bars.get(bars.size() - days - 1).getClose();
        double now  = bars.get(bars.size() - 1).getClose();
        return then != 0 ? (now - then) / then : 0;
    }
}
