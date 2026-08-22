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

import java.util.Objects;

/**
 * UpdateMetadata
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
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

  /**
   * Creates a new {@code UpdateMetadata} instance.
   *
   * @param lastUpdate     Epoch millis of last update/publish time if known, else 0.
   * @param latestVersion  Highest semver across all versions (including beta/alpha if you publish
   *                       them).
   * @param releaseVersion Highest semver among release versions.
   */
  public UpdateMetadata(final long lastUpdate, @NotNull final String latestVersion, @NotNull final String releaseVersion) {

    if(latestVersion == null) {
      throw new NullPointerException("latestVersion is marked non-null but is null");
    }
    if(releaseVersion == null) {
      throw new NullPointerException("releaseVersion is marked non-null but is null");
    }
    this.lastUpdate = lastUpdate;
    this.latestVersion = latestVersion;
    this.releaseVersion = releaseVersion;
  }

  /**
   * Epoch millis of last update/publish time if known, else 0.
   */
  public long getLastUpdate() {

    return this.lastUpdate;
  }

  /**
   * Highest semver across all versions (including beta/alpha if you publish them).
   */
  @NotNull
  public String getLatestVersion() {

    return this.latestVersion;
  }

  /**
   * Highest semver among release versions.
   */
  @NotNull
  public String getReleaseVersion() {

    return this.releaseVersion;
  }

  @Override
  public boolean equals(final Object o) {

    if(o == this) return true;
    if(!(o instanceof UpdateMetadata)) return false;
    final UpdateMetadata other = (UpdateMetadata)o;
    return this.getLastUpdate() == other.getLastUpdate()
           && Objects.equals(this.getLatestVersion(), other.getLatestVersion())
           && Objects.equals(this.getReleaseVersion(), other.getReleaseVersion());
  }

  @Override
  public int hashCode() {

    return Objects.hash(this.getLastUpdate(), this.getLatestVersion(), this.getReleaseVersion());
  }

  @Override
  public String toString() {

    return "UpdateMetadata(lastUpdate=" + this.getLastUpdate() + ", latestVersion=" + this.getLatestVersion() + ", releaseVersion=" + this.getReleaseVersion() + ")";
  }
}
