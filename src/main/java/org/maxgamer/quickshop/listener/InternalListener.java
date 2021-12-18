/*
 * This file is a part of project QuickShop, the name is InternalListener.java
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

package org.maxgamer.quickshop.listener;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.event.*;
import org.maxgamer.quickshop.util.Util;
import org.maxgamer.quickshop.util.logging.container.*;
import org.maxgamer.quickshop.util.reload.ReloadResult;
import org.maxgamer.quickshop.util.reload.ReloadStatus;

import java.util.Objects;


public class InternalListener extends AbstractQSListener {
    private final QuickShop plugin;
    private boolean loggingBalance;
    private boolean loggingAction;

    public InternalListener(QuickShop plugin) {
        super(plugin);
        this.plugin = plugin;
        plugin.getReloadManager().register(this);
        readConfig();
        this.register();
    }

    private void readConfig() {
        this.loggingBalance = plugin.getConfig().getBoolean("logging.log-balance");
        this.loggingAction = plugin.getConfig().getBoolean("logging.log-actions");
    }

    public boolean isForbidden(@NotNull Material shopMaterial, @NotNull Material itemMaterial) {
        if (!Objects.equals(shopMaterial, itemMaterial)) {
            return false;
        }
        return shopMaterial.isBlock() && shopMaterial.name().toUpperCase().endsWith("SHULKER_BOX");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void shopCreate(ShopCreateEvent event) {
        if (isForbidden(event.getShop().getLocation().getBlock().getType(), event.getShop().getItem().getType())) {
            event.setCancelled(true);
            plugin.text().of(event.getCreator(), "forbidden-vanilla-behavior").send();
            return;
        }
        if (loggingAction) {
            //Player creator = plugin.getServer().getPlayer(event.getCreator());
            plugin.logEvent(new ShopCreationLog(event.getCreator(), event.getShop().saveToInfoStorage(), event.getShop().getLocation()));

        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void shopDelete(ShopDeleteEvent event) {
        if (loggingAction) {
            plugin.logEvent(new ShopRemoveLog(Util.getNilUniqueId(), "Shop removed", event.getShop().saveToInfoStorage()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void shopModeratorChanges(ShopModeratorChangedEvent event) {
        if (loggingAction) {
            plugin.logEvent(new ShopModeratorChangedLog(event.getShop().saveToInfoStorage(), event.getModerator()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void shopPriceChanges(ShopPriceChangeEvent event) {
        if (loggingAction) {
            plugin.logEvent(new ShopPriceChangedLog(event.getShop().saveToInfoStorage(), event.getOldPrice(), event.getOldPrice()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void shopPrePurchase(ShopPurchaseEvent event) {
        if (isForbidden(event.getShop().getLocation().getBlock().getType(), event.getShop().getItem().getType())) {
            event.setCancelled(true);
            plugin.text().of(event.getPurchaser(), "forbidden-vanilla-behavior").send();
            return;
        }
        if (loggingBalance) {
            plugin.logEvent(new PlayerEconomyPreCheckLog(true, event.getPurchaser(), plugin.getEconomy().getBalance(event.getPurchaser(), event.getShop().getLocation().getWorld(), event.getShop().getCurrency())));
            plugin.logEvent(new PlayerEconomyPreCheckLog(true, event.getShop().getOwner(), plugin.getEconomy().getBalance(event.getShop().getOwner(), event.getShop().getLocation().getWorld(), event.getShop().getCurrency())));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void shopPurchase(ShopSuccessPurchaseEvent event) {
        if (loggingAction) {
            plugin.logEvent(new ShopPurchaseLog(event.getShop().saveToInfoStorage(),
                    event.getShop().getShopType(),
                    event.getPurchaser(),
                    Util.getItemStackName(event.getShop().getItem()),
                    Util.serialize(event.getShop().getItem()),
                    event.getAmount(),
                    event.getBalance(),
                    event.getTax()));
        }
        if (loggingBalance) {
            plugin.logEvent(new PlayerEconomyPreCheckLog(false, event.getPurchaser(), plugin.getEconomy().getBalance(event.getPurchaser(), event.getShop().getLocation().getWorld(), event.getShop().getCurrency())));
            plugin.logEvent(new PlayerEconomyPreCheckLog(false, event.getShop().getOwner(), plugin.getEconomy().getBalance(event.getShop().getOwner(), event.getShop().getLocation().getWorld(), event.getShop().getCurrency())));
        }
        if (event.getPurchaser().equals(event.getShop().getOwner())) {
            Player player = Bukkit.getPlayer(event.getPurchaser());
            if (player != null) {
                plugin.text().of(player, "shop-owner-self-trade").send();
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void shopInventoryCalc(ShopInventoryCalculateEvent event) {
        plugin.getDatabaseHelper().updateExternalInventoryProfileCache(event.getShop(), event.getSpace(), event.getStock());
    }


    /**
     * Callback for reloading
     *
     * @return Reloading success
     */
    @Override
    public ReloadResult reloadModule() {
        readConfig();
        return ReloadResult.builder().status(ReloadStatus.SUCCESS).build();
    }
}
