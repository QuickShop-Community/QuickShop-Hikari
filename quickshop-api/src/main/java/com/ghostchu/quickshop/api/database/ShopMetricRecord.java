package com.ghostchu.quickshop.api.database;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@AllArgsConstructor
@Data
@Builder
public class ShopMetricRecord {
    private long time;
    private long shopId;
    private ShopOperationEnum type;
    private double total;
    private double tax;
    private int amount;
    private UUID player;

}
