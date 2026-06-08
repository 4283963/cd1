package com.catfeeder.controller;

import com.catfeeder.entity.Feeder;
import com.catfeeder.entity.SensorData;
import com.catfeeder.service.FeederService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/feeders")
public class FeederController {

    @Autowired
    private FeederService feederService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllFeeders(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "alerts", required = false) boolean withAlerts) {
        Map<String, Object> result = new HashMap<>();
        List<Feeder> feeders;

        if (withAlerts) {
            feeders = feederService.getFeedersWithAlerts();
        } else if (status != null && !status.isEmpty()) {
            feeders = feederService.getAllFeeders()
                    .stream()
                    .filter(f -> status.equals(f.getStatus()))
                    .toList();
        } else {
            feeders = feederService.getAllFeeders();
        }

        result.put("total", feeders.size());
        result.put("feeders", feeders);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Feeder> getFeederById(@PathVariable Long id) {
        Optional<Feeder> feeder = feederService.getFeederById(id);
        return feeder.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/code/{feederCode}")
    public ResponseEntity<Feeder> getFeederByCode(@PathVariable String feederCode) {
        Optional<Feeder> feeder = feederService.getFeederByCode(feederCode);
        return feeder.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Feeder> createFeeder(@RequestBody Feeder feeder) {
        Feeder created = feederService.createFeeder(feeder);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Feeder> updateFeeder(@PathVariable Long id, @RequestBody Feeder feeder) {
        Feeder updated = feederService.updateFeeder(id, feeder);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{code}/sensor-data")
    public ResponseEntity<Map<String, Object>> getFeederSensorData(
            @PathVariable String code,
            @RequestParam(value = "hours", required = false, defaultValue = "24") int hours) {
        List<SensorData> sensorData = feederService.getFeederSensorData(code, hours);
        Map<String, Object> result = new HashMap<>();
        result.put("total", sensorData.size());
        result.put("data", sensorData);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{code}/sensor-data/latest")
    public ResponseEntity<SensorData> getLatestSensorData(@PathVariable String code) {
        Optional<SensorData> data = feederService.getLatestSensorData(code);
        return data.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/thresholds")
    public ResponseEntity<Map<String, Object>> getWarningThresholds() {
        Map<String, Object> result = new HashMap<>();
        result.put("foodWarningThreshold", feederService.getFoodWarningThreshold());
        result.put("waterWarningThreshold", feederService.getWaterWarningThreshold());
        return ResponseEntity.ok(result);
    }
}
