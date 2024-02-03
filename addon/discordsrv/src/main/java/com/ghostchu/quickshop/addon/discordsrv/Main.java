package com.ghostchu.quickshop.addon.discordsrv;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.addon.discordsrv.bean.NotificationFeature;
import com.ghostchu.quickshop.addon.discordsrv.command.SubCommand_Discord;
import com.ghostchu.quickshop.addon.discordsrv.database.DiscordDatabaseHelper;
import com.ghostchu.quickshop.addon.discordsrv.listener.QuickShopEventListener;
import com.ghostchu.quickshop.addon.discordsrv.message.MessageFactory;
import com.ghostchu.quickshop.addon.discordsrv.message.MessageManager;
import com.ghostchu.quickshop.addon.discordsrv.message.MessageRepository;
import com.ghostchu.quickshop.addon.discordsrv.wrapper.JDAWrapper;
import com.ghostchu.quickshop.addon.discordsrv.wrapper.discordsrv.DiscordSRVWrapper;
import com.ghostchu.quickshop.api.command.CommandContainer;
import com.ghostchu.quickshop.api.event.QSConfigurationReloadEvent;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermissionGroup;
import github.scarsz.discordsrv.api.commands.PluginSlashCommand;
import github.scarsz.discordsrv.api.commands.SlashCommandProvider;
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;

public class Main extends JavaPlugin implements Listener, SlashCommandProvider {
    private MessageFactory factory;
    private MessageManager manager;
    private JDAWrapper jdaWrapper;
    private DiscordDatabaseHelper databaseHelper;

    public void sendModeratorChannelMessage(@NotNull MessageEmbed embed) {
        String channelId = getConfig().getString("moderator-channel", "000000000000000000");
        if ("000000000000000000".equals(channelId)) {
            return;
        }
        jdaWrapper.sendChannelMessage(channelId, embed);
    }

    @Override
    public Set<PluginSlashCommand> getSlashCommands() {
        return Collections.emptySet();
    }

    public MessageManager getManager() {
        return manager;
    }

    @Override
    public void onEnable() {
        QuickShop quickshop = QuickShop.getInstance();
        saveDefaultConfig();
        manager = new MessageManager(this);
        manager.register(new MessageRepository(quickshop));
        factory = new MessageFactory(quickshop, manager);
        this.jdaWrapper = new DiscordSRVWrapper();
        quickshop.getShopPermissionManager().registerPermission(BuiltInShopPermissionGroup.STAFF.getNamespacedNode(), this, "discordalert");
        quickshop.getShopPermissionManager().registerPermission(BuiltInShopPermissionGroup.ADMINISTRATOR.getNamespacedNode(), this, "discordalert");
        try {
            this.databaseHelper = new DiscordDatabaseHelper(this, quickshop.getSqlManager(), quickshop.getDbPrefix());
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Failed to connect to database, please check your database settings.", e);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        quickshop.getCommandManager().registerCmd(CommandContainer.builder()
                .permission("quickshopaddon.discord.use")
                .description((locale) -> quickshop.text().of("addon.discord.commands.discord.description")
                        .forLocale(locale)).prefix("discord").executor(new SubCommand_Discord(quickshop, this)).build());
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(new QuickShopEventListener(this), this);
    }


    public MessageFactory getFactory() {
        return factory;
    }

    public JDAWrapper getJdaWrapper() {
        return jdaWrapper;
    }

    @Deprecated
    public boolean isServerNotifactionFeatureEnabled(@NotNull NotificationFeature feature) {
        return isServerNotificationFeatureEnabled(feature);
    }

    public boolean isServerNotificationFeatureEnabled(@NotNull NotificationFeature feature) {
        return getConfig().getBoolean("features." + feature.getConfigNode(), true);
    }


    public DiscordDatabaseHelper getDatabaseHelper() {
        return databaseHelper;
    }

    @Override
    public void saveDefaultConfig() {
        super.saveDefaultConfig();
        // perform configuration upgrade
        if (getConfig().getInt("config-version") != 2) {
            getConfig().set("moderator-channel", "000000000000000000");
            getConfig().set("config-version", 2);
            saveConfig();
        }
    }


    @Override
    public void onDisable() {
        HandlerList.unregisterAll((Plugin) this);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onQuickShopReload(QSConfigurationReloadEvent event){
        reloadConfig();
    }


}
