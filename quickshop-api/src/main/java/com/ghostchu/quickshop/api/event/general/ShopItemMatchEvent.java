package com.ghostchu.quickshop.api.event.general;
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

import com.ghostchu.quickshop.api.event.AbstractQSEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * ShopItemMatchEvent
 *
 * @author creatorfromhell
 * @since 6.2.0.10
 */
public class ShopItemMatchEvent extends AbstractQSEvent {

  protected boolean matches = false;

  protected final ItemStack original;
  protected final ItemStack comparison;

  public ShopItemMatchEvent(@Nullable final ItemStack original, @Nullable final ItemStack comparison) {

    this.original = original;
    this.comparison = comparison;
  }

  public boolean matches() {

    return matches;
  }

  public void matches(final boolean matches) {

    this.matches = matches;
  }

  public ItemStack comparison() {

    return comparison;
  }

  public ItemStack original() {

    return original;
  }
}