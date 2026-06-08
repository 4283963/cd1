package com.catfeeder.repository;

import com.catfeeder.entity.Feeder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeederRepository extends JpaRepository<Feeder, Long> {

    Optional<Feeder> findByFeederCode(String feederCode);

    @Query("SELECT f FROM Feeder f WHERE f.foodAlert = true OR f.waterAlert = true")
    List<Feeder> findFeedersWithAlerts();

    List<Feeder> findByFoodAlertTrue();

    List<Feeder> findByWaterAlertTrue();

    List<Feeder> findByStatus(String status);

    List<Feeder> findAllByOrderByNameAsc();
}
