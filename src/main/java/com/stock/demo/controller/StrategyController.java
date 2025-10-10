// src/main/java/com/stock/demo/controller/StrategyController.java
package com.stock.demo.controller;

import com.stock.demo.service.StrategyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/strategy")
public class StrategyController {

  private final StrategyService strategy;

  public StrategyController(StrategyService strategy) {
    this.strategy = strategy;
  }

  /** Run the Polygon fetch step (same code the scheduler runs at 12:45). */
  @PostMapping("/fetchLatest")
  public ResponseEntity<String> fetchLatest() {
    strategy.fetchLatest();
    return ResponseEntity.ok("fetchLatest triggered");
  }

  /** Recompute streaks (same as 12:49 job). */
  @PostMapping("/updateStreaks")
  public ResponseEntity<String> updateStreaks() {
    strategy.updateStreaks();
    return ResponseEntity.ok("updateStreaks triggered");
  }

  /** Execute trade logic (same as 12:50 job). */
  @PostMapping("/trade")
  public ResponseEntity<String> trade() {
    strategy.trade();
    return ResponseEntity.ok("trade triggered");
  }

  /** Convenience: run all three in order (fetch → update → trade). */
  @PostMapping("/runNow")
  public ResponseEntity<String> runNow() {
    strategy.fetchLatest();
    strategy.updateStreaks();
    strategy.trade();
    return ResponseEntity.ok("fetchLatest, updateStreaks, and trade executed");
  }
}
