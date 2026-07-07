package com.ghostchu.quickshop.api.shop.layout;
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
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.SignRenderSnapshot;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * RenderComponent
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public interface RenderComponent {

  /**
   * Returns the placeholder string associated with the render component.
   *
   * @return the placeholder string
   */
  String placeholder();

  /**
   * Determines if the given line contains the placeholder string
   * associated with the render component.
   *
   * @param line the line to check against the placeholder, must not be null
   * @return {@code true} if the line contains the placeholder, {@code false} otherwise
   */
  default boolean appliesTo(@NotNull final String line) {

    return line.contains(placeholder());
  }

  /**
   * Determines if this render component is a full line replacement.
   * If it is a full line replacement, no other renderers will be run
   * on the same line, and the rendering process will skip to the next line.
   *
   * @return {@code true} if this is a full line replacement, {@code false} otherwise.
   */
  boolean fullLine();

  /**
   * Determines if this render component supports snapshot rendering.
   *
   * @return {@code true} if snapshot rendering is supported, {@code false} otherwise.
   */
  boolean supportsSnapshot();

  /**
   * Renders the snapshot of the current state of the {@link SignRenderSnapshot}.
   * This method is responsible for rendering a visual representation of the
   * shop based on the details provided in the snapshot.
   *
   * @param snapshot the {@link SignRenderSnapshot} containing all the data required
   *             to render a representation of the shop, must not be null
   * @param locale the locale to be used for rendering the components
   * @return a {@link Component} representing the rendered snapshot output
   */
  Component renderSnapshot(@NotNull final SignRenderSnapshot snapshot, final ProxiedLocale locale);

  /**
   * Renders a component based on the provided shop and item data.
   *
   * @param shop the shop instance containing all relevant data needed for rendering, must not be null
   * @param item the item stack representing the item associated with the shop, must not be null
   * @param locale the locale to be used for rendering the components, defining language and formatting settings
   * @return a {@link Component} representing the rendered output based on the provided shop and item data
   */
  Component render(@NotNull final Shop shop, @NotNull final ItemStack item, final ProxiedLocale locale);
}