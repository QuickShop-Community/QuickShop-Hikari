package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.database.SimpleDatabaseHelperV2;
import com.ghostchu.quickshop.database.bean.IsolatedScanResult;
import com.ghostchu.quickshop.util.ChatSheetPrinter;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.holder.DatabaseStatusHolder;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class SubCommand_Database implements CommandHandler<CommandSender> {
    private final QuickShop plugin;

    public SubCommand_Database(QuickShop plugin) {
        this.plugin = plugin;
    }

    /**
     * Calling while command executed by specified sender
     *
     * @param sender       The command sender but will automatically convert to specified instance
     * @param commandLabel The command prefix (/qs = qs, /shop = shop)
     * @param cmdArg       The arguments (/qs create stone will receive stone)
     */
    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (cmdArg.length < 1) {
            handleStatus(sender);
            return;
        }
        //noinspection SwitchStatementWithTooFewBranches
        switch (cmdArg[0]) {
            case "trim" -> handleTrim(sender, ArrayUtils.remove(cmdArg, 0));
            default -> handleStatus(sender);
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (cmdArg.length < 2) {
            return List.of("status", "trim");
        }
        return Collections.emptyList();
    }

    private void handleTrim(@NotNull CommandSender sender, @NotNull String[] cmdArg) {
        if (cmdArg.length < 1 || !"confirm".equalsIgnoreCase(cmdArg[0])) {
            plugin.text().of(sender, "database.trim-warning").send();
            return;
        }
        plugin.text().of(sender, "database.trim-start").send();
        Util.asyncThreadRun(() -> {
            SimpleDatabaseHelperV2 databaseHelper = (SimpleDatabaseHelperV2) plugin.getDatabaseHelper();
            try {
                IsolatedScanResult<Long> result = databaseHelper.purgeIsolatedData();
                plugin.text().of(sender, "database.trim-complete", result.getIsolated().size()).send();
            } catch (SQLException e) {
                e.printStackTrace();
                plugin.text().of(sender, "database.trim-exception").send();
            }
        });
    }

    private void handleStatus(@NotNull CommandSender sender) {
        DatabaseStatusHolder holder = plugin.getDatabaseMaintenanceWatcher().getResult();
        if (holder == null) {
            plugin.text().of(sender, "database.scanning-sync").send();
            plugin.getDatabaseMaintenanceWatcher().runTaskAsynchronously(plugin);
            return;
        }
        Component statusComponent = switch (holder.getStatus()) {
            case GOOD -> plugin.text().of(sender, "database.status-good").forLocale();
            case MAINTENANCE_REQUIRED -> plugin.text().of(sender, "database.status-bad").forLocale();
        };
        ChatSheetPrinter printer = new ChatSheetPrinter(sender);
        printer.printHeader();
        printer.printLine(plugin.text().of(sender, "database.status", statusComponent).forLocale());
        printer.printLine(plugin.text().of(sender, "database.isolated").forLocale());
        printer.printLine(plugin.text().of(sender, "database.isolated-data-ids", holder.getDataIds().getIsolated().size()).forLocale());
        printer.printLine(plugin.text().of(sender, "database.isolated-shop-ids", holder.getShopIds().getIsolated().size()).forLocale());
        if (holder.getStatus() == DatabaseStatusHolder.Status.MAINTENANCE_REQUIRED) {
            printer.printLine(plugin.text().of(sender, "database.suggestion.trim").forLocale());
        }
        printer.printFooter();
    }
}
