package com.catfeeder.controller;

import com.catfeeder.entity.Alert;
import com.catfeeder.service.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/alerts")
public class AlertController {

    @Autowired
    private AlertService alertService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAlerts(
            @RequestParam(value = "active", required = false, defaultValue = "true") boolean active,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "days", required = false, defaultValue = "7") int days) {
        Map<String, Object> result = new HashMap<>();
        List<Alert> alerts;

        if (active) {
            if (type != null && !type.isEmpty()) {
                alerts = alertService.getAlertsByType(type)
                        .stream()
                        .filter(a -> !a.getResolved())
                        .toList();
            } else {
                alerts = alertService.getActiveAlerts();
            }
        } else {
            alerts = alertService.getAllAlerts(days);
        }

        result.put("total", alerts.size());
        result.put("alerts", alerts);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Alert> getAlertById(@PathVariable Long id) {
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/resolve")
    public ResponseEntity<Alert> resolveAlert(@PathVariable Long id) {
        Alert resolved = alertService.resolveAlert(id);
        return ResponseEntity.ok(resolved);
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getAlertCount() {
        Map<String, Object> result = new HashMap<>();
        result.put("active", alertService.getActiveAlertCount());
        result.put("foodLow", alertService.getActiveAlertCountByType("FOOD_LOW"));
        result.put("waterLow", alertService.getActiveAlertCountByType("WATER_LOW"));
        result.put("newCat", alertService.getActiveAlertCountByType("NEW_CAT"));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/feeder/{feederCode}")
    public ResponseEntity<Map<String, Object>> getAlertsByFeeder(@PathVariable String feederCode) {
        List<Alert> alerts = alertService.getAlertsByFeeder(feederCode);
        Map<String, Object> result = new HashMap<>();
        result.put("total", alerts.size());
        result.put("alerts", alerts);
        return ResponseEntity.ok(result);
    }
}
