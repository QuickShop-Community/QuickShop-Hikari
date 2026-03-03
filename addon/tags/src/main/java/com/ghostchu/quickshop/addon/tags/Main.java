package com.ghostchu.quickshop.addon.tags;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.addon.tags.command.SubCommand_Tag;
import com.ghostchu.quickshop.api.command.CommandContainer;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin implements Listener {

  static Main instance;
  private QuickShop plugin;

  @Override
  public void onLoad() {

    instance = this;
  }

  @Override
  public void onDisable() {

    HandlerList.unregisterAll((Plugin)this);
  }

  @Override
  public void onEnable() {

    saveDefaultConfig();
    plugin = QuickShop.getInstance();
    getLogger().info("Registering tags commands...");
    Bukkit.getPluginManager().registerEvents(this, this);
    plugin.getCommandManager().registerCmd(
            CommandContainer
                    .builder()
                    .prefix("tag")
                    .description((locale)->plugin.text().of("addon.list.commands.list").forLocale(locale))
                    .selectivePermission("quickshopaddon.list.self")
                    .selectivePermission("quickshopaddon.list.other")
                    .executor(new SubCommand_Tag(plugin))
                    .build());
  }
}
