package com.stock.demo.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a single bar (aggregate) returned by Polygon.io's /v2/aggs endpoint.
 * Example fields: open, high, low, close, volume, timestamp.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AggregateBar(
        @JsonProperty("v") long v,   // Volume
        @JsonProperty("vw") Double vw, // Volume-weighted average price (nullable)
        @JsonProperty("o") double o, // Open
        @JsonProperty("c") double c, // Close
        @JsonProperty("h") double h, // High
        @JsonProperty("l") double l, // Low
        @JsonProperty("t") long t,   // Unix timestamp in milliseconds
        @JsonProperty("n") long n    // Number of transactions
) {
    /** Converts the Polygon timestamp to the local trading day (US market time). */
    public LocalDate localDate() {
        return Instant.ofEpochMilli(t)
                .atZone(ZoneId.of("America/New_York")) // ✅ NYSE timezone is correct for DIA
                .toLocalDate();
    }

	public long v() {
		return v;
	}

	public Double vw() {
		return vw;
	}

	public double o() {
		return o;
	}

	public double c() {
		return c;
	}

	public double h() {
		return h;
	}

	public double l() {
		return l;
	}

	public long t() {
		return t;
	}

	public long n() {
		return n;
	}
    
    
}
