package com.ghostchu.quickshop.shop.display.display;

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
import com.ghostchu.quickshop.api.event.display.DisplayApplicableCheckEvent;
import com.ghostchu.quickshop.api.event.display.ShopDisplayItemSpawnEvent;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopChunk;
import com.ghostchu.quickshop.api.shop.display.DisplayType;
import com.ghostchu.quickshop.shop.display.AbstractDisplayItem;
import com.ghostchu.quickshop.util.EntityUtil;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.simplereloadlib.Reloadable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.concurrent.TimeUnit;

import static com.ghostchu.quickshop.shop.display.display.DisplayEntityItemManager.DISPLAY_ITEM_KEY_INSTANCE;

/**
 * DisplayEntityDisplayItem
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public class DisplayEntityDisplayItem extends AbstractDisplayItem implements Reloadable {

  private final ShopChunk chunkLocation;
  private final int locationHash;
  private ItemDisplay itemDisplay;
  private TextDisplay textDisplay;
  private Interaction interactionEntity;

  private boolean isSpawned = false;

  //I don't think this needs to be protected.
  DisplayEntityDisplayItem(final Shop shop, final ShopChunk chunkLocation) {

    super(shop);

    this.chunkLocation = chunkLocation;
    this.locationHash = shop.bukkitLocation().hashCode();
  }

  protected void initEntities() {

    final QuickShop plugin = QuickShop.getInstance();

    final Vector3f itemScaleVector = new Vector3f(plugin.getConfig().getFloat("shop.display-scale.x", 1.25f),
                                                  plugin.getConfig().getFloat("shop.display-scale.y", 1.25f),
                                                  plugin.getConfig().getFloat("shop.display-scale.z", 1.25f));

    final AxisAngle4f rotation = new AxisAngle4f((float)Math.toRadians(plugin.getConfig().getDouble("shop.display-rotation.degrees", 0d)),
                                                 plugin.getConfig().getFloat("shop.display-rotation.x", 0f),
                                                 plugin.getConfig().getFloat("shop.display-rotation.y", 0f),
                                                 plugin.getConfig().getFloat("shop.display-rotation.z", 1f));

    itemDisplay = EntityUtil.spawnDisplayItemFor(null, getDisplayLocation().clone().add(0, .15d, 0), shop.getItem(), itemScaleVector, rotation, Bukkit.getViewDistance() * 16, 0);

    interactionEntity = EntityUtil.spawnInteractionFor(null, getDisplayLocation().toCenterLocation().add(0.0, 0.4, 0.0), 0);
    interactionEntity.getPersistentDataContainer().set(DISPLAY_ITEM_KEY_INSTANCE, PersistentDataType.STRING, Util.locationToPDCString(shop.bukkitLocation()));

    final int blockDistance = plugin.getConfig().getInt("shop.text-display.range-blocks", 8);

    final Vector3f textScaleVector = new Vector3f(plugin.getConfig().getFloat("shop.text-display.scale.x", 1.0f),
                                                  plugin.getConfig().getFloat("shop.text-display.scale.y", 1.0f),
                                                  plugin.getConfig().getFloat("shop.text-display.scale.z", 1.0f));

    final Location textLocation = getDisplayLocation().clone().add(0, plugin.getConfig().getDouble("shop.text-display.y-offset", 0.8), 0);

    textDisplay = EntityUtil.spawnDisplayTextFor(null, textLocation, Util.getTextDisplay(shop, shop.getItem().clone()), textScaleVector, blockDistance, TextDisplay.TextAlignment.CENTER, 0);
  }

  /**
   * Check the display is or not moved.
   *
   * @return Moved
   */
  @Override
  public boolean checkDisplayIsMoved() {

    return false;
  }

  /**
   * Check the display is or not need respawn
   *
   * @return Need
   */
  @Override
  public boolean checkDisplayNeedRegen() {

    return false;
  }

  /**
   * Check target Entity is or not a QuickShop display Entity.
   *
   * @param entity Target entity
   *
   * @return Is or not
   */
  @Override
  public boolean checkIsShopEntity(final Entity entity) {

    return false;
  }

  /**
   * Fix the display moved issue.
   */
  @Override
  public void fixDisplayMoved() {

  }

  /**
   * Fix display need respawn issue.
   */
  @Override
  public void fixDisplayNeedRegen() {

  }

  /**
   * Get the display entity
   *
   * @return Target entity
   */
  @Override
  public Entity getDisplay() {

    return itemDisplay;
  }

  /**
   * Check the display is or not already spawned
   *
   * @return Spawned
   */
  @Override
  public boolean isSpawned() {

    return isSpawned;
  }

  /**
   * Check if the display should be display for the specificed player Only works with
   * VirtualDisplayItem together as for as now.
   *
   * @param player Target player
   *
   * @return Should display
   */
  @Override
  public boolean isApplicableForPlayer(final Player player) {

    final DisplayApplicableCheckEvent event = new DisplayApplicableCheckEvent(shop, player.getUniqueId());

    event.setApplicable(true);
    event.callEvent();

    return event.isApplicable();
  }

  /**
   * Remove the display entity.
   *
   * @param dontTouchWorld When it is true, display impl should avoid touch the world to avoid
   *                       unload-load loop
   */
  @Override
  public void remove(final boolean dontTouchWorld) {

    if(QuickShop.getInstance().getJavaPlugin() == null || !QuickShop.getInstance().getJavaPlugin().isEnabled()) {
      return;
    }

    if (itemDisplay != null) {
      itemDisplay.remove();
    }

    if (textDisplay != null) {
      textDisplay.remove();
    }

    if (interactionEntity != null) {
      interactionEntity.remove();
    }

    if (isSpawned()) {
      isSpawned = false;

      if(QuickShop.getInstance().getDisplayManager() instanceof DisplayEntityItemManager) {

        ((DisplayEntityItemManager) QuickShop.getInstance().getDisplayManager()).remove(this.chunkLocation, locationHash, this);
      }
    }
  }

  public void removeDisplay(final Player player) {

    if (player == null) {
      return;
    }

    if (itemDisplay != null && itemDisplay.isValid()) {
      player.hideEntity(QuickShop.getInstance().getJavaPlugin(), itemDisplay);
    }

    if (interactionEntity != null && interactionEntity.isValid()) {
      player.hideEntity(QuickShop.getInstance().getJavaPlugin(), interactionEntity);
    }

    if (textDisplay != null && textDisplay.isValid()) {
      player.hideEntity(QuickShop.getInstance().getJavaPlugin(), textDisplay);
    }
  }

  /**
   * Remove this shop's display in the whole world.(Not whole server)
   *
   * @return Success
   */
  @Override
  public boolean removeDupe() {

    return false;
  }

  /**
   * Respawn the displays, if it not exist, it will spawn new one.
   */
  @Override
  public void respawn() {

    remove(false);
    spawn();
  }

  /**
   * Add the protect flags for entity or entity's hand item. Target entity will got protect by
   * QuickShop
   *
   * @param entity Target entity
   */
  @Override
  public void safeGuard(@NotNull final Entity entity) {

  }

  /**
   * Spawn new Displays
   */
  @Override
  public void spawn() {

    Util.ensureThread(false);

    if(isSpawned || !shop.isLoaded()) {
      return;
    }
    if(new ShopDisplayItemSpawnEvent(shop, originalItemStack, DisplayType.DISPLAY_ENTITY).callCancellableEvent()) {
      Log.debug("Canceled the displayItem spawning because a plugin setCancelled the spawning event, usually this is a QuickShop Add on");
      return;
    }

    initEntities();
    //sendFakeItemToAll();
    QuickShop.folia().getScheduler().runAtLocationLater(getDisplayLocation(), task -> {
      sendFakeItemToAll();
    }, 1, TimeUnit.SECONDS);

    isSpawned = true;
  }

  public void sendFakeItemToPlayer(final Player player) {

    player.showEntity(QuickShop.getInstance().getJavaPlugin(), itemDisplay);
    player.showEntity(QuickShop.getInstance().getJavaPlugin(), interactionEntity);

    if(QuickShop.getInstance().getConfig().getBoolean("shop.text-display.enabled")) {
      player.showEntity(QuickShop.getInstance().getJavaPlugin(), textDisplay);
    }
  }

  public void sendFakeItemToAll() {

    for(final Player player : Bukkit.getOnlinePlayers()) {

      player.showEntity(QuickShop.getInstance().getJavaPlugin(), itemDisplay);
      player.showEntity(QuickShop.getInstance().getJavaPlugin(), interactionEntity);

      if(QuickShop.getInstance().getConfig().getBoolean("shop.text-display.enabled")) {
        player.showEntity(QuickShop.getInstance().getJavaPlugin(), textDisplay);
      }
    }
  }

  public int locationHash() {

    return locationHash;
  }
}
