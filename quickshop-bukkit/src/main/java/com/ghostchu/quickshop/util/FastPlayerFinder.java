package com.ghostchu.quickshop.util;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.database.DatabaseHelper;
import com.ghostchu.quickshop.common.util.GrabConcurrentTask;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.quickshop.util.performance.PerfMonitor;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

public class FastPlayerFinder {
    private final Cache<UUID, Optional<String>> nameCache = CacheBuilder.newBuilder()
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
            Optional<String> cachedName = nameCache.getIfPresent(uuid);
            if (cachedName != null && cachedName.isPresent()) {
                return cachedName.get();
            }
            perf.setContext("cache miss");
            GrabConcurrentTask<String> grabConcurrentTask = new GrabConcurrentTask<>(new BukkitFindTask(uuid), new DatabaseFindTask(plugin.getDatabaseHelper(), uuid));
            String name = grabConcurrentTask.invokeAll(3, TimeUnit.SECONDS, Objects::nonNull);
            this.nameCache.put(uuid, Optional.ofNullable(name));
            return name;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    @NotNull
    public synchronized UUID name2Uuid(@NotNull String name) {
        try (PerfMonitor perf = new PerfMonitor("UniqueID Lookup - " + name)) {
            for (Map.Entry<UUID, Optional<String>> uuidStringEntry : nameCache.asMap().entrySet()) {
                if (uuidStringEntry.getValue().isPresent()) {
                    if (uuidStringEntry.getValue().get().equals(name)) {
                        return uuidStringEntry.getKey();
                    }
                }
            }
            perf.setContext("cache miss");
            @SuppressWarnings("deprecation") OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
            String playerName = offlinePlayer.getName();
            if (playerName == null) {
                playerName = name;
            }
            this.nameCache.put(offlinePlayer.getUniqueId(), Optional.of(playerName));
            return offlinePlayer.getUniqueId();
        }
    }

    public void cache(@NotNull UUID uuid, @NotNull String name) {
        this.nameCache.put(uuid, Optional.of(name));
    }

    public boolean isCached(@NotNull UUID uuid) {
        Optional<String> value = this.nameCache.getIfPresent(uuid);
        return value != null && value.isPresent();
    }

    @NotNull
    public Cache<UUID, Optional<String>> getNameCache() {
        return nameCache;
    }

    static class BukkitFindTask implements Supplier<String> {
        public final UUID uuid;

        BukkitFindTask(@NotNull UUID uuid) {
            this.uuid = uuid;
        }

        @Override
        public String get() {
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            return player.getName();
        }
    }

    static class DatabaseFindTask implements Supplier<String> {
        private final DatabaseHelper db;
        private final UUID uuid;

        public DatabaseFindTask(@Nullable DatabaseHelper db, @NotNull UUID uuid) {
            this.db = db;
            this.uuid = uuid;
        }

        @Override
        public String get() {
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
