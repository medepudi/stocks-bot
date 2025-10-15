package com.stock.demo.dto;
//A simple DTO to carry latest trade data back to  service layer
public record LatestTrade(
 String symbol,
 String timestamp,
 double price,
 long size,
 String conditions 
) {}
