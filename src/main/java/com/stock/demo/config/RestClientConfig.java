package com.stock.demo.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {
  @Bean(name = "polygonClient")
  public RestClient polygonClient(PolygonApiProperties props) {
    return RestClient.builder().baseUrl(props.baseUrl()).build();
  }
}

