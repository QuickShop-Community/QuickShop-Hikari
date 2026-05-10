package com.ghostchu.quickshop.shop.components;

/*
 * QuickShop-Hikari
 * Copyright (C) 2026 Daniel "creatorfromhell" Vidmar
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.database.bean.DataRecord;
import com.ghostchu.quickshop.api.event.Phase;
import com.ghostchu.quickshop.api.event.management.ShopDatabaseEvent;
import com.ghostchu.quickshop.api.event.management.ShopLoadEvent;
import com.ghostchu.quickshop.api.event.management.ShopUnloadEvent;
import com.ghostchu.quickshop.api.shop.ModernShop;
import com.ghostchu.quickshop.api.shop.ShopInfoStorage;
import com.ghostchu.quickshop.api.shop.components.ShopLifecycle;
import com.ghostchu.quickshop.common.util.JsonUtil;
import com.ghostchu.quickshop.database.bean.SimpleDataRecord;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.quickshop.util.performance.PerfMonitor;
import lombok.EqualsAndHashCode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.ghostchu.quickshop.util.Util.waitForFuture;

/**
 * SimpleShopLifecycle
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public class SimpleShopLifecycle implements ShopLifecycle {

  @EqualsAndHashCode.Exclude
  protected final QuickShop plugin;
  @EqualsAndHashCode.Exclude
  protected final ModernShop<?, ?, ?, ?> shop;

  @NotNull
  private String inventoryWrapperProvider;

  protected YamlConfiguration extra;

  @NotNull
  protected String symbolLink;

  @EqualsAndHashCode.Exclude
  protected boolean dirty = false;
  @EqualsAndHashCode.Exclude
  protected boolean updating = false;
  @EqualsAndHashCode.Exclude
  protected boolean isLoaded = false;
  @EqualsAndHashCode.Exclude
  private final boolean isDeleted = false;
  @EqualsAndHashCode.Exclude
  private volatile boolean createBackup = false;

  //updating objects
  private final AtomicBoolean updatingAtomic = new AtomicBoolean(false);
  private volatile CompletableFuture<Void> inFlightUpdate;

  public SimpleShopLifecycle(@NotNull final ModernShop<?, ?, ?, ?> shop) {
    this.shop = shop;
    this.plugin = QuickShop.getInstance();
  }

  /**
   * Getting ConfigurationSection (extra data) instance of your plugin namespace)
   *
   * @param plugin The plugin and plugin name will used for namespace
   *
   * @return ExtraSection, save it through Shop#setExtra. If you don't save it, it may randomly lose
   * or save
   */
  @Override
  public @NotNull ConfigurationSection getExtra(@NotNull final Plugin plugin) {

    if(this.extra == null) {
      this.extra = new YamlConfiguration();
    }
    ConfigurationSection section = extra.getConfigurationSection(plugin.getName());
    if(section == null) {
      section = extra.createSection(plugin.getName());
    }
    return section;
  }

  /**
   * Save the extra data to the shop.
   *
   * @param plugin Plugin instace
   * @param data   The data table
   */
  @Override
  public void setExtra(@NotNull final Plugin plugin, @NotNull final ConfigurationSection data) {

    if(this.extra == null) {
      this.extra = new YamlConfiguration();
    }
    extra.set(plugin.getName(), data);
    // compress extra to null if possible
    boolean anyValid = false;
    for(final String key : extra.getKeys(false)) {
      if(!extra.isConfigurationSection(key)) {
        anyValid = true;
        break;
      }
      final ConfigurationSection section = extra.getConfigurationSection(key);
      if(section == null) continue;
      if(!section.getKeys(false).isEmpty()) {
        anyValid = true;
        break;
      }
    }
    if(!anyValid) {
      this.extra = null;
    }

    //TODO: Determine how to mark as dirty. Maybe through shop service?
    //setDirty();
  }

  /**
   * Save the plugin extra data to Json format
   *
   * @return The json string
   */
  @Override
  public @NotNull String saveExtraToYaml() {

    return extra == null? "" : extra.saveToString();
  }

  @Override
  public @NotNull DataRecord asDataRecord() {

    return new SimpleDataRecord(
            shop.meta().getOwner(),
            shop.item().encodedItem(),
            shop.item().encodedItem(),
            shop.meta().getShopName(),
            shop.meta().shopType().id(),
            shop.meta().shopState().identifier(),
            shop.price().getCurrency(),
            shop.price().price(),
            shop.meta().isUnlimited(),
            shop.item().isDisableDisplay(),
            shop.meta().getTaxAccount(),
            JsonUtil.getGson().toJson(shop.permission().getPermissionAudiences()),
            saveExtraToYaml(),
            shop.interaction().getInventoryWrapperProvider(),
            shop.lifecycle().asSymbolLink(),
            new Date(),
            shop.meta().getShopBenefit().serialize()
    );
  }

  /**
   * Getting ShopInfoStorage that you can use for storage the shop data
   *
   * @return ShopInfoStorage
   */
  @Override
  public ShopInfoStorage asInfoStorage() {

    return ShopInfoStorage.fromShop(shop);
  }

  /**
   * Gets the symbol link that created by InventoryWrapperManager
   *
   * @return InventoryWrapper
   */
  @Override
  public @NotNull String asSymbolLink() {

    return symbolLink;
  }

  /**
   * Gets if shop is dirty (so shop will be save)
   *
   * @return Is dirty
   */
  @Override
  public boolean isDirty() {

    return dirty;
  }

  /**
   * Sets dirty status
   *
   * @param isDirty Shop is dirty
   */
  @Override
  public void setDirty(final boolean isDirty) {
    this.dirty = isDirty;
  }

  /**
   * Sets shop is dirty
   */
  @Override
  public void markDirty() {
    this.dirty = true;
  }

  /**
   * Get this container shop is loaded or unloaded.
   *
   * @return Loaded
   */
  @Override
  public boolean isLoaded() {

    return this.isLoaded;
  }

  /**
   * Checks whether the shop is marked as deleted.
   *
   * @return {@code true} if the shop is deleted, {@code false} otherwise
   */
  @Override
  public boolean isDeleted() {

    return isDeleted;
  }

  @Override
  public void handleLoading() {

    Util.ensureThread(false);
    if(this.isLoaded) {
      Log.debug("Dupe load request, canceled.");
      return;
    }
    try(final PerfMonitor ignored = new PerfMonitor("Shop Inventory Locate", Duration.of(1, ChronoUnit.SECONDS))) {
      if(this.shop.interaction().getInventory() == null) {

        plugin.logger().warn("Failed to load shop: {}: {}: {}", symbolLink, this.getClass().getName(), "Inventory is null");
        if(plugin.getConfig().getBoolean("debug.delete-corrupt-shops")) {
          plugin.logger().warn("Deleting corrupt shop...");
          Util.regionThread(this.shop.bukkitLocation(), () -> plugin.getShopManager().deleteShop(this.shop));
        } else {

          plugin.logger().warn("Unloading shops from memory, set `debug.delete-corrupt-shops` to true to delete corrupted shops.");
          plugin.getShopManager().unloadShop(this.shop);
        }
        return;
      }
    }
    if(Util.fireCancellableEvent(new ShopLoadEvent(this.shop))) {
      return;
    }
    this.isLoaded = true;

    try(final PerfMonitor ignored = new PerfMonitor("Shop Display Check", Duration.of(1, ChronoUnit.SECONDS))) {
      checkDisplay();
    }
    if(plugin.getConfig().getBoolean("shop.update-sign-on-load", false)) {

      Log.debug("Scheduled sign update for shop " + this + " because updateShopSignOnLoad has been enabled.");
      plugin.getSignUpdateWatcher().scheduleSignUpdate(this.shop);
    }
  }

  @Override
  public void handleUnloading(final boolean dontTouchWorld) {

    Util.ensureThread(false);
    if(!this.isLoaded) {
      Log.debug("Dupe unload request, canceled.");
      return;
    }
    if(this.shop.interaction().preview() != null) {
      this.shop.interaction().preview().close();
    }
    if(this.shop.item().getDisplayItem() != null) {
      this.shop.item().getDisplayItem().remove(dontTouchWorld);
    }
    this.isLoaded = false;
    QuickShop.getInstance().getShopManager().getLoadedShops().remove(this.shop);
    new ShopUnloadEvent(Phase.POST, this.shop).callEvent();
  }

  /**
   * Update shop data to database
   */
  @Override
  public @NotNull CompletableFuture<Void> update() {

    //Warning! This method can be run in async thread.
    if(updating) {
      return CompletableFuture.completedFuture(null);
    }

    if(this.shop.meta().getShopId() == -1) {
      Log.debug("Skip shop database update because it not fully setup!");
      return CompletableFuture.completedFuture(null);
    }

    ShopDatabaseEvent event = new ShopDatabaseEvent(Phase.PRE_CANCELLABLE, this.shop);

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
    final CompletableFuture<Void> f = QuickShop.getInstance().getDatabaseHelper().updateShop(this.shop)
            .whenComplete((r, th) -> {
              updatingAtomic.set(false);
              if (th == null) {
                dirty = false;
              } else {
                QuickShop.getInstance().logger().warn("Could not update shop in DB!", th);
              }
            });

    inFlightUpdate = f;
    return f;
  }

  /**
   * Update shop data to database synchronously. This will create the completeable future for the
   * save function, and wait for it to complete. DON'T USE IF YOU DON'T KNOW WHAT YOU'RE DOING!
   */
  @Override
  public void updateSync() throws RuntimeException {

    final CompletableFuture<Void> future = update();

    waitForFuture(future, 15, TimeUnit.SECONDS, "updateShop(" + shop.meta().getShopId() + ")");
  }
}
