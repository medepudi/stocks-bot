package com.stock.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MarketCalendarEntry(
 String name,       // e.g., "Thanksgiving"
 String date,       // e.g., "2020-11-27"
 String exchange,   // e.g., "NASDAQ"
 String status,     // "open" | "closed" | "early-close"
 String open,       // e.g., "2020-11-27T14:30:00.000Z"
 String close       // e.g., "2020-11-27T18:00:00.000Z"
) 
{

	public String name() {
		return name;
	}

	public String date() {
		return date;
	}

	public String exchange() {
		return exchange;
	}

	public String status() {
		return status;
	}

	public String open() {
		return open;
	}

	public String close() {
		return close;
	}
	
}
