package com.ghostchu.quickshop.menu.shared;
/*
 * QuickShop-Hikari
 * Copyright (C) 2024 Daniel "creatorfromhell" Vidmar
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
import net.kyori.adventure.text.Component;
import net.tnemc.menu.core.Menu;

import java.util.List;
import java.util.UUID;

/**
 * QuickshopMenu
 *
 * @author creatorfromhell
 * @since 6.2.0.8
 */
public class QuickShopMenu extends Menu {

  /**
   * Retrieves the legacy string representation of the text associated with a given route and player.
   *
   * @param player The UUID of the player.
   * @param route The route to retrieve the text.
   * @return The legacy string representation of the text associated with the route and player.
   */
  protected String legacy(final UUID player, final String route) {
    return QuickShop.getInstance().text().of(player, route).legacy();
  }

  /**
   * Retrieves a component for a given route and player.
   *
   * @param player The UUID of the player.
   * @param route The route to retrieve the component.
   * @return The component associated with the route and player.
   */
  protected Component get(final UUID player, final String route) {
    return QuickShop.getInstance().text().of(player, route).forLocale();
  }

  /**
   * Retrieves a list of components for a given route and player.
   *
   * @param player The UUID of the player.
   * @param route The route to retrieve the list of components.
   * @return A list of components associated with the route and player.
   */
  protected List<Component> getList(final UUID player, final String route) {
    return QuickShop.getInstance().text().ofList(player, route).forLocale();
  }
}