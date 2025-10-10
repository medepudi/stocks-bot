// src/main/java/com/stock/demo/model/PositionSnapshot.java
package com.stock.demo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity @Table(name="position_snapshot")
public class PositionSnapshot {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;
  OffsetDateTime ts;
  String ticker;
  BigDecimal qty;
  BigDecimal avgPrice;
  public Long getId() {
	return id;
  }
  public void setId(Long id) {
	this.id = id;
  }
  public OffsetDateTime getTs() {
	return ts;
  }
  public void setTs(OffsetDateTime ts) {
	this.ts = ts;
  }
  public String getTicker() {
	return ticker;
  }
  public void setTicker(String ticker) {
	this.ticker = ticker;
  }
  public BigDecimal getQty() {
	return qty;
  }
  public void setQty(BigDecimal qty) {
	this.qty = qty;
  }
  public BigDecimal getAvgPrice() {
	return avgPrice;
  }
  public void setAvgPrice(BigDecimal avgPrice) {
	this.avgPrice = avgPrice;
  }
 
}
