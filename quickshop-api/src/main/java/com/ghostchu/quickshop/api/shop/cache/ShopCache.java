package com.ghostchu.quickshop.api.shop.cache;

import com.ghostchu.quickshop.api.shop.Shop;
import com.google.common.cache.CacheStats;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ShopCache {

    @Nullable Shop get(@NotNull ShopCacheNamespacedKey namespacedKey, @NotNull Location location, boolean allowLoading);

    void invalidateAll(@Nullable ShopCacheNamespacedKey namespacedKey);

    void invalidate(@Nullable ShopCacheNamespacedKey namespacedKey, @NotNull Location location);

    @NotNull CacheStats getCacheStats(@NotNull ShopCacheNamespacedKey namespacedKey);
}
