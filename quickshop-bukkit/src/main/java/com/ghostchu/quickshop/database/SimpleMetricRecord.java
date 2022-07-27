package com.ghostchu.quickshop.database;

import com.ghostchu.quickshop.api.database.MetricRecord;
import com.ghostchu.quickshop.api.database.ShopOperationEnum;
import lombok.AllArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
public class SimpleMetricRecord implements MetricRecord {
    private final long timestamp;
    private final int x;
    private final int y;
    private final int z;
    private final String world;
    private final ShopOperationEnum type;
    private final double total;
    private final double tax;
    private final int amount;
    private final UUID player;


    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getZ() {
        return z;
    }

    @Override
    public String getWorld() {
        return world;
    }

    @Override
    public ShopOperationEnum getType() {
        return type;
    }

    @Override
    public double getTotal() {
        return total;
    }

    @Override
    public double getTax() {
        return tax;
    }

    @Override
    public int getAmount() {
        return amount;
    }

    @Override
    public UUID getPlayer() {
        return player;
    }


}
