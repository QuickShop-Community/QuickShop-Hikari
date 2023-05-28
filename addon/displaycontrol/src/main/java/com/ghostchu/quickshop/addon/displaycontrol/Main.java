package com.ghostchu.quickshop.addon.displaycontrol;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.addon.displaycontrol.command.SubCommand_DisplayControl;
import com.ghostchu.quickshop.addon.displaycontrol.database.DisplayControlDatabaseHelper;
import com.ghostchu.quickshop.api.command.CommandContainer;
import com.ghostchu.quickshop.api.event.DisplayApplicableCheckEvent;
import com.ghostchu.quickshop.util.logger.Log;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;

public final class Main extends JavaPlugin implements Listener {
    static Main instance;
    private final Set<UUID> displayDisabledPlayers = new CopyOnWriteArraySet<>();
    private QuickShop plugin;
    private DisplayControlDatabaseHelper databaseHelper;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll((Plugin) this);
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        plugin = QuickShop.getInstance();
        try {
            databaseHelper = new DisplayControlDatabaseHelper(instance, plugin.getSqlManager(), plugin.getDbPrefix());
        } catch (SQLException e) {
            getLogger().log(Level.WARNING, "Failed to init database helper", e);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> displayDisabledPlayers.removeIf(uuid -> Bukkit.getPlayer(uuid) == null), 60 * 20 * 60, 60 * 20 * 60);
        plugin.getCommandManager().registerCmd(CommandContainer.builder()
                .prefix("displaycontrol")
                .permission("quickshopaddon.displaycontrol.use")
                .description((locale) -> plugin.text().of("addon.displaycontrol.command.displaycontrol").forLocale(locale))
                .executor(new SubCommand_DisplayControl(plugin, this))
                .build());

    }


    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void playerLogin(AsyncPlayerPreLoginEvent event) {
        UUID uuid = event.getUniqueId();
        try {
            @Nullable Boolean status = databaseHelper.getDisplayStatusForPlayer(uuid);
            if (status == null || !status) {
                displayDisabledPlayers.remove(uuid);
            } else {
                displayDisabledPlayers.add(uuid);
            }
        } catch (SQLException e) {
            getLogger().log(Level.WARNING, "Failed to getting the player display status from database", e);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void playerQuit(PlayerQuitEvent event) {
        //noinspection ConstantConditions
        displayDisabledPlayers.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void displaySending(DisplayApplicableCheckEvent event) {
        if (displayDisabledPlayers.contains(event.getPlayer())) {
            Log.debug("Display disabled for player " + event.getPlayer());
            event.setApplicable(false);
        }
    }

    public DisplayControlDatabaseHelper getDatabaseHelper() {
        return databaseHelper;
    }
}
