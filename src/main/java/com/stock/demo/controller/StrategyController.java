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
  
  private enum SlotParam { AUTO, REGULAR, EARLY }

  private SlotParam parseSlot(String s) {
    if (s == null || s.isBlank()) return SlotParam.AUTO;
    switch (s.trim().toLowerCase()) {
      case "regular": return SlotParam.REGULAR;
      case "early":   return SlotParam.EARLY;
      default:        return SlotParam.AUTO;
    }
  }

  private void runFetch(SlotParam slot) {
    switch (slot) {
      case REGULAR -> strategy.fetchLatest_regular();
      case EARLY   -> strategy.fetchLatest_early();
      case AUTO    -> {
        // Call both; only the correct slot will actually execute
        strategy.fetchLatest_regular();
        strategy.fetchLatest_early();
      }
    }
  }

  private void runUpdate(SlotParam slot) {
    switch (slot) {
      case REGULAR -> strategy.updateStreaks_regular();
      case EARLY   -> strategy.updateStreaks_early();
      case AUTO    -> {
        strategy.updateStreaks_regular();
        strategy.updateStreaks_early();
      }
    }
  }

  private void runTrade(SlotParam slot) {
    switch (slot) {
      case REGULAR -> strategy.trade_regular();
      case EARLY   -> strategy.trade_early();
      case AUTO    -> {
        strategy.trade_regular();
        strategy.trade_early();
      }
    }
  }


  /** Run the Polygon fetch step (regular 12:45 or early 9:45). Optional: ?slot=regular|early */
  @PostMapping("/fetchLatest")
  public ResponseEntity<String> fetchLatest(@RequestParam(required = false) String slot) {
    runFetch(parseSlot(slot));
    return ResponseEntity.ok("fetchLatest triggered");
  }

  /** Explicit regular-slot fetch (12:45 PT). */
  @PostMapping("/fetchLatest/regular")
  public ResponseEntity<String> fetchLatestRegular() {
    runFetch(SlotParam.REGULAR);
    return ResponseEntity.ok("fetchLatest (regular) triggered");
  }

  /** Explicit early-slot fetch (9:45 PT). */
  @PostMapping("/fetchLatest/early")
  public ResponseEntity<String> fetchLatestEarly() {
    runFetch(SlotParam.EARLY);
    return ResponseEntity.ok("fetchLatest (early) triggered");
  }

  /** Recompute streaks (regular 12:49 or early 9:49). Optional: ?slot=regular|early */
  @PostMapping("/updateStreaks")
  public ResponseEntity<String> updateStreaks(@RequestParam(required = false) String slot) {
    runUpdate(parseSlot(slot));
    return ResponseEntity.ok("updateStreaks triggered");
  }

  @PostMapping("/updateStreaks/regular")
  public ResponseEntity<String> updateStreaksRegular() {
    runUpdate(SlotParam.REGULAR);
    return ResponseEntity.ok("updateStreaks (regular) triggered");
  }

  @PostMapping("/updateStreaks/early")
  public ResponseEntity<String> updateStreaksEarly() {
    runUpdate(SlotParam.EARLY);
    return ResponseEntity.ok("updateStreaks (early) triggered");
  }

  /** Execute trade logic (regular 12:50 or early 9:50). Optional: ?slot=regular|early */
  @PostMapping("/trade")
  public ResponseEntity<String> trade(@RequestParam(required = false) String slot) {
    runTrade(parseSlot(slot));
    return ResponseEntity.ok("trade triggered");
  }

  @PostMapping("/trade/regular")
  public ResponseEntity<String> tradeRegular() {
    runTrade(SlotParam.REGULAR);
    return ResponseEntity.ok("trade (regular) triggered");
  }

  @PostMapping("/trade/early")
  public ResponseEntity<String> tradeEarly() {
    runTrade(SlotParam.EARLY);
    return ResponseEntity.ok("trade (early) triggered");
  }

  /** Convenience: run all three in order. Optional: ?slot=regular|early */
  @PostMapping("/runNow")
  public ResponseEntity<String> runNow(@RequestParam(required = false) String slot) {
    SlotParam sp = parseSlot(slot);
    runFetch(sp);
    runUpdate(sp);
    runTrade(sp);
    return ResponseEntity.ok("fetchLatest, updateStreaks, and trade executed");
  }
}
