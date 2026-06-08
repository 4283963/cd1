package com.catfeeder.service;

import com.catfeeder.entity.Alert;
import com.catfeeder.entity.Feeder;
import com.catfeeder.repository.AlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class AlertService {

    @Autowired
    private AlertRepository alertRepository;

    public List<Alert> getActiveAlerts() {
        return alertRepository.findByResolvedFalseOrderByCreateTimeDesc();
    }

    public List<Alert> getAllAlerts(int days) {
        LocalDateTime startTime = LocalDateTime.now().minus(days, ChronoUnit.DAYS);
        return alertRepository.findAlertsAfterTime(startTime);
    }

    public List<Alert> getAlertsByType(String type) {
        return alertRepository.findByTypeOrderByCreateTimeDesc(type);
    }

    public List<Alert> getAlertsByFeeder(String feederCode) {
        return alertRepository.findByFeederCodeOrderByCreateTimeDesc(feederCode);
    }

    public long getActiveAlertCount() {
        return alertRepository.countByResolvedFalse();
    }

    public long getActiveAlertCountByType(String type) {
        return alertRepository.countByTypeAndResolvedFalse(type);
    }

    @Transactional
    public Alert createFoodLowAlert(Feeder feeder, int foodPercent) {
        Alert alert = new Alert();
        alert.setType("FOOD_LOW");
        alert.setFeederCode(feeder.getFeederCode());
        alert.setFeederName(feeder.getName());
        alert.setSeverity("warning");
        alert.setMessage(feeder.getName() + " 粮草不足，剩余 " + foodPercent + "%，请及时补充！");
        alert.setResolved(false);
        return alertRepository.save(alert);
    }

    @Transactional
    public Alert createWaterLowAlert(Feeder feeder, int waterPercent) {
        Alert alert = new Alert();
        alert.setType("WATER_LOW");
        alert.setFeederCode(feeder.getFeederCode());
        alert.setFeederName(feeder.getName());
        alert.setSeverity("warning");
        alert.setMessage(feeder.getName() + " 水量不足，剩余 " + waterPercent + "%，请及时补充！");
        alert.setResolved(false);
        return alertRepository.save(alert);
    }

    @Transactional
    public Alert createNewCatAlert(String feederName, String catName, String imageUrl) {
        Alert alert = new Alert();
        alert.setType("NEW_CAT");
        alert.setSeverity("info");
        alert.setFeederName(feederName);
        alert.setCatName(catName);
        alert.setMessage("发现新猫咪「" + catName + "」在 " + feederName + " 出没！");
        alert.setResolved(false);
        return alertRepository.save(alert);
    }

    @Transactional
    public Alert resolveAlert(Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("告警不存在"));
        alert.setResolved(true);
        alert.setResolveTime(LocalDateTime.now());
        return alertRepository.save(alert);
    }

    @Transactional
    public void resolveFeederAlerts(String feederCode, String type) {
        List<Alert> alerts = alertRepository.findByFeederCodeOrderByCreateTimeDesc(feederCode);
        for (Alert alert : alerts) {
            if (alert.getType().equals(type) && !alert.getResolved()) {
                alert.setResolved(true);
                alert.setResolveTime(LocalDateTime.now());
                alertRepository.save(alert);
            }
        }
    }
}
