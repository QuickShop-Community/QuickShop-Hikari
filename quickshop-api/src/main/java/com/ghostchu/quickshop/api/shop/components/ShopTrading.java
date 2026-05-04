package com.ghostchu.quickshop.api.shop.components;

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

import com.ghostchu.quickshop.api.inventory.InventoryWrapper;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.trading.TradeResult;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

/**
 * ShopTrading
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public interface ShopTrading {

  /**
   * Get shop is or not in buying mode
   *
   * @return yes or no
   * @deprecated Use the #shopType() method instead.
   */
  @Deprecated(forRemoval = true, since = "6.3.0.0")
  boolean isBuying();

  /**
   * Get shop is frozen or not
   *
   * @return yes or no
   * @deprecated Use the #shopState() method instead.
   */
  @Deprecated(forRemoval = true, since = "6.3.0.0")
  boolean isFrozen();

  /**
   * Get shop is or not in selling mode
   *
   * @return yes or no
   * @deprecated Use the #shopType() method instead.
   */
  @Deprecated(forRemoval = true, since = "6.3.0.0")
  boolean isSelling();

  /**
   * Execute buy action for player with x items.
   *
   * @param buyer          The player buying
   * @param buyerInventory The buyer inventory ( may not a player inventory )
   * @param loc2Drop       The location to drops items if player inventory are full
   * @param amount         The amount to buy
   *
   * @throws Exception Possible exception thrown if anything wrong.
   */
  TradeResult buy(@NotNull QUser buyer, @NotNull InventoryWrapper buyerInventory,
                  @NotNull Location loc2Drop, int amount);

  /**
   * Execute sell action for player with x items.
   *
   * @param seller          Seller
   * @param sellerInventory Seller's inventory ( may not a player inventory )
   * @param loc2Drop        The location to be drop if buyer inventory full ( if player enter a
   *                        number that < 0, it will turn to buying item)
   * @param amount          The amount to sell
   *
   * @throws Exception Possible exception thrown if anything wrong.
   */
  TradeResult sell(@NotNull QUser seller, @NotNull InventoryWrapper sellerInventory,
                   @NotNull Location loc2Drop, int amount);
}