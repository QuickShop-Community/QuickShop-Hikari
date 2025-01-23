package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.database.DatabaseIOUtil;
import com.ghostchu.quickshop.database.SimpleDatabaseHelperV2;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.sql.SQLException;

public class SubCommand_Recovery implements CommandHandler<ConsoleCommandSender> {

  private final QuickShop plugin;

  public SubCommand_Recovery(final QuickShop plugin) {

    this.plugin = plugin;
  }

  @Override
  public void onCommand(@NotNull final ConsoleCommandSender sender, @NotNull final String commandLabel, @NotNull final CommandParser parser) {

    final File file = new File(plugin.getDataFolder(), "recovery.zip");
    if(!file.exists()) {
      plugin.text().of(sender, "importing-not-found", "recovery.zip").send();
      return;
    }

    if(parser.getArgs().isEmpty() || !"confirm".equalsIgnoreCase(parser.getArgs().get(0))) {
      plugin.text().of(sender, "importing-early-warning").send();
      return;
    }

    plugin.text().of(sender, "importing-database").send();
    Log.debug("Initializing database recovery...");
    final DatabaseIOUtil databaseIOUtil = new DatabaseIOUtil((SimpleDatabaseHelperV2)plugin.getDatabaseHelper());
    Log.debug("Unloading all shops...");
    plugin.getShopManager().getAllShops().forEach(s->plugin.getShopManager().unloadShop(s));
    Log.debug("Clean up in-memory data...");
    plugin.getShopManager().clear();
    Log.debug("Launching async thread for importing tables...");
    Util.asyncThreadRun(()->{
      try {
        databaseIOUtil.performBackup("recovery");
        databaseIOUtil.importTables(file);
        Log.debug("Re-loading shop from database...");
        Util.mainThreadRun(()->{
          plugin.getShopLoader().loadShops();
          plugin.text().of(sender, "imported-database", "recovery.zip").send();
        });
      } catch(SQLException | ClassNotFoundException e) {
        plugin.text().of(sender, "importing-failed", e.getMessage()).send();
        plugin.logger().warn("Failed to import the database from backup file.", e);
      }
    });
  }

}
