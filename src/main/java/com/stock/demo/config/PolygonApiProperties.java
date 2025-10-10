package com.stock.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.polygon")
public record PolygonApiProperties(String baseUrl, String apiKey) { }
