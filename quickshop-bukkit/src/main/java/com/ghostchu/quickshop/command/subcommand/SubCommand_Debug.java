/*
 *  This file is a part of project QuickShop, the name is SubCommand_Debug.java
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

package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.util.MsgUtil;
import lombok.AllArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.util.BlockIterator;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

@AllArgsConstructor
public class SubCommand_Debug implements CommandHandler<CommandSender> {

    private final QuickShop plugin;

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (cmdArg.length < 1) {
            switchDebug(sender);
            return;
        }

        switch (cmdArg[0]) {
            case "debug", "dev", "devmode" -> switchDebug(sender);
            case "handlerlist" -> handleHandlerList(sender, ArrayUtils.remove(cmdArg, 0));
            case "signs" -> {
                final BlockIterator bIt = new BlockIterator((LivingEntity) sender, 10);
                if (!bIt.hasNext()) {
                    plugin.text().of(sender, "not-looking-at-shop").send();
                    return;
                }
                while (bIt.hasNext()) {
                    final Block b = bIt.next();
                    final Shop shop = plugin.getShopManager().getShop(b.getLocation());
                    if (shop != null) {
                        shop.getSigns().forEach(sign -> MsgUtil.sendDirectMessage(sender, Component.text("Sign located at: " + sign.getLocation()).color(NamedTextColor.GREEN)));
                        break;
                    }
                }
            }
            case "database" -> handleDatabase(sender, ArrayUtils.remove(cmdArg, 0));
            default -> MsgUtil.sendDirectMessage(sender, Component.text("Error! No correct arguments were entered!."));
        }
    }

    private void handleDatabase(@NotNull CommandSender sender, @NotNull String[] cmdArg) {
        if (cmdArg.length < 1) {
            MsgUtil.sendDirectMessage(sender, Component.text("You must specific a operation!"));
            return;
        }
        switch (cmdArg[0]) {
            case "sql" -> {
                handleDatabaseSQL(sender, ArrayUtils.remove(cmdArg, 0));
            }
        }
    }

    private void handleDatabaseSQL(@NotNull CommandSender sender, @NotNull String[] cmdArg) {
        if (cmdArg.length < 1) {
            MsgUtil.sendDirectMessage(sender, Component.text("You must enter an valid Base64 encoded SQL!"));
            return;
        }
        try {
            byte[] b = Base64.getDecoder().decode(cmdArg[0]);
            String sql = new String(b, StandardCharsets.UTF_8);
            Integer lines = plugin.getSqlManager().executeSQL(sql);
            MsgUtil.sendDirectMessage(sender, Component.text("Executed SQL and affected " + lines + " lines."));
        } catch (Exception e) {
            MsgUtil.sendDirectMessage(sender, Component.text("You must enter an valid Base64 encoded SQL!"));
            e.printStackTrace();
        }
    }

    private void handleHandlerList(@NotNull CommandSender sender, @NotNull String[] cmdArg) {
        if (cmdArg.length < 1) {
            MsgUtil.sendDirectMessage(sender, Component.text("You must enter an Bukkit Event class"));
            return;
        }
        printHandlerList(sender, cmdArg[1]);
    }

    @NotNull
    @Override
    public List<String> onTabComplete(
            @NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return Collections.emptyList();
    }

    public void switchDebug(@NotNull CommandSender sender) {
        final boolean debug = plugin.getConfig().getBoolean("dev-mode");

        if (debug) {
            plugin.reloadConfig();
            plugin.getConfig().set("dev-mode", false);
            plugin.saveConfig();
            plugin.getReloadManager().reload();
            plugin.text().of(sender, "command.now-nolonger-debuging").send();
            return;
        }

        plugin.reloadConfig();
        plugin.getConfig().set("dev-mode", true);
        plugin.saveConfig();
        plugin.getReloadManager().reload();
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
                        LegacyComponentSerializer.legacySection().deserialize(ChatColor.AQUA
                                + listener1.getPlugin().getName()
                                + ChatColor.YELLOW
                                + " # "
                                + ChatColor.GREEN
                                + listener1.getListener().getClass().getCanonicalName()));
            }
        } catch (Exception th) {
            MsgUtil.sendDirectMessage(sender, Component.text("ERR " + th.getMessage()).color(NamedTextColor.RED));
            plugin.getLogger().log(Level.WARNING, "An error has occurred while getting the HandlerList", th);
        }
    }

}
