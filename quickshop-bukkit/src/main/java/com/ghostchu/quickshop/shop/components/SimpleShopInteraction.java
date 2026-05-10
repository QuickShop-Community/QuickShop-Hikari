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
import com.ghostchu.quickshop.api.event.Phase;
import com.ghostchu.quickshop.api.event.management.ShopClickEvent;
import com.ghostchu.quickshop.api.inventory.InventoryWrapper;
import com.ghostchu.quickshop.api.shop.ModernShop;
import com.ghostchu.quickshop.api.shop.components.ShopInteraction;
import com.ghostchu.quickshop.obj.QUserImpl;
import com.ghostchu.quickshop.shop.InventoryPreview;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import lombok.EqualsAndHashCode;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

/**
 * SimpleShopIteraction
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public class SimpleShopInteraction implements ShopInteraction<Player, InventoryPreview> {

  @EqualsAndHashCode.Exclude
  private final ModernShop<?, ?, ?, ?> shop;
  @EqualsAndHashCode.Exclude
  private InventoryPreview inventoryPreview = null;

  public SimpleShopInteraction(@NotNull final ModernShop<?, ?, ?, ?> shop) {
    this.shop = shop;
  }

  /**
   * Handles the action triggered when a player interacts with an element in the shop.
   *
   * @param player the player object interacting with the shop
   */
  @Override
  public void onClick(@NonNull final Player player) {

    Util.ensureThread(false);
    ShopClickEvent event = new ShopClickEvent(this.shop, QUserImpl.createFullFilled(player));
    event.callEvent();

    event = event.clone(Phase.MAIN);
    if(event.callCancellableEvent()) {

      Log.debug("Ignore shop click, because some plugin cancelled it.");
      return;
    }

    //setSignText(plugin.getTextManager().findRelativeLanguages(clicker));
    // not sure if we need the above

    event = event.clone(Phase.POST);
    event.callEvent();
  }

  @Override
  public InventoryPreview preview() {

    return inventoryPreview;
  }

  /**
   * @return The chest this shop is based on.
   */
  @Override
  public @Nullable InventoryWrapper getInventory() {

    return null;
  }

  /**
   * Opens a preview for the specified player. This is typically used to display shop items to the
   * player in a preview interface.
   *
   * @param player the player object for whom the preview will be displayed; must not be null
   */
  @Override
  public void openPreview(@NonNull final Player player) {

    if(inventoryPreview == null) {
      inventoryPreview = new InventoryPreview(QuickShop.getInstance(),
                                              shop.item().getItem().clone(),
                                              player.getLocale());
    }
    inventoryPreview.show(player);
  }
}
