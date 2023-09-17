package com.ghostchu.quickshop.shop.cache;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.cache.ShopCache;
import com.ghostchu.quickshop.api.shop.cache.ShopCacheNamespacedKey;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.quickshop.util.paste.GuavaCacheRender;
import com.ghostchu.quickshop.util.paste.item.SubPasteItem;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheStats;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class SimpleShopCache implements SubPasteItem, ShopCache {
    private final QuickShop plugin;
    private final Map<ShopCacheNamespacedKey, Cache<Location, BoxedShop>> CACHES = new HashMap<>();
    private final Map<ShopCacheNamespacedKey, Function<Location, Shop>> CACHE_VALUE_PROVIDER = new HashMap<>();

    public SimpleShopCache(@NotNull QuickShop plugin, @NotNull Map<@NotNull ShopCacheNamespacedKey, @NotNull Pair<@NotNull Function<Location, Shop>, @Nullable Cache<Location, BoxedShop>>> initArgs) {
        this.plugin = plugin;
        this.plugin.getPasteManager().register(plugin.getJavaPlugin(), this);
        for (Map.Entry<ShopCacheNamespacedKey, Pair<Function<Location, Shop>, Cache<Location, BoxedShop>>> entry : initArgs.entrySet()) {
            ShopCacheNamespacedKey namespacedKey = entry.getKey();
            if (CACHES.containsKey(namespacedKey) || CACHE_VALUE_PROVIDER.containsKey(namespacedKey)) {
                throw new IllegalArgumentException("The namespaced key " + namespacedKey.name() + " already registered.");
            }
            Pair<Function<Location, Shop>, Cache<Location, BoxedShop>> pair = entry.getValue();
            Function<Location, Shop> valueProvider = pair.getLeft();
            if (valueProvider == null) throw new IllegalArgumentException("The shop value provider cannot be null!");
            Cache<Location, BoxedShop> cacheContainer = pair.getRight();
            if (pair.getRight() == null)
                cacheContainer = CacheBuilder.newBuilder().expireAfterAccess(3, TimeUnit.MINUTES).recordStats().build();
            CACHES.put(namespacedKey, cacheContainer);
            CACHE_VALUE_PROVIDER.put(namespacedKey, valueProvider);
            Log.debug("Shop cache " + namespacedKey.name() + " registered.");
        }
    }

    @Nullable
    @Override
    public Shop get(@NotNull ShopCacheNamespacedKey namespacedKey, @NotNull Location location, boolean allowLoading) {
        Cache<Location, BoxedShop> targetCacheContainer = CACHES.get(namespacedKey);
        if (targetCacheContainer == null)
            throw new IllegalArgumentException("Shop cache container " + namespacedKey.name() + " not exists!");
        if (!allowLoading) {
            BoxedShop boxedShop = targetCacheContainer.getIfPresent(location);
            if (boxedShop == null) return null;
            return boxedShop.getShop();
        }
        Function<Location, Shop> provider = CACHE_VALUE_PROVIDER.get(namespacedKey);
        if (provider == null)
            throw new IllegalArgumentException("Shop cache provider " + namespacedKey.name() + " not exists");
        try {
            return targetCacheContainer.get(location, () -> new BoxedShop(provider.apply(location))).getShop();
        } catch (ExecutionException e) {
            plugin.logger().warn("Loading shops into cache failure, fallback to direct access", e);
            return provider.apply(location);
        }
    }

    @Override
    public void invalidateAll(@NotNull ShopCacheNamespacedKey namespacedKey) {
        Cache<Location, BoxedShop> targetCacheContainer = CACHES.get(namespacedKey);
        if (targetCacheContainer == null)
            throw new IllegalArgumentException("Shop cache container " + namespacedKey.name() + " not exists!");
        targetCacheContainer.invalidateAll();
    }


    @Override
    public void invalidate(@NotNull ShopCacheNamespacedKey namespacedKey, @NotNull Location location) {
        Cache<Location, BoxedShop> targetCacheContainer = CACHES.get(namespacedKey);
        if (targetCacheContainer == null)
            throw new IllegalArgumentException("Shop cache container " + namespacedKey.name() + " not exists!");
        targetCacheContainer.invalidate(location);
    }
    @Override
    @NotNull
    public CacheStats getCacheStats(@NotNull ShopCacheNamespacedKey namespacedKey) {
        Cache<Location, BoxedShop> targetCacheContainer = CACHES.get(namespacedKey);
        if (targetCacheContainer == null)
            throw new IllegalArgumentException("Shop cache container " + namespacedKey.name() + " not exists!");
        return targetCacheContainer.stats();
    }

    @Override
    public @NotNull String genBody() {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<ShopCacheNamespacedKey, Cache<Location, BoxedShop>> entry : CACHES.entrySet()) {
            builder.append("<h5>").append(entry.getKey().name()).append("</h5>");
            builder.append(renderTable(entry.getValue().stats()));
        }

        return builder.toString();
    }

    @NotNull
    private String renderTable(@NotNull CacheStats stats) {
        return GuavaCacheRender.renderTable(stats);
    }

    @Override
    public @NotNull String getTitle() {
        return "Shop Caching";
    }
}
