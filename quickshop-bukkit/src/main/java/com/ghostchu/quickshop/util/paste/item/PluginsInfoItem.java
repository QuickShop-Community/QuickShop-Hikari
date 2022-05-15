/*
 *  This file is a part of project QuickShop, the name is PluginsInfoItem.java
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

package com.ghostchu.quickshop.util.paste.item;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.paste.util.HTMLTable;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class PluginsInfoItem implements SubPasteItem {
    @Override
    public @NotNull String getTitle() {
        return "Plugins";
    }

    private boolean isAddon(Plugin plugin) {
        return plugin.getDescription().getDepend().contains(QuickShop.getInstance().getName()) || plugin.getDescription().getSoftDepend().contains(QuickShop.getInstance().getName());
    }

    @NotNull
    private String buildContent() {
        HTMLTable table = new HTMLTable(6);
        table.setTableTitle("Name", "Status", "Version", "API Version", "Addon", "Main-Class");
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            table.insert(plugin.getName(),
                    Util.boolean2Status(plugin.isEnabled()),
                    plugin.getDescription().getVersion(),
                    Objects.requireNonNullElse(plugin.getDescription().getAPIVersion(), "N/A"),
                    isAddon(plugin) ? "Yes" : "",
                    plugin.getDescription().getMain());
        }
        return table.render();
    }


    @Override
    public @NotNull String genBody() {
        return buildContent();
    }
}
