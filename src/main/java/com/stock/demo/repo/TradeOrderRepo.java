// src/main/java/com/stock/demo/repo/TradeOrderRepo.java
package com.stock.demo.repo;

import com.stock.demo.model.TradeOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeOrderRepo extends JpaRepository<TradeOrder, Long> { }
