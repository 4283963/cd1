package com.catfeeder.repository;

import com.catfeeder.entity.CatCapture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CatCaptureRepository extends JpaRepository<CatCapture, Long> {

    List<CatCapture> findByFeederCodeOrderByCaptureTimeDesc(String feederCode);

    List<CatCapture> findByCatIdOrderByCaptureTimeDesc(Long catId);

    List<CatCapture> findByIsNewCatTrueOrderByCaptureTimeDesc();

    @Query("SELECT c FROM CatCapture c WHERE c.captureTime >= :startTime ORDER BY c.captureTime DESC")
    List<CatCapture> findCapturesAfterTime(LocalDateTime startTime);

    @Query("SELECT c FROM CatCapture c WHERE c.catId IS NULL ORDER BY c.captureTime DESC")
    List<CatCapture> findUnrecognizedCaptures();

    long countByCaptureTimeAfter(LocalDateTime startTime);

    @Query("SELECT COUNT(DISTINCT c.catId) FROM CatCapture c WHERE c.catId IS NOT NULL AND c.captureTime >= :startTime")
    long countUniqueCatsAfterTime(LocalDateTime startTime);
}
