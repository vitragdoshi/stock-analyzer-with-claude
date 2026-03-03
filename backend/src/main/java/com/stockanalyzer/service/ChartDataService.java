package com.stockanalyzer.service;

import com.stockanalyzer.dto.ChartDataResponse;
import com.stockanalyzer.dto.ChartDataResponse.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ChartDataService {

    private final MockDataService mockDataService;

    public ChartDataService(MockDataService mockDataService) {
        this.mockDataService = mockDataService;
    }

    public ChartDataResponse getChartData(String symbol, String timeframe) {
        String upperSymbol = symbol.toUpperCase().trim();
        double[] prices = mockDataService.getStockPrices(upperSymbol);
        double basePrice = prices[0];

        ChartDataResponse response = new ChartDataResponse();
        response.setSymbol(upperSymbol);
        response.setTimeframe(timeframe);

        int days = switch (timeframe.toUpperCase()) {
            case "1W" -> 7;
            case "1M" -> 30;
            case "3M" -> 90;
            case "6M" -> 180;
            case "1Y" -> 365;
            case "3Y" -> 1095;
            case "5Y" -> 1825;
            default -> 90;
        };

        // Generate mock OHLCV data using a random walk seeded by symbol
        Random rng = new Random((long) upperSymbol.hashCode() * 31 + timeframe.hashCode());
        List<CandleData> candles = new ArrayList<>();
        List<VolumeData> volumes = new ArrayList<>();

        double price = basePrice * 0.7; // start lower and trend to current
        long avgVol = 5000000 + (long)(rng.nextDouble() * 15000000);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        LocalDate startDate = LocalDate.now().minusDays(days);

        // Use a gentle upward trend with noise
        double trendFactor = Math.pow(basePrice / price, 1.0 / days);

        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);

            // Skip weekends
            if (date.getDayOfWeek().getValue() >= 6) continue;

            double dailyReturn = (rng.nextGaussian() * 0.015) + (Math.log(trendFactor));
            double open = price;
            price = price * Math.exp(dailyReturn);

            double intraVolatility = price * 0.01 * (0.5 + rng.nextDouble());
            double high = Math.max(open, price) + intraVolatility * rng.nextDouble();
            double low = Math.min(open, price) - intraVolatility * rng.nextDouble();
            double close = price;

            CandleData candle = new CandleData();
            candle.setDate(date.format(fmt));
            candle.setOpen(Math.round(open * 100.0) / 100.0);
            candle.setHigh(Math.round(high * 100.0) / 100.0);
            candle.setLow(Math.round(Math.max(0.01, low) * 100.0) / 100.0);
            candle.setClose(Math.round(close * 100.0) / 100.0);

            long vol = (long)(avgVol * (0.5 + rng.nextDouble() * 1.5));
            candle.setVolume(vol);
            candles.add(candle);

            VolumeData vd = new VolumeData();
            vd.setDate(date.format(fmt));
            vd.setVolume(vol);
            vd.setAboveAverage(vol > avgVol);
            volumes.add(vd);
        }

        response.setCandles(candles);
        response.setVolumes(volumes);
        return response;
    }
}
