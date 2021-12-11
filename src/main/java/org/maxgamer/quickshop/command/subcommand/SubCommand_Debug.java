/*
 * This file is a part of project QuickShop, the name is SubCommand_Debug.java
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

package org.maxgamer.quickshop.command.subcommand;

import lombok.AllArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.util.BlockIterator;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.command.CommandHandler;
import org.maxgamer.quickshop.api.shop.Shop;
import org.maxgamer.quickshop.util.MsgUtil;
import org.maxgamer.quickshop.util.Util;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;

@AllArgsConstructor
public class SubCommand_Debug implements CommandHandler<CommandSender> {

    private final QuickShop plugin;
    private final List<String> tabCompleteList = Collections.unmodifiableList(
            Arrays.asList("debug", "dev", "devmode", "handlerlist", "jvm", "signs")
    );

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (cmdArg.length < 1) {
            switchDebug(sender);
            return;
        }

        switch (cmdArg[0]) {
            case "debug":
            case "dev":
            case "devmode":
                switchDebug(sender);
                break;
            case "handlerlist":
                if (cmdArg.length < 2) {
                    MsgUtil.sendDirectMessage(sender, "You must enter an event class");
                    break;
                }

                printHandlerList(sender, cmdArg[1]);
                break;
            case "jvm":
                RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();

                List<String> arguments = runtimeMxBean.getInputArguments();
                MsgUtil.sendDirectMessage(sender,
                        ChatColor.GOLD + "Arguments: " + ChatColor.AQUA + Util.list2String(arguments));
                MsgUtil.sendDirectMessage(sender, ChatColor.GOLD + "Name: " + ChatColor.AQUA + runtimeMxBean.getName());
                MsgUtil.sendDirectMessage(sender,
                        ChatColor.GOLD + "VM Name: " + ChatColor.AQUA + runtimeMxBean.getVmName());
                MsgUtil.sendDirectMessage(sender,
                        ChatColor.GOLD + "Uptime: " + ChatColor.AQUA + runtimeMxBean.getUptime());
                MsgUtil.sendDirectMessage(sender,
                        ChatColor.GOLD + "JVM Ver: " + ChatColor.AQUA + runtimeMxBean.getVmVersion());
                Map<String, String> sys = runtimeMxBean.getSystemProperties();
                List<String> sysData = new ArrayList<>();
                sys.keySet().forEach(key -> sysData.add(key + "=" + sys.get(key)));
                MsgUtil.sendDirectMessage(sender,
                        ChatColor.GOLD + "Sys Pro: " + ChatColor.AQUA + Util.list2String(sysData));
                break;
            case "signs":
                final BlockIterator bIt = new BlockIterator((LivingEntity) sender, 10);
                if (!bIt.hasNext()) {
                    plugin.text().of(sender, "not-looking-at-shop").send();
                    return;
                }
                while (bIt.hasNext()) {
                    final Block b = bIt.next();
                    final Shop shop = plugin.getShopManager().getShop(b.getLocation());
                    if (shop != null) {
                        shop.getSigns().forEach(sign -> MsgUtil.sendDirectMessage(sender, ChatColor.GREEN + "Sign located at: " + sign.getLocation()));
                        break;
                    }
                }
                break;
            default:
                MsgUtil.sendDirectMessage(sender, "Error! No correct arguments were entered!.");
                break;
        }
    }

    @NotNull
    @Override
    public List<String> onTabComplete(
            @NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return tabCompleteList;
    }

    public void switchDebug(@NotNull CommandSender sender) {
        final boolean debug = plugin.getConfig().getBoolean("dev-mode");

        if (debug) {
            plugin.getConfig().set("dev-mode", false);
            plugin.saveConfiguration();
            plugin.reload();
            plugin.text().of(sender, "command.now-nolonger-debuging").send();
            return;
        }

        plugin.getConfig().set("dev-mode", true);
        plugin.saveConfiguration();
        plugin.reload();
        plugin.text().of(sender, "command.now-debuging").send();
    }

    public void printHandlerList(@NotNull CommandSender sender, String event) {
        try {
            final Class<?> clazz = Class.forName(event);
            final Method method = clazz.getMethod("getHandlerList");
            final Object[] obj = new Object[0];
            final HandlerList list = (HandlerList) method.invoke(null, obj);

            for (RegisteredListener listener1 : list.getRegisteredListeners()) {
                MsgUtil.sendDirectMessage(sender,
                        ChatColor.AQUA
                                + listener1.getPlugin().getName()
                                + ChatColor.YELLOW
                                + " # "
                                + ChatColor.GREEN
                                + listener1.getListener().getClass().getCanonicalName());
            }
        } catch (Exception th) {
            MsgUtil.sendDirectMessage(sender, "ERR " + th.getMessage());
            plugin.getLogger().log(Level.WARNING, "An error has occurred while getting the HandlerList", th);
        }
    }

}
