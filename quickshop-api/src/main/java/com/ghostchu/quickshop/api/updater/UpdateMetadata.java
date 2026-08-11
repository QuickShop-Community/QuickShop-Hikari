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

import lombok.Data;
import org.jetbrains.annotations.NotNull;

/**
 * UpdateMetadata
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
@Data
public class UpdateMetadata {

  /**
   * Epoch millis of last update/publish time if known, else 0.
   */
  private final long lastUpdate;

  /**
   * Highest semver across all versions (including beta/alpha if you publish them).
   */
  @NotNull
  private final String latestVersion;

  /**
   * Highest semver among release versions.
   */
  @NotNull
  private final String releaseVersion;
}