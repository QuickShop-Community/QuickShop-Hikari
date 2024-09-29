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
  public Optional<String> getCached(@NotNull final UUID player, @NotNull final String args, @NotNull final BiFunction<UUID, String, String> loader) {

    try(PerfMonitor ignored = new PerfMonitor("PlaceHolder API Handling")) {
      return performCaches.get(compileUniqueKey(player, args), ()->Optional.ofNullable(loader.apply(player, args)));
    } catch(ExecutionException ex) {
      plugin.logger().warn("Failed to get cache for " + player + " " + args, ex);
      return Optional.empty();
    }
  }

  @NotNull
  private String compileUniqueKey(@NotNull final UUID player, @NotNull final String queryString) {

    return JsonUtil.standard().toJson(new CompiledUniqueKey(player, queryString));
  }

  private long getShopsInWorld(@NotNull final String world, final boolean loadedOnly) {

    return plugin.getShopManager().getAllShops().stream()
            .filter(shop->shop.getLocation().getWorld() != null)
            .filter(shop->shop.getLocation().getWorld().getName().equals(world))
            .filter(shop->!loadedOnly || shop.isLoaded())
            .count();
  }

  private long getLoadedPlayerShops(@NotNull final UUID uuid) {

    return plugin.getShopManager().getLoadedShops().stream().filter(shop->{
      final UUID souid = shop.getOwner().getUniqueId();
      if(souid == null) {
        return false;
      }
      return souid.equals(uuid);
    }).count();
  }

  private long getLoadedPlayerShops(@NotNull final String name) {

    return plugin.getShopManager().getLoadedShops().stream().filter(shop->{
      final String sousrname = shop.getOwner().getUsername();
      if(sousrname == null) {
        return false;
      }
      return name.equals(sousrname);
    }).count();
  }

  private long getPlayerShopsInventoryUnavailable(@NotNull final UUID uuid) {

    return plugin.getShopManager().getAllShops(uuid).stream()
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
  public String readCache(@NotNull final UUID player, @NotNull final String queryString) {

    final Optional<String> cache = performCaches.getIfPresent(compileUniqueKey(player, queryString));
    //noinspection OptionalAssignedToNull
    if(cache == null || cache.isEmpty()) {
      return null;
    }
    return cache.orElse(null);
  }

  @Override
  public ReloadResult reloadModule() throws Exception {

    init();
    return Reloadable.super.reloadModule();
  }

  public void writeCache(@NotNull final UUID player, @NotNull final String queryString, @NotNull final String queryValue) {

    performCaches.put(compileUniqueKey(player, queryString), Optional.of(queryValue));
  }

  @Data
  static class CompiledUniqueKey {

    private UUID player;
    private String queryString;

    public CompiledUniqueKey(final UUID player, final String queryString) {

      this.player = player;
      this.queryString = queryString;
    }
  }

}
