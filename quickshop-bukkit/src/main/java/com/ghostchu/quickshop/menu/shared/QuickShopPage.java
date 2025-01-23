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
import com.ghostchu.quickshop.api.shop.Shop;
import net.kyori.adventure.text.Component;
import net.tnemc.menu.core.Page;
import net.tnemc.menu.core.viewer.MenuViewer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.ghostchu.quickshop.menu.ShopKeeperMenu.SHOP_DATA_ID;

/**
 * QuickShopPage
 *
 * @author creatorfromhell
 * @since 6.2.0.8
 */
public class QuickShopPage extends Page {

  public QuickShopPage(final int pageNumber) {

    super(pageNumber);
  }

  public static Optional<OfflinePlayer> getPlayer(final UUID uuid) {

    return Optional.of(Bukkit.getOfflinePlayer(uuid));
  }

  public static Optional<Shop> getShop(final MenuViewer player) {

    final Optional<Object> idObj = player.findData(SHOP_DATA_ID);
    if(idObj.isPresent()) {

      final Long shopId = (Long)idObj.get();
      return Optional.ofNullable(QuickShop.getInstance().getShopManager().getShop(shopId));
    }
    return Optional.empty();
  }

  /**
   * Retrieves the legacy string representation of the text associated with a given route and
   * player.
   *
   * @param player The UUID of the player.
   * @param route  The route to retrieve the text.
   *
   * @return The legacy string representation of the text associated with the route and player.
   */
  public static String legacy(final UUID player, final String route, @Nullable final Object... args) {

    return QuickShop.getInstance().text().of(player, route, args).legacy();
  }

  /**
   * Retrieves a component for a given route and player.
   *
   * @param player The UUID of the player.
   * @param route  The route to retrieve the component.
   *
   * @return The component associated with the route and player.
   */
  public static Component get(final UUID player, final String route, @Nullable final Object... args) {

    return QuickShop.getInstance().text().of(player, route, args).forLocale();
  }

  /**
   * Retrieves a list of components for a given route and player.
   *
   * @param player The UUID of the player.
   * @param route  The route to retrieve the list of components.
   *
   * @return A list of components associated with the route and player.
   */
  public static List<Component> getList(final UUID player, final String route, @Nullable final Object... args) {

    return QuickShop.getInstance().text().ofList(player, route, args).forLocale();
  }
}