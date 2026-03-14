package com.stockanalyzer.config;

import org.springframework.context.annotation.Configuration;

/**
 * Cache configuration placeholder.
 *
 * Caching is implemented via {@link com.stockanalyzer.util.TtlCache} — a
 * zero-dependency, ConcurrentHashMap-backed TTL store — rather than
 * Spring's @Cacheable / Caffeine, which require additional Maven artifacts
 * not available in the offline build environment.
 *
 * TtlCache instances are created directly in each client bean (YahooFinanceClient,
 * NewsAggregatorClient) with per-cache TTL settings.
 */
@Configuration
public class CacheConfig {
    // Intentionally empty — see TtlCache utility class.
}
