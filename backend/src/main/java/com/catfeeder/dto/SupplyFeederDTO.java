package com.catfeeder.dto;

import lombok.Data;

@Data
public class SupplyFeederDTO {
    private String feederCode;
    private Integer foodAmount;
    private Integer waterAmount;
    private String operator;
    private String remark;
}
