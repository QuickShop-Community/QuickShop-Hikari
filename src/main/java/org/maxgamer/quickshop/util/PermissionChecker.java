/*
 * This file is a part of project QuickShop, the name is PermissionChecker.java
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

import com.griefcraft.lwc.LWC;
import com.griefcraft.lwc.LWCPlugin;
import com.griefcraft.model.Protection;
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
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.event.ProtectionCheckStatus;
import org.maxgamer.quickshop.api.event.ShopProtectionCheckEvent;
import org.maxgamer.quickshop.api.eventmanager.QuickEventManager;
import org.maxgamer.quickshop.eventmanager.BukkitEventManager;
import org.maxgamer.quickshop.eventmanager.QSEventManager;
import org.maxgamer.quickshop.util.holder.Result;
import org.maxgamer.quickshop.util.reload.ReloadResult;
import org.maxgamer.quickshop.util.reload.ReloadStatus;
import org.maxgamer.quickshop.util.reload.Reloadable;
import org.primesoft.blockshub.BlocksHubBukkit;

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
        listenerBlacklist.removeIf(rule -> rule.equalsIgnoreCase("ignored_listener")); // Remove default demo rule
        if(listenerBlacklist.isEmpty()){
            this.eventManager = new BukkitEventManager();
        }else{
            this.eventManager = new QSEventManager(plugin);
            plugin.getLogger().info("Loaded "+ listenerBlacklist.size()+" rules for listener blacklist.");
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
            Util.debugLog("Skipping protection checking in world " + block.getWorld().getName() + " causing it in blacklist.");
            return Result.SUCCESS;
        }

        if (plugin.getLwcPlugin() != null) {
            LWCPlugin lwc = (LWCPlugin) plugin.getLwcPlugin();
            LWC lwcInstance = lwc.getLWC();
            if (lwcInstance != null) {
                Protection protection = lwcInstance.findProtection(block.getLocation());
                if (protection != null && !protection.isOwner(player)) {
                    Util.debugLog("LWC reporting player no permission to access this block.");
                    return new Result("LWC");
                }
            }

        }

        if (plugin.getBlockHubPlugin() != null) {
            BlocksHubBukkit blocksHubBukkit = (BlocksHubBukkit) plugin.getBlockHubPlugin();
            boolean bhCanBuild = blocksHubBukkit.getApi().hasAccess(player.getUniqueId(), blocksHubBukkit.getApi().getWorld(block.getWorld().getName()), block.getX(), block.getY(), block.getZ());
            if (plugin.getConfig().getBoolean("plugin.BlockHub.only")) {
                Util.debugLog("BlockHub only mode response: " + bhCanBuild);
                return new Result("BlockHub");
            } else {
                if (!bhCanBuild) {
                    Util.debugLog("BlockHub reporting player no permission to access this region.");
                    return new Result("BlockHub");
                }
            }
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
                    Util.debugLog("An plugin blocked the protection checking event! See this stacktrace:");
                    for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
                        Util.debugLog(element.getClassName() + "." + element.getMethodName() + "(" + element.getLineNumber() + ")");
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
        plugin.getCompatibilityManager().toggleProtectionListeners(false, player);
        this.eventManager.callEvent(beMainHand);
        plugin.getCompatibilityManager().toggleProtectionListeners(true, player);

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
