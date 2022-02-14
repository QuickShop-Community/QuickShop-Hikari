/*
 *  This file is a part of project QuickShop, the name is Openinv.java
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

package com.ghostchu.quickshop.compatibility.openinv;

import com.ghostchu.quickshop.api.QuickShopAPI;
import com.ghostchu.quickshop.api.command.CommandContainer;
import com.lishid.openinv.IOpenInv;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class Openinv extends JavaPlugin implements Listener {
    public IOpenInv openInv;
    public OpenInvInventoryManager manager;
    private QuickShopAPI api;

    @Override
    public void onLoad() {
        saveDefaultConfig();
        this.api = (QuickShopAPI) Bukkit.getPluginManager().getPlugin("QuickShop-Hikari");
        openInv = (IOpenInv) Bukkit.getPluginManager().getPlugin("OpenInv");
        manager = new OpenInvInventoryManager(openInv, this);
        this.api.getInventoryWrapperRegistry().register(this, manager);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.api.getCommandManager().registerCmd(CommandContainer.builder().prefix("echest").permission("quickshop.echest").description(LegacyComponentSerializer.legacySection().deserialize(getConfig().getString("messages.description"))).executor(new OpenInvCommand(this)).build());
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    public QuickShopAPI getApi() {
        return api;
    }

    public IOpenInv getOpenInv() {
        return openInv;
    }

    public OpenInvInventoryManager getManager() {
        return manager;
    }
}
