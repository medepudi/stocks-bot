// src/main/java/com/stock/demo/controller/BrokerController.java
package com.stock.demo.controller;

import com.stock.demo.gateway.AlpacaGateway;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/broker")
public class BrokerController {

  private final AlpacaGateway alpaca;

  public BrokerController(AlpacaGateway alpaca) {
    this.alpaca = alpaca;
  }

  // GET /api/broker/account  → verifies keys & connectivity
  @GetMapping("/account")
  public Object account() {
    return alpaca.account();
  }

  // GET /api/broker/positions  → list open positions
  @GetMapping("/positions")
  public Object positions() {
    return alpaca.positions();
  }

  // POST /api/broker/market-order?symbol=DIA&side=buy&qty=1.0
  @PostMapping("/market-order")
  public Object marketOrder(
      @RequestParam String symbol,
      @RequestParam String side,          // buy | sell
      @RequestParam BigDecimal qty        // fractional ok if enabled
  ) {
    return alpaca.marketOrder(symbol, side, qty);
  }
}
