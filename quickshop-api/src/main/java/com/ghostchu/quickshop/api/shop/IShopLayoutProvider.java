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

import com.ghostchu.quickshop.api.localization.text.ProxiedLocale;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

/**
 * IShopLayout
 *
 * @author creatorfromhell
 * @since 6.2.0.11
 */
public interface IShopLayoutProvider {

  /**
   * Generates a layout template for the specified shop.
   *
   * @param shop the shop instance for which the layout template is to be generated
   * @return a LinkedList of Strings representing the layout template of the shop
   */
  LinkedList<String> layoutTemplate(final Shop shop);

  /**
   * Creates a snapshot of a shop sign that encapsulates the visual and structural data required
   * to render the sign, based on the specified shop and locale.
   *
   * @param shop the shop instance for which the sign snapshot is to be created
   * @param locale the locale to be used for formatting and rendering the snapshot
   * @return a CompletableFuture that completes with a SignRenderSnapshot, representing
   *         the state of the shop sign
   */
  CompletableFuture<SignRenderSnapshot> createSignSnapshot(@NotNull Shop shop, @NotNull final ProxiedLocale locale);

  /**
   * Renders a snapshot of a shop sign into a list of components based on the provided locale.
   *
   * @param snapshot the snapshot representing the current state of the shop sign to be rendered
   * @param locale the locale to be used for rendering the components
   * @return a list of components representing the rendered shop sign snapshot
   */
  default List<Component> renderSnapshot(final SignRenderSnapshot snapshot, final ProxiedLocale locale) {

    final List<Component> rendered = new ArrayList<>(4);

    for (int i = 0; i < 4; i++) {
      if (snapshot.template().size() <= i || snapshot.template().get(i).isBlank()) {
        rendered.add(Component.empty());
        continue;
      }

      rendered.add(switch (snapshot.template().get(i).toLowerCase(Locale.ROOT)) {
        case "header" -> renderHeaderSnapshot(snapshot, locale);
        case "trading" -> renderTradingSnapshot(snapshot, locale);
        case "item" -> snapshot.itemName();
        case "price" -> renderPriceSnapshot(snapshot, locale);
        case "level" -> snapshot.levelLine();
        default -> Component.empty();
      });
    }

    return rendered;
  }

  /**
   * Renders the header section of a shop sign snapshot into a component based on the provided locale.
   *
   * @param snapshot the snapshot representing the current state of the shop sign's header to be rendered
   * @param locale the locale to be used for rendering the header component
   * @return a component representing the rendered header section of the shop sign snapshot
   */
  Component renderHeaderSnapshot(final SignRenderSnapshot snapshot, final ProxiedLocale locale);

  /**
   * Renders the trading section of a shop sign snapshot into a component based on the provided locale.
   *
   * @param snapshot the snapshot representing the current state of the shop sign's trading section to be rendered
   * @param locale the locale to be used for rendering the trading component
   * @return a component representing the rendered trading section of the shop sign snapshot
   */
  Component renderTradingSnapshot(final SignRenderSnapshot snapshot, final ProxiedLocale locale);

  /**
   * Renders the price section of a shop sign snapshot into a component based on the provided locale.
   *
   * @param snapshot the snapshot representing the current state of the shop sign to be rendered
   * @param locale the locale to be used for rendering the price component
   * @return a component representing the rendered price section of the shop sign snapshot
   */
  Component renderPriceSnapshot(final SignRenderSnapshot snapshot, final ProxiedLocale locale);

  /**
   * Renders the layout of the specified shop into a list of components based on the provided locale.
   *
   * @param shop the shop instance for which the components are to be rendered
   * @param locale the locale to be used for rendering the components
   * @return a LinkedList of components representing the visual layout of the shop
   */
  LinkedList<Component> render(@NotNull final Shop shop, @NotNull final ProxiedLocale locale);

  /**
   * Renders the header section of the specified shop into a component.
   *
   * @param shop the shop instance for which the header component is to be rendered
   * @param locale the locale to be used for rendering the header component
   * @return a component representing the visual header of the shop
   */
  Component renderHeader(@NotNull final Shop shop, @NotNull final ProxiedLocale locale);

  /**
   * Renders the trading section of the specified shop into a component.
   *
   * @param shop the shop instance for which the trading component is to be rendered
   * @param locale the locale to be used for rendering the trading component
   * @return a component representing the visual trading section of the shop
   */
  Component renderTrading(@NotNull final Shop shop, @NotNull final ProxiedLocale locale);

  /**
   * Renders an item section of the specified shop into a component based on the provided locale.
   *
   * @param shop the shop instance for which the item component is to be rendered
   * @param locale the locale to be used for rendering the item component
   * @return a component representing the visual item section of the shop
   */
  Component renderItem(@NotNull final Shop shop, @NotNull final ProxiedLocale locale);

  /**
   * Renders the price section of the specified shop into a component based on the provided locale.
   *
   * @param shop the shop instance for which the price component is to be rendered
   * @param locale the locale to be used for rendering the price component
   * @return a component representing the visual price section of the shop
   */
  Component renderPrice(@NotNull final Shop shop, @NotNull final ProxiedLocale locale);

  /**
   * Renders the enchantment level for books, or the duration for potions into a component based on the provided locale.
   *
   * @param shop the shop instance for which the level component is to be rendered
   * @param locale the locale to be used for rendering the level component
   * @return a component representing the visual level section of the shop
   */
  Component renderLevels(final @NotNull Shop shop, final @NotNull ProxiedLocale locale);
}