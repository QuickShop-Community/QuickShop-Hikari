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

import com.ghostchu.quickshop.util.logger.Log;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.util.Util;

import java.util.UUID;

public class QuickShopPAPI extends PlaceholderExpansion {
    private QuickShop plugin;
    private final PAPICache papiCache = new PAPICache();

    @Override
    public boolean canRegister() {
        this.plugin = QuickShop.getInstance();
        return true;
    }

    /**
     * The placeholder identifier of this expansion. May not contain {@literal %},
     * {@literal {}} or _
     *
     * @return placeholder identifier that is associated with this expansion
     */
    @Override
    public @NotNull String getIdentifier() {
        return "qs";
    }

    /**
     * The author of this expansion
     *
     * @return name of the author for this expansion
     */
    @Override
    public @NotNull String getAuthor() {
        return "QuickShopBundled";
    }

    /**
     * The version of this expansion
     *
     * @return current version of this expansion
     */
    @Override
    public @NotNull String getVersion() {
        return QuickShop.getVersion();
    }

    @Override
    public @Nullable String onRequest(@NotNull OfflinePlayer player, @NotNull String params) {
       String cached = papiCache.readCache(player.getUniqueId(),params);
       if(cached != null) {
           Log.debug("Processing cached placeholder: " + params);
           return cached;
       }
        String[] args = params.split("_");
        if (args.length < 1) {
            return null;
        }
        switch (args[0]) {
            case "server-total" -> {
                return String.valueOf(plugin.getShopManager().getAllShops().size());
            }
            case "server-loaded" -> {
                return String.valueOf(plugin.getShopManager().getLoadedShops().size());
            }
            case "default-currency" -> {
                return String.valueOf(plugin.getCurrency());
            }
            case "player" -> {
                UUID uuid = null;
                if (args.length >= 3) {
                    if (Util.isUUID(args[1])) {
                        uuid = UUID.fromString(args[1]);
                    }
                }
                if (uuid == null)
                    uuid = player.getUniqueId();
                //noinspection SwitchStatementWithTooFewBranches
                switch (args[2]) {
                    case "count" -> {
                        return String.valueOf(plugin.getShopManager().getPlayerAllShops(uuid).size());
                    }
                }

                return null;
            }
        }

        return super.onRequest(player, params);
    }
}
