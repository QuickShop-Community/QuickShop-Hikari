package com.ghostchu.quickshop.compatibility.bungeecord;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class Main extends Plugin implements Listener {
    private static final String QUICKSHOP_BUNGEE_CHANNEL = "quickshop:bungee";
    private static final String SUB_CHANNEL_FORWARD = "forward";
    private static final String SUB_CHANNEL_COMMAND = "command";
    private static final String CHAT_COMMAND_REQUEST = "request";
    private static final String CHAT_COMMAND_CANCEL = "cancel";
    private final Set<UUID> pendingForward = Collections.synchronizedSet(new HashSet<>());

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    public void onEnable() {
        getProxy().getPluginManager().registerListener(this, this);
        getProxy().registerChannel(QUICKSHOP_BUNGEE_CHANNEL);
    }

    @Override
    public void onDisable() {
        this.pendingForward.clear();
        getProxy().getPluginManager().unregisterListener(this);
        getProxy().unregisterChannel(QUICKSHOP_BUNGEE_CHANNEL);
    }

    @EventHandler
    public void on(PluginMessageEvent event) {
        if (!QUICKSHOP_BUNGEE_CHANNEL.equalsIgnoreCase(event.getTag())) {
            return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
        String subChannel = in.readUTF();
        if (SUB_CHANNEL_COMMAND.equalsIgnoreCase(subChannel)) {
            // the receiver is a server when the proxy talks to a server
            if (event.getReceiver() instanceof Server) {
                String command = in.readUTF();
                processCommand(command, in);
            }
        }
    }

    private void processCommand(String command, ByteArrayDataInput in) {
        UUID uuid = UUID.fromString(in.readUTF());
        switch (command) {
            case CHAT_COMMAND_REQUEST -> this.pendingForward.add(uuid);
            case CHAT_COMMAND_CANCEL -> this.pendingForward.remove(uuid);
        }
    }

    private void forwardMessage(ProxiedPlayer player, String message) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(SUB_CHANNEL_FORWARD);
        out.writeUTF(message);
        player.sendData(QUICKSHOP_BUNGEE_CHANNEL, message.getBytes());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(ChatEvent event) {
        if (event.getSender() instanceof ProxiedPlayer player) {
            UUID uuid = player.getUniqueId();
            if (pendingForward.contains(uuid)) {
                forwardMessage(player, event.getMessage());
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDisconnect(PlayerDisconnectEvent event) {
        pendingForward.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onServerSwitch(ServerSwitchEvent event) {
        pendingForward.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onServerKick(ServerKickEvent event) {
        pendingForward.remove(event.getPlayer().getUniqueId());
    }
}
