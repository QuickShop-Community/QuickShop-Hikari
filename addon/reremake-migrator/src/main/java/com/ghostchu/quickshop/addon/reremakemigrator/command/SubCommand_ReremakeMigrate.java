package com.ghostchu.quickshop.addon.reremakemigrator.command;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.addon.reremakemigrator.Main;
import com.ghostchu.quickshop.addon.reremakemigrator.migratecomponent.*;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.util.ProgressMonitor;
import com.ghostchu.quickshop.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class SubCommand_ReremakeMigrate implements CommandHandler<ConsoleCommandSender> {
    private final Main plugin;
    private final QuickShop hikari;
    private final org.maxgamer.quickshop.QuickShop reremake;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public SubCommand_ReremakeMigrate(Main main, QuickShop hikari, org.maxgamer.quickshop.QuickShop reremake) {
        this.plugin = main;
        this.hikari = hikari;
        this.reremake = reremake;
    }

    @Override
    public void onCommand(ConsoleCommandSender sender, @NotNull String commandLabel, @NotNull CommandParser parser) {
        if (running.get()) {
            return;
        }
        if (parser.getArgs().isEmpty() || parser.getArgs().size() < 2) {
            hikari.text().of(sender, "command-incorrect", "/quickshop reremakemigrate <shouldOverrideExistShops> <shouldMigrateTransactionLogs>").send();
            return;
        }
        if (!Bukkit.getOnlinePlayers().isEmpty()) {
            hikari.text().of(sender, "addon.reremake-migrator.server-not-empty").send();
            return;
        }
        boolean shouldOverrideExistShops = Boolean.parseBoolean(parser.getArgs().get(0));
        boolean shouldMigrateTransactionLogs = Boolean.parseBoolean(parser.getArgs().get(1));
        List<MigrateComponent> migrateComponentList = new ArrayList<>();
        migrateComponentList.add(new ConfigMigrate(plugin, hikari, reremake, sender));
        migrateComponentList.add(new TranslationMigrateComponent(plugin, hikari, reremake, sender));
        migrateComponentList.add(new ShopMigrate(plugin, hikari, reremake, sender, shouldOverrideExistShops));
        if (shouldMigrateTransactionLogs) {
            migrateComponentList.add(new ShopLogsMigrate(plugin, hikari, reremake, sender));
        }
        Util.asyncThreadRun(() -> {
            running.set(true);
            plugin.setDeniedMessage(hikari.text().of(sender, "addon.reremake-migrator.join_blocking_converting").forLocale());
            for (MigrateComponent migrateComponent : new ProgressMonitor<>(migrateComponentList, triple -> hikari.text().of(sender, "addon.reremake-migrator.executing", triple.getRight().getClass().getSimpleName(), triple.getLeft(), triple.getMiddle()).send())) {
                String migrateComponentName = migrateComponent.getClass().getSimpleName();
                try {
                    if (!migrateComponent.migrate()) {
                        // Something failed during the migration?
                        hikari.text().of(sender, "addon.reremake-migrator.failed", migrateComponentName).send();
                        running.set(false);
                        return;
                    }
                } catch (Exception e) {
                    hikari.logger().warn("Failed to execute migrate component {}", migrateComponentName, e);
                    running.set(false);
                    return;
                }
            }
            hikari.text().of(sender, "addon.reremake-migrator.completed").send();
            plugin.setDeniedMessage(hikari.text().of(sender, "addon.reremake-migrator.join_blocking_finished").forLocale());
            running.set(false);
        });
    }
}
