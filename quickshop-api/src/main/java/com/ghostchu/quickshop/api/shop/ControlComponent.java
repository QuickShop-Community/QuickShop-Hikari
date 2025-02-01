package com.ghostchu.quickshop.api.shop;
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

import com.ghostchu.quickshop.api.QuickShopAPI;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * ControlComponent
 *
 * @author creatorfromhell
 * @since 6.2.0.9
 */
public interface ControlComponent {

  /**
   * Retrieves an identifier.
   *
   * @return A string representing the identifier.
   */
  String identifier();

  /**
   * Checks if the provided Player can interact with the given Shop.
   *
   * @param sender The Player object requesting interaction.
   * @param shop The Shop object to check interaction with.
   * @return True if the Player can interact with the Shop, false otherwise.
   */
  boolean applies(final @NotNull QuickShopAPI plugin, @NotNull final Player sender, @NotNull final Shop shop);

  /**
   * Generates a Component based on the player and shop provided.
   *
   * @param sender The Player object representing who triggered the generation.
   * @param shop The Shop object representing the shop for which components are being generated.
   * @return A Component object based on the provided player and shop.
   */
  Component generate(final @NotNull QuickShopAPI plugin, @NotNull final Player sender, @NotNull final Shop shop);
}