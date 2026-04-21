package com.ghostchu.quickshop.api.shop.trading;

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
import com.ghostchu.quickshop.api.shop.Shop;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

/**
 * TradeService
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public interface TradeService {

  @NotNull
  TradeResult executeBuyFromShop(@NotNull Shop shop,
                                 @NotNull QUser buyer,
                                 @NotNull InventoryWrapper buyerInventory,
                                 @NotNull Location dropLocation,
                                 int amount);

  @NotNull
  TradeResult executeBuyFromShop(@NotNull Shop shop,
                                 @NotNull QUser buyer,
                                 @NotNull InventoryWrapper buyerInventory,
                                 @NotNull Location dropLocation,
                                 int amount,
                                 @NotNull TradeOptions options);

  @NotNull
  TradeResult executeSellToShop(@NotNull Shop shop,
                                @NotNull QUser seller,
                                @NotNull InventoryWrapper sellerInventory,
                                @NotNull Location dropLocation,
                                int amount);

  @NotNull
  TradeResult executeSellToShop(@NotNull Shop shop,
                                @NotNull QUser seller,
                                @NotNull InventoryWrapper sellerInventory,
                                @NotNull Location dropLocation,
                                int amount,
                                @NotNull TradeOptions options);

  @NotNull
  TradePreview previewBuyFromShop(@NotNull Shop shop,
                                  @NotNull QUser buyer,
                                  @NotNull InventoryWrapper buyerInventory,
                                  int amount);

  @NotNull
  TradePreview previewSellToShop(@NotNull Shop shop,
                                 @NotNull QUser seller,
                                 @NotNull InventoryWrapper sellerInventory,
                                 int amount);
}