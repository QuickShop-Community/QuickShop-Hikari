/*
 * This file is a part of project QuickShop, the name is PlayerFinder.java
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

package org.maxgamer.quickshop.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * A player finder for finding player by name
 *
 * @author sandtechnology
 * @since 5.1.0.3
 */
public final class PlayerFinder {

    private static final Cache<String, UUID> string2UUIDCache = CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build();

    private PlayerFinder() {
    }

    public static UUID findUUIDByName(String name) {
        return findOfflinePlayerByName(name).getUniqueId();
    }

    @Nullable
    private static OfflinePlayer findPlayerByName(String name, Iterable<? extends OfflinePlayer> players) {
        for (OfflinePlayer player : players) {
            String playerName = player.getName();
            if (playerName != null && playerName.equalsIgnoreCase(name)) {
                return player;
            }
        }
        return null;
    }

    public static OfflinePlayer findOfflinePlayerByName(String name) {
        OfflinePlayer result;
        UUID uuid = string2UUIDCache.getIfPresent(name.toLowerCase(Locale.ROOT));
        if (uuid != null) {
            return Bukkit.getOfflinePlayer(uuid);
        } else {
            Server server = Bukkit.getServer();
            result = findPlayerByName(name, server.getOnlinePlayers());
            if (result == null) {
                result = findPlayerByName(name, Arrays.asList(server.getOfflinePlayers()));
            }
            if (result == null) {
                result = Bukkit.getServer().getOfflinePlayer(name);
            }
            string2UUIDCache.put(name.toLowerCase(Locale.ROOT), result.getUniqueId());
        }
        return result;
    }

    public static OfflinePlayer findOfflinePlayerByUUID(UUID uuid) {
        return Bukkit.getOfflinePlayer(uuid);
    }
}
