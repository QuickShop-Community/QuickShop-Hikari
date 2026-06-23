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
import com.ghostchu.quickshop.config.GuiConfig;
import net.kyori.adventure.text.Component;
import net.tnemc.menu.core.Page;
import net.tnemc.menu.core.viewer.MenuViewer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
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
    setLockEmptySlots(true);
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

  /**
   * Gets a chat message from gui.yml and formats it as a legacy string. This is used for chat
   * messages sent to players from GUI interactions.
   *
   * @param path The path under "gui.messages" section in messages.yml (e.g., "keeper.confirm-delete")
   * @param args Arguments to replace {0}, {1}, etc. placeholders
   *
   * @return The formatted legacy string
   */
  public static String guiMessage(final String path, @Nullable final Object... args) {

    return QuickShop.getInstance().text().of("gui.messages." + path, args).legacy();
  }

  /**
   * Gets an icon's display name from gui.yml as a Component. Parses MiniMessage formatting.
   *
   * @param config      The icon config
   * @param defaultName The default name if not configured
   *
   * @return The parsed Component
   */
  @NotNull
  public static Component getConfigDisplay(final UUID player, @Nullable final GuiConfig.IconConfig config, @NotNull final String defaultName) {

    return getConfigDisplay(player, config, defaultName, null);
  }

  /**
   * Gets an icon's display name from gui.yml as a Component with placeholder replacement. Parses
   * MiniMessage formatting and replaces {0}, {1}, etc. placeholders.
   *
   * @param config      The icon config
   * @param defaultName The default name if not configured
   * @param args        Arguments to replace placeholders
   *
   * @return The parsed Component
   */
  @NotNull
  public static Component getConfigDisplay(final UUID player, @Nullable final GuiConfig.IconConfig config, @NotNull final String defaultName, @Nullable final Object... args) {

    //check if this should be a lang-file lookup
    final String name = (config != null && config.getName() != null)? config.getName() : defaultName;

    if(config != null && name.startsWith("lang:")) {
      return get(player, name.replaceAll("lang:", ""), args);
    }

    return QuickShop.getInstance().platform().miniMessage().deserialize(replaceArguments(name, args));
  }

  /**
   * Gets an icon's lore from gui.yml as a list of Components. Parses MiniMessage formatting and
   * replaces {0}, {1}, etc. placeholders.
   *
   * @param config The icon config
   * @param args   Arguments to replace placeholders
   *
   * @return The parsed list of Components
   */
  @NotNull
  public static List<Component> getConfigLore(final UUID player, @Nullable final GuiConfig.IconConfig config, @Nullable final Object... args) {

    final List<Component> result = new ArrayList<>();
    if(config == null) {
      return result;
    }

    for(final String line : config.getLore()) {

      //check if this should be a lang-file lookup
      if(line.contains("lang:")) {

        result.addAll(getList(player, line.replaceAll("lang:", ""), args));
        continue;
      }
      // Replace {0}, {1}, etc. with args
      result.add(QuickShop.getInstance().platform().miniMessage().deserialize(replaceArguments(line, args)));
    }
    return result;
  }

  @NotNull
  public static Component getConfigDisplay(final UUID player, @Nullable final GuiConfig.IconConfig config, @NotNull final String configName, @NotNull final String defaultName, @Nullable final Object... args) {

    //check if this should be a lang-file lookup
    final String name = (config != null && config.section() != null)? config.section().getString(configName, defaultName) : defaultName;

    if(config != null && name.startsWith("lang:")) {
      return get(player, name.replaceAll("lang:", ""), args);
    }

    return QuickShop.getInstance().platform().miniMessage().deserialize(replaceArguments(name, args));
  }

  /**
   * Replaces placeholders in the given message with the provided arguments.
   * Placeholders in the message should be in the format {0}, {1}, etc., where
   * the numeric index corresponds to the position of the argument in the args array.
   * If an argument is null, the placeholder will be replaced with an empty string.
   *
   * @param message The message containing placeholders to be replaced.
   * @param args    The arguments to replace the placeholders. If null, no replacement is performed.
   *
   * @return The message with placeholders replaced by the corresponding arguments.
   *         If args is null, the original message is returned.
   */
  public static String replaceArguments(final String message, final Object... args) {

    if(args == null) {
      return message;
    }

    String workingStr = message;
    for(int i = 0; i < args.length; i++) {

      final Object arg = args[i];
      workingStr = workingStr.replace("{" + i + "}", (arg != null)? arg.toString() : "");
    }
    return workingStr;
  }
}