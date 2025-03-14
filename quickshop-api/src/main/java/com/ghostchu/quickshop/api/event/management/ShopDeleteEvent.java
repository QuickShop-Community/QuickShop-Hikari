package com.ghostchu.quickshop.api.event.management;
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

import com.ghostchu.quickshop.api.shop.Shop;
import org.jetbrains.annotations.NotNull;

/**
 * ShopDeleteEvent is a class representing an event that occurs when a shop is deleted.
 *
 * @author creatorfromhell
 * @since 6.2.0.9
 */
public class ShopDeleteEvent extends ShopEvent {

  protected final boolean memory;

  public ShopDeleteEvent(final @NotNull Shop shop, final boolean memory) {

    super(shop);

    this.memory = memory;
  }

  public boolean memory() {

    return memory;
  }
}