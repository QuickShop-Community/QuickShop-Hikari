package com.ghostchu.quickshop.listener;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.util.logger.Log;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

public class BungeeListener extends AbstractQSListener implements PluginMessageListener {
    private static final String CHAT_FORWARD_CHANNEL = "quickshop:bungee";
    private static final String CHAT_FORWARD_SUB_CHANNEL_FORWARD = "forward";
    private static final String CHAT_FORWARD_SUB_CHANNEL_COMMAND = "command";
    private static final String CHAT_COMMAND_REQUEST = "request";
    private static final String CHAT_COMMAND_CANCEL = "cancel";

    public BungeeListener(QuickShop plugin) {
        super(plugin);
    }

    @Override
    public void register() {
        super.register();
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, CHAT_FORWARD_CHANNEL, this);
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, CHAT_FORWARD_CHANNEL);
        plugin.getLogger().info("BungeeCord messenger listener registered!");
    }

    @Override
    public void unregister() {
        super.unregister();
        plugin.getServer().getMessenger().unregisterIncomingPluginChannel(plugin, CHAT_FORWARD_CHANNEL);
        plugin.getLogger().info("BungeeCord messenger listener unregistered!");
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerQuitEvent event) {
        notifyForCancel(event.getPlayer());
    }

    public void notifyForForward(Player player) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(CHAT_FORWARD_SUB_CHANNEL_COMMAND);
        out.writeUTF(CHAT_COMMAND_REQUEST);
        out.writeUTF(player.getUniqueId().toString());
        player.sendPluginMessage(plugin, CHAT_FORWARD_SUB_CHANNEL_COMMAND, out.toByteArray());
    }

    public void notifyForCancel(Player player) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(CHAT_FORWARD_SUB_CHANNEL_COMMAND);
        out.writeUTF(CHAT_COMMAND_CANCEL);
        out.writeUTF(player.getUniqueId().toString());
        player.sendPluginMessage(plugin, CHAT_FORWARD_SUB_CHANNEL_COMMAND, out.toByteArray());
    }


    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte @NotNull [] bytes) {
        if (!CHAT_FORWARD_CHANNEL.equalsIgnoreCase(channel)) {
            return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
        String subChannel = in.readUTF();
        if (!CHAT_FORWARD_SUB_CHANNEL_FORWARD.equalsIgnoreCase(subChannel)) {
            return;
        }
        String chatMessage = in.readUTF();
        Log.debug("Handling the plugin channel " + channel + " with sub channel " + subChannel + " on player " + player.getName() + " for message: " + chatMessage);
        plugin.getShopManager().handleChat(player, chatMessage);
    }
}
