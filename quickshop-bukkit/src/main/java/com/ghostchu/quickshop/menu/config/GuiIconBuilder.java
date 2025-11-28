package com.ghostchu.quickshop.menu.config;
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
import net.tnemc.item.AbstractItemStack;
import net.tnemc.menu.core.builder.IconBuilder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * GuiIconBuilder - Helper class to build icons from configuration
 *
 * @author creatorfromhell
 * @since 6.2.0.11
 */
public class GuiIconBuilder {

  /**
   * Creates an AbstractItemStack from IconConfig
   *
   * @param config The icon configuration
   * @return The configured AbstractItemStack
   */
  @NotNull
  public static AbstractItemStack<?> createStack(@NotNull final GuiConfig.IconConfig config) {
    AbstractItemStack<?> stack = QuickShop.getInstance().stack()
            .of(config.getMaterial(), config.getAmount());

    // Set display name if configured
    final String name = config.getName();
    if(name != null && !name.isEmpty()) {
      if(name.equals(" ")) {
        // Empty name (single space = blank display name)
        stack = stack.display(Component.empty());
      } else {
        // Direct inline MiniMessage text (e.g., "<bold><green>Buy Items</green></bold>")
        stack = stack.display(parseMiniMessage(name));
      }
    }

    // Set lore if configured
    final List<String> loreLines = config.getLore();
    if(!loreLines.isEmpty()) {
      final List<Component> lore = new ArrayList<>();
      for(final String loreLine : loreLines) {
        // Direct inline MiniMessage text (e.g., "<yellow>Click to buy</yellow>")
        lore.add(parseMiniMessage(loreLine));
      }
      stack = stack.lore(lore);
    }

    return stack;
  }

  /**
   * Creates an IconBuilder from IconConfig with slot
   *
   * @param config The icon configuration
   * @return The configured IconBuilder
   */
  @NotNull
  public static IconBuilder createIconBuilder(@NotNull final GuiConfig.IconConfig config) {
    return new IconBuilder(createStack(config))
            .withSlot(config.getSlot());
  }

  /**
   * Creates an IconBuilder with custom slot
   *
   * @param config The icon configuration
   * @param slot   The slot to use
   * @return The configured IconBuilder
   */
  @NotNull
  public static IconBuilder createIconBuilder(@NotNull final GuiConfig.IconConfig config,
                                               final int slot) {
    return new IconBuilder(createStack(config))
            .withSlot(slot);
  }

  /**
   * Creates an IconBuilder with custom display name and lore
   *
   * @param config     The icon configuration (for material and custom model data)
   * @param slot       The slot to use
   * @param name       The display name component
   * @param lore       The lore components
   * @return The configured IconBuilder
   */
  @NotNull
  public static IconBuilder createIconBuilder(@NotNull final GuiConfig.IconConfig config,
                                               final int slot,
                                               @NotNull final Component name,
                                               @NotNull final List<Component> lore) {
    AbstractItemStack<?> stack = QuickShop.getInstance().stack()
            .of(config.getMaterial(), config.getAmount())
            .display(name)
            .lore(lore);

    return new IconBuilder(stack).withSlot(slot);
  }

  /**
   * Creates an IconBuilder with custom display name
   *
   * @param config The icon configuration
   * @param slot   The slot to use
   * @param name   The display name component
   * @return The configured IconBuilder
   */
  @NotNull
  public static IconBuilder createIconBuilder(@NotNull final GuiConfig.IconConfig config,
                                               final int slot,
                                               @NotNull final Component name) {
    AbstractItemStack<?> stack = QuickShop.getInstance().stack()
            .of(config.getMaterial(), config.getAmount())
            .display(name);

    return new IconBuilder(stack).withSlot(slot);
  }

  /**
   * Creates a border IconBuilder
   *
   * @param config The border icon configuration
   * @return The configured IconBuilder for borders
   */
  @NotNull
  public static IconBuilder createBorderBuilder(@NotNull final GuiConfig.IconConfig config) {
    AbstractItemStack<?> stack = QuickShop.getInstance().stack()
            .of(config.getMaterial(), 1);

    final String name = config.getName();
    if(name != null) {
      if(name.equals(" ") || name.isEmpty()) {
        stack = stack.display(Component.empty());
      }
    }

    return new IconBuilder(stack);
  }

  /**
   * Applies custom model data to an ItemStack if configured
   *
   * @param item   The ItemStack to modify
   * @param config The icon configuration
   * @return The modified ItemStack
   */
  @NotNull
  public static ItemStack applyCustomModelData(@NotNull final ItemStack item,
                                                @NotNull final GuiConfig.IconConfig config) {
    if(config.hasCustomModelData()) {
      final ItemMeta meta = item.getItemMeta();
      if(meta != null) {
        meta.setCustomModelData(config.getCustomModelData());
        item.setItemMeta(meta);
      }
    }
    return item;
  }

  /**
   * Parses a string as MiniMessage format directly.
   *
   * @param text The MiniMessage formatted text (e.g., "<bold><green>Buy Items</green></bold>")
   * @return The parsed Component
   */
  @NotNull
  private static Component parseMiniMessage(@NotNull final String text) {
    return QuickShop.getInstance().platform().miniMessage().deserialize(text);
  }
}
