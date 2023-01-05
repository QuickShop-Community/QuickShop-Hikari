package com.ghostchu.quickshop.util;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.database.DatabaseHelper;
import com.ghostchu.quickshop.common.util.QuickExecutor;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.quickshop.util.performance.PerfMonitor;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class FastPlayerFinder {
    private final Cache<UUID, String> nameCache = CacheBuilder.newBuilder()
            .expireAfterAccess(3, TimeUnit.DAYS)
            .maximumSize(1500)
            .recordStats()
            .build();
    private final QuickShop plugin;

    public FastPlayerFinder(QuickShop plugin) {
        this.plugin = plugin;
    }

    @Nullable
    public synchronized String uuid2Name(@NotNull UUID uuid) {
        try (PerfMonitor perf = new PerfMonitor("Username Lookup - " + uuid)) {
            String cachedName = nameCache.getIfPresent(uuid);
            if (cachedName != null) {
                return cachedName;
            }
            perf.setContext("cache miss");
            String name = QuickExecutor.getCommonExecutor().invokeAny(
                    List.of(new BukkitFindTask(uuid), new DatabaseFindTask(plugin.getDatabaseHelper(), uuid)),
                    3, TimeUnit.SECONDS);
            this.nameCache.put(uuid, name);
            return name;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    public synchronized UUID name2Uuid(@NotNull String name) {
        try (PerfMonitor perf = new PerfMonitor("UniqueID Lookup - " + name)) {
            for (Map.Entry<UUID, String> uuidStringEntry : nameCache.asMap().entrySet()) {
                if (uuidStringEntry.getValue().equals(name)) {
                    return uuidStringEntry.getKey();
                }
            }
            perf.setContext("cache miss");
            @SuppressWarnings("deprecation") OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
            String playerName = offlinePlayer.getName();
            if (playerName == null) {
                playerName = name;
            }
            this.nameCache.put(offlinePlayer.getUniqueId(), playerName);
            return offlinePlayer.getUniqueId();
        }
    }

    public void cache(@NotNull UUID uuid, @NotNull String name) {
        this.nameCache.put(uuid, name);
    }

    public boolean isCached(@NotNull UUID uuid) {
        return this.nameCache.getIfPresent(uuid) != null;
    }

    @NotNull
    public Cache<UUID, String> getNameCache() {
        return nameCache;
    }

    static class BukkitFindTask implements Callable<String> {
        public final UUID uuid;

        BukkitFindTask(@NotNull UUID uuid) {
            this.uuid = uuid;
        }

        @Override
        @Nullable
        public String call() {
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            return player.getName();
        }
    }

    static class DatabaseFindTask implements Callable<String> {
        private final DatabaseHelper db;
        private final UUID uuid;

        public DatabaseFindTask(@Nullable DatabaseHelper db, @NotNull UUID uuid) {
            this.db = db;
            this.uuid = uuid;
        }

        @Override
        public @Nullable String call() {
            if (this.db == null) {
                return null;
            }
            try {
                return db.getPlayerName(uuid).get(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            } catch (ExecutionException e) {
                Log.debug("Error: a exception created while query the database for username looking up: " + e.getMessage());
                return null;
            } catch (TimeoutException e) {
                Log.debug("Warning, timeout when query the database for username looking up, slow connection?");
                return null;
            }
        }
    }
}
