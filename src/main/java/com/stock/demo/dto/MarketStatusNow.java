package com.stock.demo.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Minimal "now" status; optional but handy if you want live open/close. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MarketStatusNow(
 String market,     // "open" | "closed"
 String serverTime  // current time from Polygon server (ISO)
) {}
