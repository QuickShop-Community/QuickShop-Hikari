/*
 * This file is a part of project QuickShop, the name is FactionsUUIDIntegration.java
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

package org.maxgamer.quickshop.integration.factionsuuid;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.perms.PermissibleAction;
import com.massivecraft.factions.perms.PermissibleActionRegistry;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.integration.IntegrateStage;
import org.maxgamer.quickshop.api.integration.IntegrationStage;
import org.maxgamer.quickshop.integration.AbstractQSIntegratedPlugin;
import org.maxgamer.quickshop.util.reload.ReloadResult;
import org.maxgamer.quickshop.util.reload.ReloadStatus;

import java.util.List;

@IntegrationStage(loadStage = IntegrateStage.onEnableAfter)
public class FactionsUUIDIntegration extends AbstractQSIntegratedPlugin {
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

    public FactionsUUIDIntegration(QuickShop plugin) {
        super(plugin);
        plugin.getReloadManager().register(this);
        init();
    }

    private void init() {
        this.createFlags = plugin.getConfig().getStringList("integration.factions.create.flags");
        this.tradeFlags = plugin.getConfig().getStringList("integration.factions.trade.flags");

        this.whiteList = plugin.getConfig().getBoolean("integration.factions.whitelist-mode");
        this.createRequireOpen =
                plugin.getConfig().getBoolean("integration.factions.create.require.open");
        this.createRequireNormal =
                plugin.getConfig().getBoolean("integration.factions.create.require.normal");
        this.createRequireWilderness =
                plugin.getConfig().getBoolean("integration.factions.create.require.wilderness");
        this.createRequirePeaceful =
                plugin.getConfig().getBoolean("integration.factions.create.require.peaceful");
        this.createRequirePermanent =
                plugin.getConfig().getBoolean("integration.factions.create.require.permanent");
        this.createRequireSafeZone =
                plugin.getConfig().getBoolean("integration.factions.create.require.safezone");
        this.createRequireOwn =
                plugin.getConfig().getBoolean("integration.factions.create.require.own");
        this.createRequireWarZone =
                plugin.getConfig().getBoolean("integration.factions.create.require.warzone");

        this.tradeRequireOpen =
                plugin.getConfig().getBoolean("integration.factions.trade.require.open");
        this.tradeRequireNormal =
                plugin.getConfig().getBoolean("integration.factions.trade.require.normal");
        this.tradeRequireWilderness =
                plugin.getConfig().getBoolean("integration.factions.trade.require.wilderness");
        this.tradeRequirePeaceful =
                plugin.getConfig().getBoolean("integration.factions.trade.require.peaceful");
        this.tradeRequirePermanent =
                plugin.getConfig().getBoolean("integration.factions.trade.require.permanent");
        this.tradeRequireSafeZone =
                plugin.getConfig().getBoolean("integration.factions.trade.require.safezone");
        this.tradeRequireOwn = plugin.getConfig().getBoolean("integration.factions.trade.require.own");
        this.tradeRequireWarZone =
                plugin.getConfig().getBoolean("integration.factions.trade.require.warzone");
    }

    private boolean check(@NotNull Player player, @NotNull Location location, boolean createRequireOpen, boolean createRequireSafeZone, boolean createRequirePermanent, boolean createRequirePeaceful, boolean createRequireWilderness, boolean createRequireWarZone, boolean createRequireNormal, boolean createRequireOwn, List<String> createFlags, boolean whiteList) {
        FLocation fLocation = new FLocation(location);
        Faction faction = Board.getInstance().getFactionAt(fLocation);
        if (faction == null) {
            return !whiteList;
        }
        if (createRequireOpen && !faction.getOpen()) {
            return false;
        }
        if (createRequireSafeZone && !faction.isSafeZone()) {
            return false;
        }
        if (createRequirePermanent && !faction.isPermanent()) {
            return false;
        }
        if (createRequirePeaceful && !faction.isPeaceful()) {
            return false;
        }
        if (createRequireWilderness && !faction.isWilderness()) {
            return false;
        }
        if (createRequireOpen && !faction.getOpen()) {
            return false;
        }
        if (createRequireWarZone && !faction.isWarZone()) {
            return false;
        }
        if (createRequireNormal && !faction.isNormal()) {
            return false;
        }
        if (createRequireOwn
                && !faction.getOwnerList(fLocation).contains(player.getName())) {
            return false;
        }

        for (String flag : createFlags) {
            PermissibleAction permissibleAction = PermissibleActionRegistry.get(flag);
            if (permissibleAction != null && !faction.hasAccess(FPlayers.getInstance().getByPlayer(player), permissibleAction, fLocation)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public @NotNull String getName() {
        return "Factions";
    }

    @Override
    public boolean canCreateShopHere(@NotNull Player player, @NotNull Location location) {
        return check(player, location, createRequireOpen, createRequireSafeZone, createRequirePermanent, createRequirePeaceful, createRequireWilderness, createRequireWarZone, createRequireNormal, createRequireOwn, createFlags, whiteList);
    }

    @Override
    public boolean canTradeShopHere(@NotNull Player player, @NotNull Location location) {
        return check(player, location, tradeRequireOpen, tradeRequireSafeZone, tradeRequirePermanent, tradeRequirePeaceful, tradeRequireWilderness, tradeRequireWarZone, tradeRequireNormal, tradeRequireOwn, tradeFlags, whiteList);
    }

    @Override
    public void load() {
    }

    @Override
    public void unload() {
    }

    /**
     * Callback for reloading
     *
     * @return Reloading success
     */
    @Override
    public ReloadResult reloadModule() {
        init();
        return ReloadResult.builder().status(ReloadStatus.SUCCESS).build();
    }
}
