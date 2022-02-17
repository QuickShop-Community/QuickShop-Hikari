/*
 *  This file is a part of project QuickShop, the name is Factionsuuid.java
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

package com.ghostchu.quickshop.compatibility.factionsuuid;

import com.ghostchu.quickshop.api.QuickShopAPI;
import com.ghostchu.quickshop.api.event.ShopCreateEvent;
import com.ghostchu.quickshop.api.event.ShopPreCreateEvent;
import com.ghostchu.quickshop.api.event.ShopPurchaseEvent;
import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.perms.PermissibleAction;
import com.massivecraft.factions.perms.PermissibleActionRegistry;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class Factionsuuid extends JavaPlugin implements Listener {
    private QuickShopAPI api;
    private List<String> createFlags;
    private List<String> tradeFlags;
    private boolean createRequireOpen;
    private boolean createRequireNormal;
    private boolean createRequireWilderness;
    private boolean createRequirePeaceful;
    private boolean createRequirePermanent;
    private boolean createRequireSafeZone;
    private boolean createRequireOwn;
    private boolean createRequireWarZone;
    private boolean tradeRequireOpen;
    private boolean tradeRequireNormal;
    private boolean tradeRequireWilderness;
    private boolean tradeRequirePeaceful;
    private boolean tradeRequirePermanent;
    private boolean tradeRequireSafeZone;
    private boolean tradeRequireOwn;
    private boolean tradeRequireWarZone;
    private boolean whiteList;

    private void init() {
        this.createFlags = getConfig().getStringList("create.flags");
        this.tradeFlags = getConfig().getStringList("trade.flags");
        this.whiteList = getConfig().getBoolean("whitelist-mode");
        this.createRequireOpen =
                getConfig().getBoolean("create.require.open");
        this.createRequireNormal =
                getConfig().getBoolean("create.require.normal");
        this.createRequireWilderness =
                getConfig().getBoolean("create.require.wilderness");
        this.createRequirePeaceful =
                getConfig().getBoolean("create.require.peaceful");
        this.createRequirePermanent =
                getConfig().getBoolean("create.require.permanent");
        this.createRequireSafeZone =
                getConfig().getBoolean("create.require.safezone");
        this.createRequireOwn =
                getConfig().getBoolean("create.require.own");
        this.createRequireWarZone =
                getConfig().getBoolean("create.require.warzone");
        this.tradeRequireOpen =
                getConfig().getBoolean("trade.require.open");
        this.tradeRequireNormal =
                getConfig().getBoolean("trade.require.normal");
        this.tradeRequireWilderness =
                getConfig().getBoolean("trade.require.wilderness");
        this.tradeRequirePeaceful =
                getConfig().getBoolean("trade.require.peaceful");
        this.tradeRequirePermanent =
                getConfig().getBoolean("trade.require.permanent");
        this.tradeRequireSafeZone =
                getConfig().getBoolean("trade.require.safezone");
        this.tradeRequireOwn = getConfig().getBoolean("trade.require.own");
        this.tradeRequireWarZone =
                getConfig().getBoolean("trade.require.warzone");
    }

    @Nullable
    private String check(@NotNull Player player, @NotNull Location location, boolean createRequireOpen, boolean createRequireSafeZone, boolean createRequirePermanent, boolean createRequirePeaceful, boolean createRequireWilderness, boolean createRequireWarZone, boolean createRequireNormal, boolean createRequireOwn, List<String> createFlags, boolean whiteList) {
        FLocation fLocation = new FLocation(location);
        Faction faction = Board.getInstance().getFactionAt(fLocation);
        if (faction == null) {
            return "You're not allowed create the shop outside of a faction.";
        }
        if (createRequireOpen && !faction.getOpen()) {
            return "You're not allowed create the shop in a closed faction.";
        }
        if (createRequireSafeZone && !faction.isSafeZone()) {
            return "You're not allowed create the shop in a non-safezone.";
        }
        if (createRequirePermanent && !faction.isPermanent()) {
            return "You're not allowed create the shop in a non-permanent faction.";
        }
        if (createRequirePeaceful && !faction.isPeaceful()) {
            return "You're not allowed create the shop in a non-peaceful faction.";
        }
        if (createRequireWilderness && !faction.isWilderness()) {
            return "You're not allowed create the shop in a non-wilderness faction.";
        }
        if (createRequireWarZone && !faction.isWarZone()) {
            return "You're not allowed create the shop in a non-warzone faction.";
        }
        if (createRequireNormal && !faction.isNormal()) {
            return "You're not allowed create the shop in a non-normal faction.";
        }
        if (createRequireOwn
                && !faction.getOwnerList(fLocation).contains(player.getName())) {
            return "You're not allowed create the shop in a faction you don't own.";
        }

        for (String flag : createFlags) {
            PermissibleAction permissibleAction = PermissibleActionRegistry.get(flag);
            if (permissibleAction != null && !faction.hasAccess(FPlayers.getInstance().getByPlayer(player), permissibleAction, fLocation)) {
                return "You're not allowed create the shop in a faction you don't have access to.";
            }
        }
        return null;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPreCreation(ShopPreCreateEvent event) {
        String error = check(event.getPlayer(), event.getLocation(), createRequireOpen, createRequireSafeZone, createRequirePermanent, createRequirePeaceful, createRequireWilderness, createRequireWarZone, createRequireNormal, createRequireOwn, createFlags, whiteList);
        if (error == null)
            return;
        event.setCancelled(true, error);
    }

    @EventHandler(ignoreCancelled = true)
    public void onCreation(ShopCreateEvent event) {
        //noinspection ConstantConditions
        String error = check(event.getPlayer(), event.getShop().getLocation(), createRequireOpen, createRequireSafeZone, createRequirePermanent, createRequirePeaceful, createRequireWilderness, createRequireWarZone, createRequireNormal, createRequireOwn, createFlags, whiteList);
        if (error == null)
            return;
        event.setCancelled(true, error);
    }

    @EventHandler(ignoreCancelled = true)
    public void onTrade(ShopPurchaseEvent event) {
        //noinspection ConstantConditions
        String error = check(event.getPlayer(), event.getShop().getLocation(), tradeRequireOpen, tradeRequireSafeZone, tradeRequirePermanent, tradeRequirePeaceful, tradeRequireWilderness, tradeRequireWarZone, tradeRequireNormal, tradeRequireOwn, tradeFlags, whiteList);
        event.setCancelled(true, error);
    }
}
