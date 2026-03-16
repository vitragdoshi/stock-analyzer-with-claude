package com.stockanalyzer.model;

import java.time.LocalDate;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.jackson.Jacksonized;

/**
 * Single day OHLCV bar from Yahoo Finance chart API.
 */
@Getter
@Builder
@Jacksonized
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class HistoricalPrice {

    LocalDate date;
    double open;
    double high;
    double low;
    double close;
    double adjClose;
    long volume;
}
