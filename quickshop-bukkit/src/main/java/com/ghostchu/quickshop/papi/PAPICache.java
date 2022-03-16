/*
 *  This file is a part of project QuickShop, the name is PAPICache.java
 *  Copyright (C) Ghost_chu and contributors
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

package com.ghostchu.quickshop.papi;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.util.JsonUtil;
import com.ghostchu.simplereloadlib.ReloadResult;
import com.ghostchu.simplereloadlib.Reloadable;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PAPICache implements Reloadable {
    private long expiredTime;
    private Cache<String, String> performCaches;

    public PAPICache() {
        init();
        QuickShop.getInstance().getReloadManager().register(this);
    }

    private void init() {
        this.expiredTime = 15 * 60 * 1000;
        this.performCaches = CacheBuilder.newBuilder()
                .expireAfterWrite(expiredTime, java.util.concurrent.TimeUnit.MILLISECONDS)
                .recordStats()
                .build();
    }

    @Override
    public ReloadResult reloadModule() throws Exception {
        init();
        return Reloadable.super.reloadModule();
    }

    public long getExpiredTime() {
        return expiredTime;
    }

    public void writeCache(@NotNull UUID player, @NotNull String queryString, @NotNull String queryValue) {
        performCaches.put(compileUniqueKey(player, queryString), queryValue);
    }

    @Nullable
    public String readCache(@NotNull UUID player, @NotNull String queryString) {
        return performCaches.getIfPresent(compileUniqueKey(player, queryString));
    }

    @NotNull
    private String compileUniqueKey(@NotNull UUID player, @NotNull String queryString) {
        return JsonUtil.standard().toJson(new CompiledUniqueKey(player, queryString));
    }

    @AllArgsConstructor
    @Data
    static class CompiledUniqueKey {
        private UUID player;
        private String queryString;
    }
}
