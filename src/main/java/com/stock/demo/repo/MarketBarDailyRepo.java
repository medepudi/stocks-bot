package com.stock.demo.repo;

import com.stock.demo.model.MarketBarDaily;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MarketBarDailyRepo extends JpaRepository<MarketBarDaily, Long> {
  Optional<MarketBarDaily> findByTickerAndDate(String ticker, LocalDate date);
  List<MarketBarDaily> findByTickerOrderByDateDesc(String ticker);
  Optional<MarketBarDaily> findTopByTickerOrderByDateDesc(String ticker);


}
