package com.ghostchu.quickshop.api.database;

import java.util.UUID;

public interface MetricRecord {
    long getTimestamp();

    int getX();

    int getY();

    int getZ();

    String getWorld();

    ShopOperationEnum getType();

    double getTotal();

    double getTax();

    int getAmount();

    UUID getPlayer();
}
