package com.catfeeder.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SensorDataDTO {
    private String feederId;
    private String timestamp;
    private Boolean infraredTriggered;
    private Integer foodLevel;
    private Integer waterLevel;
    private Float temperature;
    private Integer batteryLevel;
}
