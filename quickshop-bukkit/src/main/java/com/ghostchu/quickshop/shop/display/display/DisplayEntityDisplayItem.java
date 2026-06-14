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
import com.ghostchu.quickshop.api.shop.display.DisplayType;
import com.ghostchu.quickshop.shop.display.AbstractDisplayItem;
import com.ghostchu.quickshop.util.EntityUtil;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.simplereloadlib.Reloadable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * DisplayEntityDisplayItem
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public class DisplayEntityDisplayItem extends AbstractDisplayItem implements Reloadable {

  private final Set<UUID> viewerUUIDS = new ConcurrentSkipListSet<>();

  private final ItemDisplay itemDisplay;
  private final TextDisplay textDisplay;

  private boolean isSpawned = false;

  //I don't think this needs to be protected.
  public DisplayEntityDisplayItem(final Shop shop) {

    super(shop);

    itemDisplay = EntityUtil.spawnDisplayItemFor(null, getDisplayLocation().clone().add(0, .15d, 0), shop.getItem(), new Vector3f(1.25f, 1.25f, 1.25f), Bukkit.getViewDistance() * 16, 0);

    final int blockDistance = QuickShop.getInstance().getConfig().getInt("shop.text-display.range-blocks", 8);

    final Vector3f scaleVector = new Vector3f(QuickShop.getInstance().getConfig().getFloat("shop.text-display.scale.x", 1.0f),
                                              QuickShop.getInstance().getConfig().getFloat("shop.text-display.scale.y", 1.0f),
                                              QuickShop.getInstance().getConfig().getFloat("shop.text-display.scale.z", 1.0f));

    final Location textLocation = getDisplayLocation().clone().add(0, QuickShop.getInstance().getConfig().getDouble("shop.text-display.y-offset", 0.8), 0);

    textDisplay = EntityUtil.spawnDisplayTextFor(null, textLocation, Util.getTextDisplay(shop, shop.getItem().clone()), scaleVector, blockDistance, TextDisplay.TextAlignment.CENTER, 0);
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

    if (itemDisplay == null && textDisplay == null) {
      return;
    }

    if (itemDisplay != null && !itemDisplay.isValid() && textDisplay != null && !textDisplay.isValid()) {

      return;
    }

    for (final Player player : Bukkit.getOnlinePlayers()) {

      removeDisplay(player);
    }

    if (isSpawned()) {

      isSpawned = false;
    }
  }

  public void removeDisplay(final Player player) {

    if (player == null) {
      return;
    }

    if (itemDisplay != null && itemDisplay.isValid()) {
      player.hideEntity(QuickShop.getInstance().getJavaPlugin(), itemDisplay);
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
    if(new ShopDisplayItemSpawnEvent(shop, originalItemStack, DisplayType.VIRTUALITEM).callCancellableEvent()) {
      Log.debug("Canceled the displayItem spawning because a plugin setCancelled the spawning event, usually this is a QuickShop Add on");
      return;
    }
    sendFakeItemToAll();

    isSpawned = true;
  }

  public void sendFakeItemToPlayer(final Player player) {

    player.showEntity(QuickShop.getInstance().getJavaPlugin(), itemDisplay);

    if(QuickShop.getInstance().getConfig().getBoolean("shop.text-display.enabled")) {
      player.showEntity(QuickShop.getInstance().getJavaPlugin(), textDisplay);
    }
  }

  public void sendFakeItemToAll() {

    for(final Player player : Bukkit.getOnlinePlayers()) {
      player.showEntity(QuickShop.getInstance().getJavaPlugin(), itemDisplay);

      if(QuickShop.getInstance().getConfig().getBoolean("shop.text-display.enabled")) {
        player.showEntity(QuickShop.getInstance().getJavaPlugin(), textDisplay);
      }
    }
  }
}
