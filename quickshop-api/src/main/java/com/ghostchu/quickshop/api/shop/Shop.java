package com.ghostchu.quickshop.api.shop;

import com.ghostchu.quickshop.api.QuickShopAPI;
import com.ghostchu.quickshop.api.shop.inventory.ShopInventory;
import com.ghostchu.quickshop.api.shop.meta.ShopDisplay;
import com.ghostchu.quickshop.api.shop.meta.ShopExtraHolder;
import com.ghostchu.quickshop.api.shop.meta.ShopMeta;
import com.ghostchu.quickshop.api.shop.permission.ShopPermission;
import com.ghostchu.quickshop.api.shop.trading.ShopTrading;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * A shop
 */
public interface Shop<U, L> extends Locatable<L>, ShopInventory, ShopMeta<U>, ShopTrading, ShopDisplay, ShopPermission, ShopExtraHolder {

  NamespacedKey SHOP_NAMESPACED_KEY = new NamespacedKey(QuickShopAPI.getPluginInstance(), "shopsign");

  /**
   * Get shop's item durability, if have.
   *
   * @return Shop's item durability
   */
  short getDurability();

  /**
   * Getting ConfigurationSection (extra data) instance of your plugin namespace)
   *
   * @param plugin The plugin and plugin name will used for namespace
   *
   * @return ExtraSection, save it through Shop#setExtra. If you don't save it, it may randomly lose
   * or save
   */
  @Deprecated(forRemoval = true)
  @ApiStatus.ScheduledForRemoval(inVersion = "6.4.0.0")
  @NotNull
  ConfigurationSection getExtra(@NotNull Plugin plugin);

  /**
   * Check shop is or not attached the target block
   *
   * @param paramBlock Target {@link Block}
   *
   * @return isAttached
   */
  boolean isAttached(@NotNull Block paramBlock);

  /**
   * Gets if shop is dirty (so shop will be save)
   *
   * @return Is dirty
   */
  boolean isDirty();

  /**
   * Sets dirty status
   *
   * @param isDirty Shop is dirty
   */
  void setDirty(boolean isDirty);

  /**
   * Get this container shop is loaded or unloaded.
   *
   * @return Loaded
   */
  boolean isLoaded();

  /**
   * Whether Shop is valid
   *
   * @return status
   */
  boolean isValid();

  /**
   * Execute codes when player click the shop will did things
   */
  void onClick(@NotNull Player clicker);


  @Deprecated()
  @ApiStatus.Internal
  void handleLoading();

  @Deprecated()
  @ApiStatus.Internal
  void handleUnloading(boolean dontTouchWorld);

  /**
   * open a preview for shop item
   *
   * @param player The viewer {@link Player}
   */
  void openPreview(@NotNull Player player);

  /**
   * Getting ShopInfoStorage that you can use for storage the shop data
   *
   * @return ShopInfoStorage
   */
  ShopInfoStorage saveToInfoStorage();

  /**
   * Gets the symbol link that created by InventoryWrapperManager
   *
   * @return InventoryWrapper
   */
  @NotNull
  String saveToSymbolLink();

  /**
   * Sets shop is dirty
   */
  void setDirty();

  /**
   * Save the extra data to the shop.
   *
   * @param plugin Plugin instance
   * @param data   The data table, or null to remove it
   */
  @Deprecated(forRemoval = true)
  @ApiStatus.ScheduledForRemoval(inVersion = "6.4.0.0")
  default void setExtra(@NotNull final Plugin plugin, @Nullable final ConfigurationSection data) {
    if (data == null) {
      return;
    }

    final Map<String, String> extra = new HashMap<>();
    data.getValues(true).forEach((k, v) -> extra.put(k, String.valueOf(v)));

    setExtra(plugin, extra);
  }

  /**
   * Update shop data to database
   */
  @NotNull
  CompletableFuture<Void> update();

  /**
   * Update shop data to database synchronously. This will create the completeable future for the save
   * function, and wait for it to complete. DON'T USE IF YOU DON'T KNOW WHAT YOU'RE DOING!
   *
   * @throws RuntimeException
   */
  void updateSync() throws RuntimeException;
}