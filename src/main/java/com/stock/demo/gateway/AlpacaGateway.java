// src/main/java/com/stock/demo/gateway/AlpacaGateway.java
package com.stock.demo.gateway;

import com.stock.demo.config.AlpacaApiProperties;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class AlpacaGateway {
  private final RestClient client;
  private final AlpacaApiProperties props;

  public AlpacaGateway(@Qualifier("alpacaClient")RestClient alpacaClient, AlpacaApiProperties props) {
    this.client = alpacaClient;
    this.props = props;
  }

  public record OrderResponse(String id, String status, String symbol, String qty, String side, String type){}

  public OrderResponse marketOrder(String symbol, String side, BigDecimal qty) {
    Map<String, Object> body = Map.of(
      "symbol", symbol,
      "side", side.toLowerCase(),       // "buy" | "sell"
      "type", "market",
      "time_in_force", "day",
      "qty", qty.toPlainString()
    );
    return client.post()
      .uri("/v2/orders")
      .contentType(MediaType.APPLICATION_JSON)
      .body(body)
      .retrieve()
      .body(OrderResponse.class);
  }

  // Optionally: get positions to snapshot
  public record Position(String symbol, String qty, String avg_entry_price){}
  public Position[] positions() {
    return client.get().uri("/v2/positions").retrieve().body(Position[].class);
  }
  
//in AlpacaGateway.java
public record Account(String id, String status, String buying_power, String portfolio_value) {}

public Account account() {
 return client.get().uri("/v2/account").retrieve().body(Account.class);
}

}
