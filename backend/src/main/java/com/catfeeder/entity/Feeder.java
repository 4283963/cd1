package com.catfeeder.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "feeders")
public class Feeder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String feederCode;

    @Column(nullable = false)
    private String name;

    private String location;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private Integer foodCapacity;

    private Integer currentFoodLevel;

    private Integer waterCapacity;

    private Integer currentWaterLevel;

    private Integer batteryLevel;

    private String status;

    private Boolean foodAlert = false;

    private Boolean waterAlert = false;

    private LocalDateTime lastHeartbeat;

    @Column(nullable = false)
    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
