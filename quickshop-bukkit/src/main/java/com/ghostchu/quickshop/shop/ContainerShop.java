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
import com.ghostchu.quickshop.shop.display.display.DisplayEntityItemManager;
import com.ghostchu.quickshop.shop.display.virtual.VirtualDisplayItemManager;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.quickshop.util.logging.container.ShopRemoveLog;
import com.ghostchu.quickshop.util.performance.PerfMonitor;
import com.ghostchu.simplereloadlib.ReloadResult;
import com.ghostchu.simplereloadlib.Reloadable;
import lombok.EqualsAndHashCode;
import net.kyori.adventure.key.Key;
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
import org.bukkit.block.TileState;
import org.bukkit.block.sign.Side;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus;
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
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.ghostchu.quickshop.shop.SimpleShopManager.CHEST_SHOP_OWNER;
import static com.ghostchu.quickshop.util.Util.waitForFuture;
import static java.math.BigDecimal.ZERO;

/**
 * ChestShop core
 */
@EqualsAndHashCode
public class ContainerShop implements Shop<Double, Location>, Reloadable {

  // We use deprecated method to create a fake quickshop-reremake namespace to trick bukkit to access legacy data.
  private static final NamespacedKey LEGACY_SHOP_NAMESPACED_KEY = new NamespacedKey("quickshop", "shopsign");
  private static final String LEGACY_SHOP_SIGN_RECOGNIZE_PATTERN = "§d§o ";

  private final Map<Key, String> extraMap = new ConcurrentHashMap<>();

  @NotNull
  private final Location location;
  @EqualsAndHashCode.Exclude
  private final QuickShop plugin;
  @EqualsAndHashCode.Exclude
  private final UUID runtimeRandomUniqueId = UUID.randomUUID();
  @NotNull
  private final Map<UUID, String> playerGroup;
  @EqualsAndHashCode.Exclude
  private final boolean isDeleted = false;
  private long shopId;
  private QUser owner;
  private double price;
  private IShopType shopType;
  private ShopState shopState;
  private boolean unlimited;
  @NotNull
  private ItemStack item;
  @NotNull
  private ItemStack originalItem;
  @Nullable
  @EqualsAndHashCode.Exclude
  private AbstractDisplayItem displayItem = null;
  @EqualsAndHashCode.Exclude
  private volatile boolean isLoaded = false;
  @EqualsAndHashCode.Exclude
  private volatile boolean createBackup = false;
  @EqualsAndHashCode.Exclude
  private boolean dirty;
  @EqualsAndHashCode.Exclude
  private boolean updating = false;
  @Nullable
  private String currency;
  private boolean disableDisplay;
  private QUser taxAccount;
  @NotNull
  private String inventoryWrapperProvider;
  @NotNull
  private String symbolLink;
  @Nullable
  private String shopName;

  @NotNull
  private BenefitProvider benefit;

  //updating objects
  private final AtomicBoolean updatingAtomic = new AtomicBoolean(false);
  private volatile CompletableFuture<Void> inFlightUpdate;


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
          @Nullable final Map<Key, String> extra,
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

    if (extra != null) {

      this.extraMap.putAll(extra);
    }

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

    if (this.extraMap.containsKey("currency")) {

      this.currency = this.extraMap.get("currency");
      this.extraMap.remove("currency");
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

  /**
   * Buys amount of item from Player p. Does NOT check our inventory, or balances
   *
   * @param buyer          The player to buy from
   * @param buyerInventory The buyer's inventory
   * @param loc2Drop       The location to drop items if inventory are full
   * @param amount         The amount to buy
   */
  @Override
  public TradeResult buy(@NotNull final QUser buyer,
                  @NotNull final InventoryWrapper buyerInventory,
                  @NotNull final Location loc2Drop,
                  final int amount) {

    return plugin.getShopManager().tradeService().executeSellToShop(this, buyer, buyerInventory, loc2Drop, amount);
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
        if(provider == null && AbstractDisplayItem.getNowUsing() == DisplayType.VIRTUALITEM && plugin.getDisplayManager() == null) {
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

          if(AbstractDisplayItem.getNowUsing() == DisplayType.VIRTUALITEM && plugin.getDisplayManager() instanceof final VirtualDisplayItemManager virtualManager) {

            this.displayItem = virtualManager.create(this);
          } else if(AbstractDisplayItem.getNowUsing() == DisplayType.DISPLAY_ENTITY && plugin.getDisplayManager() instanceof final DisplayEntityItemManager displayManager) {

            this.displayItem = displayManager.create(this);
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

  @Override
  public void claimShopSign(@NotNull final Sign sign) {

    if(!sign.getPersistentDataContainer().has(Shop.SHOP_NAMESPACED_KEY, ShopSignPersistentDataType.INSTANCE)) {
      sign.getPersistentDataContainer().set(Shop.SHOP_NAMESPACED_KEY, ShopSignPersistentDataType.INSTANCE, saveToShopSignStorage());
      sign.update();
    }
  }

  /**
   * Gets the currency that shop use
   *
   * @return The currency name
   */
  @Override
  public @Nullable String getCurrency() {

    final ShopCurrencyEvent event = ShopCurrencyEvent.RETRIEVE(this, this.currency);
    event.callEvent();

    return event.updated();
  }

  /**
   * Sets the currency that shop use
   *
   * @param currency The currency name; null to use default currency
   */
  @Override
  public void setCurrency(@Nullable final String currency) {

    if(Objects.equals(this.currency, currency)) {
      return;
    }
    this.currency = currency;
    setDirty();
  }

  /**
   * @return The durability of the item
   */
  @Override
  public short getDurability() {

    return (short)((Damageable)this.item.getItemMeta()).getDamage();
  }

  /**
   * Gets the plugin's k-v map to storage the data. It is spilt by plugin name, different name have
   * different map, the data won't conflict. But if you plugin name is too common, add a prefix will
   * be a good idea.
   *
   * @param plugin Plugin instance
   *
   * @return The data table
   */
  @Deprecated(forRemoval = true)
  @ApiStatus.ScheduledForRemoval(inVersion = "6.4.0.0")
  @Override
  public @NotNull ConfigurationSection getExtra(@NotNull final Plugin plugin) {

    final YamlConfiguration yaml = new YamlConfiguration();

    if (this.extraMap.isEmpty()) {
      return yaml;
    }

    final String prefix = plugin.getName().toLowerCase(Locale.ROOT) + ":";

    this.extraMap.forEach((key, value) -> {
      if (key.asString().startsWith(prefix)) {
        yaml.set(key.asString().substring(prefix.length()), value);
      }
    });

    return yaml;
  }

  /**
   * @return The chest this shop is based on.
   */
  @Override
  public @Nullable InventoryWrapper getInventory() {

    Util.ensureThread(false);
    final int chunkX = location.getBlockX() >> 4;
    final int chunkZ = location.getBlockZ() >> 4;

    if(!this.location.getWorld().isChunkLoaded(chunkX, chunkZ)) {
      return null;
    }

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

  @Override
  public @NotNull String getInventoryWrapperProvider() {

    return inventoryWrapperProvider;
  }

  /**
   * @return Returns a dummy itemstack of the item this shop is selling.
   */
  @Override
  public @NotNull ItemStack getItem() {

    final ShopItemEvent event = new ShopItemEvent(Phase.RETRIEVE, this, this.item.clone());

    return event.updated();
  }

  @Override
  public void setItem(@NotNull final ItemStack item) {

    Util.ensureThread(false);

    //Create our shop event with Pre Phase and call
    ShopItemEvent event = new ShopItemEvent(Phase.PRE, this, this.item, item);
    event.callEvent();

    //Call our Main Phase
    event = event.clone(Phase.MAIN);
    if(event.callCancellableEvent()) {

      Log.debug("A plugin cancelled the item change event.");
      return;
    }


    this.item = event.updated();
    this.originalItem = item;

    //call our Post Phase
    event.clone(Phase.POST).callEvent();

    if(this.displayItem != null) {

      this.displayItem.remove(false);
    }
    this.displayItem = null;
    checkDisplay();
    setSignText();
    setDirty();
  }

  @Override
  public @NotNull Block getShopBlock() {
    return this.location.getBlock();
  }

  /**
   * @return The location of the shops chest
   */
  @Override
  public @NotNull Location getLocation() {

    return this.location;
  }

  /**
   * Converts the location associated with this object to a Bukkit-compatible {@link Location}.
   *
   * @return the Bukkit {@link Location} representation of this object's location.
   */
  @Override
  public Location bukkitLocation() {

    return this.location;
  }

  /**
   * @return The name of the player who owns the shop.
   */
  @Override
  public @NotNull QUser getOwner() {

    return this.owner;
  }

  /**
   * Changes the owner of this shop to the given player.
   *
   * @param owner the new owner
   */
  @Override
  public void setOwner(@NotNull final QUser owner) {

    Util.ensureThread(false);
    if(this.owner.equals(owner)) {
      return;
    }
    this.owner = owner;

    //Setup PDC with new owner
    if (owner.getUniqueId() != null) {
      final Block block = this.location.getBlock();
      if(block.getState(false) instanceof final TileState tileState) {

        tileState.getPersistentDataContainer().set(CHEST_SHOP_OWNER, PersistentDataType.STRING, owner.getUniqueId().toString());
        tileState.update(true);
      }
    }

    setDirty();
    setSignText(plugin.getTextManager().findRelativeLanguages(owner, false));
  }

  /**
   * Gets registered to this shop's permission audiences.
   *
   * @return registered audiences
   */
  @Override
  public @NotNull Map<UUID, String> getPermissionAudiences() {

    final Map<UUID, String> clonedPlayerGroup = new HashMap<>(playerGroup);
    final Optional<UUID> uuid = getOwner().getUniqueIdOptional();
    if(uuid.isPresent()) {
      clonedPlayerGroup.put(getOwner().getUniqueId(), BuiltInShopPermissionGroup.ADMINISTRATOR.getNamespacedNode());
    }
    return clonedPlayerGroup;
  }

  /**
   * Gets the player's group in this shop
   *
   * @param player the player
   *
   * @return the group
   */
  @Override
  public @NotNull String getPlayerGroup(@NotNull final UUID player) {

    if(player.equals(getOwner().getUniqueId())) {
      return BuiltInShopPermissionGroup.ADMINISTRATOR.getNamespacedNode();
    }

    final String group = getPermissionAudiences().getOrDefault(player, BuiltInShopPermissionGroup.EVERYONE.getNamespacedNode());
    if(plugin.getShopPermissionManager().hasGroup(group)) {

      final ShopPlayerGroupEvent event = new ShopPlayerGroupEvent(Phase.RETRIEVE, this, player, group);
      event.callEvent();

      return event.updated();
    }
    return BuiltInShopPermissionGroup.EVERYONE.getNamespacedNode();
  }

  /**
   * @return The price per item this shop is selling
   */
  @SuppressWarnings("removal")
  @Override
  public double getPrice() {

    return this.price;
  }

  /**
   * Sets the price of the shop.
   *
   * @param price The new price of the shop.
   */
  @SuppressWarnings("removal")
  @Override
  public void setPrice(final double price) {

    Util.ensureThread(false);
    this.price = price;
    setDirty();
    setSignText();
  }

  /**
   * Retrieves the price of the shop.
   *
   * @return the price of the shop as an instance of type U, where U represents a generic type.
   */
  @Override
  public Double price() {

    return price;
  }

  /**
   * Sets the price for a shop.
   *
   * @param price the price to set for the shop; must be of type U and should not be null
   */
  @Override
  public void price(final Double price) {
    this.price = price;
  }

  /**
   * Formats a string representation based on the provided world and optional currency.
   *
   * @param world    the name of the world for which the string is being formatted; must not be
   *                 null
   * @param currency the optional currency to include in the formatted string; can be null
   *
   * @return a formatted string combining the world and currency information; never null
   */
  @Override
  public @NotNull String format(final @NotNull String world, final @Nullable String currency) {

    return plugin.getEconomyManager().provider().format(BigDecimal.valueOf(price()), world, currency);
  }

  /**
   * Formats a string representation based on the provided world, optional currency, and quantity.
   *
   * @param world    the name of the world for which the string is being formatted; must not be
   *                 null
   * @param currency the optional currency to include in the formatted string; can be null
   * @param quantity the quantity to include in the formatted string; represents a non-negative
   *                 integer
   *
   * @return a formatted string combining the world, currency, and quantity information; never null
   */
  @Override
  public @NotNull String format(final @NotNull String world, final @Nullable String currency, final int quantity) {

    return plugin.getEconomyManager().provider().format(BigDecimal.valueOf(price() * quantity), world, currency);
  }

  /**
   * Provides a comparator for comparing instances of the generic type U used in the shop's
   * pricing.
   *
   * @return a {@link Comparator} for comparing values of type U
   */
  @Override
  public Comparator<Double> priceComparator() {

    return Comparator.comparingDouble(Double::doubleValue);
  }

  /**
   * Retrieves the maximum number of items that can currently be purchased or acquired based on the
   * shop's available balance and the price of the items.
   *
   * @return the maximum number of items that can be afforded; always a non-negative integer.
   */
  @Override
  public int getMaxAffordable() {
    if(this.isUnlimited() || this.isFreeShop()) {
      return Integer.MAX_VALUE;
    }

    final BigDecimal unitPrice = BigDecimal.valueOf(this.price());
    if(unitPrice == null || unitPrice.compareTo(ZERO) <= 0) {

      return 0;
    }

    final EconomyProvider eco = QuickShop.getInstance().getEconomyManager().provider();
    if(eco == null) {

      return 0;
    }

    final BigDecimal balance = eco.balance(owner, location.getWorld().getName(), currency);

    if(balance == null || balance.compareTo(ZERO) <= 0) {

      return 0;
    }

    final BigDecimal affordable = balance.divideToIntegralValue(unitPrice);
    if(affordable.compareTo(BigDecimal.valueOf(Integer.MAX_VALUE)) > 0) {
      return Integer.MAX_VALUE;
    }
    return Math.max(0, affordable.intValue());
  }

  /**
   * Determines whether the current shop can afford the transaction of a specified quantity of
   * items.
   *
   * @param itemAmount the number of items involved in the transaction; must be a non-negative
   *                   integer
   *
   * @return true if the shop can afford the specified number of items, false otherwise
   */
  @Override
  public boolean canAfford(final int itemAmount) {
    if(itemAmount <= 0) {
      return false;
    }
    if(this.isUnlimited() || this.isFreeShop()) {
      return true;
    }

    final BigDecimal unitPrice = BigDecimal.valueOf(this.price());
    if(unitPrice == null || unitPrice.compareTo(ZERO) <= 0) {
      return false;
    }

    final EconomyProvider eco = QuickShop.getInstance().getEconomyManager().provider();
    if(eco == null) {
      return false;
    }

    final BigDecimal balance = eco.balance(owner, location.getWorld().getName(), currency);
    if(balance == null || balance.compareTo(ZERO) <= 0) {
      return false;
    }

    final BigDecimal total = unitPrice.multiply(BigDecimal.valueOf(itemAmount));
    return balance.compareTo(total) >= 0;
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

    return getRemainingStockAsync()
            .orTimeout(5, TimeUnit.SECONDS)
            .exceptionally(ex -> 0)
            .join();
  }

  public CompletableFuture<Integer> getRemainingStockAsync() {
    if(this.unlimited) {
      return CompletableFuture.completedFuture(-1);
    }

    if(Bukkit.getServer().isOwnedByCurrentRegion(location)) {

      return CompletableFuture.completedFuture(calculateRemainingStock());
    }

    final CompletableFuture<Integer> future = new CompletableFuture<>();

    try {
      QuickShop.folia().getScheduler().runAtLocation(this.location, task->{
        try {
          future.complete(calculateRemainingStock());
        } catch(final Throwable throwable) {
          future.completeExceptionally(throwable);
        }
      });
    } catch(final Throwable throwable) {
      future.completeExceptionally(throwable);
    }

    return future;
  }

  private int calculateRemainingStock() {

    final InventoryWrapper inventoryWrapper = getInventory();
    if(inventoryWrapper == null) {
      return 0;
    }

    final int stock = Util.countItems(inventoryWrapper, this);
    new ShopInventoryCalculateEvent(this, -1, stock).callEvent();
    return stock;
  }

  /**
   * Retrieves the runtime-generated random unique identifier for the current instance. DO NOT USE FOR
   * DATA STORAGE.
   *
   * @return a non-null {@link UUID} representing a unique identifier that was generated at runtime.
   */
  @Override
  public @NotNull UUID getRuntimeRandomUniqueId() {

    return this.runtimeRandomUniqueId;
  }

  @Override
  public long getShopId() {

    return this.shopId;
  }

  @Override
  public void setShopId(final long newId) {

    if(this.shopId != -1) {
      throw new IllegalStateException("Cannot set shop id once it fully created.");
    }
    this.shopId = newId;
    setDirty();
  }

  /**
   * Retrieves additional data associated with the shop in the form of key-value pairs.
   *
   * @return A non-null map containing extra shop data. The map keys and values represent custom
   * data associated with the shop, where both keys and values are strings.
   */
  @Override
  public @NotNull Map<Key, String> getExtra() {

    return Collections.unmodifiableMap(this.extraMap);
  }

  @Override
  public String getExtra(@NotNull final NamespacedKey key) {
    return this.extraMap.get(key);
  }

  @Override
  public String getExtra(@NotNull final NamespacedKey key, @Nullable final String defaultValue) {
    return this.extraMap.getOrDefault(key, defaultValue);
  }

  @Override
  public void setExtra(@NotNull final Plugin plugin, @Nullable final Map<String, String> data) {
    if (data == null) {
      return;
    }

    data.forEach((k, v) -> {

      this.extraMap.put(new NamespacedKey(plugin, k), v);
    });
    setDirty();
  }

  @Override
  public void setExtra(@NotNull final NamespacedKey key, @NotNull final String data) {
    this.extraMap.put(key, data);
    setDirty();
  }

  @Override
  public void removeExtra(@NotNull final NamespacedKey key) {
    this.extraMap.remove(key);
    setDirty();
  }

  @Override
  public void removeAll(@NotNull final Plugin plugin) {
    final String prefix = plugin.getName().toLowerCase(Locale.ROOT) + ":";

    this.extraMap.keySet().removeIf(key -> key.asString().startsWith(prefix));
    setDirty();
  }

  /**
   * Gets this shop name that set by player
   *
   * @return Shop name, or null if not set
   */
  @Override
  public @Nullable String getShopName() {

    return this.shopName;
  }

  /**
   * Sets shop name
   *
   * @param shopName shop name, null to remove currently name
   */
  @Override
  public void setShopName(@Nullable final String shopName) {

    if(com.ghostchu.quickshop.common.util.CommonUtil.strEquals(this.shopName, shopName)) {
      return;
    }
    this.shopName = shopName;
    setDirty();
  }

  /**
   * Getting the item stacking amount of the shop.
   *
   * @return The item stacking amount of the shop.
   */
  @Override
  public int getShopStackingAmount() {

    if(isStackingShop()) {
      return item.getAmount();
    }
    return 1;
  }

  /**
   * Retrieves the current state of the shop.
   *
   * @return the current state of the shop as a ShopState object
   */
  @Override
  public ShopState shopState() {

    final ShopStateEvent event = new ShopStateEvent(Phase.RETRIEVE, this, this.shopState);
    event.callEvent();

    return event.updated();
  }

  /**
   * Updates the current state of the shop based on the provided {@code ShopState}.
   *
   * @param newState the new state to set for the shop; must not be null
   */
  @Override
  public void shopState(@NotNull final ShopState newState) {

    Util.ensureThread(false);

    if(this.shopState.identifier().equalsIgnoreCase(newState.identifier())) {

      return;
    }

    ShopStateEvent event = new ShopStateEvent(Phase.PRE, this, this.shopState, newState);
    event.callEvent();

    event = event.clone(Phase.MAIN);

    if(event.callCancellableEvent()) {

      Log.debug("Some addon cancelled shop state changes, target shop: " + this);
      return;
    }

    this.shopState = event.updated();

    event = event.clone(Phase.POST);
    event.callEvent();

    this.setSignText();
    setDirty();
  }

  /**
   * Updates or processes the state of a shop based on the provided identifier.
   *
   * @param shopStateIdentifier a non-null string representing the unique identifier for the shop
   *                            state to be updated or processed.
   */
  @Override
  public void shopState(@NotNull final String shopStateIdentifier) {

    shopState(QuickShop.getInstance().getShopManager().shopStateOrDefault(shopStateIdentifier));
  }

  /**
   * Retrieves the type of shop associated with this entity.
   *
   * @return an instance of IShopType representing the shop type
   */
  @Override
  public IShopType shopType() {

    final ShopTypeEnhancedEvent event = new ShopTypeEnhancedEvent(Phase.RETRIEVE, this, this.shopType);
    event.callEvent();

    return event.updated();
  }

  /**
   * Sets the type of shop using the provided shop type parameter.
   *
   * @param newShopType the shop type to set, must not be null
   */
  @Override
  public void shopType(@NotNull final IShopType newShopType) {

    Util.ensureThread(false);

    if(this.shopType.identifier().equalsIgnoreCase(newShopType.identifier())) {

      return;
    }

    ShopTypeEnhancedEvent event = new ShopTypeEnhancedEvent(Phase.PRE, this, this.shopType, newShopType);
    event.callEvent();

    event = event.clone(Phase.MAIN);

    if(event.callCancellableEvent()) {

      Log.debug("Some addon cancelled shop type changes, target shop: " + this);
      return;
    }

    this.shopType = event.updated();

    event = event.clone(Phase.POST);
    event.callEvent();

    this.setSignText();
    setDirty();
  }

  /**
   * Specifies the type of shop based on the given identifier.
   *
   * @param shopTypeIdentifier the identifier representing the type of shop. Must not be null.
   */
  @Override
  public void shopType(@NotNull final String shopTypeIdentifier) {

    shopType(QuickShop.getInstance().getShopManager().shopTypeOrDefault(shopTypeIdentifier));
  }

  @Override
  public List<Component> getSignText(@NotNull final ProxiedLocale locale) {

    Util.ensureThread(false);

    final LinkedList<Component> lines = plugin.getShopManager().shopLayoutProvider().render(this, locale);

    final ShopSignLinesEvent event = new ShopSignLinesEvent(Phase.RETRIEVE, this, lines);
    event.callEvent();

    return event.updated();
  }

  /**
   * Retrieves the text to be displayed on the shop's sign asynchronously.
   *
   * @param locale The locale configuration used to generate the sign text.
   *
   * @return A CompletableFuture containing a list of {@link Component} objects that represent the
   * text for each line of the shop's sign.
   */
  @Override
  public CompletableFuture<List<Component>> getSignTextAsync(final @NotNull ProxiedLocale locale) {

    final Location loc = this.location.clone();
    final CompletableFuture<List<Component>> result = new CompletableFuture<>();

    QuickShop.folia().getScheduler().runAtLocation(loc, task -> {
      if (!this.isValid()) {
        result.complete(List.of());
        return;
      }

      plugin.getShopManager()
              .shopLayoutProvider()
              .createSignSnapshot(this, locale)
              .thenApply(snapshot ->plugin.getShopManager().shopLayoutProvider().renderSnapshot(snapshot, locale))
              .whenComplete((lines, throwable) -> {
                QuickShop.folia().getScheduler().runAtLocation(loc, task2 -> {
                  if (throwable != null) {

                    result.completeExceptionally(throwable);
                    return;
                  }

                  if (!this.isValid()) {

                    result.complete(List.of());
                    return;
                  }

                  final ShopSignLinesEvent event = new ShopSignLinesEvent(Phase.RETRIEVE, this, new LinkedList<>(lines));

                  event.callEvent();
                  result.complete(event.updated());
                });
              });
    });

    return result;
  }

  /**
   * Returns a list of signs that are attached to this shop (QuickShop and blank signs only)
   *
   * @return a list of signs that are attached to this shop (QuickShop and blank signs only)
   */
  @Override
  public @NotNull List<Sign> getSigns() {

    Util.ensureThread(false);
    final List<Sign> signs = new ArrayList<>(4);
    if(this.bukkitLocation().getWorld() == null) {
      return Collections.emptyList();
    }
    final Block[] blocks = new Block[4];
    blocks[0] = location.getBlock().getRelative(BlockFace.EAST);
    blocks[1] = location.getBlock().getRelative(BlockFace.NORTH);
    blocks[2] = location.getBlock().getRelative(BlockFace.SOUTH);
    blocks[3] = location.getBlock().getRelative(BlockFace.WEST);
    for(final Block b : blocks) {
      if(b == null) {
        continue;
      }
      final BlockState state = b.getState(false);
      if(!(state instanceof final Sign sign)) {
        continue;
      }
      if(!location.getBlock().equals(Util.getAttached(b))) {
        continue;
      }
      if(isShopSign(sign)) {
        claimShopSign(sign);
        signs.add(sign);
      }
    }

    return signs;
  }


  @Override
  @Nullable
  public QUser getTaxAccount() {

    QUser uuid = null;
    if(taxAccount != null) {
      uuid = taxAccount;
    } else {
      if(((SimpleShopManager)plugin.getShopManager()).getCacheTaxAccount() != null) {
        uuid = ((SimpleShopManager)plugin.getShopManager()).getCacheTaxAccount();
      }
    }
    final ShopTaxAccountEvent event = new ShopTaxAccountEvent(Phase.RETRIEVE, this, uuid);
    event.callEvent();

    return event.updated();

  }

  @Override
  public void setTaxAccount(@Nullable final QUser taxAccount) {

    if(Objects.equals(taxAccount, this.taxAccount)) {
      return;
    }

    ShopTaxAccountEvent event = new ShopTaxAccountEvent(Phase.PRE, this, this.taxAccount, taxAccount);
    event.callEvent();

    event = event.clone(Phase.MAIN);
    if(event.callCancellableEvent()) {

      return;
    }


    this.taxAccount = event.updated();

    event = event.clone(Phase.POST);
    event.callEvent();

    setDirty();
  }

  @Override
  @Nullable
  public QUser getTaxAccountActual() {

    return taxAccount;
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
   * Asynchronously determines whether the shop's inventory is available, indicating that it is
   * neither out of space nor out of stock.
   *
   * @return A CompletionStage that completes with a Boolean value indicating whether the inventory
   * is available (true) or not (false).
   */
  @Override
  public CompletableFuture<Boolean> inventoryAvailableAsync() {

    if(isUnlimited()) {
      return CompletableFuture.completedFuture(true);
    }
    if(isSelling()) {

      return getRemainingStockAsync().thenApply(stock -> stock > 0);
    }
    if(isBuying()) {
      return CompletableFuture.completedFuture(getRemainingSpace() > 0);
    }
    if(isFrozen()) {
      return CompletableFuture.completedFuture(false);
    }
    return CompletableFuture.completedFuture(true);
  }

  @Override
  public boolean isAttached(@NotNull final Block b) {

    Util.ensureThread(false);
    return this.bukkitLocation().getBlock().equals(Util.getAttached(b));
  }

  @Override
  public boolean isBuying() {

    return this.shopType.isBuying();
  }

  @Override
  public boolean isFrozen() {

    return this.shopType.isTradingBlocked() || !this.shopState.isTradingAllowed();
  }

  private boolean isDeleted() {

    return this.isDeleted;
  }

  @Override
  public boolean isDirty() {

    return this.dirty;
  }

  @Override
  public void setDirty(final boolean isDirty) {

    this.dirty = isDirty;
  }

  @Override
  public boolean isDisableDisplay() {

    final ShopDisplayEvent event = ShopDisplayEvent.RETRIEVE(this, this.disableDisplay);
    event.callEvent();

    return event.updated();
  }

  @Override
  public void setDisableDisplay(final boolean disabled) {

    if(this.disableDisplay == disabled) {
      return;
    }
    this.disableDisplay = disabled;
    setDirty();
    checkDisplay();
  }

  /**
   * Determines whether a custom item name should be used.
   *
   * @return true if a custom item name is enabled, false otherwise
   */
  @Override
  public boolean useCustomItemName() {

    if(!plugin.getConfig().getBoolean("shop.force-use-item-original-name")) {
      return false;
    }

    final ItemMeta itemMeta = this.item.getItemMeta();
    if(itemMeta == null) {
      return false;
    }

    try {
      if(itemMeta.hasItemName()) {
        return true;
      }
    } catch(final NoSuchMethodError ignore) {
      //old version
    }

    try {
      if(itemMeta.hasCustomName()) {
        return true;
      }
    } catch(final NoSuchMethodError ignore) {
      //old version
    }

    return itemMeta.hasDisplayName();
  }

  /**
   * Customizes and returns a Component representing an item name.
   *
   * @return a Component representing the customized item name
   */
  @Override
  public Component customItemName() {

    return Util.getItemStackName(getItem());
  }

  /**
   * Check if this shop is free shop
   *
   * @return Free Shop
   */
  @Override
  public boolean isFreeShop() {

    return this.price == 0.0d;
  }

  @Override
  public boolean isLoaded() {

    return this.isLoaded;
  }

  @Override
  public boolean isSelling() {

    return !this.shopType.isBuying();
  }

  /**
   * Checks if a Sign is a ShopSign
   *
   * @param sign Target {@link Sign}
   *
   * @return Is shop info sign
   */
  @Override
  public boolean isShopSign(@NotNull final Sign sign) {
    // Check for new shop sign
    final Component[] lines = new Component[sign.getLines().length];
    for(int i = 0; i < sign.getLines().length; i++) {
      lines[i] = plugin.platform().getLine(sign, i);
    }
    // Can be claim

    boolean empty = true;
    for(final Component line : lines) {
      if(!Util.isEmptyComponent(line)) {
        empty = false;
        break;
      }
    }

    if(empty) {
      return true;
    }

    // Check for exists shop sign (modern)
    ShopSignStorage shopSignStorage = sign.getPersistentDataContainer().get(SHOP_NAMESPACED_KEY, ShopSignPersistentDataType.INSTANCE);
    if(shopSignStorage == null) {
      // Try to read Reremake sign namespaced key
      shopSignStorage = sign.getPersistentDataContainer().get(LEGACY_SHOP_NAMESPACED_KEY, ShopSignPersistentDataType.INSTANCE);
    }
    if(shopSignStorage == null) {
      // Try more hard to read Reremake sign namespaced key
      if(sign.getLine(1).startsWith(LEGACY_SHOP_SIGN_RECOGNIZE_PATTERN)) {
        return true;
      }
    }
    if(shopSignStorage != null) {
      return shopSignStorage.equals(this.bukkitLocation().getWorld().getName(), this.bukkitLocation().getBlockX(), this.bukkitLocation().getBlockY(), this.bukkitLocation().getBlockZ());
    }
    return false;
  }

  /**
   * Gets shop status is stacking shop
   *
   * @return The shop stacking status
   */
  @Override
  public boolean isStackingShop() {

    return plugin.isAllowStack() && this.item.getAmount() > 1;
  }

  @Override
  public boolean isUnlimited() {

    return this.unlimited;
  }

  @Override
  public void setUnlimited(final boolean unlimited) {

    if(this.unlimited == unlimited) {
      return;
    }
    Util.ensureThread(false);
    this.unlimited = unlimited;
    setDirty();
    this.setSignText();
  }

  /**
   * Check shop is or not still Valid.
   *
   * @return isValid
   */
  @Override
  public boolean isValid() {

    Util.ensureThread(false);
    if(this.isDeleted) {
      return false;
    }
    return Util.canBeShop(this.bukkitLocation().getBlock());
  }

  /**
   * Returns true if the ItemStack matches what this shop is selling/buying
   *
   * @param item The ItemStack
   *
   * @return True if the ItemStack is the same (Excludes amounts)
   */
  @Override
  public boolean matches(@Nullable final ItemStack item) {

    if(item == null) {
      return false;
    }
    final ItemStack givenItem = item.clone();
    givenItem.setAmount(1);
    final ItemStack shopItem = this.item.clone();
    shopItem.setAmount(1);
    return plugin.getItemMatcher().matches(shopItem, givenItem);
  }

  @Override
  public void onClick(@NotNull final Player clicker) {

    Util.ensureThread(false);
    ShopClickEvent event = new ShopClickEvent(this, QUserImpl.createFullFilled(clicker));
    event.callEvent();

    event = event.clone(Phase.MAIN);
    if(event.callCancellableEvent()) {

      Log.debug("Ignore shop click, because some plugin cancelled it.");
      return;
    }
    setSignText(plugin.getTextManager().findRelativeLanguages(clicker));

    event = event.clone(Phase.POST);
    event.callEvent();
  }

  /**
   * Load ContainerShop.
   */
  @Override
  public void handleLoading() {

    Util.ensureThread(false);
    if(this.isLoaded) {
      Log.debug("Dupe load request, canceled.");
      return;
    }
    try(final PerfMonitor ignored = new PerfMonitor("Shop Inventory Locate", Duration.of(1, ChronoUnit.SECONDS))) {
      if(getInventory() == null) {
        plugin.logger().warn("Failed to load shop: {}: {}: {}", symbolLink, this.getClass().getName(), "Inventory is null");
        if(plugin.getConfig().getBoolean("debug.delete-corrupt-shops")) {
          plugin.logger().warn("Deleting corrupt shop...");
          Util.regionThread(location, () -> plugin.getShopManager().deleteShop(this));
        } else {
          plugin.logger().warn("Unloading shops from memory, set `debug.delete-corrupt-shops` to true to delete corrupted shops.");
          plugin.getShopManager().unloadShop(this);
        }
        return;
      }
    }
    if(Util.fireCancellableEvent(new ShopLoadEvent(this))) {
      return;
    }
    this.isLoaded = true;
    //disable schedule check due to performance issue
    //plugin.getShopContainerWatcher().scheduleCheck(this);
    try(final PerfMonitor ignored = new PerfMonitor("Shop Display Check", Duration.of(1, ChronoUnit.SECONDS))) {
      checkDisplay();
    }
    if(plugin.getConfig().getBoolean("shop.update-sign-on-load", false)) {
      Log.debug("Scheduled sign update for shop " + this + " because updateShopSignOnLoad has been enabled.");
      plugin.getSignUpdateWatcher().scheduleSignUpdate(this);
    }
  }

  /**
   * Unload ContainerShop.
   */
  @Override
  public void handleUnloading(final boolean dontTouchWorld) {

    Util.ensureThread(false);
    if(!this.isLoaded) {
      Log.debug("Dupe unload request, canceled.");
      return;
    }
    if(this.displayItem != null) {
      this.displayItem.remove(dontTouchWorld);
    }
    this.isLoaded = false;
    plugin.getShopManager().getLoadedShops().remove(this);
    new ShopUnloadEvent(Phase.POST, this).callEvent();
  }

  @Override
  public void openPreview(@NotNull final Player player) {

    InventoryPreview.show(player, getItem());

  }

  @Override
  public @NotNull Component ownerName(final boolean forceUsername, @NotNull final ProxiedLocale locale) {

    Component name;
    if(!forceUsername && isUnlimited()) {
      name = plugin.text().of("admin-shop").forLocale(locale.getLocale());
    } else {
      final String playerName = this.getOwner().getUsername();
      if(playerName == null) {
        name = plugin.text().of("unknown-owner").forLocale(locale.getLocale());
      } else {
        name = Component.text(playerName);
      }
    }
    if(getOwner().isRealPlayer()) {
      name = name.hoverEvent(
              plugin.text().of("real-player-component-hover", getOwner().getUniqueId(), getOwner().getUsername(), getOwner().getDisplay()).forLocale(locale.getLocale())
                            );
    } else {
      name = name.hoverEvent(
              plugin.text().of("virtual-player-component-hover", getOwner().getUniqueId(), getOwner().getUsername(), getOwner().getDisplay()).forLocale(locale.getLocale())
                            );

    }

    final ShopOwnerNameEvent event = new ShopOwnerNameEvent(Phase.RETRIEVE, this, name);
    event.callEvent();

    name = event.updated();
    return name;
  }

  @Override
  public @NotNull Component ownerName(@NotNull final ProxiedLocale locale) {

    return ownerName(false, locale);
  }

  @Override
  public @NotNull Component ownerName() {

    return ownerName(false, MsgUtil.getDefaultGameLanguageLocale());
  }

  /**
   * Check if player has permission to authorize the specified permission node.
   *
   * @param player     the player
   * @param namespace  the plugin instance for the permission node (namespace)
   * @param permission the permission node
   *
   * @return true if player has permission, false otherwise
   */
  @Override
  public boolean playerAuthorize(@NotNull final UUID player, @NotNull final Plugin namespace, @NotNull final String permission) {

    if(player.equals(getOwner().getUniqueId())) {
      Log.permission("Check permission " + namespace.getName().toLowerCase(Locale.ROOT) + "." + permission + " for " + player + " -> " + "true");
      return true;
    }

    final String group = getPlayerGroup(player);
    final boolean hasPermission = plugin.getShopPermissionManager().hasPermission(group, namespace, permission);

    final ShopPermissionCheckEvent event = new ShopPermissionCheckEvent(Phase.MAIN, this, player, namespace.getName(), permission, hasPermission);
    event.callEvent();

    Log.permission("Check permission " + namespace.getName().toLowerCase(Locale.ROOT) + "." + permission + ": " + player + " -> " + event.hasPermission());

    return event.hasPermission();
  }

  /**
   * Check if player has permission to authorize the specified permission node.
   *
   * @param player     the player
   * @param permission the permission node
   *
   * @return true if player has permission, false otherwise
   */
  @Override
  public boolean playerAuthorize(@NotNull final UUID player, @NotNull final BuiltInShopPermission permission) {

    return playerAuthorize(player, plugin.getJavaPlugin(), permission.getRawNode());
  }

  @Override
  public List<UUID> playersCanAuthorize(@NotNull final BuiltInShopPermission permission) {

    return playersCanAuthorize(plugin.getJavaPlugin(), permission.getRawNode());
  }

  @Override
  public List<UUID> playersCanAuthorize(@NotNull final BuiltInShopPermissionGroup permissionGroup) {

    return getPermissionAudiences().entrySet().stream().filter(entry->entry.getValue().equals(permissionGroup.getNamespacedNode())).map(Map.Entry::getKey).toList();
  }

  @Override
  public List<UUID> playersCanAuthorize(@NotNull final Plugin namespace, @NotNull final String permission) {

    final List<UUID> result = new ArrayList<>();
    for(final Map.Entry<UUID, String> uuidStringEntry : this.getPermissionAudiences().entrySet()) {

      final String group = uuidStringEntry.getValue();
      final boolean hasPermission = plugin.getShopPermissionManager().hasPermission(group, namespace, permission);

      final ShopPermissionCheckEvent event = new ShopPermissionCheckEvent(Phase.MAIN, this, uuidStringEntry.getKey(), namespace.getName(), permission, hasPermission);
      event.callEvent();

      if(event.hasPermission()) {
        result.add(uuidStringEntry.getKey());
      }
    }
    Log.permission("Check permission " + namespace.getName().toLowerCase(Locale.ROOT) + "." + permission + ": " + CommonUtil.list2String(result.stream().map(UUID::toString).toList()));
    return result;
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

  @Override
  public ShopInfoStorage saveToInfoStorage() {

    return new ShopInfoStorage(this.bukkitLocation().getWorld().getName(),
                               new BlockPos(this.bukkitLocation()), this.owner, this.price,
                               QuickShop.getInstance().platform().encodeStack(this.originalItem),
                               (isUnlimited())? 1 : 0, shopType().id(),
                               serializeExtra(), this.currency, this.disableDisplay,
                               this.taxAccount, inventoryWrapperProvider,
                               saveToSymbolLink(), this.playerGroup);
  }

  @Override
  @NotNull
  public String saveToSymbolLink() {

    return symbolLink;
  }

  /**
   * Sells amount of item to Player p. Does NOT check our inventory, or balances
   *
   * @param seller          The seller
   * @param sellerInventory The seller's inventory
   * @param loc2Drop        Location to drop items if inventory are full
   * @param amount          The amount to sell
   */
  @Override
  public TradeResult sell(@NotNull final QUser seller,
                   @NotNull final InventoryWrapper sellerInventory,
                   @NotNull final Location loc2Drop,
                   final int amount) {
    return plugin.getShopManager().tradeService().executeBuyFromShop(this, seller, sellerInventory, loc2Drop, amount);
  }

  @Override
  public void setDirty() {

    this.dirty = true;
  }

  @Override
  public void setInventory(@NotNull final InventoryWrapper wrapper, @NotNull final InventoryWrapperManager manager) {

    final String provider = plugin.getInventoryWrapperRegistry().find(manager);
    if(provider == null) {
      throw new IllegalArgumentException("The manager " + manager.getClass().getName() + " not registered in registry.");
    }
    this.inventoryWrapperProvider = provider;
    this.symbolLink = manager.mklink(wrapper);
    setDirty();
    Log.debug("Inventory changed: " + this.symbolLink + ", wrapper provider:" + inventoryWrapperProvider);
    new ShopInventoryChangedEvent(wrapper, manager).callEvent();
  }

  @Override
  public void setPlayerGroup(@NotNull final UUID player, @Nullable String group) {

    if(group == null) {
      group = BuiltInShopPermissionGroup.EVERYONE.getNamespacedNode();
    }

    ShopPlayerGroupEvent event = new ShopPlayerGroupEvent(Phase.PRE, this, player, getPlayerGroup(player), group);
    event.callEvent();

    if(group.equals(BuiltInShopPermissionGroup.EVERYONE.getNamespacedNode())) {

      this.playerGroup.remove(player);
    } else {

      this.playerGroup.put(player, group);
    }
    event = event.clone(Phase.POST);
    event.callEvent();

    setDirty();
  }

  @Override
  public void setPlayerGroup(@NotNull final UUID player, @Nullable BuiltInShopPermissionGroup group) {

    if(group == null) {
      group = BuiltInShopPermissionGroup.EVERYONE;
    }

    ShopPlayerGroupEvent event = new ShopPlayerGroupEvent(Phase.PRE, this, player, getPlayerGroup(player), group.getNamespacedNode());
    event.callEvent();
    if(group == BuiltInShopPermissionGroup.EVERYONE) {

      this.playerGroup.remove(player);
    } else {

      setPlayerGroup(player, group.getNamespacedNode());
    }

    event = event.clone(Phase.POST);
    event.callEvent();
    setDirty();
  }

  /**
   * Updates signs attached to the shop
   */
  @Override
  public void setSignText() {

    //Util.ensureThread(false);
    if(!Util.isLoaded(this.location)) {
      return;
    }

    final Location loc = this.location.clone();
    final CompletableFuture<List<Component>> textCompletable = getSignTextAsync(plugin.getTextManager().findRelativeLanguages(MsgUtil.getDefaultGameLanguageCode()));

    textCompletable.thenAccept(lines -> {
      QuickShop.folia().getScheduler().runAtLocation(loc, (consumer)->{
        if(!isValid()) {
          return;
        }
        this.setSignText(lines);
      });
    });
  }

  /**
   * Changes all lines of text on a sign near the shop
   *
   * @param lines The array of lines to change. Index is line number.
   */
  @Override
  public void setSignText(@NotNull final List<Component> lines) {

    Util.ensureThread(false);
    Log.debug("Globally sign text setting...");
    final List<Sign> signs = this.getSigns();

    final ShopSignLinesEvent event = new ShopSignLinesEvent(Phase.POST, this, lines);
    event.callEvent();

    for(final Sign sign : signs) {
      final boolean isGlowing = plugin.getConfig().getBoolean("shop.sign-glowing", false);
      final boolean isWaxed = plugin.getConfig().getBoolean("shop.sign-wax", false);
      plugin.platform().setLines(sign, event.updated());

      sign.getSide(Side.FRONT).setGlowingText(isGlowing);

      final DyeColor dyeColor = Util.getDyeColor();
      if(dyeColor != null) {
        sign.getSide(Side.FRONT).setColor(dyeColor);
      }
      sign.setWaxed(isWaxed);

      new ShopSignUpdateEvent(this, sign).callEvent();
      sign.update(true);
    }
    if(plugin.getSignHooker() != null) {
      Log.debug("Start sign broadcast...");
      plugin.getSignHooker().updatePerPlayerShopSignBroadcast(this.bukkitLocation(), this);
      Log.debug("Sign broadcast completed.");
    }
  }

  /**
   * Updates signs attached to the shop
   *
   * @param locale The locale to use for the sign text
   */
  @Override
  public void setSignText(@NotNull final ProxiedLocale locale) {

    //Util.ensureThread(false);
    if(!Util.isLoaded(this.location)) {
      return;
    }

    final Location loc = this.location.clone();
    final CompletableFuture<List<Component>> textCompletable = getSignTextAsync(locale);

    textCompletable.thenAccept(lines ->QuickShop.folia().getScheduler().runAtLocation(loc, (consumer)->{

      if(!isValid()) {

        return;
      }
      this.setSignText(lines);
    }));
  }

  /**
   * Updates the shop into the database.
   */
  @Override
  @NotNull
  public CompletableFuture<Void> update() {

    //Warning! This method can be run in async thread.
    if(updating) {
      return CompletableFuture.completedFuture(null);
    }

    if(this.shopId == -1) {
      Log.debug("Skip shop database update because it not fully setup!");
      return CompletableFuture.completedFuture(null);
    }

    ShopDatabaseEvent event = new ShopDatabaseEvent(Phase.PRE_CANCELLABLE, this);

    if(event.callCancellableEvent()) {

      Log.debug("The Shop update action was canceled by a plugin.");
      return CompletableFuture.completedFuture(null);
    }

    event = event.clone(Phase.POST);
    event.callEvent();

    //If already updating, just return the same future
    if(!updatingAtomic.compareAndSet(false, true)) {
      return inFlightUpdate != null ? inFlightUpdate : CompletableFuture.completedFuture(null);
    }

    //Start a new update
    final CompletableFuture<Void> f = plugin.getDatabaseHelper().updateShop(this)
            .whenComplete((r, th) -> {
              updatingAtomic.set(false);
              if (th == null) {
                dirty = false;
              } else {
                plugin.logger().warn("Could not update shop in DB!", th);
              }
            });

    inFlightUpdate = f;
    return f;
  }

  @Override
  public void updateSync() throws RuntimeException {
    final CompletableFuture<Void> future = update();

    waitForFuture(future, 15, TimeUnit.SECONDS, "updateShop(" + shopId + ")");
  }

  @Override
  public @NotNull BenefitProvider getShopBenefit() {

    final ShopBenefitEvent event = ShopBenefitEvent.RETRIEVE(this, this.benefit);
    event.callEvent();

    return event.updated();
  }

  @Override
  public void setShopBenefit(@NotNull final BenefitProvider benefit) {

    this.benefit = benefit;
    setDirty();
  }

  public @NotNull SimpleDataRecord createDataRecord() {

    return new SimpleDataRecord(
            getOwner(),
            plugin.platform().encodeStack(getItem()),
            plugin.platform().encodeStack(getItem()),
            getShopName(),
            shopType().id(),
            shopState().identifier(),
            getCurrency(),
            getPrice(),
            isUnlimited(),
            isDisableDisplay(),
            getTaxAccount(),
            JsonUtil.getGson().toJson(getPermissionAudiences()),
            serializeExtra(),
            getInventoryWrapperProvider(),
            saveToSymbolLink(),
            new Date(),
            getShopBenefit().serialize()
    );
  }

  /**
   * Returns the display item associated with this shop.
   *
   * @return The display item associated with this shop.
   */
  @Nullable
  public AbstractDisplayItem getDisplayItem() {

    return this.displayItem;
  }

  /**
   * @return The enchantments the shop has on its items.
   */
  public @NotNull Map<Enchantment, Integer> getEnchants() {

    if(this.item.hasItemMeta() && this.item.getItemMeta().hasEnchants()) {
      return Objects.requireNonNull(this.item.getItemMeta()).getEnchants();
    }
    return Collections.emptyMap();
  }

  /**
   * @return The ItemStack type of this shop
   */
  public @NotNull Material getMaterial() {

    return this.item.getType();
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

  @Override
  public ReloadResult reloadModule() throws Exception {

    if(!plugin.isAllowStack()) {
      this.item.setAmount(1);
    } else {
      this.item.setAmount(this.originalItem.getAmount());
    }
    return Reloadable.super.reloadModule();
  }

  private ShopSignStorage saveToShopSignStorage() {

    return new ShopSignStorage(this.bukkitLocation().getWorld().getName(), this.bukkitLocation().getBlockX(), this.bukkitLocation().getBlockY(), this.bukkitLocation().getBlockZ());
  }

  @Override
  public String toString() {

    return "ContainerShop{" +
           "location=" + location +
           ", plugin=" + plugin +
           ", runtimeRandomUniqueId=" + runtimeRandomUniqueId +
           ", playerGroup=" + playerGroup +
           ", isDeleted=" + isDeleted +
           ", extra=" + serializeExtra() +
           ", shopId=" + shopId +
           ", owner=" + owner +
           ", price=" + price +
           ", shopType=" + shopType +
           ", unlimited=" + unlimited +
           ", item=" + item +
           ", originalItem=" + originalItem +
           ", displayItem=" + displayItem +
           ", isLoaded=" + isLoaded +
           ", createBackup=" + createBackup +
           ", dirty=" + dirty +
           ", updating=" + updating +
           ", currency='" + currency + '\'' +
           ", disableDisplay=" + disableDisplay +
           ", taxAccount=" + taxAccount +
           ", inventoryWrapperProvider='" + inventoryWrapperProvider + '\'' +
           ", symbolLink='" + symbolLink + '\'' +
           ", shopName='" + shopName + '\'' +
           ", benefit=" + benefit +
           '}';
  }
}
