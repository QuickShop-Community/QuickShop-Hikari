package com.ghostchu.quickshop.api.database;

import com.ghostchu.quickshop.api.obj.QUser;
import lombok.Builder;
import lombok.Data;

@Data
public class ShopMetricRecord {
    private final long v = 3;
    private long time;
    private long shopId;
    private ShopOperationEnum type;
    private double total;
    private double tax;
    private int amount;
    private String player;

    public ShopMetricRecord(long time, long shopId, ShopOperationEnum type, double total, double tax, int amount, QUser player) {
        this.time = time;
        this.shopId = shopId;
        this.type = type;
        this.total = total;
        this.tax = tax;
        this.amount = amount;
        this.player = player.serialize();
    }
}
