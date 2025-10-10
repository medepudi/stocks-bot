// src/main/java/com/stock/demo/model/StrategyState.java
package com.stock.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "strategy_state", uniqueConstraints = @UniqueConstraint(columnNames = "driver_ticker"))
public class StrategyState {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;
  @Column(name="driver_ticker") String driverTicker;
  int negStreak;
  int posStreak;
  String lastSignal;
  public Long getId() {
	return id;
  }
  public void setId(Long id) {
	this.id = id;
  }
  public String getDriverTicker() {
	return driverTicker;
  }
  public void setDriverTicker(String driverTicker) {
	this.driverTicker = driverTicker;
  }
  public int getNegStreak() {
	return negStreak;
  }
  public void setNegStreak(int negStreak) {
	this.negStreak = negStreak;
  }
  public int getPosStreak() {
	return posStreak;
  }
  public void setPosStreak(int posStreak) {
	this.posStreak = posStreak;
  }
  public String getLastSignal() {
	return lastSignal;
  }
  public void setLastSignal(String lastSignal) {
	this.lastSignal = lastSignal;
  }
  
  

}
