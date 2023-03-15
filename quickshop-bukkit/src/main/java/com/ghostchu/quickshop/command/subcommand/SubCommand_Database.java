package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.database.DataTables;
import com.ghostchu.quickshop.database.SimpleDatabaseHelperV2;
import com.ghostchu.quickshop.util.FastPlayerFinder;
import com.ghostchu.quickshop.util.Util;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Calendar;
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
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull CommandParser parser) {
        if (parser.getArgs().size() < 1) {
            plugin.text().of(sender, "bad-command-usage-detailed", "trim").send();
            return;
        }
        List<String> subParams = new ArrayList<>(parser.getArgs());
        subParams.remove(0);
        switch (parser.getArgs().get(0)) {
            case "trim" -> handleTrim(sender, subParams);
            case "purgelogs" -> purgeLogs(sender, subParams);
            case "purgeplayerscache" -> purgePlayersCache(sender, subParams);
            default -> plugin.text().of(sender, "bad-command-usage-detailed", "trim").send();
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull CommandParser parser) {
        if (parser.getArgs().size() < 2) {
            return List.of("trim");
        }
        return Collections.emptyList();
    }

    private void handleTrim(@NotNull CommandSender sender, @NotNull List<String> subParams) {
        if (subParams.size() < 1 || !"confirm".equalsIgnoreCase(subParams.get(0))) {
            plugin.text().of(sender, "database.trim-warning").send();
            return;
        }
        plugin.text().of(sender, "database.trim-start").send();
        SimpleDatabaseHelperV2 databaseHelper = (SimpleDatabaseHelperV2) plugin.getDatabaseHelper();
        databaseHelper.purgeIsolated().whenComplete((data, err) -> plugin.text().of(sender, "database.trim-complete", data).send());
    }
//
//    private void handleStatus(@NotNull CommandSender sender) {
//        DatabaseStatusHolder holder = plugin.getDatabaseMaintenanceWatcher().getResult();
//        if (holder == null) {
//            plugin.text().of(sender, "database.scanning-sync").send();
//            plugin.getDatabaseMaintenanceWatcher().runTaskAsynchronously(plugin);
//            return;
//        }
//        Component statusComponent = switch (holder.getStatus()) {
//            case GOOD -> plugin.text().of(sender, "database.status-good").forLocale();
//            case MAINTENANCE_REQUIRED -> plugin.text().of(sender, "database.status-bad").forLocale();
//        };
//        ChatSheetPrinter printer = new ChatSheetPrinter(sender);
//        printer.printHeader();
//        printer.printLine(plugin.text().of(sender, "database.status", statusComponent).forLocale());
//        printer.printLine(plugin.text().of(sender, "database.isolated").forLocale());
//        printer.printLine(plugin.text().of(sender, "database.isolated-data-ids", holder.getDataIds().getIsolated().size()).forLocale());
//        printer.printLine(plugin.text().of(sender, "database.isolated-shop-ids", holder.getShopIds().getIsolated().size()).forLocale());
//        if (holder.getStatus() == DatabaseStatusHolder.Status.MAINTENANCE_REQUIRED) {
//            printer.printLine(plugin.text().of(sender, "database.suggestion.trim").forLocale());
//        }
//        printer.printFooter();
//    }

    private void purgeLogs(@NotNull CommandSender sender, @NotNull List<String> subParams) {
        // TODO: Only purge before x days
        if (subParams.size() < 1) {
            plugin.text().of(sender, "command-incorrect", "/qs database purgelogs <before-days>").send();
            return;
        }
        if (subParams.size() < 2 || !"confirm".equalsIgnoreCase(subParams.get(1))) {
            plugin.text().of(sender, "database.purge-warning").send();
            return;
        }
        try {
            int days = Integer.parseInt(subParams.get(0));
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, days);
            plugin.text().of(sender, "database.purge-task-created").send();
            SimpleDatabaseHelperV2 databaseHelper = (SimpleDatabaseHelperV2) plugin.getDatabaseHelper();
            databaseHelper.purgeLogsRecords(calendar.getTime()).whenComplete((r, e) -> {
                if (e != null) {
                    plugin.logger().warn("Failed to execute database purge.", e);
                    plugin.text().of(sender, "database.purge-done-with-error", r).send();
                } else {
                    if (r == -1) {
                        plugin.logger().warn("Failed to execute database purge, check the exception above.");
                        plugin.text().of(sender, "database.purge-done-with-error", r).send();
                    } else {
                        plugin.text().of(sender, "database.purge-done-with-line", r).send();
                    }
                }
            });
            // Then we need also purge the isolated data after purge the logs.
            plugin.text().of(sender, "database.trim-start").send();
            databaseHelper.purgeIsolated().whenComplete((data, err) -> plugin.text().of(sender, "database.trim-complete", data).send());
        } catch (NumberFormatException e) {
            plugin.text().of(sender, "not-a-number", subParams.get(0)).send();
        }
    }

    private void purgePlayersCache(CommandSender sender, @NotNull List<String> subParams) {
        plugin.text().of(sender, "database.purge-players-cache").send();
        Util.asyncThreadRun(() -> {
            DataTables.PLAYERS
                    .createDelete()
                    .build()
                    .executeAsync((lines) -> {
                        ((FastPlayerFinder) plugin.getPlayerFinder()).getNameCache().invalidateAll();
                        plugin.text().of(sender, "database.purge-players-completed", lines).send();
                    }, (error, sqlAction) -> {
                        plugin.logger().error("Failed to purge players caches!", error);
                        plugin.text().of(sender, "database.purge-players-error").send();
                    });

        });
    }
}
