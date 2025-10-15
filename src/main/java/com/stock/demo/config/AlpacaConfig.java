package com.stock.demo.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class AlpacaConfig {

  @Bean
  @Qualifier("alpacaTradingClient")
  public RestClient alpacaTradingClient(AlpacaApiProperties props) {
    return RestClient.builder()
        .baseUrl(props.trading().baseUrl())
        .defaultHeader("APCA-API-KEY-ID", props.trading().keyId())
        .defaultHeader("APCA-API-SECRET-KEY", props.trading().secretKey())
        .build();
  }

  @Bean
  @Qualifier("alpacaDataClient")
  public RestClient alpacaDataClient(AlpacaApiProperties props) {
    return RestClient.builder()
        .baseUrl(props.data().baseUrl())
        .defaultHeader("APCA-API-KEY-ID", props.data().keyId())
        .defaultHeader("APCA-API-SECRET-KEY", props.data().secretKey())
        .build();
  }
}
