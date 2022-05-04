/* V
 *  This file is a part of project QuickShop, the name is SystemInfoItem.java
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

package com.ghostchu.quickshop.util.paste.v2.item;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.util.ReflectFactory;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.paste.v2.util.HTMLTable;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public class ServerInfoItem implements SubPasteItem {
    private final String serverName;
    private final String build;
    private final String nmsVersion;
    private final String dataVersion;
    private String moddedServerType;

    private final String players;

    private final String onlineMode;

    private final String bukkitVersion;
    private final String mcVersion;
    private final String worldContainer;

    public ServerInfoItem() {
        QuickShop plugin = QuickShop.getInstance();
        this.serverName = plugin.getServer().getName();
        this.build = plugin.getServer().getVersion();
        this.nmsVersion = ReflectFactory.getNMSVersion();
        this.dataVersion = String.valueOf(plugin.getServer().getUnsafe().getDataVersion());
        this.moddedServerType = "Bukkit";
        if (PaperLib.isSpigot())
            this.moddedServerType = "Spigot";
        if (PaperLib.isPaper())
            this.moddedServerType = "Paper";
        if (plugin.getEnvironmentChecker().isFabricBasedServer())
            this.moddedServerType = "Fabric";
        if (plugin.getEnvironmentChecker().isForgeBasedServer())
            this.moddedServerType = "Forge";
        this.players = Bukkit.getOnlinePlayers().size() + "/" + Bukkit.getMaxPlayers();
        this.onlineMode = Util.boolean2Status(Bukkit.getOnlineMode());
        this.bukkitVersion = plugin.getServer().getVersion();
        this.mcVersion = plugin.getPlatform().getMinecraftVersion();
        this.worldContainer = Bukkit.getWorldContainer().getPath();
    }


    @Override
    public @NotNull String getTitle() {
        return "Server Information";
    }

    @NotNull
    private String buildContent() {
        HTMLTable table = new HTMLTable(2, true);
        table.insert("Server Name", serverName);
        table.insert("Build", build);
        table.insert("NMS Version", nmsVersion);
        table.insert("Data Version", dataVersion);
        table.insert("Modded Server Type", moddedServerType);
        table.insert("Players", players);
        table.insert("Online Mode", onlineMode);
        table.insert("Bukkit Version", bukkitVersion);
        table.insert("MC Version", mcVersion);
        table.insert("World Container", worldContainer);
        return table.render();
    }


    @Override
    public @NotNull String genBody() {
        return buildContent();
    }
}
