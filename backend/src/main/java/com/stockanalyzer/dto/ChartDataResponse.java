package com.stockanalyzer.dto;

import java.util.List;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChartDataResponse {
    String symbol;
    String timeframe;
    List<CandleData> candles;
    List<VolumeData> volumes;

    @Getter
    @Builder
    @Jacksonized
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static class CandleData {
        String date;
        double open;
        double high;
        double low;
        double close;
        long volume;
    }

    @Getter
    @Builder
    @Jacksonized
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static class VolumeData {
        String date;
        long volume;
        boolean aboveAverage;
    }
}
