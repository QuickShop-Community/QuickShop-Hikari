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
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopChunk;
import com.ghostchu.quickshop.api.shop.display.DisplayManager;
import com.ghostchu.quickshop.listener.InteractionEntityListener;
import com.ghostchu.quickshop.shop.SimpleShopChunk;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * DisplayEntityItemManager
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public class DisplayEntityItemManager implements DisplayManager<DisplayEntityDisplayItem> {

  public static final String DISPLAY_ITEM_KEY = "qs-display-interaction";

  public static final NamespacedKey DISPLAY_ITEM_KEY_INSTANCE = new NamespacedKey(QuickShop.getInstance().getJavaPlugin(), DISPLAY_ITEM_KEY);

  private final ConcurrentHashMap<ShopChunk, ConcurrentHashMap<Integer, DisplayEntityDisplayItem>> chunksMapping = new ConcurrentHashMap<>();

  private static DisplayEntityItemManager instance;

  public DisplayEntityItemManager() {

    instance = this;

    QuickShop.getInstance().getJavaPlugin().getServer().getPluginManager().registerEvents(new InteractionEntityListener(QuickShop.getInstance()), QuickShop.getInstance().getJavaPlugin());
  }

  public static DisplayEntityItemManager instance() {
    return instance;
  }

  /**
   * Retrieves a mapping between ShopChunk objects and their associated lists of display entities.
   *
   * This method provides access to the internal data structure used by the display manager to store
   * and organize displays according to their respective ShopChunk locations. The returned mapping
   * allows efficient retrieval, addition, or removal of display entities based on chunk-specific
   * contexts.
   *
   * @return A {@code ConcurrentHashMap} where the keys represent {@code ShopChunk} objects, and the
   * values are lists of display entities of type {@code T} associated with the respective chunks.
   */
  @Override
  public ConcurrentHashMap<ShopChunk, ConcurrentHashMap<Integer, DisplayEntityDisplayItem>> chunksMapping() {

    return chunksMapping;
  }

  /**
   * Loads the required resources, configurations, or internal states for this display manager. This
   * method prepares the manager for operation, ensuring it is in a ready state to handle
   * display-related actions, such as creating or managing displays.
   *
   * Typically invoked during the initialization process or when reloading the manager.
   */
  @Override
  public void load() {

  }

  /**
   * Unloads the resources, configurations, or internal states for this display manager. This method
   * is typically used to clean up or release resources when the display manager is no longer needed
   * or is being shut down.
   *
   * Invoking this method ensures that the manager releases any held resources properly and avoids
   * potential memory leaks or unintended behavior.
   */
  @Override
  public void unload() {

  }

  public DisplayEntityDisplayItem create(@NotNull final Shop shop) {

    final ShopChunk chunk = SimpleShopChunk.fromLocation(shop.bukkitLocation());

    //output if display is in chunks map

    return chunksMapping.computeIfAbsent(chunk, k -> new ConcurrentHashMap<>())
            .computeIfAbsent(shop.bukkitLocation().hashCode(),
                             k -> new DisplayEntityDisplayItem(shop, chunk));
  }

  /*
  public void addPlayer(final Player player) {

    System.out.println("add player");

    final ShopChunk chunk = SimpleShopChunk.fromLocation(player.getLocation());
    chunksMapping.computeIfPresent(chunk, (k, v) -> {
      v.values().forEach(display -> {

        QuickShop.folia().getScheduler().runAtLocationLater(display.getDisplayLocation(), task -> {
          display.sendFakeItemToPlayer(player);
        }, 1, TimeUnit.SECONDS);
      });
      return v;
    });
  }*/

  public void addPlayer(final Player player) {

    final Location playerLocation = player.getLocation();
    final World world = playerLocation.getWorld();

    if (world == null) {
      return;
    }

    final int centerChunkX = playerLocation.getBlockX() >> 4;
    final int centerChunkZ = playerLocation.getBlockZ() >> 4;
    final int chunkRadius = 8;

    for (int offsetX = -chunkRadius; offsetX <= chunkRadius; offsetX++) {
      for (int offsetZ = -chunkRadius; offsetZ <= chunkRadius; offsetZ++) {

        final int chunkX = centerChunkX + offsetX;
        final int chunkZ = centerChunkZ + offsetZ;

        final Location chunkLocation = new Location(world, chunkX << 4, playerLocation.getY(), chunkZ << 4);

        final ShopChunk chunk = SimpleShopChunk.fromLocation(chunkLocation);
        final var displays = chunksMapping.get(chunk);

        if (displays == null) {
          continue;
        }

        displays.values().forEach(display ->QuickShop.folia().getScheduler().runAtLocationLater(
                display.getDisplayLocation(), task -> {
                    if (player.isOnline() && player.getWorld().equals(world)) {
                        display.sendFakeItemToPlayer(player);
                    }
                }, 1, TimeUnit.SECONDS));
      }
    }
  }
}