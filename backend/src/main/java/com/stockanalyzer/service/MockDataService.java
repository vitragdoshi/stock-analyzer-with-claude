package com.stockanalyzer.service;

import com.stockanalyzer.dto.StockAnalysisResponse.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MockDataService {

    private static final Map<String, String[]> STOCK_META = new HashMap<>();
    private static final Map<String, double[]> STOCK_PRICES = new HashMap<>();

    static {
        STOCK_META.put("RELIANCE", new String[]{"Reliance Industries Limited", "NSE", "Oil & Gas / Conglomerate"});
        STOCK_META.put("TCS", new String[]{"Tata Consultancy Services", "NSE", "Information Technology"});
        STOCK_META.put("INFY", new String[]{"Infosys Limited", "NSE", "Information Technology"});
        STOCK_META.put("HDFC", new String[]{"HDFC Bank Limited", "NSE", "Banking & Finance"});
        STOCK_META.put("HDFCBANK", new String[]{"HDFC Bank Limited", "NSE", "Banking & Finance"});
        STOCK_META.put("WIPRO", new String[]{"Wipro Limited", "NSE", "Information Technology"});
        STOCK_META.put("ICICIBANK", new String[]{"ICICI Bank Limited", "NSE", "Banking & Finance"});
        STOCK_META.put("BAJFINANCE", new String[]{"Bajaj Finance Limited", "NSE", "NBFC"});
        STOCK_META.put("HCLTECH", new String[]{"HCL Technologies Limited", "NSE", "Information Technology"});
        STOCK_META.put("MARUTI", new String[]{"Maruti Suzuki India Limited", "NSE", "Automobile"});
        STOCK_META.put("TATAMOTORS", new String[]{"Tata Motors Limited", "NSE", "Automobile"});
        STOCK_META.put("SUNPHARMA", new String[]{"Sun Pharmaceutical Industries", "NSE", "Pharmaceuticals"});
        STOCK_META.put("TITAN", new String[]{"Titan Company Limited", "NSE", "Consumer Goods"});
        STOCK_META.put("ZOMATO", new String[]{"Zomato Limited", "NSE", "Internet & E-Commerce"});
        STOCK_META.put("PAYTM", new String[]{"One 97 Communications (Paytm)", "NSE", "Fintech"});
        STOCK_META.put("ONGC", new String[]{"Oil and Natural Gas Corporation", "NSE", "Oil & Gas"});
        STOCK_META.put("NTPC", new String[]{"NTPC Limited", "NSE", "Power & Energy"});
        STOCK_META.put("SBIN", new String[]{"State Bank of India", "NSE", "Banking"});
        STOCK_META.put("ADANIENT", new String[]{"Adani Enterprises Limited", "NSE", "Conglomerate"});
        STOCK_META.put("ADANIPORTS", new String[]{"Adani Ports & SEZ", "NSE", "Infrastructure"});

        // base price, dayHigh multiplier, dayLow multiplier, 52wHigh, 52wLow
        STOCK_PRICES.put("RELIANCE", new double[]{2856.40, 2891.25, 2831.60, 3017.00, 2220.30});
        STOCK_PRICES.put("TCS", new double[]{4125.80, 4162.50, 4099.20, 4425.00, 3311.00});
        STOCK_PRICES.put("INFY", new double[]{1834.55, 1862.70, 1815.40, 1953.90, 1351.25});
        STOCK_PRICES.put("HDFC", new double[]{1756.30, 1784.25, 1741.80, 1880.00, 1363.55});
        STOCK_PRICES.put("HDFCBANK", new double[]{1756.30, 1784.25, 1741.80, 1880.00, 1363.55});
        STOCK_PRICES.put("WIPRO", new double[]{567.45, 578.90, 561.30, 672.75, 430.25});
        STOCK_PRICES.put("ICICIBANK", new double[]{1289.70, 1312.50, 1275.40, 1340.55, 932.80});
        STOCK_PRICES.put("BAJFINANCE", new double[]{6893.25, 6971.40, 6821.75, 7830.00, 5750.00});
        STOCK_PRICES.put("HCLTECH", new double[]{1843.60, 1876.20, 1825.30, 1961.00, 1235.40});
        STOCK_PRICES.put("MARUTI", new double[]{12456.80, 12634.50, 12345.00, 13680.00, 9630.25});
        STOCK_PRICES.put("TATAMOTORS", new double[]{1023.45, 1048.90, 1009.70, 1179.00, 620.70});
        STOCK_PRICES.put("SUNPHARMA", new double[]{1789.30, 1819.60, 1765.80, 1912.55, 1043.75});
        STOCK_PRICES.put("TITAN", new double[]{3456.90, 3512.40, 3421.50, 3905.25, 2600.00});
        STOCK_PRICES.put("ZOMATO", new double[]{267.45, 275.30, 261.80, 304.70, 115.20});
        STOCK_PRICES.put("PAYTM", new double[]{456.70, 469.50, 448.30, 998.30, 310.20});
        STOCK_PRICES.put("ONGC", new double[]{289.35, 295.70, 284.50, 347.90, 195.60});
        STOCK_PRICES.put("NTPC", new double[]{387.60, 394.80, 381.25, 448.45, 220.25});
        STOCK_PRICES.put("SBIN", new double[]{876.45, 893.70, 867.80, 912.50, 543.20});
        STOCK_PRICES.put("ADANIENT", new double[]{2987.60, 3045.80, 2951.30, 3743.90, 1902.40});
        STOCK_PRICES.put("ADANIPORTS", new double[]{1345.80, 1378.90, 1325.40, 1608.00, 786.30});
    }

    public String[] getStockMeta(String symbol) {
        return STOCK_META.getOrDefault(symbol.toUpperCase(),
            new String[]{symbol + " Limited", "NSE", "General"});
    }

    public double[] getStockPrices(String symbol) {
        return STOCK_PRICES.getOrDefault(symbol.toUpperCase(),
            new double[]{500.00, 510.00, 492.00, 620.00, 380.00});
    }

    public List<String> getCompetitors(String symbol) {
        Map<String, List<String>> competitors = new HashMap<>();
        competitors.put("TCS", Arrays.asList("Infosys", "Wipro", "HCL Technologies", "Tech Mahindra"));
        competitors.put("INFY", Arrays.asList("TCS", "Wipro", "HCL Technologies", "Accenture India"));
        competitors.put("WIPRO", Arrays.asList("TCS", "Infosys", "HCL Technologies", "Cognizant"));
        competitors.put("HCLTECH", Arrays.asList("TCS", "Infosys", "Wipro", "Mphasis"));
        competitors.put("HDFCBANK", Arrays.asList("ICICI Bank", "Kotak Mahindra Bank", "Axis Bank", "SBI"));
        competitors.put("ICICIBANK", Arrays.asList("HDFC Bank", "Axis Bank", "Kotak Mahindra Bank", "SBI"));
        competitors.put("RELIANCE", Arrays.asList("ONGC", "Adani Enterprises", "Vedanta", "Tata Group"));
        competitors.put("TATAMOTORS", Arrays.asList("Maruti Suzuki", "M&M", "Hyundai India", "Bajaj Auto"));
        competitors.put("MARUTI", Arrays.asList("Tata Motors", "Hyundai India", "M&M", "Honda Cars India"));
        competitors.put("SUNPHARMA", Arrays.asList("Dr. Reddy's", "Cipla", "Divi's Labs", "Lupin"));
        competitors.put("BAJFINANCE", Arrays.asList("Shriram Finance", "Muthoot Finance", "HDFC Ltd", "Cholamandalam"));
        competitors.put("ZOMATO", Arrays.asList("Swiggy", "Magicpin", "Dunzo", "EatSure"));
        competitors.put("PAYTM", Arrays.asList("PhonePe", "Google Pay", "Amazon Pay", "CRED"));

        String sym = symbol.toUpperCase();
        return competitors.getOrDefault(sym, Arrays.asList("Competitor A", "Competitor B", "Competitor C"));
    }

    public Random getSeededRandom(String symbol) {
        return new Random(symbol.hashCode());
    }

    /** Convenience accessor for the company's display name. */
    public String getCompanyName(String symbol) {
        return getStockMeta(symbol)[0];
    }

    /** Convenience accessor for the sector string. */
    public String getSector(String symbol) {
        return getStockMeta(symbol)[2];
    }

    /** Convenience accessor for the base (current) price. */
    public double getPrice(String symbol) {
        return getStockPrices(symbol)[0];
    }
}
