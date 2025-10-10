// src/main/java/com/stock/demo/controller/NotifyController.java
package com.stock.demo.controller;

import com.stock.demo.component.TradeNotifier;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/notify")
public class NotifyController {
  private final TradeNotifier notifier;
  public NotifyController(TradeNotifier notifier) { this.notifier = notifier; }

  @PostMapping("/test")
  public String test(@RequestParam(defaultValue = "BUY") String action,
                     @RequestParam(defaultValue = "DIA") String ticker,
                     @RequestParam(defaultValue = "1.0") BigDecimal qty) {
    notifier.tradePlaced(action, ticker, qty, "TEST", "ORDER-TEST-123");
    return "sent";
  }
}
