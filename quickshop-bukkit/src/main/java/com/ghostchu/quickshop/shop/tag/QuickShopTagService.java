package com.ghostchu.quickshop.shop.tag;

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
import com.ghostchu.quickshop.api.shop.tag.TagService;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

/**
 * Default QuickShop implementation of {@link TagService}.
 *
 * <p>This implementation handles normalization, formatting, and persistence
 * operations for tags using the QuickShop database layer.</p>
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public class QuickShopTagService implements TagService {

  private static final int MAX_TAG_LENGTH = 32;
  private static final Pattern VALID_TAG_PATTERN = Pattern.compile("^[a-z_-]+$");

  private static QuickShopTagService instance;

  private final QuickShop plugin;

  /**
   * Creates a new tag service implementation.
   *
   * @param plugin the QuickShop plugin instance
   *
   * @since 6.3.0.0
   */
  public QuickShopTagService(final QuickShop plugin) {

    this.plugin = plugin;
    instance = this;
  }

  /**
   * Returns the active tag service implementation instance.
   *
   * @return the active tag service implementation
   *
   * @throws IllegalStateException if the tag service has not been initialized
   * @since 6.3.0.0
   */
  public static QuickShopTagService instance() {

    if(instance == null) {
      throw new IllegalStateException("QuickShopTagService has not been initialized.");
    }
    return instance;
  }

  @Override
  public String displayTag(final Player sender, final String stored) {

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

  @Override
  public String displayTagOrHash(final String stored) {

    if(stored == null) {
      return "";
    }

    if(stored.startsWith("@")) {
      return stored;
    }
    return "#" + stored;
  }

  @Override
  public @Nullable String normalizeTag(@Nullable String input, final boolean allowSystem) {

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

  @Override
  public CompletableFuture<Boolean> addShopTag(final UUID player, final long shopId, final String tag) {

    final DatabaseHelper db = plugin.getDatabaseHelper();
    return db.tagShop(player, shopId, tag).thenApply(result->result != null && result > 0);
  }

  @Override
  public CompletableFuture<Boolean> removeShopTag(final UUID player, final long shopId, final String tag) {

    final DatabaseHelper db = plugin.getDatabaseHelper();
    return db.removeShopTag(player, shopId, tag).thenApply(result->result != null && result > 0);
  }

  @Override
  public CompletableFuture<Boolean> removeAllShopTags(final long shopId) {

    final DatabaseHelper db = plugin.getDatabaseHelper();
    return db.removeAllShopTags(shopId).thenApply(result->result != null && result > 0);
  }

  @Override
  public CompletableFuture<Boolean> removeAllShopTagsBy(final long shopId, final UUID player) {

    final DatabaseHelper db = plugin.getDatabaseHelper();
    return db.removeAllShopTagsBy(player, shopId).thenApply(result->result != null && result > 0);
  }

  @Override
  public CompletableFuture<Boolean> toggleSystemTag(final UUID player, final long shopId, final String tag) {

    final DatabaseHelper db = plugin.getDatabaseHelper();

    return db.tagShop(player, shopId, tag).thenCompose(result->{
      if(result != null && result > 0) {
        return CompletableFuture.completedFuture(true);
      }

      return db.removeShopTag(player, shopId, tag).thenApply(r->false);
    });
  }
}