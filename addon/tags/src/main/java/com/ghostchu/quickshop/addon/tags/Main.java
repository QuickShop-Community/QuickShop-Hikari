package com.ghostchu.quickshop.addon.tags;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.addon.tags.command.SubCommand_Avoid;
import com.ghostchu.quickshop.addon.tags.command.SubCommand_Favorite;
import com.ghostchu.quickshop.addon.tags.command.SubCommand_Tag;
import com.ghostchu.quickshop.addon.tags.command.SubCommand_Watch;
import com.ghostchu.quickshop.api.command.CommandContainer;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin implements Listener {

  private static Main instance;

  private QuickShop plugin;
  private TagManager tagManager;

  public static Main instance() {

    return instance;
  }

  public QuickShop quickShop() {

    return plugin;
  }

  public TagManager tagManager() {

    return tagManager;
  }

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
    tagManager = new TagManager(this, plugin);

    Bukkit.getPluginManager().registerEvents(this, this);

    getLogger().info("Registering QuickShop-Tags commands...");


    final SubCommand_Avoid avoidCommand = new SubCommand_Avoid(this, plugin);
    final SubCommand_Favorite favoriteCommand = new SubCommand_Favorite(this, plugin);
    final SubCommand_Tag tagCommand = new SubCommand_Tag(this, plugin, tagManager);
    final SubCommand_Watch watchCommand = new SubCommand_Watch(this, plugin);

    plugin.getCommandManager().registerCmd(
            CommandContainer.builder()
                    .prefix("avoid")
                    .description((locale)->plugin.text().of("addon.tags.commands.avoid").forLocale(locale))
                    .executor(avoidCommand)
                    .build());

    plugin.getCommandManager().registerCmd(
            CommandContainer.builder()
                    .prefix("favorite")
                    .description((locale)->plugin.text().of("addon.tags.commands.favorite").forLocale(locale))
                    .executor(favoriteCommand)
                    .build());

    plugin.getCommandManager().registerCmd(
            CommandContainer.builder()
                    .prefix("tag")
                    .description((locale)->plugin.text().of("addon.tags.commands.tag").forLocale(locale))
                    .executor(tagCommand)
                    .build());

    plugin.getCommandManager().registerCmd(
            CommandContainer.builder()
                    .prefix("watch")
                    .description((locale)->plugin.text().of("addon.tags.commands.watch").forLocale(locale))
                    .executor(watchCommand)
                    .build());
  }
}
