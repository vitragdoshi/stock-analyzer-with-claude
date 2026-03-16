package com.stockanalyzer.model;

import java.time.Instant;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.jackson.Jacksonized;

/**
 * Live quote data fetched from Yahoo Finance (or NSE fallback).
 * All prices are in INR.
 */
@Data
@Builder
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuoteData {

    String symbol;
    String companyName;
    String exchange;       // NSE or BSE

    // Price
    double currentPrice;
    double previousClose;
    double dayOpen;
    double dayHigh;
    double dayLow;
    double change;
    double changePercent;

    // Volume
    long volume;
    long averageVolume;    // 3-month avg
    long averageVolume10d; // 10-day avg

    // 52-week range
    double week52High;
    double week52Low;

    // Market metrics
    double marketCap;      // in INR crore
    double beta;
    String currency;

    // Data freshness
    @Builder.Default Instant fetchedAt = Instant.now();
    boolean realData;    // false = fell back to mock
}
