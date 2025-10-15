package com.stock.demo.service;

import com.stock.demo.gateway.AlpacaGateway;
import com.stock.demo.gateway.PolygonGateway;
import com.stock.demo.dto.MarketCalendarEntry;
import com.stock.demo.model.MarketBarDaily;
import com.stock.demo.model.StrategyState;
import com.stock.demo.model.TradeOrder;
import com.stock.demo.repo.MarketBarDailyRepo;
import com.stock.demo.repo.StrategyStateRepo;
import com.stock.demo.repo.TradeOrderRepo;
import com.stock.demo.component.TradeNotifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class StrategyService {
  private static final Logger log = LoggerFactory.getLogger(StrategyService.class);

  private static final String DRIVER = "DIA";
  private static final String INVERSE = "SDOW";
  private static final String LONG_LEVERED = "UDOW";

  private static final ZoneId PT = ZoneId.of("America/Los_Angeles");
  private static final ZoneId NY = ZoneId.of("America/New_York");

  private enum Slot { REGULAR, EARLY }

  private final PolygonGateway polygon;
  private final AlpacaGateway alpaca;
  private final MarketBarDailyRepo bars;
  private final StrategyStateRepo stateRepo;
  private final TradeOrderRepo orderRepo;
  private final TradeNotifier notifier;

  public StrategyService(PolygonGateway polygon, AlpacaGateway alpaca,
                         MarketBarDailyRepo bars, StrategyStateRepo stateRepo,
                         TradeOrderRepo orderRepo, TradeNotifier notifier) {
    this.polygon = polygon;
    this.alpaca = alpaca;
    this.bars = bars;
    this.stateRepo = stateRepo;
    this.orderRepo = orderRepo;
    this.notifier = notifier;
  }

  // ---------------- Day-type detection ----------------

  /** True if TODAY is an early-close trading day (e.g., ~10:00 AM PT close). */
  private boolean isEarlyCloseToday() {
    LocalDate today = LocalDate.now(PT);//Get “today’s date” in the Pacific Time zone (PT).

    // 1) Prefer Polygon "upcoming" if it includes a record for TODAY
    try {
      var entries = polygon.upcomingMarketStatus();
      if (entries != null && entries.length > 0) {
        for (MarketCalendarEntry e : entries) {
          if (e == null || e.date() == null) continue; //Skip this entry if it’s missing or doesn’t have a date.
          LocalDate d = LocalDate.parse(e.date());
          if (!d.isEqual(today)) continue; //If this entry isn’t for today, ignore it and move on.

          String status = e.status() == null ? "" : e.status().toLowerCase(Locale.ROOT);
          if (status.contains("early")) return true; // explicit early close

          // Infer by close time if available
          if (e.close() != null && !e.close().isBlank()) {
            Instant cInst = OffsetDateTime.parse(e.close()).toInstant();
            LocalTime closePT = cInst.atZone(PT).toLocalTime();
            if (closePT.isBefore(LocalTime.NOON)) return true;
          }
          // found today's entry and it's not early-close
          return false;
        }
      }
    } catch (Exception ex) {
      log.warn("isEarlyCloseToday: polygon calendar check failed: {}", ex.toString());
    }

    // 2) Fallback to Alpaca clock: next_close today before noon PT => early close
    try {
      var c = alpaca.clock();
      if (c != null && c.next_close() != null) {
        Instant nextClose = OffsetDateTime.parse(c.next_close()).toInstant();
        ZonedDateTime nextClosePT = nextClose.atZone(PT);
        //If the next close is today and happens before noon PT, that implies today is an early close → true.
        if (nextClosePT.toLocalDate().isEqual(today)
            && nextClosePT.toLocalTime().isBefore(LocalTime.NOON)) {
          return true;
        }
      }
    } catch (Exception ex) {
      log.warn("isEarlyCloseToday: alpaca clock check failed: {}", ex.toString());
    }

    return false;
  }

  /** Run only when the current day-type matches the expected slot. */
  private void runIfSlotMatches(Slot slot, Runnable task) {
    DayOfWeek dow = ZonedDateTime.now(PT).getDayOfWeek();
    if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
      log.debug("runIfSlotMatches: weekend detected, skipping");
      return;
    }

    boolean early = isEarlyCloseToday();
    boolean ok = (slot == Slot.EARLY && early) || (slot == Slot.REGULAR && !early);
    log.info("runIfSlotMatches: slot={} | earlyCloseToday={} | run={}", slot, early, ok);
    if (ok) task.run();
  }

  // ---------------- Fetch latest (near close) ----------------

  // Regular day: 12:45 PT
  @Scheduled(cron = "0 45 12 * * MON-FRI", zone = "America/Los_Angeles")
  public void fetchLatest_regular() {
    runIfSlotMatches(Slot.REGULAR, this::fetchLatestImpl);
  }

  // Early close: 9:45 PT
  @Scheduled(cron = "0 45 9 * * MON-FRI", zone = "America/Los_Angeles")
  public void fetchLatest_early() {
    runIfSlotMatches(Slot.EARLY, this::fetchLatestImpl);
  }

  //@Transactional (optional)
  private void fetchLatestImpl() {
    LocalDate todayPT = LocalDate.now(PT);
    LocalDate todayNY = LocalDate.now(NY);
    log.info("fetchLatestImpl: start | driver={} | todayPT={} | todayNY={}", DRIVER, todayPT, todayNY);

    // 1) Upsert last ~15 CLOSED daily bars (historical)
    var from = todayPT.minusDays(15);
    var daily = polygon.getDailyAggregates(DRIVER, from, todayPT);
    int histCount = 0;
    if (daily != null && daily.results != null) {
      for (var b : daily.results) {
        LocalDate d = b.localDate(); // NY trading day from your record method
        upsertDaily(DRIVER, d,
            toBD(b.o()), toBD(b.h()), toBD(b.l()), toBD(b.c()),
            toBD(b.vw()), b.v());
        histCount++;
      }
    }
    log.debug("fetchLatestImpl: historical upserts done | driver={} | bars={}", DRIVER, histCount);

    // 2) Ensure there's a bar for TODAY before the close (provisional from latest TRADE)
    boolean haveToday = bars.findByTickerAndDate(DRIVER, todayNY).isPresent();
    log.debug("fetchLatestImpl: haveToday? {} for {}", haveToday, DRIVER);

    if (!haveToday) {
      // (optional) yesterday’s close via Polygon prev-close (kept for diagnostics)
      try {
        var prev = polygon.getPreviousClose(DRIVER);
        if (prev != null && prev.results() != null && !prev.results().isEmpty()) {
          var pb = prev.results().get(0);
          BigDecimal yClose = toBD(pb.c());
          log.debug("fetchLatestImpl: prevClose (yesterday) for {} = {}", DRIVER, yClose);
        }
      } catch (Exception e) {
        log.warn("fetchLatestImpl: prevClose lookup failed for {}: {}", DRIVER, e.toString());
      }

      try {
        // Use Alpaca multi-symbol (future-proof) but for now just DRIVER
        var resp = alpaca.latestTradesMulti(List.of(DRIVER));
        if (resp != null && resp.trades() != null) {
          var mt = resp.trades().get(DRIVER.toUpperCase(Locale.ROOT));
          if (mt == null) {
            log.warn("fetchLatestImpl: latestTradesMulti returned no trade for {}", DRIVER);
          } else {
            // Parse timestamp into NY local date and verify it is today
            Instant instant = Instant.parse(mt.t()); // handles nanos
            LocalDate tradeDateNY = instant.atZone(NY).toLocalDate();

            if (tradeDateNY.equals(todayNY)) {
              BigDecimal last = BigDecimal.valueOf(mt.p()).setScale(6, RoundingMode.HALF_UP);
              upsertDaily(DRIVER, todayNY, null, null, null, last, null, null);
              log.info("fetchLatestImpl: provisional upsert for {} | date={} | last={}", DRIVER, todayNY, last);
            } else {
              log.warn("fetchLatestImpl: stale trade for {} | tradeDateNY={} != todayNY={} | skipping",
                  DRIVER, tradeDateNY, todayNY);
            }
          }
        } else {
          log.warn("fetchLatestImpl: latestTradesMulti returned null/empty for {}", DRIVER);
        }
      } catch (Exception e) {
        log.error("fetchLatestImpl: error obtaining latest trade for {}: {}", DRIVER, e.toString());
      }
    }

    log.info("fetchLatestImpl: done | driver={} | status={}", DRIVER, haveToday ? "DAILY" : "PROVISIONAL");
  }

  private void upsertDaily(String ticker, LocalDate date,
                           BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close,
                           BigDecimal vwap, Long volume) {
    MarketBarDaily entity = bars.findByTickerAndDate(ticker, date)
        .orElseGet(() -> {
          MarketBarDaily m = new MarketBarDaily();
          m.setTicker(ticker);
          m.setDate(date);
          return m;
        });
    if (open   != null) entity.setOpen(open);
    if (high   != null) entity.setHigh(high);
    if (low    != null) entity.setLow(low);
    if (close  != null) entity.setClose(close);
    if (vwap   != null) entity.setVwap(vwap);
    if (volume != null) entity.setVolume(volume);
    bars.save(entity);
  }

  private static BigDecimal toBD(Number n) {
    if (n == null) return null;
    if (n instanceof BigDecimal bd) return bd;
    return BigDecimal.valueOf(n.doubleValue());
  }

  // ---------------- Update streaks (a few minutes later) ----------------

  // Regular day: 12:49 PT
  @Scheduled(cron = "0 49 12 * * MON-FRI", zone = "America/Los_Angeles")
  public void updateStreaks_regular() {
    runIfSlotMatches(Slot.REGULAR, this::updateStreaksImpl);
  }

  // Early close: 9:49 PT
  @Scheduled(cron = "0 49 9 * * MON-FRI", zone = "America/Los_Angeles")
  public void updateStreaks_early() {
    runIfSlotMatches(Slot.EARLY, this::updateStreaksImpl);
  }

  private void updateStreaksImpl() {
	  var rows = bars.findByTickerOrderByDateDesc(DRIVER); // newest first
	  if (rows.size() < 2) return;                         // need at least today + yesterday

	  BigDecimal today  = rows.get(0).getClose();
	  BigDecimal yday   = rows.get(1).getClose();

	  // Direction of TODAY vs YESTERDAY: +1 up, -1 down, 0 flat
	  int directionToday = sign(today.compareTo(yday));

	  // Walk from newest → older, counting how many consecutive steps
	  // go in the SAME direction as the first non-flat step.
	  int streakDir = 0;   // +1 (up) or -1 (down). 0 means “haven’t found a move yet”.
	  int streakCnt = 0;

	  for (int i = 0; i + 1 < rows.size(); i++) {
	    BigDecimal newer = rows.get(i).getClose();
	    BigDecimal older = rows.get(i + 1).getClose();
	    int step = sign(newer.compareTo(older)); // +1 up, -1 down, 0 flat

	    if (step == 0) break;                 // flat ends the streak immediately
	    if (streakDir == 0) {                 // first directional move sets the direction
	      streakDir = step;
	      streakCnt = 1;
	    } else if (step == streakDir) {       // same direction → extend streak
	      streakCnt++;
	    } else {                              // direction flipped → streak ends
	      break;
	    }
	  }

	  int pos = (streakDir > 0) ? streakCnt : 0;
	  int neg = (streakDir < 0) ? streakCnt : 0;

	  StrategyState st = stateRepo.findByDriverTicker(DRIVER)
	      .orElseGet(() -> { var s = new StrategyState(); s.setDriverTicker(DRIVER); return s; });

	  st.setPosStreak(pos);
	  st.setNegStreak(neg);
	  st.setLastSignal(directionLabel(directionToday));
	  stateRepo.save(st);
	}

	// Helpers: tiny, self-explanatory
	private static int sign(int cmp) {
	  // cmp is -1, 0, or +1 already, but this keeps intent obvious
	  return Integer.compare(cmp, 0);
	}

	private static String directionLabel(int dir) {
	  return (dir > 0) ? "POS_FLOW" : (dir < 0) ? "NEG_FLOW" : "FLAT";
	}



  // ---------------- Trade (at bell window) ----------------

  // Regular day: 12:50 PT
  @Scheduled(cron = "0 50 12 * * MON-FRI", zone = "America/Los_Angeles")
  public void trade_regular() {
    runIfSlotMatches(Slot.REGULAR, this::tradeImpl);
  }

  // Early close: 9:50 PT
  @Scheduled(cron = "0 50 9 * * MON-FRI", zone = "America/Los_Angeles")
  public void trade_early() {
    runIfSlotMatches(Slot.EARLY, this::tradeImpl);
  }

  private void tradeImpl() {
	  Optional<StrategyState> opt = stateRepo.findByDriverTicker(DRIVER);
	  if (opt.isEmpty()) {
	    log.debug("tradeImpl: no strategy state yet for {}", DRIVER);
	    return;
	  }

	  StrategyState st = opt.get();

	  // primitives; guaranteed non-null
	  int pos = st.getPosStreak();
	  int neg = st.getNegStreak();

	  // --- TEST THRESHOLDS (lowered) ---
	  final int BUY_TRANCHE_1 = 3; // was 3
	  final int BUY_TRANCHE_2 = 4; // was 4

	  if (neg >= 1) {
	    // DIA negative vs yesterday → sell SDOW; buy UDOW on 2nd/3rd down days (for testing)
	    place("SELL", INVERSE, currentPositionQty(INVERSE));
	    if (neg >= BUY_TRANCHE_1) {
	      place("BUY", LONG_LEVERED, pctToQty(new BigDecimal("0.001"), LONG_LEVERED));
	    }
	    if (neg >= BUY_TRANCHE_2) {
	      place("BUY", LONG_LEVERED, pctToQty(new BigDecimal("0.001"), LONG_LEVERED));
	    }
	    if (neg >= 5) {
		      place("BUY", LONG_LEVERED, pctToQty(new BigDecimal("0.001"), LONG_LEVERED));
		    }
	    if (neg >= 6) {
		      place("BUY", LONG_LEVERED, pctToQty(new BigDecimal("0.001"), LONG_LEVERED));
		    }
	    st.setLastSignal("NEG_FLOW");
	  } else if (pos >= 1) {
	    // DIA positive vs yesterday → sell UDOW; buy SDOW on 2nd/3rd up days (for testing)
	    place("SELL", LONG_LEVERED, currentPositionQty(LONG_LEVERED));
	    if (pos >= BUY_TRANCHE_1) {
	      place("BUY", INVERSE, pctToQty(new BigDecimal("0.001"), INVERSE));
	    }
	    if (pos >= BUY_TRANCHE_2) {
	      place("BUY", INVERSE, pctToQty(new BigDecimal("0.001"), INVERSE));
	    }
	    if (pos >= 5) {
		      place("BUY", INVERSE, pctToQty(new BigDecimal("0.001"), INVERSE));
		    }
	    if (pos >= 6) {
		      place("BUY", INVERSE, pctToQty(new BigDecimal("0.001"), INVERSE));
		    }
	    st.setLastSignal("POS_FLOW");
	  }

	  stateRepo.save(st);
	  log.info("tradeImpl: completed for {} | posStreak={} | negStreak={} | lastSignal={}",
	      DRIVER, st.getPosStreak(), st.getNegStreak(), st.getLastSignal());
	}


  // --- helpers (stub); wire these to your portfolio value source and Alpaca positions ---
// to see How much money do I have
  private BigDecimal portfolioValueUsd() {
	  try {
	    var acct = alpaca.account();
	    if (acct != null && acct.non_marginable_buying_power() != null) {
	      return new BigDecimal(acct.non_marginable_buying_power());
	    }
	  } catch (Exception e) {
	    log.warn("portfolioValueUsd: failed to get account value, fallback to default: {}", e.toString());
	  }
	  // fallback if API fails
	 // return new BigDecimal("100000");
	  return null;
	}

  //to see whats the current price
  private BigDecimal lastPrice(String ticker) {
	  try {
	    // Try latest saved bar from DB
	    var barOpt = bars.findTopByTickerOrderByDateDesc(ticker);
	    if (barOpt.isPresent() && barOpt.get().getClose() != null) {
	      return barOpt.get().getClose();
	    }

	    // Otherwise, fall back to Alpaca’s latest trade
	    var resp = alpaca.latestTradesMulti(List.of(ticker));
	    if (resp != null && resp.trades() != null) {
	      var mt = resp.trades().get(ticker.toUpperCase(Locale.ROOT));
	      if (mt != null) {
	        return BigDecimal.valueOf(mt.p());
	      }
	    }
	  } catch (Exception e) {
	    log.warn("lastPrice: failed for {}: {}", ticker, e.toString());
	  }
	  // fallback hardcoded
	  return new BigDecimal("400");
	}


  private BigDecimal pctToQty(BigDecimal pct, String ticker) {
	  BigDecimal portfolio = portfolioValueUsd();
	    if (portfolio == null) {
	        log.warn("pctToQty: portfolio value not available");
	        return BigDecimal.ZERO;
	    }

	    BigDecimal price = lastPrice(ticker);
	    if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
	        log.warn("pctToQty: invalid price for {}", ticker);
	        return BigDecimal.ZERO;
	    }

	    BigDecimal notional = portfolio.multiply(pct);
	    BigDecimal qty = notional.divide(price, 6, RoundingMode.DOWN);
	    qty = qty.setScale(0, RoundingMode.DOWN);
     return qty;
  }

  private BigDecimal currentPositionQty(String ticker) {
    try {
      var positions = alpaca.positions(); // call Alpaca
      if (positions == null || positions.length == 0) {
        return BigDecimal.ZERO;
      }
      for (var p : positions) {
        if (p == null) continue;
        if (ticker.equalsIgnoreCase(p.symbol())) {
          // Alpaca qty is a string; can be negative for short positions.
          return new BigDecimal(p.qty());
        }
      }
    } catch (Exception e) {
      log.warn("currentPositionQty: failed to load positions: {}", e.toString());
    }
    return BigDecimal.ZERO;
  }

  private void place(String action, String ticker, BigDecimal qty) {
    if (qty == null || qty.signum() <= 0) {
      log.debug("place: skip {} {} invalid qty={}", action, ticker, qty);
      return;
    }
    try {
      var resp = alpaca.marketOrder(ticker, action, qty);
      var row = new TradeOrder();
      row.setAction(action);
      row.setTicker(ticker);
      row.setQty(qty);
      row.setStatus(resp != null ? resp.status() : "REQUESTED");
      row.setBrokerOrderId(resp != null ? resp.id() : null);
      orderRepo.save(row);
      notifier.tradePlaced(action, ticker, qty, row.getStatus(), row.getBrokerOrderId());
      log.info("place: {} {} qty={} status={} orderId={}", action, ticker, qty, row.getStatus(), row.getBrokerOrderId());
    } catch (Exception e) {
      log.error("place: {} {} qty={} failed: {}", action, ticker, qty, e.toString());
      notifier.tradePlaced(action, ticker, qty, "ERROR", null);
    }
  }
}
