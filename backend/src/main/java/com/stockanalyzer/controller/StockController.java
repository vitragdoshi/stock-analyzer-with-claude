package com.stockanalyzer.controller;

import com.stockanalyzer.dto.ChartDataResponse;
import com.stockanalyzer.dto.StockAnalysisResponse;
import com.stockanalyzer.service.ChartDataService;
import com.stockanalyzer.service.StockAnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/stock")
public class StockController {

    private final StockAnalysisService analysisService;
    private final ChartDataService chartDataService;

    public StockController(StockAnalysisService analysisService, ChartDataService chartDataService) {
        this.analysisService = analysisService;
        this.chartDataService = chartDataService;
    }

    @GetMapping("/analyze/{symbol}")
    public ResponseEntity<StockAnalysisResponse> analyzeStock(@PathVariable String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        StockAnalysisResponse response = analysisService.analyze(symbol.trim().toUpperCase());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/chart/{symbol}")
    public ResponseEntity<ChartDataResponse> getChartData(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "3M") String timeframe) {
        ChartDataResponse response = chartDataService.getChartData(symbol, timeframe);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "Indian Stock Analyzer API");
        status.put("version", "1.0.0");
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
        result.put("note", "Additional symbols are supported with generic mock data");
        return ResponseEntity.ok(result);
    }
}
