package com.ghostchu.quickshop.api.database;

import java.util.UUID;

public interface MetricRecord {
    int getAmount();

    UUID getPlayer();

    double getTax();

    long getTimestamp();

    double getTotal();

    ShopOperationEnum getType();

    String getWorld();

    int getX();

    int getY();

    int getZ();
}
