package com.ghostchu.quickshop.util;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.database.DatabaseHelper;
import com.ghostchu.quickshop.api.shop.PlayerFinder;
import com.ghostchu.quickshop.common.util.GrabConcurrentTask;
import com.ghostchu.quickshop.common.util.JsonUtil;
import com.ghostchu.quickshop.common.util.QuickExecutor;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.quickshop.util.paste.GuavaCacheRender;
import com.ghostchu.quickshop.util.paste.item.SubPasteItem;
import com.ghostchu.quickshop.util.performance.PerfMonitor;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheStats;
import com.google.common.reflect.TypeToken;
import com.google.gson.annotations.SerializedName;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class FastPlayerFinder implements PlayerFinder, SubPasteItem {

  private final Cache<UUID, Optional<String>> nameCache = CacheBuilder.newBuilder()
          .expireAfterAccess(3, TimeUnit.DAYS)
          .maximumSize(5000)
          .recordStats()
          .build();
  private final Map<WeakReference<ExecutorService>, Map<Object, CompletableFuture<?>>> handling = new ConcurrentHashMap<>();
  private final QuickShop plugin;
  private final Timer cleanupTimer;
  private final PlayerFinderResolver resolver;

  public FastPlayerFinder(final QuickShop plugin) {

    this.plugin = plugin;
    this.resolver = new PlayerFinderResolver(this, plugin);
    cleanupTimer = new Timer("Failure lookup clean timer");
    plugin.getPasteManager().register(plugin.getJavaPlugin(), this);
    cleanupTimer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {

        nameCache.asMap().entrySet().removeIf(entry->entry.getValue().isEmpty());
        handling.entrySet().removeIf(entry->entry.getKey().get() == null);
      }
    }, 0, 1000 * 60 * 60);
  }

  public void bakeCaches() {

    final File file = new File("usercache.json");
    if(!file.exists()) {
      Log.debug("Not found usercache.json at " + file.getAbsolutePath());
      return;
    }
    Log.debug("Loading usercache.json at " + file.getAbsolutePath());
    try(FileReader reader = new FileReader(file)) {
      final List<UserCacheBean> userCacheBeans = JsonUtil.getGson().fromJson(reader, new TypeToken<List<UserCacheBean>>() {
      }.getType());
      final List<UserCacheBean> fullCacheBeans = userCacheBeans.stream()
              .filter(b->b.getUuid() != null)
              .filter(b->b.getName() != null)
              .toList();
      cacheInBatch(fullCacheBeans);
      Log.debug("Loaded " + userCacheBeans.size() + " entries from usercache.json");
    } catch(Exception e) {
      Log.debug("Giving up usercache.json loading: " + e.getMessage());
    }
  }


  public void cacheInBatch(final List<UserCacheBean> cacheBeans) {

    cacheBeans.forEach(b->nameCache.put(b.getUuid(), Optional.of(b.getName())));
    if(PackageUtil.parsePackageProperly("disableDatabaseCacheWrite").asBoolean(false)) {
      return;
    }
    final List<Triple<UUID, String, String>> batchUpdate = new ArrayList<>();
    cacheBeans.forEach(b->batchUpdate.add(new ImmutableTriple<>(b.getUuid(), null, b.getName())));
    final DatabaseHelper databaseHelper = plugin.getDatabaseHelper();
    if(databaseHelper != null) {
      Log.debug("Caching " + cacheBeans.size() + " usernames into database...");
      databaseHelper.updatePlayerProfileInBatch(batchUpdate)
              .thenAccept(i->Log.debug("Username caches update successfully, total " + i + " records updated."))
              .exceptionally((e)->{
                Log.debug("Failed to bake caches: " + e.getMessage());
                return null;
              });

    } else {
      Log.debug("Database not ready, skipping cache write.");
    }
  }

  @Override
  public @Nullable String uuid2Name(@NotNull final UUID uuid) {

    return uuid2Name(uuid, true, QuickExecutor.getPrimaryProfileIoExecutor());
  }

  @Override
  public @Nullable String uuid2Name(@NotNull final UUID uuid, final boolean writeCache, @NotNull final ExecutorService executorService) {

    return uuid2NameFuture(uuid, writeCache, executorService).join();
  }

  @Override
  public @Nullable UUID name2Uuid(@NotNull final String name) {

    return name2Uuid(name, true, QuickExecutor.getPrimaryProfileIoExecutor());
  }

  @Override
  public @Nullable UUID name2Uuid(@NotNull final String name, final boolean writeCache, @NotNull final ExecutorService executorService) {

    return name2UuidFuture(name, writeCache, executorService).join();
  }

  @Override
  public @NotNull CompletableFuture<String> uuid2NameFuture(@NotNull final UUID uuid) {

    return uuid2NameFuture(uuid, true, QuickExecutor.getPrimaryProfileIoExecutor());
  }

  @NotNull
  private Map<Object, CompletableFuture<?>> getExecutorRef(@NotNull final ExecutorService executorService) {

    for(final Map.Entry<WeakReference<ExecutorService>, Map<Object, CompletableFuture<?>>> entry : this.handling.entrySet()) {
      final ExecutorService service = entry.getKey().get();
      if(service == executorService) {
        return entry.getValue();
      }
    }
    final Map<Object, CompletableFuture<?>> map = new ConcurrentHashMap<>();
    this.handling.put(new WeakReference<>(executorService), map);
    Log.debug("Created new executor caching region for executor service: " + executorService);
    return map;
  }

  @Override
  public @NotNull CompletableFuture<String> uuid2NameFuture(@NotNull final UUID uuid, final boolean writeCache, @NotNull final ExecutorService executorService) {

    final Optional<String> lookupName = nameCache.getIfPresent(uuid);
    if(lookupName != null && lookupName.isPresent()) {
      return CompletableFuture.completedFuture(lookupName.get());
    }

    final Map<Object, CompletableFuture<?>> handling = getExecutorRef(executorService);
    @SuppressWarnings("unchecked") final CompletableFuture<String> inProgress = (CompletableFuture<String>)handling.get(uuid);
    if(inProgress != null) {
      Log.debug("Reused " + inProgress + " for uuid2Name lookup: uuid=" + uuid + ", writeCache=" + writeCache + ", executorService=" + executorService);
      return inProgress;
    }
    final CompletableFuture<String> future =
            CompletableFuture.supplyAsync(
                    ()->resolver.uuid2Name(uuid, executorService, (name)->{
                      handling.remove(uuid);
                      if(writeCache) {
                        cache(uuid, name);
                      }
                    }),
                    QuickExecutor.getPrimaryProfileIoExecutor());
    handling.put(uuid, future);
    return future;
  }

  @Override
  public @NotNull CompletableFuture<UUID> name2UuidFuture(@NotNull final String name) {

    return name2UuidFuture(name, true, QuickExecutor.getPrimaryProfileIoExecutor());
  }

  @Override
  public @NotNull CompletableFuture<UUID> name2UuidFuture(@NotNull final String name, final boolean writeCache, @NotNull final ExecutorService executorService) {

    for(final Map.Entry<UUID, Optional<String>> entry : nameCache.asMap().entrySet()) {
      if(entry.getValue().isPresent() && entry.getValue().get().equals(name)) {
        return CompletableFuture.completedFuture(entry.getKey());
      }
    }
    final Map<Object, CompletableFuture<?>> handling = getExecutorRef(executorService);
    @SuppressWarnings("unchecked") final CompletableFuture<UUID> inProgress = (CompletableFuture<UUID>)handling.get(name);
    if(inProgress != null) {
      Log.debug("Reused " + inProgress + " for name2Uuid lookup: name=" + name + ", writeCache=" + writeCache + ", executorService=" + executorService);
      return inProgress;
    }
    final CompletableFuture<UUID> future =
            CompletableFuture.supplyAsync(
                    ()->resolver.name2Uuid(name, executorService, (uuid)->{
                      handling.remove(name);
                      if(writeCache) {
                        cache(uuid, name);
                      }
                    }),
                    QuickExecutor.getPrimaryProfileIoExecutor());
    handling.put(name, future);
    return future;
  }

  @Override
  public void cache(@NotNull final UUID uuid, @Nullable final String name) {

    if(name == null) {
      return;
    }
    this.nameCache.put(uuid, Optional.of(name));
    if(PackageUtil.parsePackageProperly("disableDatabaseCacheWrite").asBoolean(false)) {
      return;
    }
    final DatabaseHelper databaseHelper = plugin.getDatabaseHelper();
    if(databaseHelper != null) {
      databaseHelper.updatePlayerProfile(uuid, null, name);
    }
  }


  @Override
  public boolean isCached(@NotNull final UUID uuid) {

    final Optional<String> value = this.nameCache.getIfPresent(uuid);
    return value != null && value.isPresent();
  }

  @NotNull
  public Cache<UUID, Optional<String>> getNameCache() {

    return nameCache;
  }

  @Override
  public @NotNull String genBody() {

    return "<h5>Username Cache</h5>" + renderTable(this.nameCache.stats());
  }

  @Override
  public @NotNull String getTitle() {

    return "PlayerFinder";
  }

  @NotNull
  private String renderTable(@NotNull final CacheStats stats) {

    return GuavaCacheRender.renderTable(stats);
  }

  public static class PlayerFinderResolver {

    private final QuickShop plugin;
    private final FastPlayerFinder parent;

    public PlayerFinderResolver(final FastPlayerFinder fastPlayerFinder, final QuickShop plugin) {

      this.plugin = plugin;
      this.parent = fastPlayerFinder;
    }

    @Nullable
    public String uuid2Name(@NotNull final UUID uuid, @NotNull final ExecutorService executorService, @NotNull final Consumer<String> endCallback) {

      String name = null;
      try(PerfMonitor perf = new PerfMonitor("Username Lookup - " + uuid)) {
        final GrabConcurrentTask<String> grabConcurrentTask = new GrabConcurrentTask<>(executorService, new DatabaseFindNameTask(plugin.getDatabaseHelper(), uuid), new BukkitFindNameTask(uuid), new EssentialsXFindNameTask(uuid), new PlayerDBFindNameTask(uuid));
        name = grabConcurrentTask.invokeAll("Username Lookup - " + uuid, 10, TimeUnit.SECONDS, Objects::nonNull);
        return name;
      } catch(InterruptedException e) {
        return null;
      } finally {
        endCallback.accept(name);
      }
    }

    @NotNull
    public UUID name2Uuid(@NotNull final String name, @NotNull final ExecutorService executorService, @NotNull final Consumer<UUID> endCallback) {

      UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
      try(PerfMonitor perf = new PerfMonitor("UniqueID Lookup - " + name)) {
        final GrabConcurrentTask<UUID> grabConcurrentTask = new GrabConcurrentTask<>(executorService, new DatabaseFindUUIDTask(plugin.getDatabaseHelper(), name), new BukkitFindUUIDTask(name), new EssentialsXFindUUIDTask(name), new PlayerDBFindUUIDTask(name));
        // This cannot fail.
        final UUID lookupResult = grabConcurrentTask.invokeAll("UniqueID Lookup - " + name, 15, TimeUnit.SECONDS, Objects::nonNull);
        if(lookupResult != null) {
          uuid = lookupResult;
        }
        return uuid;
      } catch(InterruptedException e) {
        return uuid;
      } finally {
        endCallback.accept(uuid);
      }
    }
  }

  static class BukkitFindUUIDTask implements Supplier<UUID> {

    public final String name;

    BukkitFindUUIDTask(@NotNull final String name) {

      this.name = name;
    }

    @Override
    public UUID get() {

      if(!PackageUtil.parsePackageProperly("bukkitFindUUIDTask").asBoolean(true)) {
        return null;
      }
      final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
      final UUID uuid = offlinePlayer.getUniqueId();
      Log.debug("Lookup result: " + uuid);
      return uuid;
    }
  }

  static class PlayerDBFindUUIDTask implements Supplier<UUID> {

    public final String name;

    PlayerDBFindUUIDTask(@NotNull final String name) {

      this.name = name;
    }

    @Override
    public UUID get() {

      if(!PackageUtil.parsePackageProperly("playerDBFindUUIDTask").asBoolean(false)) {
        return null;
      }
      final HttpResponse<String> response = Unirest.get("https://playerdb.co/api/player/minecraft/" + name).asString();
      final PlayerDBResponse playerDBResponse = JsonUtil.getGson().fromJson(response.getBody(), PlayerDBResponse.class);
      if(!playerDBResponse.getSuccess()) {
        return null;
      }
      return UUID.fromString(playerDBResponse.getData().getPlayer().getId());
    }

    static class PlayerDBResponse {

      @SerializedName("code")
      private String code;
      @SerializedName("message")
      private String message;
      @SerializedName("data")
      private DataDTO data;
      @SerializedName("success")
      private Boolean success;

      public String getCode() {

        return code;
      }

      public void setCode(final String code) {

        this.code = code;
      }

      public String getMessage() {

        return message;
      }

      public void setMessage(final String message) {

        this.message = message;
      }

      public DataDTO getData() {

        return data;
      }

      public void setData(final DataDTO data) {

        this.data = data;
      }

      public Boolean getSuccess() {

        return success;
      }

      public void setSuccess(final Boolean success) {

        this.success = success;
      }

      public static class DataDTO {

        @SerializedName("player")
        private PlayerDTO player;

        public PlayerDTO getPlayer() {

          return player;
        }

        public void setPlayer(final PlayerDTO player) {

          this.player = player;
        }

        public static class PlayerDTO {

          @SerializedName("meta")
          private MetaDTO meta;
          @SerializedName("username")
          private String username;
          @SerializedName("id")
          private String id;
          @SerializedName("raw_id")
          private String rawId;
          @SerializedName("avatar")
          private String avatar;
          @SerializedName("name_history")
          private List<?> nameHistory;

          public MetaDTO getMeta() {

            return meta;
          }

          public void setMeta(final MetaDTO meta) {

            this.meta = meta;
          }

          public String getUsername() {

            return username;
          }

          public void setUsername(final String username) {

            this.username = username;
          }

          public String getId() {

            return id;
          }

          public void setId(final String id) {

            this.id = id;
          }

          public String getRawId() {

            return rawId;
          }

          public void setRawId(final String rawId) {

            this.rawId = rawId;
          }

          public String getAvatar() {

            return avatar;
          }

          public void setAvatar(final String avatar) {

            this.avatar = avatar;
          }

          public List<?> getNameHistory() {

            return nameHistory;
          }

          public void setNameHistory(final List<?> nameHistory) {

            this.nameHistory = nameHistory;
          }

          public static class MetaDTO {

            @SerializedName("cached_at")
            private Integer cachedAt;

            public Integer getCachedAt() {

              return cachedAt;
            }

            public void setCachedAt(final Integer cachedAt) {

              this.cachedAt = cachedAt;
            }
          }
        }
      }
    }
  }

  static class PlayerDBFindNameTask implements Supplier<String> {

    public final UUID uuid;

    PlayerDBFindNameTask(@NotNull final UUID uuid) {

      this.uuid = uuid;
    }

    @Override
    public String get() {

      if(!PackageUtil.parsePackageProperly("playerDBFindNameTask").asBoolean(false)) {
        return null;
      }
      final HttpResponse<String> response = Unirest.get("https://playerdb.co/api/player/minecraft/" + uuid).asString();
      final PlayerDBResponse playerDBResponse = JsonUtil.getGson().fromJson(response.getBody(), PlayerDBResponse.class);
      if(!playerDBResponse.getSuccess()) {
        return null;
      }
      return playerDBResponse.getData().getPlayer().getUsername();
    }

    static class PlayerDBResponse {

      @SerializedName("code")
      private String code;
      @SerializedName("message")
      private String message;
      @SerializedName("data")
      private DataDTO data;
      @SerializedName("success")
      private Boolean success;

      public String getCode() {

        return code;
      }

      public void setCode(final String code) {

        this.code = code;
      }

      public String getMessage() {

        return message;
      }

      public void setMessage(final String message) {

        this.message = message;
      }

      public DataDTO getData() {

        return data;
      }

      public void setData(final DataDTO data) {

        this.data = data;
      }

      public Boolean getSuccess() {

        return success;
      }

      public void setSuccess(final Boolean success) {

        this.success = success;
      }

      public static class DataDTO {

        @SerializedName("player")
        private PlayerDTO player;

        public PlayerDTO getPlayer() {

          return player;
        }

        public void setPlayer(final PlayerDTO player) {

          this.player = player;
        }

        public static class PlayerDTO {

          @SerializedName("meta")
          private MetaDTO meta;
          @SerializedName("username")
          private String username;
          @SerializedName("id")
          private String id;
          @SerializedName("raw_id")
          private String rawId;
          @SerializedName("avatar")
          private String avatar;
          @SerializedName("name_history")
          private List<?> nameHistory;

          public MetaDTO getMeta() {

            return meta;
          }

          public void setMeta(final MetaDTO meta) {

            this.meta = meta;
          }

          public String getUsername() {

            return username;
          }

          public void setUsername(final String username) {

            this.username = username;
          }

          public String getId() {

            return id;
          }

          public void setId(final String id) {

            this.id = id;
          }

          public String getRawId() {

            return rawId;
          }

          public void setRawId(final String rawId) {

            this.rawId = rawId;
          }

          public String getAvatar() {

            return avatar;
          }

          public void setAvatar(final String avatar) {

            this.avatar = avatar;
          }

          public List<?> getNameHistory() {

            return nameHistory;
          }

          public void setNameHistory(final List<?> nameHistory) {

            this.nameHistory = nameHistory;
          }

          public static class MetaDTO {

            @SerializedName("cached_at")
            private Integer cachedAt;

            public Integer getCachedAt() {

              return cachedAt;
            }

            public void setCachedAt(final Integer cachedAt) {

              this.cachedAt = cachedAt;
            }
          }
        }
      }
    }
  }

  static class BukkitFindNameTask implements Supplier<String> {

    public final UUID uuid;

    BukkitFindNameTask(@NotNull final UUID uuid) {

      this.uuid = uuid;
    }

    @Override
    public String get() {

      if(!PackageUtil.parsePackageProperly("bukkitFindNameTask").asBoolean(true)) {
        return null;
      }
      final OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
      final String name = player.getName();
      Log.debug("Lookup result: " + name);
      return name;
    }
  }


  static class EssentialsXFindUUIDTask implements Supplier<UUID> {

    public final String name;

    EssentialsXFindUUIDTask(@NotNull final String name) {

      this.name = name;
    }

    @Override
    public UUID get() {

      try {
        if(!PackageUtil.parsePackageProperly("essentialsXFindUUIDTask").asBoolean(true)) {
          return null;
        }
        final Plugin essPlugin = Bukkit.getPluginManager().getPlugin("Essentials");
        if(essPlugin == null || !essPlugin.isEnabled()) {
          return null;
        }
        final Essentials ess = (Essentials)essPlugin;
        final User user = ess.getUser(name);
        if(user == null) {
          return null;
        }
        final UUID uuid = user.getUUID();
        Log.debug("Lookup result: " + uuid);
        return uuid;
      } catch(Throwable th) {
        return null;
      }
    }
  }

  static class EssentialsXFindNameTask implements Supplier<String> {

    public final UUID uuid;

    EssentialsXFindNameTask(@NotNull final UUID uuid) {

      this.uuid = uuid;
    }

    @Override
    public String get() {

      try {
        if(!PackageUtil.parsePackageProperly("essentialsXFindNameTask").asBoolean(true)) {
          return null;
        }
        final Plugin essPlugin = Bukkit.getPluginManager().getPlugin("Essentials");
        if(essPlugin == null || !essPlugin.isEnabled()) {
          return null;
        }
        final Essentials ess = (Essentials)essPlugin;
        final User user = ess.getUser(uuid);
        if(user == null) {
          return null;
        }
        final String name = user.getName();
        Log.debug("Lookup result: " + name);
        return name;
      } catch(Throwable th) {
        return null;
      }
    }
  }


  static class DatabaseFindNameTask implements Supplier<String> {

    private final DatabaseHelper db;
    private final UUID uuid;

    public DatabaseFindNameTask(@Nullable final DatabaseHelper db, @NotNull final UUID uuid) {

      this.db = db;
      this.uuid = uuid;
    }

    @Override
    public String get() {

      if(!PackageUtil.parsePackageProperly("databaseFindNameTask").asBoolean(true)) {
        return null;
      }
      if(this.db == null) {
        return null;
      }
      try {
        final String name = db.getPlayerName(uuid).get(30, TimeUnit.SECONDS);
        Log.debug("Lookup result: " + name);
        return name;
      } catch(InterruptedException e) {
        Thread.currentThread().interrupt();
        return null;
      } catch(ExecutionException e) {
        Log.debug("Error: a exception created while query the database for username looking up: " + e.getMessage());
        return null;
      } catch(TimeoutException e) {
        Log.debug("Warning, timeout when query the database for username looking up, slow connection?");
        return null;
      }
    }
  }

  static class DatabaseFindUUIDTask implements Supplier<UUID> {

    private final DatabaseHelper db;
    private final String name;

    public DatabaseFindUUIDTask(@Nullable final DatabaseHelper db, @NotNull final String name) {

      this.db = db;
      this.name = name;
    }

    @Override
    public UUID get() {

      if(!PackageUtil.parsePackageProperly("databaseFindUUIDTask").asBoolean(true)) {
        return null;
      }
      if(this.db == null) {
        return null;
      }/**/
      try {
        final UUID uuid = db.getPlayerUUID(name).get(30, TimeUnit.SECONDS);
        Log.debug("Lookup result: " + uuid);
        return uuid;
      } catch(InterruptedException e) {
        Thread.currentThread().interrupt();
        return null;
      } catch(ExecutionException e) {
        Log.debug("Error: a exception created while query the database for unique id looking up: " + e.getMessage());
        return null;
      } catch(TimeoutException e) {
        Log.debug("Warning, timeout when query the database for unique id looking up, slow connection?");
        return null;
      }
    }
  }

  public static class UserCacheBean {

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
