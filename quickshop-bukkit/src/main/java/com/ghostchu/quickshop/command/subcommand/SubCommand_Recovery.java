/*
 *  This file is a part of project QuickShop, the name is SubCommand_Recovery.java
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

package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.database.DatabaseIOUtil;
import com.ghostchu.quickshop.database.SimpleDatabaseHelperV2;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import lombok.AllArgsConstructor;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;

@AllArgsConstructor
public class SubCommand_Recovery implements CommandHandler<ConsoleCommandSender> {

    private final QuickShop plugin;

    @Override
    public void onCommand(@NotNull ConsoleCommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        File file = new File(plugin.getDataFolder(), "recovery.zip");
        if (!file.exists()) {
            plugin.text().of(sender, "importing-not-found").send();
            return;
        }

        if (cmdArg.length < 1 || !cmdArg[0].equalsIgnoreCase("confirm")) {
            plugin.text().of(sender, "importing-early-warning").send();
            return;
        }

        plugin.text().of(sender, "importing-database").send();
        Log.debug("Initializing database recovery...");
        DatabaseIOUtil databaseIOUtil = new DatabaseIOUtil((SimpleDatabaseHelperV2) plugin.getDatabaseHelper());
        Log.debug("Unloading all shops...");
        plugin.getShopManager().getLoadedShops().forEach(Shop::onUnload);
        Log.debug("Clean up in-memory data...");
        plugin.getShopManager().clear();
        Log.debug("Reset shop cache...");
        plugin.getShopCache().invalidateAll();
        Log.debug("Launching async thread for importing tables...");
        Util.asyncThreadRun(() -> {
            try {
                databaseIOUtil.importTables(file);
                Log.debug("Re-loading shop from database...");
                Util.mainThreadRun(() -> {
                    plugin.getShopLoader().loadShops();
                    plugin.text().of(sender,"imported-database").send();
                });
            } catch (SQLException | IOException | ClassNotFoundException e) {
                plugin.text().of(sender,"importing-failed",e.getMessage()).send();
                plugin.getLogger().log(Level.WARNING,"Failed to import the database from backup file.",e);
            }
        });

//        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
//            try {
//                //Util.backupDatabase();
//                plugin.getShopLoader().recoverFromFile(Util.readToString(file));
//            } catch (Exception e) {
//                plugin.getLogger().log(Level.WARNING, "Failed to recover the data because of the following error:", e);
//            }
//        });

    }

}
