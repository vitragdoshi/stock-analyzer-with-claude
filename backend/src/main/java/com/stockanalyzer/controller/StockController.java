package com.stockanalyzer.controller;

import com.stockanalyzer.dto.ChartDataResponse;
import com.stockanalyzer.dto.StockAnalysisResponse;
import com.stockanalyzer.inference.InferenceEngine;
import com.stockanalyzer.inference.ScoreCard;
import com.stockanalyzer.service.ChartDataService;
import com.stockanalyzer.service.StockAnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/stock")
public class StockController {

    private final StockAnalysisService analysisService;
    private final ChartDataService     chartDataService;
    private final InferenceEngine      inferenceEngine;

    public StockController(StockAnalysisService analysisService,
                           ChartDataService chartDataService,
                           InferenceEngine inferenceEngine) {
        this.analysisService  = analysisService;
        this.chartDataService = chartDataService;
        this.inferenceEngine  = inferenceEngine;
    }

    // ── Existing endpoints ────────────────────────────────────────────────

    @GetMapping("/analyze/{symbol}")
    public ResponseEntity<StockAnalysisResponse> analyzeStock(@PathVariable String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(analysisService.analyze(symbol.trim().toUpperCase()));
    }

    @GetMapping("/chart/{symbol}")
    public ResponseEntity<ChartDataResponse> getChartData(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "3M") String timeframe) {
        return ResponseEntity.ok(chartDataService.getChartData(symbol, timeframe));
    }

    // ── New: inference-only endpoint ──────────────────────────────────────

    /**
     * GET /api/stock/inference/{symbol}
     *
     * Returns a lightweight ScoreCard produced by the inference engine.
     * Faster than /analyze because it skips the mock sub-analyses and
     * returns only the multi-factor weighted recommendation with full reasoning.
     *
     * Data sources: Yahoo Finance (prices, fundamentals, analyst ratings,
     * earnings) + RSS news feeds (ET Markets, Moneycontrol, Google News).
     */
    @GetMapping("/inference/{symbol}")
    public ResponseEntity<ScoreCard> getInferenceScore(@PathVariable String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            ScoreCard card = inferenceEngine.analyse(symbol.trim().toUpperCase());
            return ResponseEntity.ok(card);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ── Health / meta ─────────────────────────────────────────────────────

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "Indian Stock Analyzer API");
        status.put("version", "2.0.0");
        status.put("inferenceEngine", "ENABLED");
        status.put("dataSources", "Yahoo Finance, Google News RSS, ET Markets RSS, Moneycontrol RSS");
        return ResponseEntity.ok(status);
    }

    @GetMapping("/symbols")
    public ResponseEntity<Map<String, Object>> getAvailableSymbols() {
        Map<String, Object> result = new HashMap<>();
        result.put("symbols", new String[]{
            "RELIANCE", "TCS", "INFY", "HDFCBANK", "WIPRO",
            "ICICIBANK", "BAJFINANCE", "HCLTECH", "MARUTI", "TATAMOTORS",
            "SUNPHARMA", "TITAN", "ZOMATO", "PAYTM", "ONGC",
            "NTPC", "SBIN", "ADANIENT", "ADANIPORTS"
        });
        result.put("note", "Any NSE symbol is supported. Use /api/stock/inference/{symbol} " +
                "for a fast real-data scorecard, or /api/stock/analyze/{symbol} for the full report.");
        result.put("inferenceEndpoint", "/api/stock/inference/{symbol}");
        return ResponseEntity.ok(result);
    }
}
