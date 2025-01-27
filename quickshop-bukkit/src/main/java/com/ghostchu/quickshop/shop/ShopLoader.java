package com.ghostchu.quickshop.shop;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.database.bean.DataRecord;
import com.ghostchu.quickshop.api.database.bean.InfoRecord;
import com.ghostchu.quickshop.api.database.bean.ShopRecord;
import com.ghostchu.quickshop.api.economy.Benefit;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopType;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.common.util.JsonUtil;
import com.ghostchu.quickshop.common.util.Timer;
import com.ghostchu.quickshop.economy.SimpleBenefit;
import com.ghostchu.quickshop.util.PackageUtil;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.quickshop.util.paste.item.SubPasteItem;
import com.google.common.reflect.TypeToken;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A class allow plugin load shops fast and simply.
 */
public class ShopLoader implements SubPasteItem {

  private final QuickShop plugin;
  private final ExecutorService executorService;
  /* This may contains broken shop, must use null check before load it. */
  private int errors;

  /**
   * The shop load allow plugin load shops fast and simply.
   *
   * @param plugin Plugin main class
   */
  public ShopLoader(@NotNull final QuickShop plugin) {

    this.plugin = plugin;
    this.executorService = Executors.newWorkStealingPool(PackageUtil
                                                                 .parsePackageProperly("parallelism")
                                                                 .asInteger(CommonUtil.multiProcessorThreadRecommended()));
  }

  public void loadShops() {

    loadShops(null);
  }

  /**
   * Load all shops in the specified world
   *
   * @param worldName The world name, null if load all shops
   */
  public void loadShops(@Nullable final String worldName) {

    if(worldName != null) {
      if(Bukkit.getWorld(worldName) == null) {
        plugin.logger().warn("World {} not exists, skip loading shops in this world.", worldName);
        return;
      }
      if(plugin.getConfig().getStringList("database-loading-blacklist-worlds").contains(worldName)) {
        return;
      }
    }
    final boolean deleteCorruptShops = plugin.getConfig().getBoolean("debug.delete-corrupt-shops", false);
    plugin.logger().info("Loading shops from database...");
    final Timer dbFetchTimer = new Timer(true);
    final List<ShopRecord> records = plugin.getDatabaseHelper().listShops(worldName, deleteCorruptShops);
    plugin.logger().info("Used {}ms to fetch {} shops from database.", dbFetchTimer.stopAndGetTimePassed(), records.size());
    plugin.logger().info("Loading shops into memory...");
    final Timer shopTotalTimer = new Timer(true);
    final AtomicInteger successCounter = new AtomicInteger(0);
    final AtomicInteger chunkNotLoaded = new AtomicInteger(0);
    final List<Shop> shopsLoadInNextTick = new CopyOnWriteArrayList<>();
    for(final ShopRecord record : records) {
      loadShopFromShopRecord(worldName, record, deleteCorruptShops,
                             shopsLoadInNextTick, successCounter, chunkNotLoaded)
              .exceptionally(e->{
                plugin.logger().warn("Failed to load shop {}", record, e);
                return null;
              }).join();
    }
    Util.mainThreadRun(()->shopsLoadInNextTick.forEach(shop->{
      try {
        plugin.getShopManager().loadShop(shop);
      } catch(final Throwable e) {
        plugin.logger().error("Failed to load shop {}.", shop.getShopId(), e);
      }
    }));
    plugin.logger().info("Used {}ms to load {} shops into memory ({} shops will be loaded after chunks/world loaded).", shopTotalTimer.stopAndGetTimePassed(), successCounter.get(), chunkNotLoaded.get());
  }

  private CompletableFuture<Void> loadShopFromShopRecord(final String worldName, final ShopRecord shopRecord, final boolean deleteCorruptShops, final List<Shop> shopsLoadInNextTick, final AtomicInteger successCounter, final AtomicInteger chunkNotLoaded) {

    return CompletableFuture.supplyAsync(()->{
      final InfoRecord infoRecord = shopRecord.getInfoRecord();
      final DataRecord dataRecord = shopRecord.getDataRecord();
      final Timer singleShopLoadingTimer = new Timer(true);
      final ShopLoadResult result = loadSingleShop(infoRecord, dataRecord, worldName, shopsLoadInNextTick);
      switch(result) {
        case LOADED -> successCounter.incrementAndGet();
        case LOAD_AFTER_CHUNK_LOADED -> chunkNotLoaded.incrementAndGet();
        case WORLD_NOT_MATCH_SKIPPED -> {
          // Do nothing
        }
        case FAILED -> {
          if(deleteCorruptShops) {
            plugin.getDatabaseHelper().removeShopMap(infoRecord.getWorld(), infoRecord.getX(), infoRecord.getY(), infoRecord.getZ());
            plugin.logger().warn("Shop {} is corrupted, removed from database.", infoRecord.getShopId());
          }
        }
      }
      Log.timing("Shop loading completed: " + result.name(), singleShopLoadingTimer);
      return null;
    }, this.executorService);
  }


  private ShopLoadResult loadSingleShop(final InfoRecord infoRecord, final DataRecord dataRecord, @Nullable final String worldName, @NotNull final List<Shop> shopsLoadInNextTick) {
    // World check
    if(worldName != null) {
      if(!worldName.equals(infoRecord.getWorld())) {
        return ShopLoadResult.WORLD_NOT_MATCH_SKIPPED;
      }
      if(plugin.getConfig().getStringList("database-loading-blacklist-worlds").contains(worldName)) {
        return ShopLoadResult.WORLD_NOT_MATCH_SKIPPED;
      }
    }
    if(Bukkit.getWorld(infoRecord.getWorld()) == null) {
      return ShopLoadResult.LOAD_AFTER_CHUNK_LOADED;
    }
    // Shop basic check
    if(dataRecord.getInventoryWrapper() == null) {
      return ShopLoadResult.FAILED;
    }
    if(dataRecord.getInventorySymbolLink() != null
       && !dataRecord.getInventoryWrapper().isEmpty()
       && plugin.getInventoryWrapperRegistry().get(dataRecord.getInventoryWrapper()) == null) {
      Log.debug("InventoryWrapperProvider not exists! Shop won't be loaded!");
      return ShopLoadResult.FAILED;
    }

    final int x = infoRecord.getX();
    final int y = infoRecord.getY();
    final int z = infoRecord.getZ();
    final Shop shop;
    final DataRawDatabaseInfo rawInfo = new DataRawDatabaseInfo(dataRecord);
    final Location location = new Location(Bukkit.getWorld(infoRecord.getWorld()), x, y, z);

    final ItemStack stack = (rawInfo.getNewItem() == null)? rawInfo.getItem() : rawInfo.getNewItem();
    try {
      shop = new ContainerShop(plugin,
                               infoRecord.getShopId(),
                               location,
                               rawInfo.getPrice(),
                               stack,
                               rawInfo.getOwner(),
                               rawInfo.isUnlimited(),
                               rawInfo.getType(),
                               rawInfo.getExtra(),
                               rawInfo.getCurrency(),
                               rawInfo.isHologram(),
                               rawInfo.getTaxAccount(),
                               rawInfo.getInvWrapper(),
                               rawInfo.getInvSymbolLink(),
                               rawInfo.getName(),
                               rawInfo.getPermissions(),
                               rawInfo.getBenefits());
    } catch(final Exception e) {
      if(e instanceof IllegalStateException) {
        plugin.logger().warn("Failed to load the shop, skipping...", e);
      }
      exceptionHandler(e, location);
      return ShopLoadResult.FAILED;
    }
    // Dirty check
    if(rawInfo.isNeedUpdate()) {
      shop.setDirty();
    }
    // Null check
    if(shopNullCheck(shop)) {
      return ShopLoadResult.FAILED;
    }
    // Load to RAM
    plugin.getShopManager().registerShop(shop, false); // persist=false to load to memory (it already persisted)
    if(Util.isLoaded(location)) {
      // Load to World
      //plugin.getShopManager().loadShop(shop); // Patch the shops won't load around the spawn
      shopsLoadInNextTick.add(shop);
    } else {
      return ShopLoadResult.LOAD_AFTER_CHUNK_LOADED;
    }
    return ShopLoadResult.LOADED;
  }

  private void exceptionHandler(@NotNull final Exception ex, @Nullable final Location shopLocation) {

    errors++;
    @NotNull final Logger logger = plugin.logger();
    logger.warn("##########FAILED TO LOAD SHOP##########");
    logger.warn("  >> Error Info:");
    logger.warn(ex.getMessage());
    logger.warn("  >> Error Trace");
    logger.warn("Stacktrace: ", ex);
    logger.warn("  >> Target Location Info");
    logger.warn("Location: {}", shopLocation);
    String blockType = "N/A";
    if(shopLocation != null) {
      if(Util.isLoaded(shopLocation)) {
        blockType = shopLocation.getBlock().getType().name();
      } else {
        blockType = "Not loaded yet";
      }
    } else {
      logger.warn("Block: {}", "Location is null");
    }
    logger.warn("Block: {}", blockType);
    logger.warn("#######################################");
    if(errors > 10) {
      logger.error(
              "QuickShop detected too many errors when loading shops, you should backup your shop database and ask the developer for help");
    }
  }

  private boolean shopNullCheck(@Nullable final Shop shop) {

    if(shop == null) {
      Log.debug("Shop object is null");
      return true;
    }
    if(shop.getItem() == null) {
      Log.debug("Shop itemStack is null");
      return true;
    }
    if(shop.getItem().getType() == Material.AIR) {
      Log.debug("Shop itemStack type can't be AIR");
      return true;
    }
    if(shop.getItem().getAmount() <= 0) {
      Log.debug("Shop itemStack amount can't be 0");
      return true;
    }
    if(shop.getLocation() == null) {
      Log.debug("Shop location is null");
      return true;
    }
    if(shop.getOwner() == null) {
      Log.debug("Shop owner is null");
      return true;
    }
    return false;
  }

  @Override
  public @NotNull String genBody() {

    return "<p>Errors: " + errors + "</p>";
  }

  @Override
  public @NotNull String getTitle() {

    return "Shop Loader";
  }

  public enum ShopLoadResult {
    LOADED,
    LOAD_AFTER_CHUNK_LOADED,
    WORLD_NOT_MATCH_SKIPPED,
    FAILED
  }

  @Getter
  @Setter
  public static class DataRawDatabaseInfo {

    private QUser owner;
    private String name;
    private ShopType type;
    private String currency;
    private double price;
    private boolean unlimited;
    private boolean hologram;
    private QUser taxAccount;
    private Map<UUID, String> permissions;
    private YamlConfiguration extra;
    private String invWrapper;
    private String invSymbolLink;
    private long createTime;
    private ItemStack item;
    private ItemStack newItem;
    private boolean needUpdate = false;

    private Benefit benefits;


    DataRawDatabaseInfo(@NotNull final DataRecord dataRecord) {

      this.owner = dataRecord.getOwner();
      this.price = dataRecord.getPrice();
      this.type = ShopType.fromID(dataRecord.getType());
      this.unlimited = dataRecord.isUnlimited();
      final String extraStr = dataRecord.getExtra();
      this.name = dataRecord.getName();
      //handle old shops
      this.currency = dataRecord.getCurrency();
      this.hologram = dataRecord.isHologram();
      this.taxAccount = null;
      if(dataRecord.getTaxAccount() != null) {
        this.taxAccount = getTaxAccount();
      }
      this.invSymbolLink = dataRecord.getInventorySymbolLink();
      this.invWrapper = dataRecord.getInventoryWrapper();
      this.benefits = SimpleBenefit.deserialize(dataRecord.getBenefit());
      final String permissionJson = dataRecord.getPermissions();

      if(!StringUtils.isEmpty(permissionJson) && CommonUtil.isJson(permissionJson)) {
        final Type typeToken = new TypeToken<Map<UUID, String>>() {
        }.getType();
        this.permissions = new HashMap<>(JsonUtil.getGson().fromJson(permissionJson, typeToken));
      } else {
        this.permissions = new HashMap<>();
      }

      this.item = deserializeItem(dataRecord.getItem());

      if(!dataRecord.getEncoded().isEmpty()) {
        this.newItem = QuickShop.getInstance().getPlatform().decodeStack(dataRecord.getEncoded());
      }
      this.extra = deserializeExtra(extraStr);
    }

    private @Nullable ItemStack deserializeItem(@NotNull final String itemConfig) {

      try {
        return Util.deserialize(itemConfig);
      } catch(final Exception e) {
        QuickShop.getInstance().logger().warn("Failed load shop data, because target config can't deserialize the ItemStack", e);
        Log.debug("Failed to load data to the ItemStack: " + itemConfig);
        return null;
      }
    }

    private @Nullable YamlConfiguration deserializeExtra(@NotNull final String extraString) {

      if(StringUtils.isEmpty(extraString)) {
        return null;
      }
      YamlConfiguration yamlConfiguration = new YamlConfiguration();
      try {
        yamlConfiguration.loadFromString(extraString);
      } catch(final InvalidConfigurationException e) {
        yamlConfiguration = new YamlConfiguration();
        needUpdate = true;
      }
      return yamlConfiguration;
    }


    @Override
    public String toString() {

      return JsonUtil.getGson().toJson(this);
    }
  }

  @Getter
  @Setter
  public static class ShopDatabaseInfo {

    private int shopId;
    private int dataId;

    ShopDatabaseInfo(final ResultSet origin) {

      try {
        this.shopId = origin.getInt("id");
        this.dataId = origin.getInt("data");
      } catch(final Exception ex) {
        ex.printStackTrace();
      }
    }
  }

  @Getter
  @Setter
  public static class ShopMappingInfo {

    private int shopId;
    private String world;
    private int x;
    private int y;
    private int z;

    ShopMappingInfo(final ResultSet origin) {

      try {
        this.shopId = origin.getInt("shop");
        this.x = origin.getInt("x");
        this.y = origin.getInt("y");
        this.z = origin.getInt("z");
        this.world = origin.getString("world");
      } catch(final Exception ex) {
        ex.printStackTrace();
      }
    }
  }

}
