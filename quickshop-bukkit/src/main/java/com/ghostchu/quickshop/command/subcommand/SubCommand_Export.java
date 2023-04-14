package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.database.DatabaseIOUtil;
import com.ghostchu.quickshop.database.SimpleDatabaseHelperV2;
import com.ghostchu.quickshop.util.Util;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class SubCommand_Export implements CommandHandler<ConsoleCommandSender> {
    private final QuickShop plugin;

    public SubCommand_Export(QuickShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public synchronized void onCommand(@NotNull ConsoleCommandSender sender, @NotNull String commandLabel, @NotNull CommandParser parser) {
        plugin.text().of(sender, "exporting-database").send();
        File file = new File(QuickShop.getInstance().getDataFolder(), "export-" + System.currentTimeMillis() + ".zip");

        DatabaseIOUtil databaseIOUtil = new DatabaseIOUtil((SimpleDatabaseHelperV2) plugin.getDatabaseHelper());
        Util.asyncThreadRun(() -> {
            try {
                databaseIOUtil.exportTables(file);
                plugin.text().of(sender, "exported-database", file.toString()).send();
            } catch (SQLException | IOException e) {
                plugin.logger().warn("Exporting database failed.", e);
                plugin.text().of(sender, "exporting-failed", e.getMessage()).send();
            }
        });

    }


}
