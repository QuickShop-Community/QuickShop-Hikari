package com.ghostchu.quickshop.shop;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.event.modification.ShopCreateSuccessEvent;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopChunk;
import com.ghostchu.quickshop.api.shop.ShopManager;
import com.ghostchu.quickshop.api.shop.cache.ShopCache;
import com.ghostchu.quickshop.api.shop.cache.ShopCacheNamespacedKey;
import com.ghostchu.quickshop.api.shop.cache.ShopInventoryCountCache;
import com.ghostchu.quickshop.common.util.QuickExecutor;
import com.ghostchu.quickshop.shop.cache.BoxedShop;
import com.ghostchu.quickshop.shop.cache.SimpleShopCache;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.economyformatter.EconomyFormatter;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.quickshop.util.performance.PerfMonitor;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.papermc.lib.PaperLib;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

// This class is extract from SimpleShopManager because it is too big...
@ApiStatus.Experimental
public abstract class AbstractShopManager implements ShopManager {

  @Getter
  protected ShopCache shopCache;
  protected static final DecimalFormat STANDARD_FORMATTER = new DecimalFormat("#.#########");
  // the performance impact on busy server
  protected final Cache<UUID, Shop> shopRuntimeUUIDCaching =
          CacheBuilder.newBuilder()
                  .expireAfterAccess(10, TimeUnit.MINUTES)
                  .maximumSize(50)
                  .weakValues()
                  .initialCapacity(50)
                  .build();
  protected final QuickShop plugin;

  protected final EconomyFormatter formatter;
  protected final Map<String, Map<ShopChunk, Map<Location, Shop>>> shops = Maps.newConcurrentMap();
  protected final Set<Shop> loadedShops = Sets.newConcurrentHashSet(); // Handle it by collection to reduce


  public AbstractShopManager(@NotNull final QuickShop plugin) {

    Util.ensureThread(false);
    this.plugin = plugin;
    this.formatter = new EconomyFormatter(plugin, plugin::getEconomy);

  }

  public void init() {

    final Map<@NotNull ShopCacheNamespacedKey, @NotNull Pair<@NotNull Function<Location, Shop>, @Nullable Cache<Location, BoxedShop>>> map = new HashMap<>();
    // SINGLE
    map.put(ShopCacheNamespacedKey.SINGLE, new ImmutablePair<>(this::getShop, null));
    map.put(ShopCacheNamespacedKey.INCLUDE_ATTACHED, new ImmutablePair<>(this::getShopIncludeAttached, null));
    shopCache = new SimpleShopCache(plugin, map);
  }

  /**
   * Adds a shop to the world. Does NOT require the chunk or world to be loaded Call shop.onLoad by
   * yourself
   *
   * @param shop The shop to add
   */
  protected void addShopToLookupTable(@NotNull final Shop shop) {

    final String world = shop.getLocation().getWorld().getName();
    final Map<ShopChunk, Map<Location, Shop>> inWorld = shops.computeIfAbsent(world, k->new MapMaker().initialCapacity(3).makeMap());
    // There's no world storage yet. We need to create that map.
    // Put it in the data universe
    // Calculate the chunks coordinates. These are 1,2,3 for each chunk, NOT
    // location rounded to the nearest 16.
    final int x = (int)Math.floor((shop.getLocation().getBlockX()) / 16.0);
    final int z = (int)Math.floor((shop.getLocation().getBlockZ()) / 16.0);
    // Get the chunk set from the world info
    final ShopChunk shopChunk = new SimpleShopChunk(world, x, z);
    final Map<Location, Shop> inChunk =
            inWorld.computeIfAbsent(shopChunk, k->new MapMaker().initialCapacity(1).makeMap());
    // That chunk data hasn't been created yet - Create it!
    // Put it in the world
    // Put the shop in its location in the chunk list.
    inChunk.put(shop.getLocation(), shop);
  }

  @Override
  public void bakeShopRuntimeRandomUniqueIdCache(@NotNull final Shop shop) {

    shopRuntimeUUIDCaching.put(shop.getRuntimeRandomUniqueId(), shop);
  }

  /**
   * Format the price use formatter
   *
   * @param d price
   *
   * @return formatted price
   */
  @Override
  public @NotNull String format(final double d, @NotNull final World world, @Nullable final String currency) {

    return formatter.format(d, world, currency);
  }

  /**
   * Format the price use formatter
   *
   * @param d price
   *
   * @return formatted price
   */
  @Override
  public @NotNull String format(final double d, @NotNull final Shop shop) {

    return formatter.format(d, shop);
  }

  @NotNull
  @Override
  public CompletableFuture<@NotNull List<Shop>> queryTaggedShops(@NotNull final UUID tagger, @NotNull final String tag) {

    Util.ensureThread(true);
    return CompletableFuture.supplyAsync(()->plugin.getDatabaseHelper().listShopsTaggedBy(tagger, tag).stream().map(this::getShop).toList(), QuickExecutor.getCommonExecutor());

  }

  @Override
  public void loadShop(@NotNull final Shop shop) {
    //noinspection deprecation
    shop.handleLoading();
    this.loadedShops.add(shop);
  }

  @Override
  public void unloadShop(@NotNull final Shop shop) {

    unloadShop(shop, false);
  }

  @Override
  public void unloadShop(@NotNull final Shop shop, final boolean dontTouchWorld) {
    //noinspection deprecation
    shop.handleUnloading(dontTouchWorld);
    this.loadedShops.remove(shop);
  }

  /**
   * Removes a shop from the world. Does NOT remove it from the database. * REQUIRES * the world to
   * be loaded Call shop.onUnload by your self.
   *
   * @param shop The shop to remove
   */
  private void removeShopFromLookupTable(@NotNull final Shop shop) {

    final Location loc = shop.getLocation();
    final String world = Objects.requireNonNull(loc.getWorld()).getName();
    final Map<ShopChunk, Map<Location, Shop>> inWorld = this.getShops().get(world);
    if(inWorld == null) {
      return;
    }
    final int x = (int)Math.floor((loc.getBlockX()) / 16.0);
    final int z = (int)Math.floor((loc.getBlockZ()) / 16.0);
    final ShopChunk shopChunk = new SimpleShopChunk(world, x, z);
    final Map<Location, Shop> inChunk = inWorld.get(shopChunk);
    if(inChunk == null) {
      return;
    }
    inChunk.remove(loc);
    shopCache.invalidate(null, shop.getLocation());
  }


  protected void processCreationFail(@NotNull final Shop shop, @NotNull final QUser owner, @NotNull final Throwable e2) {

    plugin.logger().error("Shop create failed, auto fix failed, the changes may won't commit to database.", e2);
    plugin.text().of(owner, "shop-creation-failed").send();
    Util.mainThreadRun(()->{
      unloadShop(shop);
      unregisterShop(shop, true);
      removeShopFromLookupTable(shop);
    });
  }

  @Override
  public CompletableFuture<@Nullable Integer> clearShopTags(@NotNull final UUID tagger, @NotNull final Shop shop) {

    return plugin.getDatabaseHelper().removeShopAllTag(tagger, shop.getShopId());
  }

  @Override
  public CompletableFuture<@Nullable Integer> clearTagFromShops(@NotNull final UUID tagger, @NotNull String tag) {

    tag = tag.trim().toLowerCase(Locale.ROOT);
    tag = tag.replace(" ", "_");
    return plugin.getDatabaseHelper().removeTagFromShops(tagger, tag);
  }

  @Override
  public CompletableFuture<@Nullable Integer> removeTag(@NotNull final UUID tagger, @NotNull final Shop shop, @NotNull String tag) {

    tag = tag.trim().toLowerCase(Locale.ROOT);
    tag = tag.replace(" ", "_");
    return plugin.getDatabaseHelper().removeShopTag(tagger, shop.getShopId(), tag);
  }

  @Override
  public CompletableFuture<@Nullable Integer> tagShop(@NotNull final UUID tagger, @NotNull final Shop shop, @NotNull String tag) {

    tag = tag.trim().toLowerCase(Locale.ROOT);
    tag = tag.replace(" ", "_");
    return plugin.getDatabaseHelper().tagShop(tagger, shop.getShopId(), tag);
  }

  @Override
  @NotNull
  public List<String> listTags(@NotNull final UUID tagger) {

    Util.ensureThread(true);
    return plugin.getDatabaseHelper().listTags(tagger);
  }

  @Override
  public CompletableFuture<?> unregisterShop(@NotNull final Shop shop, final boolean persist) {

    removeShopFromLookupTable(shop);
    if(!persist) return CompletableFuture.completedFuture(null);
    final Location loc = shop.getLocation();
    return plugin.getDatabaseHelper().removeShopMap(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())
            .thenCombine(plugin.getDatabaseHelper().removeShop(shop.getShopId()), (a, b)->null)
            .exceptionally(throwable->{
              plugin.logger().warn("Failed to remove shop from database", throwable);
              return null;
            });
  }


  /**
   * Returns all shops in the memory, include unloaded.
   *
   * <p>Make sure you have caching this, because this need a while to get all shops
   *
   * @return All shop in the database
   */
  @Override
  public @NotNull List<Shop> getAllShops() {

    try(final PerfMonitor ignored = new PerfMonitor("Getting all shops")) {
      final List<Shop> shopsCollected = new ArrayList<>();
      for(final Map<ShopChunk, Map<Location, Shop>> shopMapData : getShops().values()) {
        for(final Map<Location, Shop> shopData : shopMapData.values()) {
          shopsCollected.addAll(shopData.values());
        }
      }
      return shopsCollected;
    }
  }

  /**
   * Get all loaded shops.
   *
   * @return All loaded shops.
   */
  @Override
  public @NotNull Set<Shop> getLoadedShops() {

    return this.loadedShops;
  }

  /**
   * Get a players all shops.
   *
   * <p>Make sure you have caching this, because this need a while to get player's all shops
   *
   * @param playerUUID The player's uuid.
   *
   * @return The list have this player's all shops.
   */
  @Override
  public @NotNull List<Shop> getAllShops(@NotNull final QUser playerUUID) {

    final List<Shop> playerShops = new ArrayList<>(10);
    for(final Shop shop : getAllShops()) {
      if(shop.getOwner().equals(playerUUID)) {
        playerShops.add(shop);
      }
    }
    return playerShops;
  }

  @Override
  public @NotNull List<Shop> getAllShops(@NotNull final UUID playerUUID) {

    final List<Shop> playerShops = new ArrayList<>(10);
    for(final Shop shop : getAllShops()) {
      final UUID shopUuid = shop.getOwner().getUniqueIdIfRealPlayer().orElse(null);
      if(playerUUID.equals(shopUuid)) {
        playerShops.add(shop);
      }
    }
    return playerShops;
  }


  /**
   * Gets a shop by shop Id
   *
   * @param shopId shop Id
   *
   * @return The shop object
   */
  @Override
  public @Nullable Shop getShop(final long shopId) {

    for(final Shop shop : getAllShops()) {
      if(shop.getShopId() == shopId) {
        return shop;
      }
    }
    return null;
  }

  /**
   * Gets a shop in a specific location
   *
   * @param loc                  The location to get the shop from
   * @param skipShopableChecking whether to check is shopable, this will cause chunk loading
   *
   * @return The shop at that location
   */
  @Override
  public @Nullable Shop getShop(@NotNull Location loc, final boolean skipShopableChecking) {

    if(!skipShopableChecking && !Util.isShoppables(loc.getBlock().getType())) {
      return null;
    }
    final ShopChunk shopChunk = SimpleShopChunk.fromLocation(loc);
    final Map<Location, Shop> inChunk = getShops(shopChunk);
    if(inChunk == null) {
      return null;
    }
    loc = loc.clone();
    // Fix double chest XYZ issue
    loc.setX(loc.getBlockX());
    loc.setY(loc.getBlockY());
    loc.setZ(loc.getBlockZ());
    // We can do this because WorldListener updates the world reference so
    // the world in loc is the same as world in inChunk.get(loc)
    return inChunk.get(loc);
  }

  @Override
  @Nullable
  public Shop getShopFromRuntimeRandomUniqueId(@NotNull final UUID runtimeRandomUniqueId) {

    return getShopFromRuntimeRandomUniqueId(runtimeRandomUniqueId, false);
  }

  @Override
  @Nullable
  public Shop getShopFromRuntimeRandomUniqueId(
          @NotNull final UUID runtimeRandomUniqueId, final boolean includeInvalid) {

    final Shop shop = shopRuntimeUUIDCaching.getIfPresent(runtimeRandomUniqueId);
    if(shop == null) {
      for(final Shop shopWithoutCache : this.getLoadedShops()) {
        if(shopWithoutCache.getRuntimeRandomUniqueId().equals(runtimeRandomUniqueId)) {
          return shopWithoutCache;
        }
      }
      return null;
    }
    if(includeInvalid) {
      return shop;
    }
    if(shop.isValid()) {
      return shop;
    }
    return null;
  }

  @Nullable
  public Shop findShopIncludeAttached(@NotNull final Location loc, final boolean fromAttach) {

    Shop shop = getShop(loc);

    // failed, get attached shop
    if(shop == null) {

      final Block block = loc.getBlock();
      if(!Util.isShoppables(block.getType())) {
        return null;
      }
      final Block currentBlock = loc.getBlock();
      if(!fromAttach) {
        // sign
        if(Util.isWallSign(currentBlock.getType())) {
          final Block attached = Util.getAttached(currentBlock);
          if(attached != null) {
            shop = this.findShopIncludeAttached(attached.getLocation(), true);
          }
        } else {
          // optimize for performance
          final BlockState state = PaperLib.getBlockState(currentBlock, false).getState();
          if(!(state instanceof InventoryHolder)) {
            return null;
          }
          @Nullable final Block half = Util.getSecondHalf(currentBlock);
          if(half != null) {
            shop = getShop(half.getLocation());
          }
        }
      }
    }
    return shop;
  }

  /**
   * Gets a shop in a specific location Include the attached shop, e.g DoubleChest shop.
   *
   * @param loc The location to get the shop from
   *
   * @return The shop at that location
   */
  @Override
  public @Nullable Shop getShopIncludeAttached(@Nullable final Location loc) {

    if(loc == null) {
      Log.debug("Location is null.");
      return null;
    }
    return findShopIncludeAttached(loc, false);
  }

  @Override
  public CompletableFuture<?> registerShop(@NotNull final Shop shop, final boolean persist) {
    // save to database
    addShopToLookupTable(shop);
    if(!persist) return CompletableFuture.completedFuture(null);
    return plugin.getDatabaseHelper().createData(shop).thenCompose(plugin.getDatabaseHelper()::createShop)
            .thenAccept(id->{
              Log.debug("DEBUG: Setting shop id");
              shop.setShopId(id);
              Log.debug("DEBUG: Creating shop map");
              plugin.getDatabaseHelper().createShopMap(id, shop.getLocation()).join();
              Log.debug("DEBUG: Creating shop successfully");
              shop.setDirty();
              new ShopCreateSuccessEvent(shop, shop.getOwner()).callEvent();
            })
            .exceptionally(err->{
              processCreationFail(shop, shop.getOwner(), err);
              return null;
            });
  }


  /**
   * Returns a map of World - Chunk - Shop
   *
   * @return a map of World - Chunk - Shop
   */
  @Override
  public @NotNull Map<String, Map<ShopChunk, Map<Location, Shop>>> getShops() {

    return this.shops;
  }

  /**
   * Returns a map of Shops
   *
   * @param c The chunk to search. Referencing doesn't matter, only coordinates and world are used.
   *
   * @return Shops
   */
  @Override
  public @Nullable Map<Location, Shop> getShops(@NotNull final Chunk c) {

    return getShops(c.getWorld().getName(), c.getX(), c.getZ());
  }

  @Override
  public @Nullable Map<Location, Shop> getShops(@NotNull final String world, final int chunkX, final int chunkZ) {

    final Map<ShopChunk, Map<Location, Shop>> inWorld = this.getShops(world);
    if(inWorld == null) {

      return null;
    }
    return inWorld.get(new SimpleShopChunk(world, chunkX, chunkZ));
  }

  @Override
  public @Nullable Map<Location, Shop> getShops(@NotNull final ShopChunk shopChunk) {

    return getShops(shopChunk.getWorld(), shopChunk.getX(), shopChunk.getZ());
  }

  /**
   * Returns a map of Chunk - Shop
   *
   * @param world The name of the world (case sensitive) to get the list of shops from
   *
   * @return a map of Chunk - Shop
   */
  @Override
  public @Nullable Map<ShopChunk, Map<Location, Shop>> getShops(@NotNull final String world) {

    return this.shops.get(world);
  }

  /**
   * Get the all shops in the world.
   *
   * @param world The world you want get the shops.
   *
   * @return The list have this world all shops
   */
  @Override
  public @NotNull List<Shop> getShopsInWorld(@NotNull final World world) {

    final List<Shop> worldShops = new ArrayList<>();
    for(final Shop shop : getAllShops()) {
      final Location location = shop.getLocation();
      if(location.isWorldLoaded() && Objects.equals(location.getWorld(), world)) {
        worldShops.add(shop);
      }
    }
    return worldShops;
  }

  @Override
  public @NotNull List<Shop> getShopsInWorld(@NotNull final String worldName) {

    final List<Shop> worldShops = new ArrayList<>();
    for(final Shop shop : getAllShops()) {
      final Location location = shop.getLocation();
      if(location.isWorldLoaded() && StringUtils.equals(worldName, location.getWorld().getName())) {
        worldShops.add(shop);
      }
    }
    return worldShops;
  }


  @Override
  @NotNull
  public CompletableFuture<@NotNull ShopInventoryCountCache> queryShopInventoryCacheInDatabase(@NotNull final Shop shop) {

    Util.ensureThread(true);
    return plugin.getDatabaseHelper().queryInventoryCache(shop.getShopId());
  }


  static class TagParser {

    private final List<String> tags;
    private final ShopManager shopManager;
    private final UUID tagger;
    private final Map<String, List<Shop>> singleCaching = new HashMap<>();

    public TagParser(final UUID tagger, final ShopManager shopManager, final List<String> tags) {

      Util.ensureThread(true);
      this.shopManager = shopManager;
      this.tags = tags;
      this.tagger = tagger;
    }

    public List<Shop> parseTags() {

      final List<Shop> finalShop = new ArrayList<>();
      for(final String tag : tags) {
        final ParseResult result = parseSingleTag(tag);
        if(result.getBehavior() == Behavior.INCLUDE) {
          finalShop.addAll(result.getShops());
        } else if(result.getBehavior() == Behavior.EXCLUDE) {
          finalShop.removeAll(result.getShops());
        }
      }
      return finalShop;
    }

    public ParseResult parseSingleTag(final String tag) throws IllegalArgumentException {

      Util.ensureThread(true);
      Behavior behavior = Behavior.INCLUDE;
      if(tag.startsWith("-")) {
        behavior = Behavior.EXCLUDE;
      }
      final String tagName = tag.substring(1);
      if(tagName.isEmpty()) {
        throw new IllegalArgumentException("Tag name can't be empty");
      }
      final List<Shop> shops = singleCaching.computeIfAbsent(tag, (t)->shopManager.queryTaggedShops(tagger, t).join());
      return new ParseResult(behavior, shops);
    }

    enum Behavior {
      INCLUDE,
      EXCLUDE
    }

    @AllArgsConstructor
    @Data
    static class ParseResult {

      private final Behavior behavior;
      private final List<Shop> shops;
    }
  }

}
