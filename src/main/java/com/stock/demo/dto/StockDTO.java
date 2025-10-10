package com.stock.demo.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

public record StockDTO(
        String ticker,
        LocalDate date,
        double open,
        double high,
        double low,
        double close,
        long volume,
        double vwap,
        long trades
) {
    public static StockDTO from(String ticker, AggregateBar bar) {
        LocalDate d = Instant.ofEpochMilli(bar.t())
                .atZone(ZoneOffset.UTC)
                .toLocalDate();

        return new StockDTO(
                ticker,
                d,
                bar.o(),
                bar.h(),
                bar.l(),
                bar.c(),
                bar.v(),
                bar.vw(),
                bar.n()
        );
    }

	public String ticker() {
		return ticker;
	}

	public LocalDate date() {
		return date;
	}

	public double open() {
		return open;
	}

	public double high() {
		return high;
	}

	public double low() {
		return low;
	}

	public double close() {
		return close;
	}

	public long volume() {
		return volume;
	}

	public double vwap() {
		return vwap;
	}

	public long trades() {
		return trades;
	}
    
    
}
