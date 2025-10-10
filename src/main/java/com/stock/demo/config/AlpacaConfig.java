// src/main/java/com/stock/demo/config/AlpacaConfig.java
package com.stock.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class AlpacaConfig {
  @Bean(name = "alpacaClient")
  public RestClient alpacaClient(AlpacaApiProperties props) {
    return RestClient.builder()
        .baseUrl(props.baseUrl())
        .defaultHeader("APCA-API-KEY-ID", props.keyId())
        .defaultHeader("APCA-API-SECRET-KEY", props.secretKey())
        .build();
  }
}
