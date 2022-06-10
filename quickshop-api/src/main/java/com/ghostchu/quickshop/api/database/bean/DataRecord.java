package com.ghostchu.quickshop.api.database.bean;

import java.util.UUID;

public interface DataRecord {

    UUID getOwner();

    String getItem();

    String getName();

    int getType();

    String getCurrency();

    double getPrice();

    boolean isUnlimited();

    boolean isHologram();

    UUID getTaxAccount();

    String getPermissions();

    String getInventoryWrapper();

    String getInventorySymbolLink();

    long getCreateTime();

    String getExtra();
}
