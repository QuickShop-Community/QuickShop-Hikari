package com.ghostchu.quickshop.util;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.database.DatabaseHelper;
import com.ghostchu.quickshop.api.shop.PlayerFinder;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.common.util.GrabConcurrentTask;
import com.ghostchu.quickshop.common.util.JsonUtil;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.quickshop.util.performance.PerfMonitor;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

public class FastPlayerFinder implements PlayerFinder {
    private final Cache<UUID, Optional<String>> nameCache = CacheBuilder.newBuilder()
            .expireAfterAccess(3, TimeUnit.DAYS)
            .maximumSize(2500)
            .recordStats()
            .build();
    private final QuickShop plugin;

    public FastPlayerFinder(QuickShop plugin) {
        this.plugin = plugin;
        loadFromUserCache();
    }

    private void loadFromUserCache() {
        File file = new File("usercache.json");
        if (!file.exists()) {
            Log.debug("Not found usercache.json at " + file.getAbsolutePath());
            return;
        }
        Log.debug("Loading usercache.json at " + file.getAbsolutePath());
        try (FileReader reader = new FileReader(file)) {
            List<UserCacheBean> userCacheBeans = JsonUtil.getGson().fromJson(reader, new TypeToken<List<UserCacheBean>>() {
            }.getType());
            userCacheBeans.forEach(bean -> {
                if (bean.getUuid() != null && bean.getName() != null) {
                    nameCache.put(bean.getUuid(), Optional.of(bean.getName()));
                }
            });
            Log.debug("Loaded " + userCacheBeans.size() + " entries from usercache.json");
        } catch (Exception e) {
            Log.debug("Giving up usercache.json loading: " + e.getMessage());
        }
    }

    @Override
    @Nullable
    public String uuid2Name(@NotNull UUID uuid) {
        try (PerfMonitor perf = new PerfMonitor("Username Lookup - " + uuid)) {
            Optional<String> cachedName = nameCache.getIfPresent(uuid);
            if (cachedName != null && cachedName.isPresent()) {
                return cachedName.get();
            }
            perf.setContext("cache miss");
            GrabConcurrentTask<String> grabConcurrentTask = new GrabConcurrentTask<>(new BukkitFindNameTask(uuid), new DatabaseFindNameTask(plugin.getDatabaseHelper(), uuid), new EssentialsXFindNameTask(uuid));
            String name = grabConcurrentTask.invokeAll(3, TimeUnit.SECONDS, Objects::nonNull);
            this.nameCache.put(uuid, Optional.ofNullable(name));
            return name;
        } catch (InterruptedException e) {
            plugin.logger().warn("Interrupted when looking up username for " + uuid, e);
            return null;
        }
    }

    @Override
    @NotNull
    public UUID name2Uuid(@NotNull String name) {
        try (PerfMonitor perf = new PerfMonitor("UniqueID Lookup - " + name)) {
            for (Map.Entry<UUID, Optional<String>> uuidStringEntry : nameCache.asMap().entrySet()) {
                if (uuidStringEntry.getValue().isPresent()) {
                    if (uuidStringEntry.getValue().get().equals(name)) {
                        return uuidStringEntry.getKey();
                    }
                }
            }
            perf.setContext("cache miss");
            GrabConcurrentTask<UUID> grabConcurrentTask = new GrabConcurrentTask<>(new BukkitFindUUIDTask(name), new EssentialsXFindUUIDTask(name), new DatabaseFindUUIDTask(plugin.getDatabaseHelper(), name));
            // This cannot fail.
            UUID uuid = grabConcurrentTask.invokeAll(1, TimeUnit.DAYS, Objects::nonNull);
            if (uuid == null) {
                return CommonUtil.getNilUniqueId();
            }
            this.nameCache.put(uuid, Optional.of(name));
            return uuid;
        } catch (InterruptedException e) {
            plugin.logger().warn("Interrupted when looking up UUID for " + name, e);
            return CommonUtil.getNilUniqueId();
        }
    }

    @Override
    public void cache(@NotNull UUID uuid, @NotNull String name) {
        this.nameCache.put(uuid, Optional.of(name));
    }

    @Override
    public boolean isCached(@NotNull UUID uuid) {
        Optional<String> value = this.nameCache.getIfPresent(uuid);
        return value != null && value.isPresent();
    }

    @NotNull
    public Cache<UUID, Optional<String>> getNameCache() {
        return nameCache;
    }

    static class BukkitFindUUIDTask implements Supplier<UUID> {
        public final String name;

        BukkitFindUUIDTask(@NotNull String name) {
            this.name = name;
        }

        @Override
        public UUID get() {
            if (!PackageUtil.parsePackageProperly("bukkitFindUUIDTask").asBoolean(true)) {
                return null;
            }
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
            UUID uuid = offlinePlayer.getUniqueId();
            Log.debug("Lookup result: " + uuid);
            return uuid;
        }
    }

    static class BukkitFindNameTask implements Supplier<String> {
        public final UUID uuid;

        BukkitFindNameTask(@NotNull UUID uuid) {
            this.uuid = uuid;
        }

        @Override
        public String get() {
            if (!PackageUtil.parsePackageProperly("bukkitFindNameTask").asBoolean(true)) {
                return null;
            }
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            String name = player.getName();
            Log.debug("Lookup result: " + name);
            return name;
        }
    }


    static class EssentialsXFindUUIDTask implements Supplier<UUID> {
        public final String name;

        EssentialsXFindUUIDTask(@NotNull String name) {
            this.name = name;
        }

        @Override
        public UUID get() {
            try {
                if (!PackageUtil.parsePackageProperly("essentialsXFindUUIDTask").asBoolean(true)) {
                    return null;
                }
                Plugin essPlugin = Bukkit.getPluginManager().getPlugin("Essentials");
                if (essPlugin == null || !essPlugin.isEnabled()) return null;
                Essentials ess = (Essentials) essPlugin;
                User user = ess.getUser(name);
                if (user == null) return null;
                UUID uuid = user.getUUID();
                Log.debug("Lookup result: " + uuid);
                return uuid;
            } catch (Throwable th) {
                return null;
            }
        }
    }

    static class EssentialsXFindNameTask implements Supplier<String> {
        public final UUID uuid;

        EssentialsXFindNameTask(@NotNull UUID uuid) {
            this.uuid = uuid;
        }

        @Override
        public String get() {
            try {
                if (!PackageUtil.parsePackageProperly("essentialsXFindNameTask").asBoolean(true)) {
                    return null;
                }
                Plugin essPlugin = Bukkit.getPluginManager().getPlugin("Essentials");
                if (essPlugin == null || !essPlugin.isEnabled()) return null;
                Essentials ess = (Essentials) essPlugin;
                User user = ess.getUser(uuid);
                if (user == null) return null;
                String name = user.getName();
                Log.debug("Lookup result: " + name);
                return name;
            } catch (Throwable th) {
                return null;
            }
        }
    }


    static class DatabaseFindNameTask implements Supplier<String> {
        private final DatabaseHelper db;
        private final UUID uuid;

        public DatabaseFindNameTask(@Nullable DatabaseHelper db, @NotNull UUID uuid) {
            this.db = db;
            this.uuid = uuid;
        }

        @Override
        public String get() {
            if (!PackageUtil.parsePackageProperly("databaseFindNameTask").asBoolean(true)) {
                return null;
            }
            if (this.db == null) {
                return null;
            }
            try {
                String name = db.getPlayerName(uuid).get(30, TimeUnit.SECONDS);
                Log.debug("Lookup result: " + name);
                return name;
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

    static class DatabaseFindUUIDTask implements Supplier<UUID> {
        private final DatabaseHelper db;
        private final String name;

        public DatabaseFindUUIDTask(@Nullable DatabaseHelper db, @NotNull String name) {
            this.db = db;
            this.name = name;
        }

        @Override
        public UUID get() {
            if (!PackageUtil.parsePackageProperly("databaseFindUUIDTask").asBoolean(true)) {
                return null;
            }
            if (this.db == null) {
                return null;
            }/**/
            try {
                UUID uuid = db.getPlayerUUID(name).get(30, TimeUnit.SECONDS);
                Log.debug("Lookup result: " + uuid);
                return uuid;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            } catch (ExecutionException e) {
                Log.debug("Error: a exception created while query the database for unique id looking up: " + e.getMessage());
                return null;
            } catch (TimeoutException e) {
                Log.debug("Warning, timeout when query the database for unique id looking up, slow connection?");
                return null;
            }
        }
    }

    static class UserCacheBean {
        private String name;
        private UUID uuid;

        public UUID getUuid() {
            return uuid;
        }

        public String getName() {
            return name;
        }
    }
}
