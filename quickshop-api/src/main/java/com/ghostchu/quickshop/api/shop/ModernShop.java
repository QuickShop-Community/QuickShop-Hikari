package com.ghostchu.quickshop.api.shop;

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

import com.ghostchu.quickshop.api.shop.components.ShopInteraction;
import com.ghostchu.quickshop.api.shop.components.ShopItem;
import com.ghostchu.quickshop.api.shop.components.ShopLifecycle;
import com.ghostchu.quickshop.api.shop.components.ShopMeta;
import com.ghostchu.quickshop.api.shop.components.ShopPermission;
import com.ghostchu.quickshop.api.shop.components.ShopPrice;
import com.ghostchu.quickshop.api.shop.components.ShopTrading;
import com.ghostchu.quickshop.api.shop.components.ShopWorldAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * ShopModern
 * T = price object, such as BigDecimal or Double
 * S = Location object such as Bukkit's Location object
 * U = Player object such as Bukkit's Player object
 * V = Preview object such as Bukkit's Inventory object
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public interface ModernShop<T, S, U, V> extends Locatable<S> {

  /**
   * WARNING: This UUID will changed after plugin reload, shop reload or server restart DO NOT USE
   * IT TO STORE DATA!
   *
   * @return Random UUID
   */
  @NotNull
  UUID getRuntimeRandomUniqueId();

  ShopItem item();

  ShopInteraction<U, V> interaction();

  ShopLifecycle lifecycle();

  ShopMeta meta();

  ShopPermission permission();

  ShopPrice<T> price();

  ShopTrading trading();

  //This may need to be moved to the manager not entirely sold on it being in the shop object.
  ShopWorldAdapter worldAdapter();
}