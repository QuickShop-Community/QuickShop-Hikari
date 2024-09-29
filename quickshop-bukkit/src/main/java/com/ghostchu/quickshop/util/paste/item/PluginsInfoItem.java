package com.ghostchu.quickshop.util.paste.item;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.util.paste.util.HTMLTable;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class PluginsInfoItem implements SubPasteItem {

  @Override
  public @NotNull String genBody() {

    return buildContent();
  }

  @Override
  public @NotNull String getTitle() {

    return "Plugins";
  }

  @NotNull
  private String buildContent() {

    final HTMLTable table = new HTMLTable(6);
    table.setTableTitle("Name", "Status", "Version", "API Version", "Addon", "Main-Class");
    for(final Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
      table.insert(plugin.getName(),
                   CommonUtil.boolean2Status(plugin.isEnabled()),
                   plugin.getDescription().getVersion(),
                   Objects.requireNonNullElse(plugin.getDescription().getAPIVersion(), "N/A"),
                   isAddon(plugin)? "Yes" : "",
                   plugin.getDescription().getMain());
    }
    return table.render();
  }

  private boolean isAddon(final Plugin plugin) {

    final String plugName = QuickShop.getInstance().getJavaPlugin().getName();
    return plugin.getDescription().getDepend().contains(plugName) || plugin.getDescription().getSoftDepend().contains(plugName);
  }
}
