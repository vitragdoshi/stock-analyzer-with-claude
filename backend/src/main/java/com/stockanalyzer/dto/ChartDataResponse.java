package com.stockanalyzer.dto;

import java.util.List;

public class ChartDataResponse {
    private String symbol;
    private String timeframe;
    private List<CandleData> candles;
    private List<VolumeData> volumes;

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getTimeframe() { return timeframe; }
    public void setTimeframe(String timeframe) { this.timeframe = timeframe; }

    public List<CandleData> getCandles() { return candles; }
    public void setCandles(List<CandleData> candles) { this.candles = candles; }

    public List<VolumeData> getVolumes() { return volumes; }
    public void setVolumes(List<VolumeData> volumes) { this.volumes = volumes; }

    public static class CandleData {
        private String date;
        private double open;
        private double high;
        private double low;
        private double close;
        private long volume;

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }

        public double getOpen() { return open; }
        public void setOpen(double open) { this.open = open; }

        public double getHigh() { return high; }
        public void setHigh(double high) { this.high = high; }

        public double getLow() { return low; }
        public void setLow(double low) { this.low = low; }

        public double getClose() { return close; }
        public void setClose(double close) { this.close = close; }

        public long getVolume() { return volume; }
        public void setVolume(long volume) { this.volume = volume; }
    }

    public static class VolumeData {
        private String date;
        private long volume;
        private boolean aboveAverage;

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }

        public long getVolume() { return volume; }
        public void setVolume(long volume) { this.volume = volume; }

        public boolean isAboveAverage() { return aboveAverage; }
        public void setAboveAverage(boolean aboveAverage) { this.aboveAverage = aboveAverage; }
    }
}
