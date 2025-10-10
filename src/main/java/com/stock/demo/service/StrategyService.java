// src/main/java/com/stock/demo/service/StrategyService.java
package com.stock.demo.service;

import com.stock.demo.gateway.AlpacaGateway;
import com.stock.demo.gateway.PolygonGateway;
import com.stock.demo.model.MarketBarDaily;
import com.stock.demo.model.StrategyState;
import com.stock.demo.model.TradeOrder;
import com.stock.demo.repo.MarketBarDailyRepo;
import com.stock.demo.repo.StrategyStateRepo;
import com.stock.demo.repo.TradeOrderRepo;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.util.Optional;

@Service
public class StrategyService {
  private static final String DRIVER = "DIA";
  private static final String INVERSE = "SDOW";

  private final PolygonGateway polygon;
  private final AlpacaGateway alpaca;
  private final MarketBarDailyRepo bars;
  private final StrategyStateRepo stateRepo;
  private final TradeOrderRepo orderRepo;

  public StrategyService(PolygonGateway polygon, AlpacaGateway alpaca,
                         MarketBarDailyRepo bars, StrategyStateRepo stateRepo,
                         TradeOrderRepo orderRepo) {
    this.polygon = polygon;
    this.alpaca = alpaca;
    this.bars = bars;
    this.stateRepo = stateRepo;
    this.orderRepo = orderRepo;
  }

  // 12:45 PM PT — pull yesterday/today bars so streaks have the latest close
  //@Scheduled(cron = "0 45 12 * * MON-FRI", zone = "America/Los_Angeles")
  @Scheduled(cron = "0 45 21 * * *", zone = "America/Los_Angeles")
  public void fetchLatest() {
    LocalDate today = LocalDate.now(ZoneId.of("America/Los_Angeles"));
    // Pull last ~10 days for resilience
    polygon.getDailyAggregates(DRIVER, today.minusDays(15), today)
           .getResults()
           .forEach(bar -> {/* your existing StockService is already persisting */});
  }

  // 12:49 PM PT — update streaks based on the most recent two closes
  //@Scheduled(cron = "0 49 12 * * MON-FRI", zone = "America/Los_Angeles")
  @Scheduled(cron = "0 46 21 * * *", zone = "America/Los_Angeles")
  public void updateStreaks() {
    // Find the last two trading days we have in DB for DIA
    // For brevity, assume “latest two rows by date” are available:
    var lastTwo = bars.findAll().stream()
      .filter(b -> DRIVER.equals(b.getTicker()))
      .sorted((a,b)->b.getDate().compareTo(a.getDate()))
      .limit(2)
      .toList();
    if (lastTwo.size() < 2) return;

    MarketBarDaily latest = lastTwo.get(0);
    MarketBarDaily prev   = lastTwo.get(1);
    boolean up = latest.getClose().compareTo(prev.getClose()) > 0;
    boolean down = latest.getClose().compareTo(prev.getClose()) < 0;

    StrategyState st = stateRepo.findByDriverTicker(DRIVER)
      .orElseGet(() -> {
        StrategyState s = new StrategyState();
        s.setDriverTicker(DRIVER);
        return s;
      });

    if (up) {
      st.setPosStreak(st.getPosStreak()+1);
      st.setNegStreak(0);
    } else if (down) {
      st.setNegStreak(st.getNegStreak()+1);
      st.setPosStreak(0);
    } // flat day => leave as-is

    stateRepo.save(st);
  }

  // 12:50 PM PT — place orders based on current streak state
  //@Scheduled(cron = "0 50 12 * * MON-FRI", zone = "America/Los_Angeles")
  @Scheduled(cron = "0 48 21 * * *", zone = "America/Los_Angeles")
  public void trade() {
    Optional<StrategyState> opt = stateRepo.findByDriverTicker(DRIVER);
    if (opt.isEmpty()) return;
    StrategyState st = opt.get();

    if (st.getNegStreak() >= 1) {
      // DIA is negative today vs yesterday
      int n = st.getNegStreak();
      // BUY DIA 20% on 3rd day and every additional day (3rd, 4th, 5th…)
      if (n >= 3) {
        place("BUY", DRIVER, pctToQty(BigDecimal.valueOf(0.01), DRIVER));
        st.setLastSignal("BUY_DIA");
      }
      // Also, if we hold SDOW we should sell it now (rule: sell SDOW when DIA is negative)
      // (In production, check broker positions and place SELL SDOW if qty>0)
      place("SELL", INVERSE, currentPositionQty(INVERSE));
      st.setLastSignal("SELL_SDOW");
    } else if (st.getPosStreak() >= 1) {
      // DIA is positive today vs yesterday
      int p = st.getPosStreak();

      // If any DIA position is open, sell DIA on first positive day
      place("SELL", DRIVER, currentPositionQty(DRIVER));
      st.setLastSignal("SELL_DIA");

      // If 3rd consecutive positive day, BUY SDOW 20%
      if (p >= 3) {
        place("BUY", INVERSE, pctToQty(BigDecimal.valueOf(0.20), INVERSE));
        st.setLastSignal("BUY_SDOW");
      }
    }

    stateRepo.save(st);
  }

  // --- helpers (stub); wire these to your portfolio value source and Alpaca positions ---

  private BigDecimal portfolioValueUsd() {
    // Replace with Alpaca account API or your own ledger
    return new BigDecimal("100000"); // example
  }

  private BigDecimal lastPrice(String ticker) {
    // you can map from latest MarketBarDaily close or hit Polygon last quote
    return new BigDecimal("400"); // placeholder
  }

  private BigDecimal pctToQty(BigDecimal pct, String ticker) {
    BigDecimal notional = portfolioValueUsd().multiply(pct);
    return notional.divide(lastPrice(ticker), 6, java.math.RoundingMode.DOWN);
  }

  private BigDecimal currentPositionQty(String ticker) {
    // query alpacaGateway.positions() and parse qty for ticker
    return BigDecimal.ZERO; // placeholder; implement reading from broker
  }

  private void place(String action, String ticker, BigDecimal qty) {
    if (qty.signum() <= 0) return;
    var resp = alpaca.marketOrder(ticker, action, qty);
    var row = new TradeOrder();
    row.setAction(action);
    row.setTicker(ticker);
    row.setQty(qty);
    row.setStatus(resp != null ? resp.status() : "REQUESTED");
    row.setBrokerOrderId(resp != null ? resp.id() : null);
    orderRepo.save(row);
  }
}
