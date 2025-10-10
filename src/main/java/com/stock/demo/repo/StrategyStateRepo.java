// src/main/java/com/stock/demo/repo/StrategyStateRepo.java
package com.stock.demo.repo;

import com.stock.demo.model.StrategyState;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StrategyStateRepo extends JpaRepository<StrategyState, Long> {
  Optional<StrategyState> findByDriverTicker(String driverTicker);
}
