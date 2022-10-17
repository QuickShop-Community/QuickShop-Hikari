package com.ghostchu.quickshop.papi;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.util.JsonUtil;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.simplereloadlib.ReloadResult;
import com.ghostchu.simplereloadlib.Reloadable;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheStats;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class PAPICache implements Reloadable {
    private QuickShop plugin;
    private long expiredTime;
    private Cache<String, String> performCaches;

    public PAPICache() {
        init();
        QuickShop.getInstance().getReloadManager().register(this);
    }

    private void init() {
        this.plugin = QuickShop.getInstance();
        this.expiredTime = 15 * 60 * 1000;
        this.performCaches = CacheBuilder.newBuilder()
                .expireAfterWrite(expiredTime, java.util.concurrent.TimeUnit.MILLISECONDS)
                .recordStats()
                .build();
    }

    public String getCached(@NotNull UUID player, @NotNull String[] args) {
        try {
            return performCaches.get(compileUniqueKey(player, args), () -> getValue(player, args));
        } catch (ExecutionException ex) {
            return null;
        }
    }

    @NotNull
    private String compileUniqueKey(@NotNull UUID player, @NotNull String[] args) {
        return compileUniqueKey(player, String.join("_", args));
    }

    private String getValue(@NotNull UUID player, String[] original) {
        // Make a copy with not-present values being null.
        String[] args = Arrays.copyOf(original, 3);

        // Invalid placeholder (%qs_%). Shouldn't happen at all, but you never know...
        if (Util.isNullOrEmpty(args[0])) {
            return null;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            // %qs_shops-total[_world]%
            case "shops-total" -> {
                // %qs_shops-total%
                if (Util.isNullOrEmpty(args[1])) {
                    return String.valueOf(plugin.getShopManager().getAllShops().size());
                }

                // %qs_shops-total_<world>%
                return String.valueOf(getShopsInWorld(args[1], false));
            }

            // %qs_shops-loaded[_world]%
            case "shops-loaded" -> {
                //%qs_shops-loaded%
                if (Util.isNullOrEmpty(args[1])) {
                    return String.valueOf(plugin.getShopManager().getLoadedShops().size());
                }

                // %qs_shops-loaded_<world>%
                return String.valueOf(getShopsInWorld(args[1], true));
            }

            // %qs_default-currency%
            case "default-currency" -> {
                return plugin.getCurrency();
            }

            case "player" -> {
                // Invalid placeholder (%qs_player_%)
                if (Util.isNullOrEmpty(args[1])) {
                    return null;
                }

                switch (args[1].toLowerCase(Locale.ROOT)) {
                    // %qs_player_shops-total[_uuid]%
                    case "shops-total" -> {
                        // %qs_player_shops-total%
                        if (Util.isNullOrEmpty(args[2])) {
                            return String.valueOf(plugin.getShopManager().getPlayerAllShops(player));
                        }

                        // Not valid UUID provided
                        if (!CommonUtil.isUUID(args[2])) {
                            return null;
                        }

                        // %qs_player_shop-total_<uuid>%
                        return String.valueOf(plugin.getShopManager().getPlayerAllShops(UUID.fromString(args[2])));
                    }

                    // %qs_player_shops-loaded[_uuid]%
                    case "shops-loaded" -> {
                        // %qs_shops-loaded%
                        if (Util.isNullOrEmpty(args[2])) {
                            return String.valueOf(getLoadedPlayerShops(player));
                        }

                        // Not valid UUID provided
                        if (!CommonUtil.isUUID(args[2])) {
                            return null;
                        }

                        // %qs_shops-loaded_<uuid>%
                        return String.valueOf(getLoadedPlayerShops(UUID.fromString(args[2])));
                    }

                    // %qs_player_shops-inventory-unavailable[_uuid]%
                    case "shops-inventory-unavailable" -> {
                        // %qs_player_shops-inventory-unavailable%
                        if (Util.isNullOrEmpty(args[2])) {
                            return String.valueOf(getPlayerShopsInventoryUnavailable(player));
                        }

                        // Not valid UUID provided
                        if (!CommonUtil.isUUID(args[2])) {
                            return null;
                        }

                        // %qs_player_shops-inventory-unavailable[_uuid]%
                        return String.valueOf(getPlayerShopsInventoryUnavailable(UUID.fromString(args[2])));
                    }

                    // Unknown QS player placeholder
                    default -> {
                        return null;
                    }
                }
            }

            // Unknown QS placeholder
            default -> {
                return null;
            }
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
        return performCaches.getIfPresent(compileUniqueKey(player, queryString));
    }

    @Override
    public ReloadResult reloadModule() throws Exception {
        init();
        return Reloadable.super.reloadModule();
    }

    public void writeCache(@NotNull UUID player, @NotNull String queryString, @NotNull String queryValue) {
        performCaches.put(compileUniqueKey(player, queryString), queryValue);
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
