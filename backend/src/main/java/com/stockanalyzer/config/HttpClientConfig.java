package com.stockanalyzer.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class HttpClientConfig {

    /**
     * RestTemplate used by all external data clients.
     * - Connect timeout: 8 s (external APIs can be slow to connect)
     * - Read timeout: 15 s (Yahoo Finance chart calls can be large)
     * - User-Agent mimics a modern browser so APIs that do basic bot
     *   filtering still serve us the data.
     */
    @Bean
    public RestTemplate externalRestTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(8))
                .setReadTimeout(Duration.ofSeconds(15))
                .defaultHeader("User-Agent",
                        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 " +
                        "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .defaultHeader("Accept", "application/json, text/html, */*")
                .defaultHeader("Accept-Language", "en-US,en;q=0.9")
                .build();
    }
}
