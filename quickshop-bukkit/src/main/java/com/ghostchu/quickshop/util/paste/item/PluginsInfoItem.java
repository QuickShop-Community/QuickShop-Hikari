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
