package com.ghostchu.quickshop.compatibility.bungeecord.geyser;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.geyser.api.GeyserApi;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class Main extends Plugin implements Listener {
    private static final String BUNGEE_CHANNEL = "quickshopcompat:bcgeyser";
    private static final String RESPONSE_PREFIX = "CLIENTTYPE";
    private final Set<UUID> pendingForward = Collections.synchronizedSet(new HashSet<>());
    private boolean isGeyserInstalled;
    private boolean isFloodgateInstalled;

    @Override
    public void onEnable() {
        getProxy().getPluginManager().registerListener(this, this);
        getProxy().registerChannel(BUNGEE_CHANNEL);
        isGeyserInstalled = getProxy().getPluginManager().getPlugin("Geyser-BungeeCord") != null;
        isFloodgateInstalled = getProxy().getPluginManager().getPlugin("floodgate") != null;
    }

    @Override
    public void onDisable() {
        this.pendingForward.clear();
        getProxy().getPluginManager().unregisterListener(this);
        getProxy().unregisterChannel(BUNGEE_CHANNEL);
    }

    @EventHandler
    public void switchServer(ServerConnectedEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        boolean isGeyserPlayer = false;
        boolean isFloodgatePlayer = false;
        if (isGeyserInstalled) {
            isGeyserPlayer = GeyserApi.api().isBedrockPlayer(uuid);
        }
        if (isFloodgateInstalled) {
            isFloodgatePlayer = FloodgateApi.getInstance().isFloodgatePlayer(uuid);
            if (!isFloodgatePlayer) {
                isFloodgatePlayer = FloodgateApi.getInstance().isFloodgateId(uuid);
            }
        }
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(RESPONSE_PREFIX);
        out.writeUTF(uuid.toString());
        int playerType = 0; // 0=Java
        if (isGeyserPlayer) {
            playerType++; // 1=Geyser
        }
        if (isFloodgatePlayer) {
            playerType++; // 2=Floodgate, FG based on Geyser so increase the number
        }
        out.writeShort(playerType);
        event.getServer().getInfo().sendData(BUNGEE_CHANNEL, out.toByteArray());
    }
}
