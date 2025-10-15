package com.stock.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Trade(
    @JsonProperty("t") String timestamp,  // e.g. "2025-10-13T19:44:59.123456Z"
    @JsonProperty("x") String exchange,   // exchange code
    @JsonProperty("p") double price,      // trade price
    @JsonProperty("s") long size,         // trade size
    @JsonProperty("c") java.util.List<String> conditions // trade conditions
) {}

