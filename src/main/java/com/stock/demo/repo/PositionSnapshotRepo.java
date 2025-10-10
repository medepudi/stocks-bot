// src/main/java/com/stock/demo/repo/PositionSnapshotRepo.java
package com.stock.demo.repo;

import com.stock.demo.model.PositionSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PositionSnapshotRepo extends JpaRepository<PositionSnapshot, Long> { }
