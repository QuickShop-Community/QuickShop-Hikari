package com.ghostchu.quickshop.api.database.bean;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.UUID;

public interface DataRecord {
    @NotNull
    Date getCreateTime();

    @Nullable
    String getCurrency();

    @NotNull
    String getExtra();

    @Nullable
    String getInventorySymbolLink();

    @Nullable
    String getInventoryWrapper();

    @NotNull
    String getItem();

    @Nullable
    String getName();

    @NotNull
    UUID getOwner();

    @NotNull
    String getPermissions();

    double getPrice();

    @Nullable
    UUID getTaxAccount();

    int getType();

    boolean isHologram();

    boolean isUnlimited();

    @NotNull
    String getBenefit();
}
