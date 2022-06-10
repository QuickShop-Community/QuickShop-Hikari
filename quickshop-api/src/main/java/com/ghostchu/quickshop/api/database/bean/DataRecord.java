package com.ghostchu.quickshop.api.database.bean;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.UUID;

public interface DataRecord {
    @NotNull
    UUID getOwner();

    @NotNull
    String getItem();

    @Nullable
    String getName();

    int getType();

    @Nullable
    String getCurrency();

    double getPrice();

    boolean isUnlimited();

    boolean isHologram();

    @Nullable
    UUID getTaxAccount();

    @NotNull
    String getPermissions();

    @Nullable
    String getInventoryWrapper();

    @Nullable
    String getInventorySymbolLink();

    @NotNull
    Date getCreateTime();

    @NotNull
    String getExtra();
}
