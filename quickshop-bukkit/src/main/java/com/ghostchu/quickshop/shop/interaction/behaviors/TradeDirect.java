package com.ghostchu.quickshop.shop.interaction.behaviors;
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

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.QuickShopAPI;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.interaction.InteractionBehavior;
import com.ghostchu.quickshop.api.shop.interaction.InteractionClick;
import com.ghostchu.quickshop.api.shop.interaction.InteractionType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.ghostchu.quickshop.util.ShopUtil.buyFromShop;
import static com.ghostchu.quickshop.util.ShopUtil.sellToShop;

/**
 * TradeDirect
 *
 * @author creatorfromhell
 * @since 6.2.0.11
 */
public class TradeDirect implements InteractionBehavior {

  /**
   * Retrieves the identifier associated with this InteractionBehavior.
   *
   * @return the identifier as a String.
   */
  @Override
  public String identifier() {

    return "TRADE_DIRECT";
  }

  /**
   * Used to handle interactions that a player has with a shop.
   *
   * @param shop        The shop involved in the interaction, not null.
   * @param player      The player involved in the interaction, not null.
   * @param event       The PlayerInteractEvent that triggered the interaction, not null.
   * @param clickType   The type of click that triggered the interaction, not null.
   * @param interaction The type of interaction that occurred, can be null.
   */
  @Override
  public void handle(final @NotNull QuickShopAPI plugin, final @Nullable Shop shop, final @NotNull Player player, final @NotNull PlayerInteractEvent event, final @NotNull InteractionClick clickType, final @Nullable InteractionType interaction) {

    if(shop == null) {
      return;
    }

    if(shop.isFrozen()) {
      ((QuickShop)plugin).text().of(event.getPlayer(), "shop-cannot-trade-when-freezing").send();
      return;
    }

    if(shop.isBuying()) {
      if(sellToShop(event.getPlayer(), shop, true, false)) {
        event.setCancelled(true);
        event.setUseInteractedBlock(Event.Result.DENY);
        event.setUseItemInHand(Event.Result.DENY);
      }
      return;
    }

    if(shop.isSelling()) {

      if(buyFromShop(event.getPlayer(), shop, true, false)) {
        event.setCancelled(true);
        event.setUseInteractedBlock(Event.Result.DENY);
        event.setUseItemInHand(Event.Result.DENY);
      }
    }
  }

  @Override
  public void handle(final @NotNull QuickShopAPI plugin, final @Nullable Shop shop, final @NotNull Player player, final @NotNull PlayerInteractEntityEvent event, final @NotNull InteractionClick clickType, final @Nullable InteractionType interaction) {
    if(shop == null) return;

    //send control panel
    handle(plugin, event, shop, player);

    //cancel event stuff
    event.setCancelled(true);
  }

  @Override
  public void handle(final @NotNull QuickShopAPI plugin, final @Nullable Shop shop, final @NotNull Player player, final @NotNull EntityDamageByEntityEvent event, final @NotNull InteractionClick clickType, final @Nullable InteractionType interaction) {
    if(shop == null) return;

    //send control panel
    handle(plugin, event, shop, player);

    //cancel event stuff
    event.setCancelled(true);
  }

  private void handle(final @NotNull QuickShopAPI plugin, final Cancellable event, @NotNull final Shop shop, final @NotNull Player player) {

    if(shop.isFrozen()) {
      ((QuickShop)plugin).text().of(player, "shop-cannot-trade-when-freezing").send();
      return;
    }

    if(shop.isBuying()) {
      if(sellToShop(player, shop, true, false)) {
        event.setCancelled(true);
      }
      return;
    }

    if(shop.isSelling()) {

      if(buyFromShop(player, shop, true, false)) {
        event.setCancelled(true);
      }
    }
  }
}
