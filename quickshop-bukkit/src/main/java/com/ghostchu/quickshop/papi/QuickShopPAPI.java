/*
 *  This file is a part of project QuickShop, the name is QuickShopPAPI.java
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
import com.ghostchu.quickshop.util.logger.Log;
import lombok.Getter;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class QuickShopPAPI extends PlaceholderExpansion {
    
    @Getter
    private final PAPICache papiCache = new PAPICache();

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "qs";
    }

    @Override
    public @NotNull String getAuthor() {
        return "QuickShopBundled";
    }

    @Override
    public @NotNull String getVersion() {
        return QuickShop.getVersion();
    }
    
    // Prevent the expansion being unregistered on /papi reload
    @Override
    public boolean persist(){
        return true;
    }
    
    @Override
    public @Nullable String onRequest(@NotNull OfflinePlayer player, @NotNull String params) {
        String cached = papiCache.readCache(player.getUniqueId(), params);
        if (cached != null) {
            Log.debug("Processing cached placeholder: " + params);
            return cached;
        }
        String[] args = params.split("_");
        if (args.length < 1) {
            return null;
        }
        
        return papiCache.getCached(player.getUniqueId(), args);
    }
}
