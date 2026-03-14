package com.stockanalyzer.client;

import com.stockanalyzer.model.NewsArticle;
import com.stockanalyzer.util.TtlCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Aggregates stock news from multiple free, publicly available RSS feeds.
 *
 * Sources:
 *   1. Google News RSS  – company-specific; best relevance
 *   2. Economic Times Markets RSS
 *   3. Moneycontrol News RSS
 *   4. Business Standard Markets RSS
 *
 * Uses Java's built-in DOM XML parser — no external RSS library required.
 * Results are cached in a TtlCache (15 min TTL).
 */
@Component
public class NewsAggregatorClient {

    private static final Logger log = LoggerFactory.getLogger(NewsAggregatorClient.class);

    private static final String GOOGLE_NEWS_TEMPLATE =
            "https://news.google.com/rss/search?q=%s+NSE+stock+India&hl=en-IN&gl=IN&ceid=IN:en";

    // Only feeds confirmed reachable (HTTP 200) in connectivity tests.
    // ET Markets and Business Standard return 403/000 and are excluded.
    private static final List<String> GENERAL_FEEDS = List.of(
            "https://www.moneycontrol.com/rss/latestnews.xml"
    );

    private static final Map<String, Double> SOURCE_CREDIBILITY = Map.of(
            "Economic Times",    0.90,
            "Moneycontrol",      0.85,
            "Business Standard", 0.88,
            "Reuters",           0.95,
            "Bloomberg",         0.95,
            "Mint",              0.85,
            "Financial Express", 0.80,
            "NDTV Profit",       0.78,
            "Zee Business",      0.70,
            "Google News",       0.65
    );
    private static final double DEFAULT_CREDIBILITY = 0.60;

    // 15-minute TTL cache
    private final TtlCache<List<NewsArticle>> newsCache =
            new TtlCache<>(TimeUnit.MINUTES.toMillis(15));

    // RFC-822 date formatter common in RSS feeds
    private static final DateTimeFormatter RFC822 =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);

    // ── Public API ────────────────────────────────────────────────────────

    public List<NewsArticle> fetchNews(String symbol, String companyName, int maxArticles) {
        String cacheKey = symbol.toLowerCase() + "_" +
                (companyName != null ? companyName.toLowerCase() : "");
        List<NewsArticle> hit = newsCache.get(cacheKey);
        if (hit != null) return hit;

        List<NewsArticle> all = new ArrayList<>();

        // Company-specific Google News
        String query = buildQuery(symbol, companyName);
        all.addAll(fetchFeed(
                String.format(GOOGLE_NEWS_TEMPLATE, urlEncode(query)),
                "Google News", symbol, companyName, true));

        // General market feeds filtered for relevance
        for (String feedUrl : GENERAL_FEEDS) {
            all.addAll(fetchFeed(feedUrl, extractSource(feedUrl), symbol, companyName, false));
        }

        List<NewsArticle> result = deduplicate(all).stream()
                .sorted(Comparator.comparing(NewsArticle::getPublishedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(maxArticles)
                .collect(Collectors.toList());

        if (!result.isEmpty()) newsCache.put(cacheKey, result);
        return result;
    }

    // ── RSS parsing via built-in DOM ──────────────────────────────────────

    private List<NewsArticle> fetchFeed(String feedUrl, String sourceName,
                                        String symbol, String company,
                                        boolean isSpecific) {
        List<NewsArticle> articles = new ArrayList<>();
        try {
            URL url = new URL(feedUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(6_000);
            conn.setReadTimeout(10_000);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; StockAnalyzer/2.0)");
            conn.setRequestProperty("Accept", "application/rss+xml, application/xml, text/xml, */*");

            try (InputStream is = conn.getInputStream()) {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
                dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(is);
                doc.getDocumentElement().normalize();

                NodeList items = doc.getElementsByTagName("item");
                for (int i = 0; i < items.getLength(); i++) {
                    Element item = (Element) items.item(i);
                    String title   = text(item, "title");
                    String link    = text(item, "link");
                    String desc    = stripHtml(text(item, "description"));
                    String pubDate = text(item, "pubDate");

                    if (title.isEmpty()) continue;
                    if (!isSpecific && !isRelevant(title + " " + desc, symbol, company)) continue;

                    NewsArticle a = new NewsArticle();
                    a.setTitle(title);
                    a.setSummary(desc.length() > 500 ? desc.substring(0, 500) + "\u2026" : desc);
                    a.setUrl(link.isEmpty() ? "" : link);
                    a.setSource(sourceName);
                    a.setPublishedAt(parseDate(pubDate));
                    a.setSourceCredibility(
                            SOURCE_CREDIBILITY.getOrDefault(sourceName, DEFAULT_CREDIBILITY));
                    a.setCompanySpecific(isSpecific || isRelevant(title, symbol, company));
                    a.setRelevanceScore(computeRelevance(title + " " + desc, symbol, company));
                    articles.add(a);
                }
            }
        } catch (Exception e) {
            log.warn("RSS fetch failed [{}]: {}", feedUrl, e.getMessage());
        }
        return articles;
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private String text(Element parent, String tagName) {
        NodeList nl = parent.getElementsByTagName(tagName);
        if (nl.getLength() == 0) return "";
        return nl.item(0).getTextContent().trim();
    }

    private String stripHtml(String html) {
        return html.replaceAll("<[^>]+>", "")
                .replaceAll("&amp;", "&").replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">").replaceAll("&nbsp;", " ").trim();
    }

    private Instant parseDate(String raw) {
        if (raw == null || raw.isEmpty()) return Instant.now();
        try {
            return ZonedDateTime.parse(raw.trim(), RFC822).toInstant();
        } catch (DateTimeParseException e) {
            try { return Instant.parse(raw.trim()); } catch (Exception ex) {
                return Instant.now();
            }
        }
    }

    private boolean isRelevant(String text, String symbol, String company) {
        String t = text.toLowerCase();
        if (t.contains(symbol.toLowerCase())) return true;
        if (company != null && !company.isEmpty()) {
            String co = company.toLowerCase().split("\\s+")[0];
            if (co.length() > 3) return t.contains(co);
        }
        return false;
    }

    private double computeRelevance(String text, String symbol, String company) {
        double score = 0;
        String t = text.toLowerCase();
        if (t.contains(symbol.toLowerCase())) score += 0.6;
        if (company != null) {
            for (String w : company.toLowerCase().split("\\s+")) {
                if (w.length() > 3 && t.contains(w)) score += 0.2;
            }
        }
        return Math.min(1.0, score);
    }

    private String buildQuery(String symbol, String companyName) {
        if (companyName != null && !companyName.isEmpty()) {
            String[] parts = companyName.split("\\s+");
            return (parts.length >= 2 ? parts[0] + " " + parts[1] : parts[0]) + " stock";
        }
        return symbol + " NSE stock";
    }

    private List<NewsArticle> deduplicate(List<NewsArticle> articles) {
        Map<String, NewsArticle> seen = new LinkedHashMap<>();
        for (NewsArticle a : articles) {
            String norm = a.getTitle().toLowerCase().replaceAll("[^a-z0-9]", "");
            String key = norm.substring(0, Math.min(60, norm.length()));
            seen.putIfAbsent(key, a);
        }
        return new ArrayList<>(seen.values());
    }

    private String extractSource(String feedUrl) {
        if (feedUrl.contains("economictimes")) return "Economic Times";
        if (feedUrl.contains("moneycontrol"))  return "Moneycontrol";
        if (feedUrl.contains("business-standard")) return "Business Standard";
        if (feedUrl.contains("ndtv"))          return "NDTV Profit";
        return "Market Feed";
    }

    private String urlEncode(String s) {
        return s.replace(" ", "+").replace("&", "%26").replace(",", "%2C");
    }
}
