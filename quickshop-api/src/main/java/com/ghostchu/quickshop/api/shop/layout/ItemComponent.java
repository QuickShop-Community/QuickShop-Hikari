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

import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * ItemComponent
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public interface ItemComponent {

  /**
   * Determines whether the component is enabled.
   *
   * @return {@code true} if the component is enabled, {@code false} otherwise.
   */
  boolean enabled();

  /**
   * Determines whether the given {@link ItemStack} is applicable to this component.
   *
   * @param item the {@link ItemStack} to be checked, must not be null
   * @return {@code true} if the item is applicable, {@code false} otherwise
   */
  boolean appliesTo(@NotNull final ItemStack item);

  /**
   * Determines if the given {@link RenderComponent} and {@link ItemStack} are applicable to this component.
   *
   * @param renderComponent the {@link RenderComponent} to be evaluated, must not be null
   * @param item the {@link ItemStack} to check applicability for, must not be null
   * @return {@code true} if the render component and item stack are applicable, {@code false} otherwise
   */
  boolean appliesTo(@NotNull final RenderComponent renderComponent, final @NotNull ItemStack item);

  /**
   * Renders a visual representation of the provided {@link ItemStack}.
   *
   * @param item the {@link ItemStack} to be rendered, must not be null
   * @return a {@link Component} representing the rendered output of the given {@link ItemStack}
   */
  Component renderName(@NotNull final ItemStack item);

  /**
   * Renders a visual representation of the provided {@link RenderComponent} and {@link ItemStack}.
   *
   * @param renderComponent the {@link RenderComponent} to be used for generating the rendered output,
   *                        must not be null
   * @param item the {@link ItemStack} associated with the rendering process,
   *             must not be null
   * @return a {@link Component} representing the rendered output based on the given render component and item stack
   */
  Component renderFor(@NotNull final RenderComponent renderComponent, final @NotNull ItemStack item);
}