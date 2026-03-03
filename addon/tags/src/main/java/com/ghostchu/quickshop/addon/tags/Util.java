package com.ghostchu.quickshop.addon.tags;

/*
 * QuickShop-Hikari
 * Copyright (C) 2026 Daniel "creatorfromhell" Vidmar
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
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.Shop;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Util
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public class Util {

  public static final String SYS_FAV = "@fav";
  public static final String SYS_WATCH = "@watch";
  public static final String SYS_AVOID = "@avoid";
  public static final int MAX_TAG_LENGTH = 32;
  //we only want letters, underscores and dashes
  public static final Pattern VALID_TAG_PATTERN = Pattern.compile("^[a-z_-]+$");

  //our shop-specific methods
  public static void handleAdd(final Player sender, @NotNull final Shop shop, @NotNull final CommandParser parser, final int tagPosition) {
    if(parser.getArgs().size() < tagPosition) {
      //sendUsage(sender);
      return;
    }

    final String tag = normalizeTag(parser.getArgs().get(tagPosition - 1), false);
    if(tag == null) {
      QuickShop.getInstance().text().of(sender, "tag-invalid").send();
      return;
    }
    com.ghostchu.quickshop.util.Util.regionThread(sender.getLocation(), () -> {
      QuickShop.getInstance().getDatabaseHelper().tagShop(sender.getUniqueId(), shop.getShopId(), tag);
      QuickShop.getInstance().text().of(sender, "tag-added", tag).send();
    });
  }

  public static void handleRemove(final Player sender, @NotNull final Shop shop, @NotNull final CommandParser parser) {
    if(parser.getArgs().size() < 2) {
      //sendUsage(sender);
      return;
    }
  }

  public static void handleClear(final Player sender, @NotNull final Shop shop) {

  }

  public static boolean handleToggleSystem(final Player sender, @NotNull final Shop shop, final String tag) {

  }

  public static void handleUnsetSystem(final Player sender, @NotNull final Shop shop, final String tag) {

  }

  public static void handleClearAll(final Player sender, @NotNull final Shop shop) {

    //TODO: User parameter
    if(!sender.hasPermission("quickshop.tag.clearall")) {
      QuickShop.getInstance().text().of(sender, "no-permission").send();
      return;
    }


  }

  //our global methods
  public static void handleTaggedList(final Player sender, @NotNull final CommandParser parser) {
  }

  public static void handleTaggedList(final Player sender, @NotNull final ArrayDeque<String> tags) {
  }

  public static void handleTags(final Player sender) {

  }

  public static void handleTags(final Player sender, @NotNull final Shop shop) {

  }

  public static void handleRemoveTagFromAllShops(final Player sender, @NotNull final CommandParser parser) {

  }

  public static String displayTag(final Player sender, final String stored) {
    if(stored == null) {
      return "";
    }

    return switch (stored) {
      case SYS_FAV -> "Favorite";
      case SYS_WATCH -> "Watch";
      case SYS_AVOID -> "Avoid";
      default -> "#" + stored;
    };
  }

  public static String displayTagOrHash(final String stored) {
    if(stored == null) {
      return "";
    }

    if(stored.startsWith("@")) {
      return stored;
    }
    return "#" + stored;
  }

  @Nullable
  public static String normalizeTag(@Nullable String input, final boolean allowSystem) {

    if (input == null) {
      return null;
    }

    if (input.startsWith("#")) {
      input = input.substring(1);
    }

    input = input.trim().toLowerCase(Locale.ROOT);

    if (input.isEmpty()) {
      return null;
    }

    if (input.length() > MAX_TAG_LENGTH) {
      return null;
    }

    if (!allowSystem && input.startsWith("@")) {
      return null;
    }

    if (!VALID_TAG_PATTERN.matcher(input).matches()) {
      return null;
    }

    return input;
  }
}