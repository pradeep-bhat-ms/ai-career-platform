package com.pradeep.aicareerplatform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    /**
     * Reusable client for calling external public APIs (GitHub, etc).
     * Unauthenticated GitHub REST API calls are rate-limited to 60/hour per
     * IP — fine for a single developer testing this locally. If this needs
     * higher limits later, add a GITHUB_TOKEN env var and attach it as an
     * Authorization header per-request (never hardcode it here).
     */
    @Bean
    public WebClient githubWebClient() {
        return WebClient.builder()
                .baseUrl("https://api.github.com")
                .defaultHeader("Accept", "application/vnd.github+json")
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                .build();
    }
}