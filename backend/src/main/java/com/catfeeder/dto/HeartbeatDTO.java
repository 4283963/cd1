package com.catfeeder.dto;

import lombok.Data;

@Data
public class HeartbeatDTO {
    private String feederId;
    private String timestamp;
    private String status;
    private Integer foodLevel;
    private Integer waterLevel;
    private Integer batteryLevel;
}
