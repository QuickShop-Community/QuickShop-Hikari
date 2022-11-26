package com.ghostchu.compatibility.velocity;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class Main {
    private static final MinecraftChannelIdentifier QUICKSHOP_BUNGEE_CHANNEL = MinecraftChannelIdentifier.create("quickshop", "bungee");
    private static final String SUB_CHANNEL_FORWARD = "forward";
    private static final String SUB_CHANNEL_COMMAND = "command";
    private static final String CHAT_COMMAND_REQUEST = "request";
    private static final String CHAT_COMMAND_CANCEL = "cancel";
    private final Set<UUID> pendingForward = Collections.synchronizedSet(new HashSet<>());

    @Inject
    private ProxyServer proxy;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        proxy.getChannelRegistrar().register(QUICKSHOP_BUNGEE_CHANNEL);
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        this.pendingForward.clear();
        proxy.getChannelRegistrar().unregister(QUICKSHOP_BUNGEE_CHANNEL);
    }

    @Subscribe
    public void on(PluginMessageEvent event) {
        if (!QUICKSHOP_BUNGEE_CHANNEL.equals(event.getIdentifier())) {
            return;
        }
        ByteArrayDataInput in = event.dataAsDataStream();
        String subChannel = in.readUTF();
        if (SUB_CHANNEL_COMMAND.equalsIgnoreCase(subChannel)) {
            // the receiver is a server when the proxy talks to a server
            if (event.getSource() instanceof ServerConnection) {
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

    private void forwardMessage(Player player, String message) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(SUB_CHANNEL_FORWARD);
        out.writeUTF(message);
        player.getCurrentServer().ifPresent(server ->
            server.sendPluginMessage(QUICKSHOP_BUNGEE_CHANNEL, message.getBytes())
        );
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        // Workaround against kicking of players with valid signed key and client 1.19.1 or higher by velocity
        if (player.getIdentifiedKey() != null
                && player.getProtocolVersion().equals(ProtocolVersion.MINECRAFT_1_19_1)) {
            return;
        }
        UUID uuid = player.getUniqueId();
        if (pendingForward.contains(uuid)) {
            forwardMessage(player, event.getMessage());
            event.setResult(PlayerChatEvent.ChatResult.denied());
        }
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onDisconnect(DisconnectEvent event) {
        pendingForward.remove(event.getPlayer().getUniqueId());
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onServerSwitch(ServerPreConnectEvent event) {
        pendingForward.remove(event.getPlayer().getUniqueId());
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onServerKick(ServerPostConnectEvent event) {
        pendingForward.remove(event.getPlayer().getUniqueId());
    }
}