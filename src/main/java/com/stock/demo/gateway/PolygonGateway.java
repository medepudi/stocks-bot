package com.stock.demo.gateway;

import com.stock.demo.config.PolygonApiProperties;
import com.stock.demo.dto.AggregateBar;
import com.stock.demo.dto.MarketCalendarEntry;
import com.stock.demo.dto.MarketStatusNow;
import com.stock.demo.dto.StockAggResponse;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;

//src/main/java/com/stock/demo/gateway/PolygonGateway.java
@Component
public class PolygonGateway {
private final RestClient client;
private final PolygonApiProperties props;

public PolygonGateway(@Qualifier("polygonClient")RestClient polygonClient, PolygonApiProperties props) {
 this.client = polygonClient;
 this.props = props;
}

public StockAggResponse getDailyAggregates(String ticker, LocalDate from, LocalDate to) {
 return client.get()
   .uri(uriBuilder -> uriBuilder
     .path("/v2/aggs/ticker/{ticker}/range/1/day/{from}/{to}")
     .queryParam("adjusted", "true")
     .queryParam("sort", "asc")
     .queryParam("apiKey", props.apiKey())
     .build(ticker, from, to))
   .retrieve()
   .body(StockAggResponse.class);
}

/** GET /v1/marketstatus/upcoming */
public MarketCalendarEntry[] upcomingMarketStatus() {
  return client.get()
      .uri(uriBuilder -> uriBuilder
          .path("/v1/marketstatus/upcoming")
          .queryParam("apiKey", props.apiKey())
          .build())
      .retrieve()
      .body(MarketCalendarEntry[].class);
}

/** GET /v1/marketstatus/now (optional) */
public MarketStatusNow marketStatusNow() {
  return client.get()
      .uri(uriBuilder -> uriBuilder
          .path("/v1/marketstatus/now")
          .queryParam("apiKey", props.apiKey())
          .build())
      .retrieve()
      .body(MarketStatusNow.class);
}

public record PrevCloseResponse(java.util.List<AggregateBar> results, String status) {}

public PrevCloseResponse getPreviousClose(String ticker) {
return client.get()
   .uri(uriBuilder -> uriBuilder
       .path("/v2/aggs/ticker/{ticker}/prev")
       .queryParam("adjusted", "true")
       .queryParam("apiKey", props.apiKey())
       .build(ticker))
   .retrieve()
   .body(PrevCloseResponse.class);
}

}
