package com.catfeeder.controller;

import com.catfeeder.dto.CatFeaturesDTO;
import com.catfeeder.dto.FeedingEventDTO;
import com.catfeeder.dto.HeartbeatDTO;
import com.catfeeder.dto.SensorDataDTO;
import com.catfeeder.service.IotService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/iot")
public class IotController {

    @Autowired
    private IotService iotService;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/sensor")
    public ResponseEntity<Map<String, Object>> receiveSensorData(@RequestBody SensorDataDTO sensorData) {
        Map<String, Object> result = iotService.processSensorData(sensorData);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/capture")
    public ResponseEntity<Map<String, Object>> receiveCapture(
            @RequestParam("image") MultipartFile image,
            @RequestParam("feederId") String feederId,
            @RequestParam(value = "timestamp", required = false) String timestamp,
            @RequestParam(value = "catFeatures", required = false) String catFeaturesJson) {

        CatFeaturesDTO catFeatures = null;
        if (catFeaturesJson != null && !catFeaturesJson.isEmpty()) {
            try {
                catFeatures = objectMapper.readValue(catFeaturesJson, CatFeaturesDTO.class);
            } catch (Exception e) {
                // 忽略解析错误
            }
        }

        Map<String, Object> result = iotService.processCatCapture(feederId, image, timestamp, catFeatures);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/feeding")
    public ResponseEntity<Map<String, Object>> receiveFeedingEvent(@RequestBody FeedingEventDTO feedingEvent) {
        Map<String, Object> result = iotService.processFeedingEvent(feedingEvent);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/heartbeat")
    public ResponseEntity<Map<String, Object>> receiveHeartbeat(@RequestBody HeartbeatDTO heartbeat) {
        Map<String, Object> result = iotService.processHeartbeat(heartbeat);
        return ResponseEntity.ok(result);
    }
}
