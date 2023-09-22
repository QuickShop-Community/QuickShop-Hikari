package com.ghostchu.quickshop.addon.reremakemigrator;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.addon.reremakemigrator.command.SubCommand_ReremakeMigrate;
import com.ghostchu.quickshop.api.command.CommandContainer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {
    static Main instance;
    private QuickShop hikari;
    private org.maxgamer.quickshop.QuickShop reremake;
    private String deniedMessage;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        hikari = QuickShop.getInstance();
        reremake = org.maxgamer.quickshop.QuickShop.getInstance();
        getLogger().info("Found QuickShop-Hikari: " + hikari.getJavaPlugin().getDescription().getVersion());
        getLogger().info("Found QuickShop-Reremake: " + reremake.getDescription().getVersion());
        Bukkit.getPluginManager().registerEvents(this, this);
        hikari.getCommandManager().registerCmd(
                CommandContainer
                        .builder()
                        .prefix("migratefromreremake")
                        .description((locale) -> hikari.text().of("addon.reremake-migrator.commands.migratefromreremake").forLocale(locale))
                        .permission("quickshopaddon.reremakemigrator.migrator-admin")
                        .executor(new SubCommand_ReremakeMigrate(this, hikari, reremake))
                        .build());
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll((Plugin) this);
    }

    public QuickShop getHikari() {
        return hikari;
    }

    public org.maxgamer.quickshop.QuickShop getReremake() {
        return reremake;
    }

    public void setDeniedMessage(String deniedMessage) {
        this.deniedMessage = deniedMessage;
    }

    public void setDeniedMessage(Component deniedMessage) {
        this.deniedMessage = LegacyComponentSerializer.legacySection().serialize(deniedMessage);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerJoin(AsyncPlayerPreLoginEvent event) {
        if (this.deniedMessage != null) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, this.deniedMessage);
        }
    }
}
