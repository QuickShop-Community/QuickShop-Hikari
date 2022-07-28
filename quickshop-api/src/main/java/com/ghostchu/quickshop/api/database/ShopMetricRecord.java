package com.ghostchu.quickshop.api.database;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

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

    public ShopMetricRecord(long time, long shopId, ShopOperationEnum type, double total, double tax, int amount, UUID player) {
        this.time = time;
        this.shopId = shopId;
        this.type = type;
        this.total = total;
        this.tax = tax;
        this.amount = amount;
        this.player = player;
    }
}
