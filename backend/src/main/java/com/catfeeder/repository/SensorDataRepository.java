package com.catfeeder.repository;

import com.catfeeder.entity.SensorData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SensorDataRepository extends JpaRepository<SensorData, Long> {

    List<SensorData> findByFeederCodeOrderByRecordTimeDesc(String feederCode);

    List<SensorData> findByFeederCodeAndRecordTimeAfterOrderByRecordTimeDesc(String feederCode, LocalDateTime startTime);

    Optional<SensorData> findFirstByFeederCodeOrderByRecordTimeDesc(String feederCode);

    @Query("SELECT s FROM SensorData s WHERE s.feederCode = :feederCode AND s.recordTime >= :startTime ORDER BY s.recordTime ASC")
    List<SensorData> findSensorDataForTrend(String feederCode, LocalDateTime startTime);
}
