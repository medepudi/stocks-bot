package com.stock.demo.gateway;

import com.stock.demo.config.PolygonApiProperties;
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
}
