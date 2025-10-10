package com.stock.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.stock.demo.config.AlpacaApiProperties;
import com.stock.demo.config.PolygonApiProperties;

@SpringBootApplication(scanBasePackages = "com.stock.demo")
@EnableScheduling
@EnableConfigurationProperties({ PolygonApiProperties.class, AlpacaApiProperties.class })
public class StockApplication {
  public static void main(String[] args) {
    SpringApplication.run(StockApplication.class, args);
  }
}

