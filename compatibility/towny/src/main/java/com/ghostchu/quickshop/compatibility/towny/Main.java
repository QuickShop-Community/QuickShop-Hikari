/*
 *  This file is a part of project QuickShop, the name is Towny.java
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

package com.ghostchu.quickshop.compatibility.towny;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.QuickShopAPI;
import com.ghostchu.quickshop.api.event.*;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.compatibility.CompatibilityModule;
import com.ghostchu.quickshop.util.Util;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.PlotClearEvent;
import com.palmergames.bukkit.towny.event.TownRemoveResidentEvent;
import com.palmergames.bukkit.towny.event.town.TownUnclaimEvent;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.bukkit.towny.utils.ShopPlotUtil;
import lombok.Getter;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class Main extends CompatibilityModule implements Listener {
    @Getter
    private QuickShopAPI api;
    private List<TownyFlags> createFlags;
    private List<TownyFlags> tradeFlags;
    private boolean whiteList;

    @Override
    public void init() {
        api = (QuickShopAPI) Bukkit.getPluginManager().getPlugin("QuickShop-Hikari");
        createFlags = TownyFlags.deserialize(getConfig().getStringList("create"));
        tradeFlags = TownyFlags.deserialize(getConfig().getStringList("trade"));
        whiteList = getConfig().getBoolean("whitelist-mode");
    }

    private boolean isWorldIgnored(World world) {
        if (getConfig().getBoolean("ignore-disabled-worlds", false)) {
            return !TownyAPI.getInstance().isTownyWorld(world);
        }
        return false;
    }

    @EventHandler(ignoreCancelled = true)
    public void ownerDisplayOverride(ShopOwnerNameGettingEvent event){
        if(!getConfig().getBoolean("allow-owner-name-override",true)){
            return;
        }
        Shop shop = event.getShop();
        // Town name override check
        Town town = TownyShopUtil.getShopTown(shop);
        if(town != null){
            event.setName(LegacyComponentSerializer.legacySection().deserialize(town.getFormattedName()));
            return;
        }
        // Nation name override check
        Nation nation = TownyShopUtil.getShopNation(shop);
        if(nation != null){
            event.setName(LegacyComponentSerializer.legacySection().deserialize(nation.getFormattedName()));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void permissionOverride(ShopAuthorizeCalculateEvent event) {
        if (!getConfig().getBoolean("allow-permission-override", true))
            return;
        Location shopLoc = event.getShop().getLocation();
        if (isWorldIgnored(shopLoc.getWorld())) return;
        Town town = TownyAPI.getInstance().getTown(shopLoc);
        if (town == null) return;
        if (town.getMayor().getUUID().equals(event.getAuthorizer())) {
            if (event.getNamespace().equals(QuickShop.getInstance()) && event.getPermission().equals(BuiltInShopPermission.DELETE.getRawNode())) {
                event.setResult(true);
                return;
            }
        }
        try {
            Nation nation = town.getNation();
            if (nation.getKing().getUUID().equals(event.getAuthorizer())) {
                if (event.getNamespace().equals(QuickShop.getInstance()) && event.getPermission().equals(BuiltInShopPermission.DELETE.getRawNode())) {
                    event.setResult(true);
                }
            }
        } catch (NotRegisteredException ignored) {

        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPreCreation(ShopPreCreateEvent event) {
        if (isWorldIgnored(event.getLocation().getWorld())) return;
        if (checkFlags(event.getPlayer(), event.getLocation(), this.createFlags)) {
            return;
        }
        event.setCancelled(true, "Towny Blocked");
    }

    @EventHandler(ignoreCancelled = true)
    public void onCreation(ShopCreateEvent event) {
        if (isWorldIgnored(event.getShop().getLocation().getWorld())) return;
        //noinspection ConstantConditions
        if (checkFlags(event.getPlayer(), event.getShop().getLocation(), this.createFlags)) {
            return;
        }
        event.setCancelled(true, "Towny Blocked");
    }

    @EventHandler(ignoreCancelled = true)
    public void onTrading(ShopPurchaseEvent event) {
        if (isWorldIgnored(event.getShop().getLocation().getWorld())) return;
        //noinspection ConstantConditions
        if (checkFlags(event.getPlayer(), event.getShop().getLocation(), this.tradeFlags)) {
            return;
        }
        event.setCancelled(true, "Towny Blocked");
    }

    @EventHandler
    public void onPlayerLeave(TownRemoveResidentEvent event) {
        if (isWorldIgnored(event.getTown().getWorld())) return;
        if (!getConfig().getBoolean("delete-shop-on-resident-leave", false)) {
            return;
        }
        Util.mainThreadRun(() -> purgeShops(event.getTown().getTownBlocks(), event.getResident().getUUID(),null,"Town removed a resident"));
    }

    @EventHandler
    public void onPlotClear(PlotClearEvent event) {
        if (isWorldIgnored(event.getTownBlock().getWorldCoord().getBukkitWorld())) return;
        if (!getConfig().getBoolean("delete-shop-on-plot-clear", false)) {
            return;
        }
        Util.mainThreadRun(() -> purgeShops(event.getTownBlock().getWorldCoord(), null,null,"Plot cleared"));
    }

    @EventHandler
    public void onPlotUnclaim(TownUnclaimEvent event) {
        if (isWorldIgnored(event.getWorldCoord().getBukkitWorld())) return;
        if (!getConfig().getBoolean("delete-shop-on-plot-unclaimed")) {
            return;
        }
        Util.mainThreadRun(() -> purgeShops(event.getWorldCoord(), null, null, "Town Unclaimed"));
    }

    public void purgeShops(@NotNull Collection<TownBlock> worldCoords, @Nullable UUID owner, @Nullable UUID deleter, @NotNull String reason) {
        for (TownBlock townBlock : worldCoords) {
            purgeShops(townBlock.getWorldCoord(), owner, deleter, reason);
        }
    }

    public void purgeShops(@NotNull WorldCoord worldCoord, @Nullable UUID owner, @Nullable UUID deleter, @NotNull String reason) {
        //Getting all shop with world-chunk-shop mapping
        for (Shop shop : api.getShopManager().getAllShops()) {
            if (!Objects.equals(shop.getLocation().getWorld(), worldCoord.getBukkitWorld()))
                continue;
            if (WorldCoord.parseWorldCoord(shop.getLocation()).equals(worldCoord)) {
                if (owner != null && shop.getOwner().equals(owner)) {
                    recordDeletion(deleter, shop, reason);
                    shop.delete();
                }
            }
        }
    }

    private boolean checkFlags(@NotNull Player player, @NotNull Location location, @NotNull List<TownyFlags> flags) {
        if (isWorldIgnored(location.getWorld())) return true;
        if (!whiteList && !ShopPlotUtil.isShopPlot(location)) {
            return true;
        }
        for (TownyFlags flag : flags) {
            switch (flag) {
                case OWN:
                    if (!ShopPlotUtil.doesPlayerOwnShopPlot(player, location)) {
                        return false;
                    }
                    break;
                case MODIFY:
                    if (!ShopPlotUtil.doesPlayerHaveAbilityToEditShopPlot(player, location)) {
                        return false;
                    }
                    break;
                case SHOPTYPE:
                    if (!ShopPlotUtil.isShopPlot(location)) {
                        return false;
                    }
                default:
                    // Ignore
            }
        }
        return true;
    }
}
