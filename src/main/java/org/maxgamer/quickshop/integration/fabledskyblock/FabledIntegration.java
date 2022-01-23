/*
 * This file is a part of project QuickShop, the name is FabledIntegration.java
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

package org.maxgamer.quickshop.integration.fabledskyblock;

import com.songoda.skyblock.api.SkyBlockAPI;
import com.songoda.skyblock.api.island.Island;
import com.songoda.skyblock.api.island.IslandRole;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.integration.IntegrateStage;
import org.maxgamer.quickshop.api.integration.IntegrationStage;
import org.maxgamer.quickshop.integration.AbstractQSIntegratedPlugin;
import org.maxgamer.quickshop.util.reload.ReloadResult;
import org.maxgamer.quickshop.util.reload.ReloadStatus;

@IntegrationStage(loadStage = IntegrateStage.onEnableAfter)
public class FabledIntegration extends AbstractQSIntegratedPlugin {

    private boolean ignoreDisabledWorlds;
    private boolean whitelist;

    public FabledIntegration(QuickShop plugin) {
        super(plugin);
        plugin.getReloadManager().register(this);
        loadConfiguration();
        registerListener();
    }

    private void loadConfiguration() {
        ignoreDisabledWorlds = plugin.getConfig().getBoolean("integration.fabledskyblock.ignore-disabled-worlds");
        whitelist = plugin.getConfig().getBoolean("integration.fabledskyblock.whitelist-mode");
    }

    /**
     * Return the integrated plugin name.
     * For example, Residence
     *
     * @return integrated plugin
     */
    @Override
    public @NotNull String getName() {
        return "FabledSkyblock";
    }

    /**
     * Check if a player can create shop here
     *
     * @param player   the player want to create shop
     * @param location shop location
     * @return If you can create shop here
     */
    @Override
    public boolean canCreateShopHere(@NotNull Player player, @NotNull Location location) {
        Island island = SkyBlockAPI.getIslandManager().getIslandAtLocation(location);
        if (island == null) return whitelist;
        return island.getRole(player).equals(IslandRole.MEMBER) || island.getRole(player).equals(IslandRole.OWNER)
                || island.getRole(player).equals(IslandRole.OPERATOR);
    }

    /**
     * Check if a player can trade with shop here
     *
     * @param player   the player want to trade with shop
     * @param location shop location
     * @return If you can trade with shop here
     */
    @Override
    public boolean canTradeShopHere(@NotNull Player player, @NotNull Location location) {
        if (SkyBlockAPI.getIslandManager().getIslandAtLocation(location) == null) {
            return ignoreDisabledWorlds;
        }
        return true;
    }

    /**
     * Check if a player can delete a shop here
     *
     * @param player   the player want to delete the shop
     * @param location shop location
     * @return If you can delete the shop here
     */
    @Override
    public boolean canDeleteShopHere(@NotNull Player player, @NotNull Location location) {
        Island island = SkyBlockAPI.getIslandManager().getIslandAtLocation(location);
        if (island == null) return whitelist;
        return island.getRole(player).equals(IslandRole.MEMBER) || island.getRole(player).equals(IslandRole.OWNER)
                || island.getRole(player).equals(IslandRole.OPERATOR);
    }

    /**
     * Loading logic
     * Execute Stage defined by IntegrationStage
     */
    @Override
    public void load() {

    }

    /**
     * Unloding logic
     * Will execute when Quickshop unloading
     */
    @Override
    public void unload() {

    }

    @Override
    public ReloadResult reloadModule() {
        loadConfiguration();
        return ReloadResult.builder().status(ReloadStatus.SUCCESS).build();
    }
}
