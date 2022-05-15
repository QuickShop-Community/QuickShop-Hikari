/*
 *  This file is a part of project QuickShop, the name is PermissionChecker.java
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

package com.ghostchu.quickshop.util;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.event.ProtectionCheckStatus;
import com.ghostchu.quickshop.api.event.ShopProtectionCheckEvent;
import com.ghostchu.quickshop.api.eventmanager.QuickEventManager;
import com.ghostchu.quickshop.eventmanager.BukkitEventManager;
import com.ghostchu.quickshop.eventmanager.QSEventManager;
import com.ghostchu.quickshop.util.holder.Result;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.simplereloadlib.ReloadResult;
import com.ghostchu.simplereloadlib.ReloadStatus;
import com.ghostchu.simplereloadlib.Reloadable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A helper to resolve issue around other plugins with BlockBreakEvent
 *
 * @author Ghost_chu and sandtechnology
 */
public class PermissionChecker implements Reloadable {
    private final QuickShop plugin;

    private boolean usePermissionChecker;

    private QuickEventManager eventManager;


    public PermissionChecker(@NotNull QuickShop plugin) {
        this.plugin = plugin;
        plugin.getReloadManager().register(this);
        init();
    }

    private void init() {
        usePermissionChecker = this.plugin.getConfig().getBoolean("shop.protection-checking");
        List<String> listenerBlacklist = plugin.getConfig().getStringList("shop.protection-checking-blacklist");
        listenerBlacklist.removeIf("ignored_listener"::equalsIgnoreCase); // Remove default demo rule
        if (listenerBlacklist.isEmpty()) {
            this.eventManager = new BukkitEventManager();
        } else {
            this.eventManager = new QSEventManager(plugin);
            plugin.getLogger().info("Loaded " + listenerBlacklist.size() + " rules for listener blacklist.");
        }
        plugin.getLogger().info("EventManager selected: " + this.eventManager.getClass().getSimpleName());
    }

    /**
     * Check player can build in target location
     *
     * @param player   Target player
     * @param location Target location
     * @return Result represent if you can build there
     */
    @NotNull
    public Result canBuild(@NotNull Player player, @NotNull Location location) {
        return canBuild(player, location.getBlock());
    }

    /**
     * Check player can build in target block
     *
     * @param player Target player
     * @param block  Target block
     * @return Result represent if you can build there
     */
    @NotNull
    public Result canBuild(@NotNull Player player, @NotNull Block block) {

        if (plugin.getConfig().getStringList("shop.protection-checking-blacklist").contains(block.getWorld().getName())) {
            Log.debug("Skipping protection checking in world " + block.getWorld().getName() + " causing it in blacklist.");
            return Result.SUCCESS;
        }

        if (!usePermissionChecker) {
            return Result.SUCCESS;
        }
        final Result isCanBuild = new Result();

        BlockBreakEvent beMainHand;

        beMainHand = new BlockBreakEvent(block, player) {

            @Override
            public void setCancelled(boolean cancel) {
                //tracking cancel plugin
                if (cancel && !isCancelled()) {
                    Log.debug("An plugin blocked the protection checking event! See this stacktrace:");
                    for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
                        Log.debug(element.getClassName() + "." + element.getMethodName() + "(" + element.getLineNumber() + ")");
                    }
                    isCanBuild.setMessage(Thread.currentThread().getStackTrace()[2].getClassName());
                    out:
                    for (StackTraceElement element : Thread.currentThread().getStackTrace()) {

                        for (RegisteredListener listener : getHandlerList().getRegisteredListeners()) {
                            if (listener.getListener().getClass().getName().equals(element.getClassName())) {
                                isCanBuild.setResult(false);
                                isCanBuild.setMessage(listener.getPlugin().getName());
                                isCanBuild.setListener(listener.getListener().getClass().getName());
                                break out;
                            }
                        }
                    }
                }
                super.setCancelled(cancel);
            }
        };
        // Call for event for protection check start
        this.eventManager.callEvent(new ShopProtectionCheckEvent(block.getLocation(), player, ProtectionCheckStatus.BEGIN, beMainHand));
        beMainHand.setDropItems(false);
        beMainHand.setExpToDrop(0);

        //register a listener to cancel test event
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler(priority = EventPriority.HIGHEST)
            public void onTestEvent(BlockBreakEvent event) {
                if (event.equals(beMainHand)) {
                    // Call for event for protection check end
                    eventManager.callEvent(
                            new ShopProtectionCheckEvent(
                                    block.getLocation(), player, ProtectionCheckStatus.END, beMainHand));
                    if (!event.isCancelled()) {
                        //Ensure this test will no be logged by some plugin
                        beMainHand.setCancelled(true);
                        isCanBuild.setResult(true);
                    }
                    HandlerList.unregisterAll(this);
                }
            }
        }, plugin);
        this.eventManager.callEvent(beMainHand);

        return isCanBuild;
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
