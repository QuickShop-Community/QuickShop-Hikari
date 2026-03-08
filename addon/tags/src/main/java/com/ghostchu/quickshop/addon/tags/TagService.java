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
import com.ghostchu.quickshop.api.database.DatabaseHelper;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

/**
 * TagService
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public class TagService {


  public static final long TOTAL_INDEX = -1L;
  public static final String SYS_FAV = "@fav";
  public static final String SYS_WATCH = "@watch";
  public static final String SYS_AVOID = "@avoid";

  private static final int MAX_TAG_LENGTH = 32;
  private static final Pattern VALID_TAG_PATTERN = Pattern.compile("^[a-z_-]+$");

  private final Main tagsMain;
  private final QuickShop plugin;

  public TagService(final Main tagsMain, final QuickShop plugin) {

    this.tagsMain = tagsMain;
    this.plugin = plugin;
  }

  public static String displayTag(final Player sender, final String stored) {

    if(stored == null) {
      return "";
    }

    return switch(stored) {
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

    if(input == null) {
      return null;
    }

    if(input.startsWith("#")) {
      input = input.substring(1);
    }

    input = input.trim().toLowerCase(Locale.ROOT);

    if(input.isEmpty()) {
      return null;
    }

    if(input.length() > MAX_TAG_LENGTH) {
      return null;
    }

    if(!allowSystem && input.startsWith("@")) {
      return null;
    }

    if(!VALID_TAG_PATTERN.matcher(input).matches()) {
      return null;
    }

    return input;
  }

  public CompletableFuture<Boolean> addShopTag(final UUID player, final long shopId, final String tag) {

    final DatabaseHelper db = plugin.getDatabaseHelper();

    return db.tagShop(player, shopId, tag).thenCompose(result->{

      if(result != null && result > 0) {
        return CompletableFuture.completedFuture(true);
      }

      return CompletableFuture.completedFuture(false);
    });
  }

  public CompletableFuture<Boolean> removeShopTag(final UUID player, final long shopId, final String tag) {

    final DatabaseHelper db = plugin.getDatabaseHelper();

    return db.removeShopTag(player, shopId, tag).thenCompose(result->{

      if(result != null && result > 0) {
        return CompletableFuture.completedFuture(true);
      }

      return CompletableFuture.completedFuture(false);
    });
  }

  public CompletableFuture<Boolean> removeAllShopTags(final long shopId) {

    final DatabaseHelper db = plugin.getDatabaseHelper();
    return db.removeAllShopTags(shopId).thenCompose(result->{
      if(result != null && result > 0) {
        return CompletableFuture.completedFuture(true);
      }

      return CompletableFuture.completedFuture(false);
    });
  }

  public CompletableFuture<Boolean> removeAllShopTagsBy(final long shopId, final UUID player) {

    final DatabaseHelper db = plugin.getDatabaseHelper();
    return db.removeAllShopTagsBy(player, shopId).thenCompose(result->{
      if(result != null && result > 0) {
        return CompletableFuture.completedFuture(true);
      }

      return CompletableFuture.completedFuture(false);
    });
  }

  public CompletableFuture<Boolean> toggleSystemTag(final UUID player, final long shopId, final String tag) {

    final DatabaseHelper db = plugin.getDatabaseHelper();

    return db.tagShop(player, shopId, tag).thenCompose(result->{
      if(result != null && result > 0) {
        return CompletableFuture.completedFuture(true);
      }

      return db.removeShopTag(player, shopId, tag)
              .thenApply(r->false);
    });
  }
}