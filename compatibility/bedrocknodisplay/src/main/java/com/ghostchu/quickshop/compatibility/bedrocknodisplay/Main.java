package com.ghostchu.quickshop.compatibility.bedrocknodisplay;

import com.ghostchu.quickshop.api.event.DisplayApplicableCheckEvent;
import com.ghostchu.quickshop.api.shop.display.DisplayType;
import com.ghostchu.quickshop.compatibility.CompatibilityModule;
import com.ghostchu.quickshop.shop.display.AbstractDisplayItem;
import com.ghostchu.quickshop.util.logger.Log;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.geyser.api.GeyserApi;
import org.geysermc.geyser.api.connection.GeyserConnection;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class Main extends CompatibilityModule implements Listener {
    private final Set<UUID> BEDROCK_MAPPING = new HashSet<>();

    @Override
    public void init() {
        // There no init stuffs need to do
        if (AbstractDisplayItem.getNowUsing() != DisplayType.VIRTUALITEM) {
            getLogger().severe("You are using a display provider that is not virtual item, this module will not work.");
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        boolean isGeyser = false;
        boolean isFloodGate = false;
        //noinspection ConstantValue
        if (GeyserApi.api() != null) {
            GeyserConnection connection = GeyserApi.api().connectionByUuid(uuid);
            isGeyser = connection != null;
        }
        if (FloodgateApi.getInstance() != null) {
            isFloodGate = FloodgateApi.getInstance().isFloodgatePlayer(uuid);
        }
        if (isGeyser || isFloodGate) {
            BEDROCK_MAPPING.add(uuid);
            Log.debug("Player " + event.getPlayer().getUniqueId() + " is a bedrock player, disabling the virtual display item sending");
        } else {
            BEDROCK_MAPPING.remove(uuid);
            Log.debug("Player " + event.getPlayer().getUniqueId() + " not a bedrock player.");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        BEDROCK_MAPPING.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onDisplayApplicableCheck(DisplayApplicableCheckEvent event) {
        if (BEDROCK_MAPPING.contains(event.getPlayer())) {
            event.setApplicable(false);
        }
    }

    @Override
    public void onDisable() {
        BEDROCK_MAPPING.clear();
        super.onDisable();
    }
}
