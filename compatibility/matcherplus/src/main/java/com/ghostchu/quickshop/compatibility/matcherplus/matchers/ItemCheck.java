package com.ghostchu.quickshop.compatibility.matcherplus.matchers;
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

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * ItemCheck
 *
 * @author creatorfromhell
 * @since 6.2.0.10
 */
public interface ItemCheck {

  /**
   * Check if this check applies to the specified ItemStack
   *
   * @param stack the ItemStack to check
   * @return true if the check applies to the ItemStack, otherwise false
   */
  boolean applies(final @Nullable ItemStack stack);

  /**
   * Checks if two ItemStack objects match each other.
   *
   * @param stack the first ItemStack to compare
   * @param compare the second ItemStack to compare
   * @return true if the two ItemStack objects match, false otherwise
   */
  boolean matches(final @Nullable ItemStack stack, final @Nullable ItemStack compare);
}