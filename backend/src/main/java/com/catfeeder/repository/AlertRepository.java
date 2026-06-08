package com.catfeeder.repository;

import com.catfeeder.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findByResolvedFalseOrderByCreateTimeDesc();

    List<Alert> findByTypeOrderByCreateTimeDesc(String type);

    List<Alert> findByFeederCodeOrderByCreateTimeDesc(String feederCode);

    List<Alert> findBySeverityAndResolvedFalseOrderByCreateTimeDesc(String severity);

    long countByResolvedFalse();

    long countByTypeAndResolvedFalse(String type);

    @Query("SELECT a FROM Alert a WHERE a.createTime >= :startTime ORDER BY a.createTime DESC")
    List<Alert> findAlertsAfterTime(LocalDateTime startTime);
}
