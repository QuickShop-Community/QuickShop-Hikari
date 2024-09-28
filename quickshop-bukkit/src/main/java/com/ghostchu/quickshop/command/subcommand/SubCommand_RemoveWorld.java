package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.util.logger.Log;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SubCommand_RemoveWorld implements CommandHandler<CommandSender> {

  private final QuickShop plugin;

  public SubCommand_RemoveWorld(final QuickShop plugin) {

    this.plugin = plugin;
  }

  @Override
  public void onCommand(@NotNull final CommandSender sender, @NotNull final String commandLabel, @NotNull final CommandParser parser) {

    if(parser.getArgs().isEmpty()) {
      plugin.text().of(sender, "command.no-world-given").send();
      return;
    }
    final World world = Bukkit.getWorld(parser.getArgs().get(0));
    if(world == null) {
      plugin.text().of(sender, "world-not-exists", parser.getArgs().get(0)).send();
      return;
    }
    int shopsDeleted = 0;
    for(final Shop shop : plugin.getShopManager().getAllShops()) {
      if(Objects.equals(shop.getLocation().getWorld(), world)) {
        plugin.getShopManager().deleteShop(shop);
        shopsDeleted++;
      }
    }
    Log.debug("Successfully deleted all shops in world " + parser.getArgs().get(0) + "!");
    plugin.text().of(sender, "shops-removed-in-world", shopsDeleted, world.getName()).send();
  }

}
