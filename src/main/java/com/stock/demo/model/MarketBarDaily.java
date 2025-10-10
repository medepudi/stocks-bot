// src/main/java/com/stock/demo/model/MarketBarDaily.java
package com.stock.demo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "market_bar_daily", uniqueConstraints = @UniqueConstraint(columnNames = {"ticker","date"}))
public class MarketBarDaily {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;
  String ticker;
  LocalDate date;
  BigDecimal open, high, low, close, vwap;
  Long volume;
  public Long getId() {
	return id;
  }
  public void setId(Long id) {
	this.id = id;
  }
  public String getTicker() {
	return ticker;
  }
  public void setTicker(String ticker) {
	this.ticker = ticker;
  }
  public LocalDate getDate() {
	return date;
  }
  public void setDate(LocalDate date) {
	this.date = date;
  }
  public BigDecimal getOpen() {
	return open;
  }
  public void setOpen(BigDecimal open) {
	this.open = open;
  }
  public BigDecimal getHigh() {
	return high;
  }
  public void setHigh(BigDecimal high) {
	this.high = high;
  }
  public BigDecimal getLow() {
	return low;
  }
  public void setLow(BigDecimal low) {
	this.low = low;
  }
  public BigDecimal getClose() {
	return close;
  }
  public void setClose(BigDecimal close) {
	this.close = close;
  }
  public BigDecimal getVwap() {
	return vwap;
  }
  public void setVwap(BigDecimal vwap) {
	this.vwap = vwap;
  }
  public Long getVolume() {
	return volume;
  }
  public void setVolume(Long volume) {
	this.volume = volume;
  }

}
