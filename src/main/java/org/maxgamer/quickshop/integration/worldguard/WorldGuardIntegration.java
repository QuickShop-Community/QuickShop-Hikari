/*
 * This file is a part of project QuickShop, the name is WorldGuardIntegration.java
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

package org.maxgamer.quickshop.integration.worldguard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.integration.IntegrateStage;
import org.maxgamer.quickshop.api.integration.IntegrationStage;
import org.maxgamer.quickshop.integration.AbstractQSIntegratedPlugin;
import org.maxgamer.quickshop.util.Util;
import org.maxgamer.quickshop.util.reload.ReloadResult;
import org.maxgamer.quickshop.util.reload.ReloadStatus;

import java.util.List;
import java.util.logging.Level;

@SuppressWarnings("DuplicatedCode")
@IntegrationStage(loadStage = IntegrateStage.onLoadAfter)
public class WorldGuardIntegration extends AbstractQSIntegratedPlugin {
    private final StateFlag createFlag = createOrGet("quickshop-create", false);
    private final StateFlag tradeFlag = createOrGet("quickshop-trade", true);
    private List<WorldGuardFlags> createFlags;
    private List<WorldGuardFlags> tradeFlags;
    private boolean anyOwner;
    private boolean whiteList;
    private boolean respectGlobalRegion;
    private boolean load = false;

    public WorldGuardIntegration(QuickShop plugin) {
        super(plugin);
        plugin.getReloadManager().register(this);
    }

    private StateFlag createOrGet(String key, boolean def) {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        Flag<?> result = WorldGuard.getInstance().getFlagRegistry().get(key);
        if (!(result instanceof StateFlag)) {
            result = new StateFlag(key, def);
            try {
                registry.register(result);
                plugin.getLogger().info(ChatColor.GREEN + getName() + " flags register successfully.");
                Util.debugLog("Success register " + getName() + " flags.");
            } catch (FlagConflictException | IllegalStateException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to register " + getName() + " flags.", e);
            }
        }
        return (StateFlag) result;
    }

    @Override
    public void load() {
        if (load) {
            return;
        }
        init();
        load = true;
    }

    private void init() {
        this.whiteList = plugin.getConfig().getBoolean("integration.worldguard.whitelist-mode");
        this.anyOwner = plugin.getConfig().getBoolean("integration.worldguard.any-owner");
        respectGlobalRegion = plugin.getConfig().getBoolean("integration.worldguard.respect-global-region");
        createFlags =
                WorldGuardFlags.deserialize(
                        plugin.getConfig().getStringList("integration.worldguard.create"));
        tradeFlags =
                WorldGuardFlags.deserialize(
                        plugin.getConfig().getStringList("integration.worldguard.trade"));
    }

    @Override
    public void unload() {
        load = false;
    }

    private void checkIfLoaded() {
        if (!load) {
            load();
            Util.debugLog(getName() + " Integration not loaded, loading...");
        }
    }

    @Override
    public @NotNull String getName() {
        return "WorldGuard";
    }

    @Override
    public boolean canCreateShopHere(@NotNull Player player, @NotNull Location location) {
        checkIfLoaded();
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        com.sk89q.worldedit.util.Location wgLoc = BukkitAdapter.adapt(location);
        boolean canBypass =
                WorldGuard.getInstance()
                        .getPlatform()
                        .getSessionManager()
                        .hasBypass(localPlayer, BukkitAdapter.adapt(location.getWorld()));
        if (canBypass) {
            Util.debugLog(
                    "Player "
                            + player.getName()
                            + " bypassing the protection checks, because player have bypass permission in WorldGuard");
            return true;
        }
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet applicableRegionSet = query.getApplicableRegions(wgLoc);
        //Regions not included global one
        if (applicableRegionSet.getRegions().isEmpty()) {
            if (!respectGlobalRegion) {
                return !whiteList;
            } else {
                //So check it manually
                RegionManager worldManger = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(location.getWorld()));
                if (worldManger != null && !worldManger.hasRegion("__global__")) {
                    //If not have, just check whitelist
                    return !whiteList;
                }
            }
        }
        //Passed, but flag calculation will include the global region
        for (WorldGuardFlags flag : createFlags) {
            switch (flag) {
                case BUILD:
                    if (!applicableRegionSet.testState(localPlayer, Flags.BUILD)) {
                        return false;
                    }
                    break;
                case FLAG:
                    if (!applicableRegionSet.testState(localPlayer, this.createFlag)) {
                        return false;
                    }
                    break;
                case CHEST_ACCESS:
                    if (!applicableRegionSet.testState(localPlayer, Flags.CHEST_ACCESS)) {
                        return false;
                    }
                    break;
                case INTERACT:
                    if (!applicableRegionSet.testState(localPlayer, Flags.INTERACT)) {
                        return false;
                    }
                    break;
                case OWN:
                    if (anyOwner) {
                        if (applicableRegionSet.getRegions().stream().noneMatch(region -> region.isOwner(localPlayer))) {
                            return false;
                        }
                    } else {
                        if (!applicableRegionSet.isOwnerOfAll(localPlayer)) {
                            return false;
                        }
                    }
                default:
                    // do nothing
            }
        }
        return true;
    }

    @Override
    public boolean canTradeShopHere(@NotNull Player player, @NotNull Location location) {
        checkIfLoaded();
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        com.sk89q.worldedit.util.Location wgLoc = BukkitAdapter.adapt(location);
        boolean canBypass =
                WorldGuard.getInstance()
                        .getPlatform()
                        .getSessionManager()
                        .hasBypass(localPlayer, BukkitAdapter.adapt(location.getWorld()));
        if (canBypass) {
            Util.debugLog(
                    "Player "
                            + player.getName()
                            + " bypassing the protection checks, because player have bypass permission in WorldGuard");
            return true;
        }
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet applicableRegionSet = query.getApplicableRegions(wgLoc);
        //Regions not included global one
        if (applicableRegionSet.getRegions().isEmpty()) {
            if (!respectGlobalRegion) {
                return !whiteList;
            } else {
                //So check it manually
                RegionManager worldManger = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(location.getWorld()));
                if (worldManger != null && !worldManger.hasRegion("__global__")) {
                    //If not have, just check whitelist
                    return !whiteList;
                }
            }
        }
        //Passed, but flag calculation will include the global region
        for (WorldGuardFlags flag : tradeFlags) {
            switch (flag) {
                case BUILD:
                    if (!applicableRegionSet.testState(localPlayer, Flags.BUILD)) {
                        return false;
                    }
                    break;
                case FLAG:
                    if (!applicableRegionSet.testState(localPlayer, this.tradeFlag)) {
                        return false;
                    }
                    break;
                case CHEST_ACCESS:
                    if (!applicableRegionSet.testState(localPlayer, Flags.CHEST_ACCESS)) {
                        return false;
                    }
                    break;
                case INTERACT:
                    if (!applicableRegionSet.testState(localPlayer, Flags.INTERACT)) {
                        return false;
                    }
                    break;
                case OWN:
                    if (anyOwner) {
                        return applicableRegionSet.getRegions().stream().anyMatch(region -> region.isOwner(localPlayer));
                    } else {
                        return applicableRegionSet.isOwnerOfAll(localPlayer);
                    }
                default:
                    // do nothing
            }
        }
        return true;
    }

    /**
     * Callback for reloading
     *
     * @return Reloading success
     */
    @Override
    public ReloadResult reloadModule() {
        init();
        return ReloadResult.builder().status(ReloadStatus.REQUIRE_RESTART).build();
    }
}
