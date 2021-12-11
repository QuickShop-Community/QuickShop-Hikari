/*
 * This file is a part of project QuickShop, the name is ResidenceIntegration.java
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

package org.maxgamer.quickshop.integration.residence;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
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
import java.util.Map;

@SuppressWarnings("DuplicatedCode")
@IntegrationStage(loadStage = IntegrateStage.onEnableAfter)
public class ResidenceIntegration extends AbstractQSIntegratedPlugin {
    private static final String CREATE_FLAG = "quickshop-create";
    private static final String TRADE_FLAG = "quickshop-trade";
    private List<String> createLimits;
    private List<String> tradeLimits;
    private boolean whiteList;

    public ResidenceIntegration(QuickShop plugin) {
        super(plugin);
        plugin.getReloadManager().register(this);
        init();
    }

    private void init() {
        this.whiteList = plugin.getConfig().getBoolean("integration.residence.whitelist-mode");
        this.createLimits = plugin.getConfig().getStringList("integration.residence.create");
        this.tradeLimits = plugin.getConfig().getStringList("integration.residence.trade");
    }

    @Override
    public @NotNull String getName() {
        return "Residence";
    }

    private boolean playerHas(FlagPermissions permissions, Player player, String name, boolean def) {
        Flags internalFlag = Flags.getFlag(name);
        if (internalFlag == null) {
            Map<String, Boolean> permPlayerMap = permissions.getPlayerFlags(player.getName());
            Map<String, Boolean> permGlobalMap = permissions.getFlags();
            if (permPlayerMap != null) {
                return permPlayerMap.getOrDefault(name, permGlobalMap.getOrDefault(name, def));
            } else {
                return permGlobalMap.getOrDefault(name, def);
            }
        } else {
            return permissions.playerHas(player, internalFlag, def);
        }
    }

    @Override
    public boolean canCreateShopHere(@NotNull Player player, @NotNull Location location) {
        ClaimedResidence residence = Residence.getInstance().getResidenceManager().getByLoc(location);
        for (String limit : this.createLimits) {
            if ("FLAG".equalsIgnoreCase(limit)) {
                if (residence == null) {
                    // Check world permission
                    if (!playerHas(Residence.getInstance()
                                    .getWorldFlags()
                                    .getPerms(location.getWorld().getName())
                            , player, CREATE_FLAG, !whiteList)) {
                        return false;
                    }
                } else {
                    if (!playerHas(residence
                                    .getPermissions()
                            , player, CREATE_FLAG, false)) {
                        return false;
                    }
                }
            } else {
                // Not flag
                if (residence == null) {
                    if (!playerHas(Residence.getInstance()
                                    .getWorldFlags()
                                    .getPerms(location.getWorld().getName())
                            , player, limit, !whiteList)) {
                        return false;
                    }
                } else {
                    if (!playerHas(residence.getPermissions(), player, limit, false)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean canTradeShopHere(@NotNull Player player, @NotNull Location location) {
        ClaimedResidence residence = Residence.getInstance().getResidenceManager().getByLoc(location);

        for (String limit : this.tradeLimits) {
            if ("FLAG".equalsIgnoreCase(limit)) {
                if (residence == null) {
                    // Check world permission
                    if (!playerHas(Residence.getInstance()
                                    .getWorldFlags()
                                    .getPerms(location.getWorld().getName())
                            , player, TRADE_FLAG, !whiteList)) {
                        return false;
                    }
                } else {
                    if (!playerHas(residence
                                    .getPermissions()
                            , player, TRADE_FLAG, true)) {
                        return false;
                    }
                }
            } else {
                // Not flag
                if (residence == null) {
                    if (!playerHas(Residence.getInstance()
                                    .getWorldFlags()
                                    .getPerms(location.getWorld().getName())
                            , player, limit, !whiteList)) {
                        return false;
                    }
                } else {
                    if (!playerHas(residence.getPermissions(), player, limit, false)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public void load() {
        FlagPermissions.addFlag(CREATE_FLAG);
        FlagPermissions.addFlag(TRADE_FLAG);
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
