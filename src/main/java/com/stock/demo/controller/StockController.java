package com.stock.demo.controller;

import com.stock.demo.dto.StockDTO;
import com.stock.demo.service.StockService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
@RestController
@RequestMapping("/api/stocks")
public class StockController {

  private final StockService service;

  public StockController(StockService service) {
    this.service = service;
  }

  @GetMapping("/ping")
  public String ping() { return "ok"; }

  // GET /api/stocks/DIA/agg?from=2025-10-01&to=2025-10-08
  @GetMapping("/{ticker}/agg")
  public List<StockDTO> getDailyAggregates(
      @PathVariable("ticker") String ticker,
      @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam("to")   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
  ) {
    return service.getDailyAggregates(ticker, from, to);
  }
}
