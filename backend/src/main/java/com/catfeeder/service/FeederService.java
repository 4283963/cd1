package com.catfeeder.service;

import com.catfeeder.entity.Feeder;
import com.catfeeder.entity.SensorData;
import com.catfeeder.repository.FeederRepository;
import com.catfeeder.repository.SensorDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class FeederService {

    @Autowired
    private FeederRepository feederRepository;

    @Autowired
    private SensorDataRepository sensorDataRepository;

    @Autowired
    private AlertService alertService;

    @Value("${app.food-warning-threshold:20}")
    private int foodWarningThreshold;

    @Value("${app.water-warning-threshold:20}")
    private int waterWarningThreshold;

    public List<Feeder> getAllFeeders() {
        return feederRepository.findAllByOrderByNameAsc();
    }

    public Optional<Feeder> getFeederById(Long id) {
        return feederRepository.findById(id);
    }

    public Optional<Feeder> getFeederByCode(String feederCode) {
        return feederRepository.findByFeederCode(feederCode);
    }

    public List<Feeder> getFeedersWithAlerts() {
        return feederRepository.findFeedersWithAlerts();
    }

    @Transactional
    public Feeder createFeeder(Feeder feeder) {
        if (feeder.getStatus() == null) {
            feeder.setStatus("offline");
        }
        if (feeder.getCurrentFoodLevel() == null) {
            feeder.setCurrentFoodLevel(0);
        }
        if (feeder.getCurrentWaterLevel() == null) {
            feeder.setCurrentWaterLevel(0);
        }
        feeder.setFoodAlert(false);
        feeder.setWaterAlert(false);
        return feederRepository.save(feeder);
    }

    @Transactional
    public Feeder updateFeeder(Long id, Feeder feederUpdate) {
        Feeder feeder = feederRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("喂养机不存在"));

        if (feederUpdate.getName() != null) feeder.setName(feederUpdate.getName());
        if (feederUpdate.getLocation() != null) feeder.setLocation(feederUpdate.getLocation());
        if (feederUpdate.getLongitude() != null) feeder.setLongitude(feederUpdate.getLongitude());
        if (feederUpdate.getLatitude() != null) feeder.setLatitude(feederUpdate.getLatitude());
        if (feederUpdate.getFoodCapacity() != null) feeder.setFoodCapacity(feederUpdate.getFoodCapacity());
        if (feederUpdate.getWaterCapacity() != null) feeder.setWaterCapacity(feederUpdate.getWaterCapacity());

        return feederRepository.save(feeder);
    }

    @Transactional
    public void updateFeederStatus(String feederCode, Integer foodLevel, Integer waterLevel,
                                    Integer batteryLevel, String status) {
        Feeder feeder = feederRepository.findByFeederCode(feederCode).orElse(null);

        if (feeder == null) {
            feeder = new Feeder();
            feeder.setFeederCode(feederCode);
            feeder.setName(feederCode);
            feeder.setStatus("online");
            feeder.setFoodCapacity(100);
            feeder.setWaterCapacity(100);
        }

        if (foodLevel != null) {
            feeder.setCurrentFoodLevel(foodLevel);
            checkFoodAlert(feeder, foodLevel);
        }
        if (waterLevel != null) {
            feeder.setCurrentWaterLevel(waterLevel);
            checkWaterAlert(feeder, waterLevel);
        }
        if (batteryLevel != null) {
            feeder.setBatteryLevel(batteryLevel);
        }
        if (status != null) {
            feeder.setStatus(status);
        }
        feeder.setLastHeartbeat(LocalDateTime.now());

        feederRepository.save(feeder);
    }

    private void checkFoodAlert(Feeder feeder, Integer foodLevel) {
        int foodPercent = calculatePercentage(foodLevel, feeder.getFoodCapacity());
        boolean shouldAlert = foodPercent < foodWarningThreshold;

        if (shouldAlert && !feeder.getFoodAlert()) {
            feeder.setFoodAlert(true);
            alertService.createFoodLowAlert(feeder, foodPercent);
        } else if (!shouldAlert && feeder.getFoodAlert()) {
            feeder.setFoodAlert(false);
            alertService.resolveFeederAlerts(feeder.getFeederCode(), "FOOD_LOW");
        }
    }

    private void checkWaterAlert(Feeder feeder, Integer waterLevel) {
        int waterPercent = calculatePercentage(waterLevel, feeder.getWaterCapacity());
        boolean shouldAlert = waterPercent < waterWarningThreshold;

        if (shouldAlert && !feeder.getWaterAlert()) {
            feeder.setWaterAlert(true);
            alertService.createWaterLowAlert(feeder, waterPercent);
        } else if (!shouldAlert && feeder.getWaterAlert()) {
            feeder.setWaterAlert(false);
            alertService.resolveFeederAlerts(feeder.getFeederCode(), "WATER_LOW");
        }
    }

    private int calculatePercentage(Integer level, Integer capacity) {
        if (level == null || capacity == null || capacity == 0) {
            return 0;
        }
        return (int) ((level * 100.0) / capacity);
    }

    public List<SensorData> getFeederSensorData(String feederCode, int hours) {
        LocalDateTime startTime = LocalDateTime.now().minus(hours, ChronoUnit.HOURS);
        return sensorDataRepository.findByFeederCodeAndRecordTimeAfterOrderByRecordTimeDesc(feederCode, startTime);
    }

    public Optional<SensorData> getLatestSensorData(String feederCode) {
        return sensorDataRepository.findFirstByFeederCodeOrderByRecordTimeDesc(feederCode);
    }

    @Transactional
    public SensorData recordSensorData(String feederCode, Integer foodLevel, Integer waterLevel,
                                        Float temperature, Integer batteryLevel, Boolean infraredTriggered,
                                        LocalDateTime recordTime) {
        SensorData sensorData = new SensorData();
        sensorData.setFeederCode(feederCode);
        sensorData.setFoodLevel(foodLevel);
        sensorData.setWaterLevel(waterLevel);
        sensorData.setTemperature(temperature);
        sensorData.setBatteryLevel(batteryLevel);
        sensorData.setInfraredTriggered(infraredTriggered);
        sensorData.setRecordTime(recordTime != null ? recordTime : LocalDateTime.now());

        sensorData = sensorDataRepository.save(sensorData);

        updateFeederStatus(feederCode, foodLevel, waterLevel, batteryLevel, "online");

        return sensorData;
    }

    public int getFoodWarningThreshold() {
        return foodWarningThreshold;
    }

    public int getWaterWarningThreshold() {
        return waterWarningThreshold;
    }
}
