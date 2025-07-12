package com.ghostchu.quickshop.command;

import com.ghostchu.quickshop.api.command.CommandManager;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class QuickShopCommand extends BukkitCommand {

  private final CommandManager manager;

  public QuickShopCommand(@NotNull final String name, final CommandManager commandManager, @NotNull final List<String> aliases) {

    super(name, "QuickShop command", "/quickshop", aliases);
    this.manager = commandManager;
  }

  @Override
  public boolean execute(@NotNull final CommandSender sender, @NotNull final String commandLabel, @NotNull final String[] args) {

    return this.manager.onCommand(sender, this, commandLabel, args);
  }

  @NotNull
  @Override
  public List<String> tabComplete(@NotNull final CommandSender sender, @NotNull final String alias, @NotNull final String[] args) throws IllegalArgumentException {
    final List<String> items = this.manager.onTabComplete(sender, this, alias, args);
    if(items == null) {
      return super.tabComplete(sender, alias, args);
    }

    return items;
  }
}
