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

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.updater.UpdateMetadata;
import com.ghostchu.quickshop.api.updater.UpdateProvider;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.quickshop.util.paste.item.SubPasteItem;
import com.ghostchu.quickshop.util.paste.util.HTMLTable;
import com.vdurmont.semver4j.Semver;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * UpdateManager
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public class UpdateManager implements SubPasteItem {

  private final QuickShop plugin;

  // Register providers here (or via ServiceLoader later if you want)
  private final Map<String, UpdateProvider> providers = new HashMap<>();

  @Getter
  private UpdateProvider activeProvider;

  private UpdateMetadata cachedMetadata;
  private boolean cachedResult = true;
  private long lastCheck = 0;

  // Same 1 hour cache you used before
  private static final long CACHE_MS = TimeUnit.HOURS.toMillis(1);

  public UpdateManager(final QuickShop plugin) {
    this.plugin = plugin;

    // built-ins
    registerProvider(new NexusUpdateProvider());
    registerProvider(new ModrinthUpdateProvider("quickshop-hikari"));

    reloadActiveProvider();

    plugin.getPasteManager().register(plugin.getJavaPlugin(), this);
  }

  public void unregister() {

    this.plugin.getPasteManager().unregister(this.plugin.getJavaPlugin(), this);
  }

  public void registerProvider(@NotNull final UpdateProvider provider) {
    providers.put(provider.id().toLowerCase(Locale.ROOT), provider);
  }

  public void reloadActiveProvider() {
    final String configured = plugin.getConfig().getString("updater-source", "modrinth");
    this.activeProvider = providers.getOrDefault((configured == null)? "modrinth" : configured.toLowerCase(Locale.ROOT), providers.get("nexus"));
  }

  public boolean isEnabled() {
    return plugin.getConfig().getBoolean("updater", false);
  }

  @NotNull
  public String getLatestVersion() {
    if(!isEnabled() || cachedMetadata == null) {
      return plugin.getVersion();
    }
    // Keep old behavior: show releaseVersion (what most server owners want)
    return cachedMetadata.getReleaseVersion();
  }

  @NotNull
  public String getUpdateUrl() {
    return activeProvider.updateUrl();
  }

  public boolean isLatest() {
    if(Bukkit.isPrimaryThread()) {
      Log.debug(Level.WARNING, "Warning: isLatest shouldn't be called on PrimaryThread", Log.Caller.create());
      return cachedResult;
    }
    if(!isEnabled()) {
      cachedResult = true;
      return true;
    }

    updateCacheIfRequired();
    if(cachedMetadata == null) {
      cachedResult = true;
      return true;
    }

    final Semver localVer = plugin.getSemVersion();
    final Semver remoteReleaseVer = new Semver(cachedMetadata.getReleaseVersion());
    final Semver remoteLatestVer = new Semver(cachedMetadata.getLatestVersion());

    final Semver selectedRemoteVer = remoteReleaseVer.isGreaterThan(remoteLatestVer)
                                     ? remoteReleaseVer
                                     : remoteLatestVer;

    cachedResult = !selectedRemoteVer.isGreaterThan(localVer);
    return cachedResult;
  }

  private void updateCacheIfRequired() {
    if((lastCheck + CACHE_MS) < System.currentTimeMillis()) {
      lastCheck = System.currentTimeMillis();
      cachedMetadata = activeProvider.fetchMetadata();
    }
  }

  @Override
  public @NotNull String genBody() {
    if(cachedMetadata == null) {
      return "<p>No metadata found.</p>";
    }
    final HTMLTable table = new HTMLTable(5);
    table.setTableTitle("Provider", "Last Update", "Latest Version", "Release Version", "Update URL");
    table.insert(
            activeProvider.displayName(),
            String.valueOf(cachedMetadata.getLastUpdate()),
            cachedMetadata.getLatestVersion(),
            cachedMetadata.getReleaseVersion(),
            activeProvider.updateUrl()
                );
    return table.render();
  }

  @Override
  public @NotNull String getTitle() {
    return "UpdateManager (updater)";
  }

  @Nullable
  public UpdateMetadata getCachedMetadata() {
    return cachedMetadata;
  }
}