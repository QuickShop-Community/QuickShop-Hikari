package com.ghostchu.quickshop.papi;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.common.util.JsonUtil;
import com.ghostchu.quickshop.util.performance.PerfMonitor;
import com.ghostchu.simplereloadlib.ReloadResult;
import com.ghostchu.simplereloadlib.Reloadable;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheStats;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;

public class PAPICache implements Reloadable {
    private QuickShop plugin;
    private long expiredTime;
    private Cache<String, Optional<String>> performCaches;

    public PAPICache() {
        init();
        QuickShop.getInstance().getReloadManager().register(this);
    }

    private void init() {
        this.plugin = QuickShop.getInstance();
        this.expiredTime = plugin.getConfig().getLong("plugin.PlaceHolderAPI.cache", 900000);
        this.performCaches = CacheBuilder.newBuilder()
                .expireAfterWrite(expiredTime, java.util.concurrent.TimeUnit.MILLISECONDS)
                .recordStats()
                .build();
    }

    @NotNull
    public Optional<String> getCached(@NotNull UUID player, @NotNull String args, @NotNull BiFunction<UUID, String, String> loader) {
        try (PerfMonitor ignored = new PerfMonitor("PlaceHolder API Handling")) {
            return performCaches.get(compileUniqueKey(player, args), () -> Optional.ofNullable(loader.apply(player, args)));
        } catch (ExecutionException ex) {
            plugin.logger().warn("Failed to get cache for " + player + " " + args, ex);
            return Optional.empty();
        }
    }

    @NotNull
    private String compileUniqueKey(@NotNull UUID player, @NotNull String queryString) {
        return JsonUtil.standard().toJson(new CompiledUniqueKey(player, queryString));
    }

    private long getShopsInWorld(@NotNull String world, boolean loadedOnly) {
        return plugin.getShopManager().getAllShops().stream()
                .filter(shop -> shop.getLocation().getWorld() != null)
                .filter(shop -> shop.getLocation().getWorld().getName().equals(world))
                .filter(shop -> !loadedOnly || shop.isLoaded())
                .count();
    }

    private long getLoadedPlayerShops(@NotNull UUID uuid) {
        return plugin.getShopManager().getPlayerAllShops(uuid).stream()
                .filter(Shop::isLoaded)
                .count();
    }

    private long getPlayerShopsInventoryUnavailable(@NotNull UUID uuid) {
        return plugin.getShopManager().getPlayerAllShops(uuid).stream()
                .filter(Shop::inventoryAvailable)
                .count();
    }

    public long getExpiredTime() {
        return expiredTime;
    }

    public @NotNull CacheStats getStats() {
        return performCaches.stats();
    }

    @Nullable
    public String readCache(@NotNull UUID player, @NotNull String queryString) {
        Optional<String> cache = performCaches.getIfPresent(compileUniqueKey(player, queryString));
        //noinspection OptionalAssignedToNull
        if (cache == null || cache.isEmpty()) return null;
        return cache.orElse(null);
    }

    @Override
    public ReloadResult reloadModule() throws Exception {
        init();
        return Reloadable.super.reloadModule();
    }

    public void writeCache(@NotNull UUID player, @NotNull String queryString, @NotNull String queryValue) {
        performCaches.put(compileUniqueKey(player, queryString), Optional.of(queryValue));
    }

    @Data
    static class CompiledUniqueKey {
        private UUID player;
        private String queryString;

        public CompiledUniqueKey(UUID player, String queryString) {
            this.player = player;
            this.queryString = queryString;
        }
    }


}
