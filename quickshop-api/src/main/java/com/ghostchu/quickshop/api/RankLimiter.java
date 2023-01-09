package com.ghostchu.quickshop.api;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface RankLimiter {


    int getShopLimit(@NotNull Player p);

    @Deprecated(forRemoval = true)
    @ApiStatus.Internal
    @ApiStatus.Obsolete
    @NotNull Map<String, Integer> getLimits();

    boolean isLimit();
}
