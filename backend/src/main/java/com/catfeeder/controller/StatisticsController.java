package com.catfeeder.controller;

import com.catfeeder.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = statisticsService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/daily-visits")
    public ResponseEntity<List<Map<String, Object>>> getDailyVisitStats(
            @RequestParam(value = "days", required = false, defaultValue = "7") int days) {
        List<Map<String, Object>> stats = statisticsService.getDailyVisitStats(days);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/cat-ranking")
    public ResponseEntity<List<Map<String, Object>>> getCatVisitRanking(
            @RequestParam(value = "limit", required = false, defaultValue = "10") int limit) {
        List<Map<String, Object>> ranking = statisticsService.getCatVisitRanking(limit);
        return ResponseEntity.ok(ranking);
    }

    @GetMapping("/feeder/{feederCode}")
    public ResponseEntity<Map<String, Object>> getFeederStatistics(
            @PathVariable String feederCode,
            @RequestParam(value = "days", required = false, defaultValue = "7") int days) {
        Map<String, Object> stats = statisticsService.getFeederStatistics(feederCode, days);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/cat/{catId}")
    public ResponseEntity<Map<String, Object>> getCatStatistics(
            @PathVariable Long catId,
            @RequestParam(value = "days", required = false, defaultValue = "7") int days) {
        Map<String, Object> stats = statisticsService.getCatStatistics(catId, days);
        return ResponseEntity.ok(stats);
    }
}
