package com.ghostchu.quickshop.shop;


import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.ServiceInjector;
import com.ghostchu.quickshop.api.economy.EconomyProvider;
import com.ghostchu.quickshop.api.economy.benefit.BenefitProvider;
import com.ghostchu.quickshop.api.event.Phase;
import com.ghostchu.quickshop.api.event.general.ShopSignUpdateEvent;
import com.ghostchu.quickshop.api.event.inventory.ShopInventoryCalculateEvent;
import com.ghostchu.quickshop.api.event.inventory.ShopInventoryChangedEvent;
import com.ghostchu.quickshop.api.event.management.ShopClickEvent;
import com.ghostchu.quickshop.api.event.management.ShopDatabaseEvent;
import com.ghostchu.quickshop.api.event.management.ShopLoadEvent;
import com.ghostchu.quickshop.api.event.management.ShopPermissionCheckEvent;
import com.ghostchu.quickshop.api.event.management.ShopUnloadEvent;
import com.ghostchu.quickshop.api.event.settings.type.ShopCurrencyEvent;
import com.ghostchu.quickshop.api.event.settings.type.ShopDisplayEvent;
import com.ghostchu.quickshop.api.event.settings.type.ShopItemEvent;
import com.ghostchu.quickshop.api.event.settings.type.ShopOwnerNameEvent;
import com.ghostchu.quickshop.api.event.settings.type.ShopPlayerGroupEvent;
import com.ghostchu.quickshop.api.event.settings.type.ShopSignLinesEvent;
import com.ghostchu.quickshop.api.event.settings.type.ShopStateEvent;
import com.ghostchu.quickshop.api.event.settings.type.ShopTaxAccountEvent;
import com.ghostchu.quickshop.api.event.settings.type.ShopTypeEnhancedEvent;
import com.ghostchu.quickshop.api.event.settings.type.benefit.ShopBenefitEvent;
import com.ghostchu.quickshop.api.inventory.InventoryWrapper;
import com.ghostchu.quickshop.api.inventory.InventoryWrapperManager;
import com.ghostchu.quickshop.api.localization.text.ProxiedLocale;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.serialize.BlockPos;
import com.ghostchu.quickshop.api.shop.IShopType;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopInfoStorage;
import com.ghostchu.quickshop.api.shop.display.DisplayType;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermissionGroup;
import com.ghostchu.quickshop.api.shop.state.ShopState;
import com.ghostchu.quickshop.api.shop.trading.TradeResult;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.common.util.JsonUtil;
import com.ghostchu.quickshop.database.bean.SimpleDataRecord;
import com.ghostchu.quickshop.obj.QUserImpl;
import com.ghostchu.quickshop.shop.datatype.ShopSignPersistentDataType;
import com.ghostchu.quickshop.shop.display.AbstractDisplayItem;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.quickshop.util.logging.container.ShopRemoveLog;
import com.ghostchu.quickshop.util.performance.PerfMonitor;
import com.ghostchu.simplereloadlib.ReloadResult;
import com.ghostchu.simplereloadlib.Reloadable;
import lombok.EqualsAndHashCode;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.ghostchu.quickshop.util.Util.waitForFuture;
import static java.math.BigDecimal.ZERO;

/**
 * ChestShop core
 */
@EqualsAndHashCode
public class ContainerShop implements Shop<Double, Location>, Reloadable {
  @EqualsAndHashCode.Exclude
  private final QuickShop plugin;


  /**
   * Adds a new shop. You need call ShopManager#loadShop if you create from outside of ShopLoader.
   *
   * @param location  The location of the chest block
   * @param price     The cost per item
   * @param item      The itemstack with the properties we want. This is .cloned, no need to worry
   *                  about references
   * @param owner     The shop owner
   * @param type      The shop type
   * @param unlimited The unlimited
   * @param plugin    The plugin instance
   * @param extra     The extra data saved by addon
   */
  public ContainerShop(
          @NotNull final QuickShop plugin,
          final long shopId,
          @NotNull final Location location,
          final double price,
          @NotNull final ItemStack item,
          @NotNull final QUser owner,
          final boolean unlimited,
          @NotNull final IShopType type,
          @NotNull final ShopState state,
          @Nullable final YamlConfiguration extra,
          @Nullable final String currency,
          final boolean disableDisplay,
          @Nullable final QUser taxAccount,
          @NotNull final String inventoryWrapperProvider,
          @NotNull final String symbolLink,
          @Nullable final String shopName,
          @NotNull final Map<UUID, String> playerGroup,
          @NotNull final BenefitProvider shopBenefit) {

    this.shopId = shopId;
    this.shopName = shopName;
    this.location = location;
    this.price = price;
    this.benefit = shopBenefit;


    // Upgrade the shop moderator
    this.owner = owner;
    if(item == null) {

      throw new IllegalArgumentException("Loaded item is null. This is usually from an invalid shop.");
    }

    this.item = item.clone();
    this.originalItem = item.clone();
    this.plugin = plugin;
    this.playerGroup = new HashMap<>(playerGroup);
    if(!plugin.isAllowStack()) {
      this.item.setAmount(1);
    }
    if(item.hasItemMeta()) {
      final ItemMeta meta = item.getItemMeta();
      if(meta.hasDisplayName() && meta.getDisplayName().matches("\\{.*}")) {
        //https://hub.spigotmc.org/jira/browse/SPIGOT-5964
        meta.setDisplayName(meta.getDisplayName());
        //Correct both items
        item.setItemMeta(meta);
        this.item.setItemMeta(meta);
      }
    }
    this.shopType = type;
    this.shopState = state;
    this.unlimited = unlimited;
    this.extra = extra;
    this.currency = currency;
    this.disableDisplay = disableDisplay;
    this.taxAccount = taxAccount;
    this.dirty = false;
    if(symbolLink == null) {
      throw new IllegalArgumentException("SymbolLink cannot be null");
    }
    if(inventoryWrapperProvider == null) {
      throw new IllegalArgumentException("InventoryWrapperProvider cannot be null");
    }
    this.symbolLink = symbolLink;
    this.inventoryWrapperProvider = inventoryWrapperProvider;
    updateShopData();
    // ContainerShop constructor is not allowed to write any persistent data to disk
    // ContainerShop constructor may run on both ServerThread and AsyncThread
  }

  private void updateShopData() {

    final ConfigurationSection section = getExtra(plugin.getJavaPlugin());
    if(section.getString("currency") != null) {
      this.currency = section.getString("currency");
      section.set("currency", null);
      Log.debug("Shop " + this + " currency data upgrade successful.");
      setDirty();
    }

  }

  /**
   * Add an item to shops chest.
   *
   * @param item   The itemstack. The amount does not matter, just everything else
   * @param amount The amount to add to the shop.
   */
  @Override
  public void add(@NotNull ItemStack item, final int amount) {

    Util.ensureThread(false);
    if(this.unlimited) {
      return;
    }
    item = item.clone();
    final int itemMaxStackSize = Util.getItemMaxStackSize(item.getType());
    final InventoryWrapper inv = this.getInventory();
    if(inv == null) {
      throw new IllegalArgumentException("Failed to add item to shop " + this + ", the inventory is null!");
    }
    int remains = amount;
    while(remains > 0) {
      final int stackSize = Math.min(remains, itemMaxStackSize);
      item.setAmount(stackSize);
      Objects.requireNonNull(inv).addItem(item);
      remains -= stackSize;
    }
    this.setSignText();
  }

  @Override
  public void checkDisplay() {

    Util.ensureThread(false);
    final boolean displayStatus = plugin.isDisplayEnabled() && !isDisableDisplay() && this.isLoaded() && !this.isDeleted();

    if(!displayStatus) {
      if(this.displayItem != null) {
        this.displayItem.remove(false);
      }
      return;
    }
    if(this.displayItem == null) {
      try {
        final DisplayProvider provider = ServiceInjector.getInjectedService(DisplayProvider.class, null);
        if(provider == null && AbstractDisplayItem.getNowUsing() == DisplayType.VIRTUALITEM && plugin.getVirtualDisplayItemManager() == null) {
          plugin.logger().warn("Invalid display provider! " +
                               "No compatible display backend found. " +
                               "This may occur if ProtocolLib or PacketEvents is missing, outdated, or incompatible with your Minecraft version, " +
                               "or if this QuickShop-Hikari build does not yet support the current server version. " +
                               "Shops will function normally, but displays above containers are disabled.");
          return;
        }

        if(provider != null) {
          this.displayItem = provider.provide(this);
        } else {

          if(AbstractDisplayItem.getNowUsing() == DisplayType.VIRTUALITEM) {

            if(plugin.getVirtualDisplayItemManager() != null) {
              this.displayItem = plugin.getVirtualDisplayItemManager().createVirtualDisplayItem(this);
            }
          }
        }

        if(this.displayItem == null) {
          plugin.logger().warn("Invalid display provider! " +
                               "No compatible display backend found. " +
                               "This may occur if ProtocolLib or PacketEvents is missing, outdated, or incompatible with your Minecraft version, " +
                               "or if this QuickShop-Hikari build does not yet support the current server version. " +
                               "Shops will function normally, but displays above containers are disabled.");
          return;
        }
      } catch(final Throwable anyError) {
        plugin.logger().warn("Failed to init the displayItem for shop {}, the display now disabled for this shop. Do you have ProtocolLib or packetevents installed?", this, anyError);
        return;
      }
    }
    if(this.displayItem != null) {
      if(!this.displayItem.isSpawned()) {
        /* Not spawned yet. */
        this.displayItem.spawn();
      } else {
        /* If not spawned, we didn't need check these, only check them when we need. */
        if(this.displayItem.checkDisplayNeedRegen()) {
          this.displayItem.fixDisplayNeedRegen();
        } else {
          /* If display was regened, we didn't need check it moved, performance! */
          if(this.displayItem.checkDisplayIsMoved()) {
            this.displayItem.fixDisplayMoved();
          }
        }
      }
      /* Dupe is always need check, if enabled display */
      this.displayItem.removeDupe();
    }
  }

  /**
   * @return The chest this shop is based on.
   */
  @Override
  public @Nullable InventoryWrapper getInventory() {

    Util.ensureThread(false);
    try {
      final InventoryWrapper inventoryWrapper = locateInventory(symbolLink);
      if(inventoryWrapper.isValid()) {
        return inventoryWrapper;
      }
    } catch(final Exception e) {
      Log.debug("Cannot locate the Inventory with symbol link: " + symbolLink + ", provider: " + inventoryWrapperProvider);
      return null;
    }
    if(!createBackup) {
      createBackup = false;
      if(createBackup) {
        plugin.getShopManager().deleteShop(this);
      }
    } else {
      plugin.getShopManager().unregisterShop(this, false);
    }
    plugin.logEvent(new ShopRemoveLog(QUserImpl.createFullFilled(CommonUtil.getNilUniqueId(), "SYSTEM", false), "Inventory Invalid", this.saveToInfoStorage()));
    Log.debug("Inventory doesn't exist anymore: " + this + " shop was deleted.");
    return null;
  }

  /**
   * Returns the number of free spots in the chest for the particular item.
   *
   * @return remaining space
   */
  @Override
  public int getRemainingSpace() {

    if(this.unlimited) {

      return -1;
    }

    if(Bukkit.isPrimaryThread()) {

      if(this.getInventory() == null) {
        Log.debug("Failed to calc RemainingSpace for shop " + this + ": Inventory null.");
        return 0;
      }

      final int space = Util.countSpace(this.getInventory(), this);
      new ShopInventoryCalculateEvent(this, space, -1).callEvent();
      Log.debug("Space count is: " + space);
      return space;
    } else {

      return plugin.getShopManager().queryShopInventoryCacheInDatabase(this).join().getSpace();
    }
  }

  /**
   * Returns the number of items this shop has in stock.
   *
   * @return The number of items available for purchase.
   */
  @Override
  public int getRemainingStock() {

    if(this.unlimited) {
      return -1;
    }

    if(Bukkit.getServer().isOwnedByCurrentRegion(location)) {

      if(this.getInventory() == null) {
        return 0;
      }
      final int stock = Util.countItems(this.getInventory(), this);
      new ShopInventoryCalculateEvent(this, -1, stock).callEvent();
      return stock;
    }

    final CompletableFuture<Integer> future = new CompletableFuture<>();

    QuickShop.folia()
      .getScheduler()
      .runAtLocation(
        this.location,
        task->{
          if(this.getInventory() == null) {
            future.complete(0);
            return;
          }

          final int stock = Util.countItems(this.getInventory(), this);
          new ShopInventoryCalculateEvent(this, -1, stock).callEvent();

          future.complete(stock);
        });

    return future.join();
  }

  @Override
  public boolean inventoryAvailable() {

    if(isUnlimited()) {
      return true;
    }
    if(isSelling()) {
      return getRemainingStock() > 0;
    }
    if(isBuying()) {
      return getRemainingSpace() > 0;
    }
    if(isFrozen()) {
      return false;
    }
    return true;
  }

  /**
   * Removes an item from the shop.
   *
   * @param item   The itemstack. The amount does not matter, just everything else
   * @param amount The amount to remove from the shop.
   */
  @Override
  public void remove(@NotNull ItemStack item, final int amount) {

    Util.ensureThread(false);
    if(this.unlimited) {
      return;
    }
    item = item.clone();
    final int itemMaxStackSize = Util.getItemMaxStackSize(item.getType());
    final InventoryWrapper inv = this.getInventory();
    if(inv == null) {
      plugin.logger().warn("Failed to process item remove, reason: {} x{} to shop {}: Inventory null.", item, amount, this);
      return;
    }
    int remains = amount;
    while(remains > 0) {
      final int stackSize = Math.min(remains, itemMaxStackSize);
      item.setAmount(stackSize);
      Objects.requireNonNull(inv).removeItem(item);
      remains -= stackSize;
    }
    this.setSignText();
  }

  private @NotNull InventoryWrapper locateInventory(@Nullable final String symbolLink) {

    if(symbolLink == null || symbolLink.isEmpty()) {
      throw new IllegalStateException("Symbol link is empty, that's not right bro.");
    }
    final InventoryWrapperManager manager = plugin.getInventoryWrapperRegistry().get(getInventoryWrapperProvider());
    if(manager == null) {
      throw new IllegalStateException("Failed load shop data, the InventoryWrapper provider " + getInventoryWrapperProvider() + " invalid or failed to load!");
    }
    try {
      // this.symbolLink = manager.mklink(inventoryWrapper);
      return manager.locate(symbolLink);
    } catch(final Exception e) {
      throw new IllegalStateException("Failed load shop data, the InventoryWrapper provider " + getInventoryWrapperProvider() + " returns error: " + e.getMessage(), e);
    }
  }
}
