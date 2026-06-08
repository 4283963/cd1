package com.catfeeder.repository;

import com.catfeeder.entity.FeedingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FeedingRecordRepository extends JpaRepository<FeedingRecord, Long> {

    List<FeedingRecord> findByFeederCodeOrderByFeedingTimeDesc(String feederCode);

    List<FeedingRecord> findByCatIdOrderByFeedingTimeDesc(Long catId);

    List<FeedingRecord> findByFeedingTimeAfterOrderByFeedingTimeDesc(LocalDateTime startTime);

    @Query("SELECT SUM(f.amount) FROM FeedingRecord f WHERE f.feederCode = :feederCode AND f.feedingTime >= :startTime")
    Integer sumFoodAmountByFeederAndTime(String feederCode, LocalDateTime startTime);

    @Query("SELECT SUM(f.amount) FROM FeedingRecord f WHERE f.catId = :catId AND f.feedingTime >= :startTime")
    Integer sumFoodAmountByCatAndTime(Long catId, LocalDateTime startTime);

    @Query("SELECT COUNT(f) FROM FeedingRecord f WHERE f.catId = :catId AND f.feedingTime >= :startTime")
    long countFeedingsByCatAndTime(Long catId, LocalDateTime startTime);
}
