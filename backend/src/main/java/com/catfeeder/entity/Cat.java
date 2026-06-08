package com.catfeeder.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "cats")
public class Cat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String catCode;

    @Column(nullable = false)
    private String name;

    private String furColor;

    private String furPattern;

    private String bodyType;

    private String eyeColor;

    private String gender;

    private Integer estimatedAge;

    @Column(length = 500)
    private String description;

    private String avatarUrl;

    @Column(nullable = false)
    private Boolean isNeutered = false;

    @Column(nullable = false)
    private Boolean isNew = true;

    private LocalDateTime firstSeenTime;

    private LocalDateTime lastSeenTime;

    private Integer visitCount = 0;

    @Column(nullable = false)
    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
        if (firstSeenTime == null) {
            firstSeenTime = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
