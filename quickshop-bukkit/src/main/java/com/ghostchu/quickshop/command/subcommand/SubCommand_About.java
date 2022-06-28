/*
 *  This file is a part of project QuickShop, the name is SubCommand_About.java
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
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.Util;
import lombok.AllArgsConstructor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class SubCommand_About implements CommandHandler<CommandSender> {
    private final QuickShop plugin;

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        MsgUtil.sendDirectMessage(sender, LegacyComponentSerializer.legacySection().deserialize(ChatColor.AQUA + "QuickShop " + ChatColor.YELLOW + QuickShop.getFork()));
        MsgUtil.sendDirectMessage(sender, LegacyComponentSerializer.legacySection().deserialize(ChatColor.AQUA
                + "Version "
                + ChatColor.YELLOW
                + ">> "
                + ChatColor.GREEN
                + QuickShop.getVersion()));
        if (plugin.getBuildInfo().getGitInfo().getBranch().toUpperCase().contains("ORIGIN/LTS")) {
            MsgUtil.sendDirectMessage(sender, LegacyComponentSerializer.legacySection().deserialize(
                    ChatColor.AQUA
                            + "Release "
                            + ChatColor.YELLOW
                            + ">> "
                            + ChatColor.GREEN
                            + LegacyComponentSerializer.legacySection().serialize(plugin.text().of(sender, "updatenotify.label.lts").forLocale())));
        } else if (plugin.getBuildInfo().getGitInfo().getBranch().toUpperCase().contains("ORIGIN/RELEASE")) {
            MsgUtil.sendDirectMessage(sender,
                    LegacyComponentSerializer.legacySection().deserialize(ChatColor.AQUA
                            + "Release "
                            + ChatColor.YELLOW
                            + ">> "
                            + ChatColor.GREEN
                            + LegacyComponentSerializer.legacySection().serialize(plugin.text().of(sender, "updatenotify.label.stable").forLocale())));
        } else {
            MsgUtil.sendDirectMessage(sender,
                    LegacyComponentSerializer.legacySection().deserialize(ChatColor.AQUA
                            + "Release "
                            + ChatColor.YELLOW
                            + ">> "
                            + ChatColor.GREEN
                            + LegacyComponentSerializer.legacySection().serialize(plugin.text().of(sender, "updatenotify.label.unstable").forLocale())));
        }
        MsgUtil.sendDirectMessage(sender,
                LegacyComponentSerializer.legacySection().deserialize(ChatColor.AQUA
                        + "Developers "
                        + ChatColor.YELLOW
                        + ">> "
                        + ChatColor.GREEN
                        + Util.list2String(plugin.getDescription().getAuthors())));
        MsgUtil.sendDirectMessage(sender, LegacyComponentSerializer.legacySection().deserialize(ChatColor.GOLD + "Powered by Community"));
        MsgUtil.sendDirectMessage(sender, LegacyComponentSerializer.legacySection().deserialize(ChatColor.RED + "Made with ❤"));
    }


}
