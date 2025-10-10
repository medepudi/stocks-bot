package com.stock.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.alpaca")
public record AlpacaApiProperties(String baseUrl, String keyId, String secretKey, String accountId) { }
