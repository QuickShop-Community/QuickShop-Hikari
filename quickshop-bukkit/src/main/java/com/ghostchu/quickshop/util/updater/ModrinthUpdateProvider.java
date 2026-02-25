package com.ghostchu.quickshop.util.updater;

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

import com.ghostchu.quickshop.api.updater.UpdateMetadata;
import com.ghostchu.quickshop.api.updater.UpdateProvider;
import com.ghostchu.quickshop.util.logger.Log;
import com.vdurmont.semver4j.Semver;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

/**
 * ModrinthUpdateProvider
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public class ModrinthUpdateProvider implements UpdateProvider {

  private final String projectId;
  private final String apiUrl = "https://api.modrinth.com/v2/project/quickshop-hikari/version?include_changelog=false";

  public ModrinthUpdateProvider(@NotNull final String projectId) {
    this.projectId = projectId;
  }

  @Override
  public @NotNull String id() {
    return "modrinth";
  }

  @Override
  public @NotNull String displayName() {
    return "Modrinth";
  }

  @Override
  public @Nullable UpdateMetadata fetchMetadata() {
    try {
      final HttpResponse<String> resp = Unirest.get(apiUrl)
              .header("User-Agent", "QuickShop-Hikari Updater (" + projectId + ")")
              .asString();

      if(!resp.isSuccess()) {
        Log.debug("Failed to fetch metadata from Modrinth: " + resp.getStatus() + "-" + resp.getStatusText());
        return null;
      }

      final JSONArray arr = new JSONArray(resp.getBody());
      if(arr.isEmpty()) {
        Log.debug("Modrinth returned empty versions list for: " + projectId);
        return null;
      }

      Semver bestAny = null;
      Semver bestRelease = null;
      long lastUpdate = 0;

      for(int i = 0; i < arr.length(); i++) {
        final JSONObject v = arr.getJSONObject(i);

        final String versionNumberRaw = v.optString("version_number", "");
        if(versionNumberRaw.isEmpty()) continue;

        //You may publish like "v6.2.0.11" — Semver4j doesn’t like leading 'v'
        final String versionNumber = stripLeadingV(versionNumberRaw);

        final Semver semver;
        try {
          semver = new Semver(versionNumber);
        } catch(final Exception ignored) {
          //Skip non-semver version labels
          continue;
        }

        //Track latest by semver
        if(bestAny == null || semver.isGreaterThan(bestAny)) {
          bestAny = semver;
        }

        final String versionType = v.optString("version_type", "release");
        if("release".equalsIgnoreCase(versionType)) {
          if(bestRelease == null || semver.isGreaterThan(bestRelease)) {
            bestRelease = semver;
          }
        }

        // date_published is ISO-8601, e.g. "2024-01-01T12:34:56.000Z"
        final String published = v.optString("date_published", "");
        if(!published.isEmpty()) {
          try {
            final long ts = Instant.parse(published).toEpochMilli();
            if(ts > lastUpdate) lastUpdate = ts;
          } catch(final Exception ignored) {
          }
        }
      }

      if(bestAny == null) {
        Log.debug("Modrinth: no semver-compatible versions found for: " + projectId);
        return null;
      }
      if(bestRelease == null) {
        //If you *never* mark versions release, fall back to any
        bestRelease = bestAny;
      }

      return new UpdateMetadata(lastUpdate, bestAny.getValue(), bestRelease.getValue());
    } catch(final Exception e) {
      Log.debug("Failed to fetch/parse metadata from Modrinth: " + e.getMessage());
      return null;
    }
  }

  private static String stripLeadingV(@NotNull final String s) {
    if(s.length() >= 2 && (s.charAt(0) == 'v' || s.charAt(0) == 'V') && Character.isDigit(s.charAt(1))) {
      return s.substring(1);
    }
    return s;
  }
}