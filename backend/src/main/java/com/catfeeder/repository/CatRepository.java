package com.catfeeder.repository;

import com.catfeeder.entity.Cat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CatRepository extends JpaRepository<Cat, Long> {

    Optional<Cat> findByCatCode(String catCode);

    List<Cat> findByIsNewTrue();

    List<Cat> findByFurColorAndFurPatternAndBodyType(String furColor, String furPattern, String bodyType);

    @Query("SELECT c FROM Cat c WHERE c.lastSeenTime >= :time ORDER BY c.lastSeenTime DESC")
    List<Cat> findRecentlySeenCats(LocalDateTime time);

    @Query("SELECT COUNT(c) FROM Cat c WHERE c.firstSeenTime >= :startOfDay")
    long countNewCatsToday(LocalDateTime startOfDay);

    List<Cat> findAllByOrderByVisitCountDesc();

    List<Cat> findAllByOrderByLastSeenTimeDesc();
}
