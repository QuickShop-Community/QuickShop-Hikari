/*
 * This file is a part of project QuickShop, the name is UpdateWatcher.java
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

package org.maxgamer.quickshop.watcher;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitTask;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.util.MsgUtil;
import org.maxgamer.quickshop.util.updater.QuickUpdater;
import org.maxgamer.quickshop.util.updater.VersionType;
import org.maxgamer.quickshop.util.updater.impl.JenkinsUpdater;

import java.util.List;
import java.util.Random;

//TODO: This is a shit, need refactor
public class UpdateWatcher implements Listener {

    private final QuickUpdater updater = new JenkinsUpdater(QuickShop.getInstance().getBuildInfo());
    private final Random random = new Random();
    private BukkitTask cronTask = null;

    public QuickUpdater getUpdater() {
        return updater;
    }

    public BukkitTask getCronTask() {
        return cronTask;
    }

    public void init() {
        cronTask = QuickShop.getInstance().getServer().getScheduler().runTaskTimerAsynchronously(QuickShop.getInstance(), () -> {
            if (!updater.isLatest(getUpdater().getCurrentRunning())) {
                if (updater.getCurrentRunning() == VersionType.STABLE) {
                    QuickShop.getInstance()
                            .getLogger()
                            .info(
                                    "A new version of QuickShop has been released! [" + updater.getRemoteServerVersion() + "]");
                    QuickShop.getInstance()
                            .getLogger()
                            .info("Update here: https://www.spigotmc.org/resources/62575/");

                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (QuickShop.getPermissionManager()
                                .hasPermission(player, "quickshop.alerts")) {
                            List<String> notifys =
                                    QuickShop.getInstance().text().ofList(player, "updatenotify.list").forLocale();
                            int notifyNum = -1;
                            if (notifys.size() > 1) {
                                notifyNum = random.nextInt(notifys.size());
                            }
                            String notify;
                            if (notifyNum > 0) { // Translate bug.
                                notify = notifys.get(notifyNum);
                            } else {
                                notify = "New update {0} now avaliable! Please update!";
                            }
                            notify = MsgUtil.fillArgs(notify, updater.getRemoteServerVersion(), QuickShop.getInstance().getBuildInfo().getBuildTag());
                            player.sendMessage(ChatColor.GREEN + "---------------------------------------------------");
                            player.sendMessage(ChatColor.GREEN + notify);
                            player.sendMessage(ChatColor.GREEN + "Type command " + ChatColor.YELLOW + "/qs update" + ChatColor.GREEN + " or click the link below to update QuickShop :)");
                            player.sendMessage(ChatColor.AQUA + " https://www.spigotmc.org/resources/62575/");
                            player.sendMessage(ChatColor.GREEN + "---------------------------------------------------");
                        }
                    }
                } else {
                    QuickShop.getInstance()
                            .getLogger()
                            .info(
                                    "A new version of QuickShop snapshot has been released! [" + updater.getRemoteServerVersion() + "]");
                    QuickShop.getInstance()
                            .getLogger()
                            .info("Update here: https://ci.codemc.io/job/Ghost-chu/job/QuickShop-Reremake-SNAPSHOT");

                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (QuickShop.getPermissionManager()
                                .hasPermission(player, "quickshop.alerts")) {
                            List<String> notifys =
                                    QuickShop.getInstance().text().ofList(player, "updatenotify.list").forLocale();
                            int notifyNum = -1;
                            if (notifys.size() > 1) {
                                notifyNum = random.nextInt(notifys.size());
                            }
                            String notify;
                            if (notifyNum > 0) { // Translate bug.
                                notify = notifys.get(notifyNum);
                            } else {
                                notify = "New update {0} now avaliable! Please update!";
                            }
                            notify = MsgUtil.fillArgs(notify, updater.getRemoteServerVersion(), QuickShop.getInstance().getBuildInfo().getBuildTag());
                            player.sendMessage(ChatColor.GREEN + "---------------------------------------------------");
                            player.sendMessage(ChatColor.GREEN + notify);
                            player.sendMessage(ChatColor.GREEN + "Type command " + ChatColor.YELLOW + "/qs update" + ChatColor.GREEN + " or click the link below to update QuickShop :)");
                            player.sendMessage(ChatColor.AQUA + " https://ci.codemc.io/job/Ghost-chu/job/QuickShop-Reremake-SNAPSHOT");
                            player.sendMessage(ChatColor.GREEN + "---------------------------------------------------");
                        }
                    }
                }
            }

        }, 1, 20 * 60 * 60);
    }

    public void uninit() {
        if (cronTask == null) {
            return;
        }
        cronTask.cancel();
    }

    @EventHandler
    public void playerJoin(PlayerJoinEvent e) {

        QuickShop.getInstance().getServer().getScheduler().runTaskLaterAsynchronously(QuickShop.getInstance(), () -> {
            if (!QuickShop.getPermissionManager().hasPermission(e.getPlayer(), "quickshop.alerts") || getUpdater().isLatest(getUpdater().getCurrentRunning())) {
                return;
            }
            List<String> notifys = QuickShop.getInstance().text().ofList(e.getPlayer(), "updatenotify.list").forLocale();
            int notifyNum = random.nextInt(notifys.size());
            String notify = notifys.get(notifyNum);
            notify = MsgUtil.fillArgs(notify, updater.getRemoteServerVersion(), QuickShop.getInstance().getBuildInfo().getBuildTag());

            e.getPlayer().sendMessage(ChatColor.GREEN + "---------------------------------------------------");
            e.getPlayer().sendMessage(ChatColor.GREEN + notify);
            e.getPlayer().sendMessage(ChatColor.GREEN + "Type command " + ChatColor.YELLOW + "/qs update" + ChatColor.GREEN + " or click the link below to update QuickShop :)");
            e.getPlayer().sendMessage(ChatColor.AQUA + " https://www.spigotmc.org/resources/62575/");
            e.getPlayer().sendMessage(ChatColor.GREEN + "---------------------------------------------------");
        }, 80);
    }

}
