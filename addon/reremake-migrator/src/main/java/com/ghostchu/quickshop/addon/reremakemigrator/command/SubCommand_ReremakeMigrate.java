package com.ghostchu.quickshop.addon.reremakemigrator.command;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.addon.reremakemigrator.Main;
import com.ghostchu.quickshop.addon.reremakemigrator.migratecomponent.ConfigMigrate;
import com.ghostchu.quickshop.addon.reremakemigrator.migratecomponent.MigrateComponent;
import com.ghostchu.quickshop.addon.reremakemigrator.migratecomponent.ShopMigrate;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
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
    private final AtomicBoolean running =  new AtomicBoolean(false);

    public SubCommand_ReremakeMigrate(Main main, QuickShop hikari, org.maxgamer.quickshop.QuickShop reremake) {
        this.plugin = main;
        this.hikari = hikari;
        this.reremake = reremake;
    }

    @Override
    public void onCommand(ConsoleCommandSender sender, @NotNull String commandLabel, @NotNull CommandParser parser) {
        if(running.get()){
            return;
        }
        if (parser.getArgs().isEmpty()) {
            hikari.text().of(sender, "command-incorrect", "/quickshop reremakemigrate <shouldOverrideExistShops>").send();
            return;
        }
        if(!Bukkit.getOnlinePlayers().isEmpty()){
            hikari.text().of(sender, "addon.reremake-migrator.server-not-empty").send();
            return;
        }
        boolean shouldOverrideExistShops = Boolean.parseBoolean(parser.getArgs().get(0));
        List<MigrateComponent> migrateComponentList = new ArrayList<>();
        migrateComponentList.add(new ConfigMigrate(plugin, hikari,reremake, sender));
        migrateComponentList.add(new ShopMigrate(plugin, hikari,reremake, sender, shouldOverrideExistShops));
        Util.asyncThreadRun(()->{
            running.set(true);
            int count = 0;
            for (MigrateComponent migrateComponent : migrateComponentList) {
                count++;
                hikari.text().of(sender, "addon.reremake-migrator.executing", migrateComponent.getClass().getSimpleName(),count, migrateComponentList.size()).send();
                migrateComponent.migrate();
            }
            running.set(false);
        });
    }
}
