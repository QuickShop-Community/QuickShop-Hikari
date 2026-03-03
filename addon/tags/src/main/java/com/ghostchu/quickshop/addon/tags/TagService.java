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

  private static final int MAX_TAG_LENGTH = 32;
  private static final Pattern VALID_TAG_PATTERN = Pattern.compile("^[a-z_-]+$");

  private final Main tagsMain;
  private final QuickShop plugin;

  public TagService(final Main tagsMain, final QuickShop plugin) {
    this.tagsMain = tagsMain;
    this.plugin = plugin;
  }

  public @Nullable String normalizePlayerTag(String input) {
    if (input == null) return null;

    if (input.startsWith("#")) {
      input = input.substring(1);
    }

    input = input.trim().toLowerCase(Locale.ROOT);

    if (input.isEmpty()) return null;
    if (input.length() > MAX_TAG_LENGTH) return null;
    if (input.startsWith("@")) return null;

    if (!VALID_TAG_PATTERN.matcher(input).matches()) {
      return null;
    }

    return input;
  }

  public CompletableFuture<Boolean> toggleSystemTag(final UUID player, final long shopId, final String tag) {
    final var db = plugin.getDatabaseHelper();

    return db.tagShop(player, shopId, tag).thenCompose(result -> {
      if (result != null && result > 0) {
        return CompletableFuture.completedFuture(true);
      }

      return db.removeShopTag(player, shopId, tag)
              .thenApply(r -> false);
    });
  }
}