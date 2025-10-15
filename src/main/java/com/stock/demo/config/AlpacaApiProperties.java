package com.stock.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.alpaca")
public record AlpacaApiProperties(Trading trading, Data data) {

    public record Trading(String baseUrl, String keyId, String secretKey, String accountId) {}
    public record Data(String baseUrl, String keyId, String secretKey) {}
}
