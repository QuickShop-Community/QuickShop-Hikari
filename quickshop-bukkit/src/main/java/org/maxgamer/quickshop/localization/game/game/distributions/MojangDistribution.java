/*
 * This file is a part of project QuickShop, the name is MojangDistribution.java
 *  Copyright (C) PotatoCraft Studio and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.maxgamer.quickshop.localization.game.game.distributions;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonElement;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.localization.game.game.distributions.bean.GameManifest;
import org.maxgamer.quickshop.localization.game.game.distributions.bean.VersionManifest;
import org.maxgamer.quickshop.util.JsonUtil;
import org.maxgamer.quickshop.util.Util;
import org.maxgamer.quickshop.util.mojangapi.MojangApiMirror;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MojangDistribution {
    protected final Cache<String, String> requestCachePool = CacheBuilder.newBuilder()
            .initialCapacity(1)
            .expireAfterWrite(7, TimeUnit.DAYS)
            .recordStats()
            .build();
    private final QuickShop plugin;
    private final MojangApiMirror mirror;

    public MojangDistribution(QuickShop plugin, MojangApiMirror mirror) {
        this.plugin = plugin;
        this.mirror = mirror;
        Util.getCacheFolder().mkdirs();

    }

    @Nullable
    public VersionManifest getVersionManifest() {
        String url = mirror.getLauncherMetaRoot() + "/mc/game/version_manifest.json";
        if (requestCachePool.getIfPresent(url) != null) {
            return JsonUtil.standard().fromJson(requestCachePool.getIfPresent(url), VersionManifest.class);
        }
        if (!grabIntoCaches(url)) {
            return null;
        }
        return JsonUtil.standard().fromJson(requestCachePool.getIfPresent(url), VersionManifest.class);
    }


    @Nullable
    public GameManifest getGameManifest(VersionManifest versionManifest, String gameVersion) {
        for (VersionManifest.VersionsDTO version : versionManifest.getVersions()) {
            if (version.getId().equals(gameVersion)) {
                String url = version.getUrl();
                if (!grabIntoCaches(url)) {
                    return null;
                }
                return JsonUtil.standard().fromJson(requestCachePool.getIfPresent(url), GameManifest.class);
            }
        }
        return null;
    }

    @NotNull
    public List<String> getAvailableLanguages() {
        List<String> languages = new ArrayList<>();
        VersionManifest versionManifest = getVersionManifest();
        if (versionManifest == null) {
            return Collections.emptyList();
        }
        GameManifest gameManifest = getGameManifest(versionManifest, plugin.getPlatform().getMinecraftVersion());
        if (gameManifest == null) {
            return Collections.emptyList();
        }
        if (!grabIntoCaches(gameManifest.getAssetIndex().getUrl())) {
            return Collections.emptyList();
        }
        String versionMapping = requestCachePool.getIfPresent(gameManifest.getAssetIndex().getUrl());
        if (versionMapping == null) {
            return Collections.emptyList();
        }
        for (Map.Entry<String, JsonElement> objects : JsonUtil.parser().parse(versionMapping).getAsJsonObject().get("objects").getAsJsonObject().entrySet()) {
            if (objects.getKey().startsWith("minecraft/lang/")) {
                languages.add(StringUtils.substringBetween("minecraft/lang/", ".json"));
            }
        }
        return languages;
    }


    public boolean grabIntoCaches(String url) {
        HttpResponse<String> response = Unirest.get(url).asString();
        if (!response.isSuccess()) {
            plugin.getLogger().warning("Couldn't get manifest: " + response.getStatus() + "/" + response.getStatusText() + ", please report to QuickShop!");
            return false;
        }
        requestCachePool.put(url, response.getBody());
        return true;
    }
}
