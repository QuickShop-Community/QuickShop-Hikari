package com.ghostchu.quickshop.api.updater;

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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * UpdateProvider
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public interface UpdateProvider {

  /**
   * Stable id used in config, e.g. "nexus", "modrinth".
   */
  @NotNull
  String id();

  /**
   * Human readable name for logs / paste output.
   */
  @NotNull
  String displayName();

  /**
   * Where users should download updates (project page, etc).
   */
  @NotNull
  default String updateUrl() {
    return "https://modrinth.com/plugin/quickshop-hikari";
  }

  /**
   * Fetch remote metadata. Return null if unavailable (network / parse issues).
   * Should be called async (never on the primary thread).
   */
  @Nullable
  UpdateMetadata fetchMetadata();
}