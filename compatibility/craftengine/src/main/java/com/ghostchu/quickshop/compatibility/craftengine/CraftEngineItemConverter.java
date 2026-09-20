package com.ghostchu.quickshop.compatibility.craftengine;

/*
 * QuickShop-Hikari
 * Copyright (C) 2025 Daniel "creatorfromhell" Vidmar
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

import net.momirealms.craftengine.bukkit.api.BukkitAdaptor;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.core.item.network.ItemPacketSource;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;
import java.util.logging.Logger;

/**
 * CraftEngineItemConverter
 *
 * <p>CraftEngine does not always store the client visible model of a custom item on the server side
 * item stack. It converts the item stack while it is written into an outgoing packet, based on the
 * client version of the receiving player. Packets which are built by other plugins bypass that
 * conversion, which makes a shop display lose the custom item model (the plain underlying material,
 * or a missing model, is shown instead).</p>
 *
 * <p>This converter asks CraftEngine for the exact item which has to be written into the packet for
 * the given player, using the same API CraftEngine uses for its own packets.</p>
 *
 * @author YuanYuanOwO
 * @since 6.3.0.3
 */
final class CraftEngineItemConverter {

  private final Logger logger;
  private boolean failureLogged;
  private boolean activeLogged;

  CraftEngineItemConverter(final Logger logger) {

    this.logger = logger;
  }

  /**
   * Builds the item which has to be written into the packet for the given player.
   *
   * @param player    the player which will receive the packet
   * @param itemStack the server side item stack of the shop display
   *
   * @return the client bound item, or the very same instance when no conversion happened
   */
  ItemStack toClientItem(final Player player, final ItemStack itemStack) {

    try {

      final net.momirealms.craftengine.core.entity.player.Player craftEnginePlayer = BukkitAdaptor.adapt(player);
      if(craftEnginePlayer == null) {
        //The player is not known to CraftEngine yet, keep the server side item stack
        return itemStack;
      }
      //The display item is sent as entity data, so let CraftEngine build the matching client bound item
      final Optional<ItemStack> clientBound = BukkitItemManager.instance()
              .s2c(itemStack, craftEnginePlayer, ItemPacketSource.ENTITY_DATA);
      if(clientBound.isEmpty()) {
        //Not a CraftEngine item, or nothing to convert for this client, keep the server side item stack
        return itemStack;
      }
      if(!this.activeLogged) {

        this.activeLogged = true;
        this.logger.info("CraftEngine client bound item conversion is active for shop display items.");
      }
      return clientBound.get();
    } catch(final Throwable throwable) {

      //Never break the shop display because CraftEngine changed its API, just fall back to the raw item
      if(!this.failureLogged) {

        this.failureLogged = true;
        this.logger.warning("Failed to build the CraftEngine client bound item, "
                                    + "the shop display item will show the server side item: " + throwable);
      }
      return itemStack;
    }
  }
}
