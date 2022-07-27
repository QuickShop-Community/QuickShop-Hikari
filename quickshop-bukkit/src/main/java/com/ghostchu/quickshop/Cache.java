package com.ghostchu.quickshop;

import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.shop.SimpleShopManager;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheStats;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

/**
 * Cache is a utilities to quick access shops on large network with Caffeine Cache Library
 *
 * @author Ghost_chu
 */
public class Cache {
    private final QuickShop plugin;
    private final com.google.common.cache.Cache<Location, BoxedShop> accessCaching = CacheBuilder
            .newBuilder()
            .initialCapacity(5000)
            .expireAfterAccess(120, TimeUnit.MINUTES)
            .recordStats()
            .build();

    public Cache(QuickShop plugin) {
        this.plugin = plugin;
    }

    public @NotNull CacheStats getStats() {
        return accessCaching.stats();
    }


    /**
     * Gets shop from plugin caching
     *
     * @param location The shop location that you want to get
     * @param attached Does search for attached
     * @return The shop, null for no shops found in caching and memory
     */
    @Nullable
    public Shop find(@NotNull Location location, boolean attached) {
        BoxedShop boxedShop = accessCaching.getIfPresent(location);
        //Cache is invalid, generated a new one
        if (boxedShop == null || !boxedShop.isValid()) {
            Shop shop;
            if (attached) {
                shop = ((SimpleShopManager) plugin.getShopManager()).findShopIncludeAttached(location, false);
            } else {
                shop = plugin.getShopManager().getShop(location);
            }
            setCache(location, shop);
            return shop;
        } else {
            //Cache is valid
            return boxedShop.getShop();
        }
    }

    /**
     * Update and invalidate the caching
     *
     * @param location The location that you want to update
     * @param shop     null for invalidate and Shop object for update
     */
    public void setCache(@NotNull Location location, @Nullable Shop shop) {
        accessCaching.put(location, new BoxedShop(shop));
    }

    public void invalidate(@NotNull Location location) {
        accessCaching.invalidate(location);
    }

    public void invalidateAll() {
        accessCaching.invalidateAll();
    }


    private static class BoxedShop {
        @Nullable
        private final WeakReference<Shop> shopWeakRef;

        public BoxedShop(Shop shop) {
            if (shop != null) {
                this.shopWeakRef = new WeakReference<>(shop);
            } else {
                shopWeakRef = null;
            }
        }

        @Nullable
        public Shop getShop() {
            return shopWeakRef == null ? null : shopWeakRef.get();
        }

        public boolean isValid() {
            if (shopWeakRef != null) {
                Shop shop = shopWeakRef.get();
                if (shop != null) {
                    return shop.isValid();
                }
            }
            return false;
        }
    }
}
