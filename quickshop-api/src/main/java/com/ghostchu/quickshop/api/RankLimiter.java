package com.ghostchu.quickshop.api;

import com.ghostchu.quickshop.api.obj.QUser;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface RankLimiter {


    int getShopLimit(@NotNull QUser p);

    @Deprecated(forRemoval = true)
    @ApiStatus.Internal
    @ApiStatus.Obsolete
    @NotNull Map<String, Integer> getLimits();

    boolean isLimit();
}
