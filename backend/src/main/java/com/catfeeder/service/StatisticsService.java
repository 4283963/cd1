package com.catfeeder.service;

import com.catfeeder.entity.Cat;
import com.catfeeder.entity.CatCapture;
import com.catfeeder.entity.FeedingRecord;
import com.catfeeder.repository.CatCaptureRepository;
import com.catfeeder.repository.CatRepository;
import com.catfeeder.repository.FeedingRecordRepository;
import com.catfeeder.repository.FeederRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    @Autowired
    private CatRepository catRepository;

    @Autowired
    private CatCaptureRepository catCaptureRepository;

    @Autowired
    private FeedingRecordRepository feedingRecordRepository;

    @Autowired
    private FeederRepository feederRepository;

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime startOfWeek = LocalDateTime.now().with(LocalDateTime.now().getDayOfWeek()).minusDays(6)
                .withHour(0).withMinute(0).withSecond(0).withNano(0);

        stats.put("totalCats", catRepository.count());
        stats.put("newCatsToday", catRepository.countNewCatsToday(startOfDay));
        stats.put("totalFeeders", feederRepository.count());
        stats.put("feedersWithAlerts", feederRepository.findFeedersWithAlerts().size());
        stats.put("activeAlerts", catRepository.count());

        long capturesToday = catCaptureRepository.countByCaptureTimeAfter(startOfDay);
        stats.put("capturesToday", capturesToday);

        long uniqueCatsToday = catCaptureRepository.countUniqueCatsAfterTime(startOfDay);
        stats.put("uniqueCatsToday", uniqueCatsToday);

        List<Cat> topVisitedCats = catRepository.findAllByOrderByVisitCountDesc()
                .stream().limit(5).collect(Collectors.toList());
        stats.put("topVisitedCats", topVisitedCats);

        return stats;
    }

    public List<Map<String, Object>> getDailyVisitStats(int days) {
        List<Map<String, Object>> stats = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (int i = days - 1; i >= 0; i--) {
            LocalDateTime dayStart = now.minus(i, ChronoUnit.DAYS)
                    .withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime dayEnd = dayStart.plus(1, ChronoUnit.DAYS);

            Map<String, Object> dayStat = new HashMap<>();
            dayStat.put("date", dayStart.toLocalDate().toString());
            dayStat.put("visits", catCaptureRepository.countByCaptureTimeAfter(dayStart)
                    - catCaptureRepository.countByCaptureTimeAfter(dayEnd));
            dayStat.put("uniqueCats", catCaptureRepository.countUniqueCatsAfterTime(dayStart));

            stats.add(dayStat);
        }

        return stats;
    }

    public List<Map<String, Object>> getCatVisitRanking(int limit) {
        List<Cat> cats = catRepository.findAllByOrderByVisitCountDesc()
                .stream().limit(limit).collect(Collectors.toList());

        List<Map<String, Object>> ranking = new ArrayList<>();
        int rank = 1;
        for (Cat cat : cats) {
            Map<String, Object> catStat = new HashMap<>();
            catStat.put("rank", rank++);
            catStat.put("catId", cat.getId());
            catStat.put("catCode", cat.getCatCode());
            catStat.put("name", cat.getName());
            catStat.put("avatarUrl", cat.getAvatarUrl());
            catStat.put("visitCount", cat.getVisitCount());
            catStat.put("lastSeen", cat.getLastSeenTime());
            ranking.add(catStat);
        }

        return ranking;
    }

    public Map<String, Object> getFeederStatistics(String feederCode, int days) {
        Map<String, Object> stats = new HashMap<>();
        LocalDateTime startTime = LocalDateTime.now().minus(days, ChronoUnit.DAYS);

        List<FeedingRecord> records = feedingRecordRepository
                .findByFeedingTimeAfterOrderByFeedingTimeDesc(startTime)
                .stream()
                .filter(r -> r.getFeederCode().equals(feederCode))
                .collect(Collectors.toList());

        int totalFood = records.stream()
                .mapToInt(FeedingRecord::getAmount)
                .sum();

        long visitCount = catCaptureRepository.countByCaptureTimeAfter(startTime);

        stats.put("totalFoodDispensed", totalFood);
        stats.put("totalVisits", visitCount);
        stats.put("periodDays", days);
        stats.put("dailyRecords", generateDailyFeedingStats(feederCode, days));

        return stats;
    }

    private List<Map<String, Object>> generateDailyFeedingStats(String feederCode, int days) {
        List<Map<String, Object>> stats = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (int i = days - 1; i >= 0; i--) {
            LocalDateTime dayStart = now.minus(i, ChronoUnit.DAYS)
                    .withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime dayEnd = dayStart.plus(1, ChronoUnit.DAYS);

            Integer foodAmount = feedingRecordRepository.sumFoodAmountByFeederAndTime(feederCode, dayStart);

            Map<String, Object> dayStat = new HashMap<>();
            dayStat.put("date", dayStart.toLocalDate().toString());
            dayStat.put("foodAmount", foodAmount != null ? foodAmount : 0);

            stats.add(dayStat);
        }

        return stats;
    }

    public Map<String, Object> getCatStatistics(Long catId, int days) {
        Map<String, Object> stats = new HashMap<>();
        LocalDateTime startTime = LocalDateTime.now().minus(days, ChronoUnit.DAYS);

        long visitCount = feedingRecordRepository.countFeedingsByCatAndTime(catId, startTime);
        Integer totalFood = feedingRecordRepository.sumFoodAmountByCatAndTime(catId, startTime);

        List<CatCapture> captures = catCaptureRepository.findByCatIdOrderByCaptureTimeDesc(catId)
                .stream()
                .filter(c -> c.getCaptureTime().isAfter(startTime))
                .collect(Collectors.toList());

        stats.put("visitCount", visitCount);
        stats.put("totalFoodConsumed", totalFood != null ? totalFood : 0);
        stats.put("captureCount", captures.size());
        stats.put("periodDays", days);

        return stats;
    }
}
