package com.stockanalyzer.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;

/**
 * Manages the Yahoo Finance crumb + session-cookie lifecycle.
 *
 * Yahoo Finance's quoteSummary API (v10) requires a two-step handshake:
 *   1. GET https://fc.yahoo.com   → sets session cookies in the client's cookie jar
 *   2. GET https://query2.finance.yahoo.com/v1/test/getcrumb  → returns a short crumb token
 *
 * The crumb must be appended as ?crumb=<value> on every quoteSummary request,
 * and the same session cookies must accompany each request.
 *
 * Crumbs are valid for ~24 hours; this provider refreshes automatically.
 * Thread-safety is guaranteed via synchronized methods.
 */
@Component
public class YahooFinanceCrumbProvider {

    private static final Logger log = LoggerFactory.getLogger(YahooFinanceCrumbProvider.class);

    private static final String FC_URL    = "https://fc.yahoo.com";
    private static final String CRUMB_URL =
            "https://query2.finance.yahoo.com/v1/test/getcrumb";

    private static final String UA =
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    // Crumb validity window (re-fetch 30 min before expiry to be safe)
    private static final Duration CRUMB_TTL = Duration.ofHours(23).plusMinutes(30);

    private final CookieManager cookieManager;
    private final HttpClient     httpClient;

    private String  crumb;
    private Instant crumbFetchedAt;

    public YahooFinanceCrumbProvider() {
        this.cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        this.httpClient    = HttpClient.newBuilder()
                .cookieHandler(cookieManager)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Return the current crumb, refreshing if necessary.
     * Returns {@code null} if the crumb cannot be obtained.
     */
    public synchronized String getCrumb() {
        if (isStale()) {
            refresh();
        }
        return crumb;
    }

    /**
     * Build a full quoteSummary URL with the crumb appended.
     * If no crumb is available, the URL is returned without it
     * (the call may fail with 401 in that case).
     */
    public String buildSummaryUrl(String baseUrl) {
        String c = getCrumb();
        return (c != null && !c.isEmpty()) ? baseUrl + "&crumb=" + c : baseUrl;
    }

    /**
     * Add the session cookies to an outgoing {@link HttpRequest.Builder}.
     * Required so Yahoo Finance accepts the request.
     */
    public HttpClient getHttpClient() {
        return httpClient;
    }

    // ── Private ───────────────────────────────────────────────────────────

    private boolean isStale() {
        return crumb == null
                || crumbFetchedAt == null
                || Instant.now().isAfter(crumbFetchedAt.plus(CRUMB_TTL));
    }

    private void refresh() {
        log.info("Refreshing Yahoo Finance crumb…");
        try {
            // Step 1 – prime the cookie jar
            httpClient.send(
                    HttpRequest.newBuilder(URI.create(FC_URL))
                            .GET()
                            .header("User-Agent", UA)
                            .header("Accept", "*/*")
                            .timeout(Duration.ofSeconds(10))
                            .build(),
                    HttpResponse.BodyHandlers.discarding());

            // Step 2 – fetch crumb
            HttpResponse<String> resp = httpClient.send(
                    HttpRequest.newBuilder(URI.create(CRUMB_URL))
                            .GET()
                            .header("User-Agent", UA)
                            .header("Accept", "text/plain, */*")
                            .header("Referer", "https://finance.yahoo.com/")
                            .timeout(Duration.ofSeconds(10))
                            .build(),
                    HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() == 200 && resp.body() != null && !resp.body().isBlank()) {
                crumb = resp.body().strip();
                crumbFetchedAt = Instant.now();
                log.info("Yahoo Finance crumb obtained: {} ({}…)", crumb.substring(0, Math.min(4, crumb.length())), "****");
            } else {
                log.warn("Crumb fetch returned HTTP {}: {}", resp.statusCode(), resp.body());
                crumb = null;
            }
        } catch (Exception e) {
            log.warn("Crumb refresh failed: {}", e.getMessage());
            crumb = null;
        }
    }
}
