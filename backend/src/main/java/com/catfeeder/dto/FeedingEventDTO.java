package com.catfeeder.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FeedingEventDTO {
    private String feederId;
    private String timestamp;
    private Integer amount;
    private Long catId;
}
