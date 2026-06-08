package com.catfeeder.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "sensor_data")
public class SensorData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String feederCode;

    private Boolean infraredTriggered;

    private Integer foodLevel;

    private Integer waterLevel;

    private Float temperature;

    private Integer batteryLevel;

    @Column(nullable = false)
    private LocalDateTime recordTime;

    @Column(nullable = false)
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        if (recordTime == null) {
            recordTime = LocalDateTime.now();
        }
    }
}
