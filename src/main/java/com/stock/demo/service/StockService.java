// src/main/java/com/stock/demo/service/StockService.java
package com.stock.demo.service;

import com.stock.demo.component.TradeNotifier;
import com.stock.demo.dto.StockAggResponse;
import com.stock.demo.dto.StockDTO;
import com.stock.demo.gateway.PolygonGateway;
import com.stock.demo.model.MarketBarDaily;
import com.stock.demo.repo.MarketBarDailyRepo;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class StockService {
  private final PolygonGateway gateway;
  private final MarketBarDailyRepo barRepo;
  private final TradeNotifier notifier;
  
  private static final String DRIVER = "DIA";

  public StockService(PolygonGateway gateway, MarketBarDailyRepo barRepo,   TradeNotifier notifier) {
    this.gateway = gateway;
    this.barRepo = barRepo;
    this.notifier = notifier;
  }

  public List<StockDTO> getDailyAggregates(String ticker, LocalDate from, LocalDate to) {
    StockAggResponse resp = gateway.getDailyAggregates(ticker, from, to);
    if (resp == null || resp.getResults() == null) return List.of();

    // map & persist idempotently
    resp.getResults().forEach(bar -> {
      LocalDate d = bar.localDate();
      barRepo.findByTickerAndDate(resp.getTicker(), d).orElseGet(() -> {
        MarketBarDaily e = new MarketBarDaily();
        e.setTicker(resp.getTicker());
        e.setDate(d);
        e.setOpen(BigDecimal.valueOf(bar.o()));
        e.setHigh(BigDecimal.valueOf(bar.h()));
        e.setLow(BigDecimal.valueOf(bar.l()));
        e.setClose(BigDecimal.valueOf(bar.c()));
        e.setVolume(bar.v());
        if (bar.vw() != null) {
          e.setVwap(BigDecimal.valueOf(bar.vw()));
        }
        return barRepo.save(e);
      });
    });

    return resp.getResults().stream()
        .map(b -> StockDTO.from(resp.getTicker(), b))
        .toList();
  }
}
