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
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;

/**
 * DisplayEntityItemManager
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public class DisplayEntityItemManager {

  private final ConcurrentHashMap<Long, DisplayEntityDisplayItem> displayItems = new ConcurrentHashMap<>();

  private static DisplayEntityItemManager instance;

  public DisplayEntityItemManager() {

    instance = this;
  }

  public static DisplayEntityItemManager instance() {
    return instance;
  }

  public DisplayEntityDisplayItem create(@NotNull final Shop shop) {

    if (displayItems.containsKey(shop.getShopId())) {

      return displayItems.get(shop.getShopId());
    }

    final DisplayEntityDisplayItem displayEntityDisplayItem = new DisplayEntityDisplayItem(shop);
    displayItems.put(shop.getShopId(), displayEntityDisplayItem);
    return displayEntityDisplayItem;
  }

  public void addPlayer(final Player player) {

    displayItems.values().forEach(displayEntityDisplayItem -> displayEntityDisplayItem.sendFakeItemToPlayer(player));
  }
}