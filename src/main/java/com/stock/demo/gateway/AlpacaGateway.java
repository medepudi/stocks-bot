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

  private final RestClient tradingClient;
  private final RestClient dataClient;

  private final AlpacaApiProperties props;

  public AlpacaGateway(
      @Qualifier("alpacaTradingClient") RestClient alpacaTradingClient,
      @Qualifier("alpacaDataClient") RestClient alpacaDataClient,
      AlpacaApiProperties props
  ) {
    this.tradingClient = alpacaTradingClient;
    this.dataClient = alpacaDataClient;
    this.props = props;
  }

  // ---------------- Trading endpoints (paper-api/api.alpaca.markets) ----------------

  public record OrderResponse(String id, String status, String symbol, String qty, String side, String type) {}

  public OrderResponse marketOrder(String symbol, String side, BigDecimal qty) {
    Map<String, Object> body = Map.of(
        "symbol", symbol,
        "side", side.toLowerCase(),
        "type", "market",
        "time_in_force", "day",
        "qty", qty.toPlainString()
    );
    return tradingClient.post()
        .uri("/v2/orders")
        .contentType(MediaType.APPLICATION_JSON)
        .body(body)
        .retrieve()
        .body(OrderResponse.class);
  }

  public record Position(String symbol, String qty, String avg_entry_price) {}
  public Position[] positions() {
    return tradingClient.get().uri("/v2/positions").retrieve().body(Position[].class);
  }

  public record Account(String id, String status, String non_marginable_buying_power, String buying_power) {}
  public Account account() {
    return tradingClient.get().uri("/v2/account").retrieve().body(Account.class);
  }

  public record Clock(boolean is_open, String next_open, String next_close) {}
  public Clock clock() {
    return tradingClient.get().uri("/v2/clock").retrieve().body(Clock.class);
  }

  // ---------------- Market data endpoints (data.alpaca.markets) ----------------
  
  //   GET https://data.alpaca.markets/v2/stocks/quotes/latest?symbols=DIA
//In AlpacaGateway
public record Quote(String t, double ap, double bp, long as, long bs) {}
public record LatestQuotesResponse(java.util.Map<String, Quote> quotes) {}

public Quote latestQuote(String symbol) {
 LatestQuotesResponse resp = dataClient.get()
     .uri("/v2/stocks/quotes/latest?symbols={symbols}", symbol)
     .retrieve()
     .body(LatestQuotesResponse.class);
 return (resp == null || resp.quotes() == null) ? null : resp.quotes().get(symbol);
}


  // If you also want the LATEST TRADE (single-symbol):
  //   GET https://data.alpaca.markets/v2/stocks/{symbol}/trades/latest
  public record LatestTrade(String symbol, String timestamp, double price, long size, String conditions) {}

  public LatestTrade latestTrade(String symbol) {
    // Response:
    // { "symbol":"DIA", "trade": { "t":"...", "p":340.12, "s":100, "x":"P", "c":["@","T"] } }
    record InnerTrade(String t, double p, long s, String x, java.util.List<String> c) {}
    record Wrapper(String symbol, InnerTrade trade) {}

    Wrapper w = dataClient.get()
        .uri("/v2/stocks/{symbol}/trades/latest", symbol)
        .retrieve()
        .body(Wrapper.class);

    if (w == null || w.trade == null) return null;

    return new LatestTrade(
        w.symbol(),
        w.trade.t(),
        w.trade.p(),
        w.trade.s(),
        (w.trade.c() == null ? null : String.join(",", w.trade.c()))
    );
  }
  

public record MultiTrade(
   double p,        // price
   long   s,        // size
   String t,        // timestamp
   String x,        // exchange/venue
   String z,        // tape
   java.util.List<String> c, // conditions
   long   i         // trade id
) {}

public record MultiTradesResponse(java.util.Map<String, MultiTrade> trades) {}

/** 
* Calls: https://data.alpaca.markets/v2/stocks/trades/latest?symbols=A,B,C
* Returns a symbol->price map (BigDecimal) for all symbols that came back.
*/
public MultiTradesResponse  latestTradesMulti(java.util.Collection<String> symbols) {
	 if (symbols == null || symbols.isEmpty()) return new MultiTradesResponse(Map.of());
// normalize to comma-separated list
 String joined = symbols.stream()
     .filter(s -> s != null && !s.isBlank())
     .map(s -> s.toUpperCase(java.util.Locale.ROOT))
     .distinct()
     .collect(java.util.stream.Collectors.joining(",")); // DIA,UDOW,...
 return dataClient.get()
	      .uri("/v2/stocks/trades/latest?symbols={symbols}", joined)
	      .retrieve()
	      .body(MultiTradesResponse.class);
	}

}
