package com.catfeeder.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "cat_captures")
public class CatCapture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String feederCode;

    private Long catId;

    @Column(nullable = false)
    private String imageUrl;

    private String furColor;

    private String furPattern;

    private String bodyType;

    private String eyeColor;

    @Column(length = 1000)
    private String features;

    private Boolean isNewCat = false;

    private Boolean recognized = false;

    @Column(nullable = false)
    private LocalDateTime captureTime;

    @Column(nullable = false)
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        if (captureTime == null) {
            captureTime = LocalDateTime.now();
        }
    }
}
