// src/main/java/com/stock/demo/model/TradeOrder.java
package com.stock.demo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity @Table(name="trade_order")
public class TradeOrder {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;
  OffsetDateTime ts;
  String action;      // BUY/SELL
  String ticker;
  BigDecimal qty;
  BigDecimal notional;
  String status;      // REQUESTED/...
  String brokerOrderId;
  String brokerMsg;
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
  public String getAction() {
	return action;
  }
  public void setAction(String action) {
	this.action = action;
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
  public BigDecimal getNotional() {
	return notional;
  }
  public void setNotional(BigDecimal notional) {
	this.notional = notional;
  }
  public String getStatus() {
	return status;
  }
  public void setStatus(String status) {
	this.status = status;
  }
  public String getBrokerOrderId() {
	return brokerOrderId;
  }
  public void setBrokerOrderId(String brokerOrderId) {
	this.brokerOrderId = brokerOrderId;
  }
  public String getBrokerMsg() {
	return brokerMsg;
  }
  public void setBrokerMsg(String brokerMsg) {
	this.brokerMsg = brokerMsg;
  }
  
  
}
