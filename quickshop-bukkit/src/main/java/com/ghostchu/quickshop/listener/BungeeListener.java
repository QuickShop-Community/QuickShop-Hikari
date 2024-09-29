package com.ghostchu.quickshop.listener;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.util.logger.Log;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
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

  public BungeeListener(final QuickShop plugin) {

    super(plugin);
  }

  @Override
  public void register() {

    super.register();
    Bukkit.getMessenger().registerIncomingPluginChannel(plugin.getJavaPlugin(), CHAT_FORWARD_CHANNEL, this);
    Bukkit.getMessenger().registerOutgoingPluginChannel(plugin.getJavaPlugin(), CHAT_FORWARD_CHANNEL);
    plugin.logger().info("BungeeCord messenger listener registered!");
  }

  @Override
  public void unregister() {

    super.unregister();
    Bukkit.getMessenger().unregisterIncomingPluginChannel(plugin.getJavaPlugin(), CHAT_FORWARD_CHANNEL);
    plugin.logger().info("BungeeCord messenger listener unregistered!");
  }

  @EventHandler
  public void onPlayerDisconnect(final PlayerQuitEvent event) {

    notifyForCancel(event.getPlayer());
  }

  public void notifyForCancel(final Player player) {

    if(!plugin.getJavaPlugin().isEnabled()) {
      return;
    }
    final ByteArrayDataOutput out = ByteStreams.newDataOutput();
    out.writeUTF(CHAT_FORWARD_SUB_CHANNEL_COMMAND);
    out.writeUTF(CHAT_COMMAND_CANCEL);
    out.writeUTF(player.getUniqueId().toString());
    player.sendPluginMessage(plugin.getJavaPlugin(), CHAT_FORWARD_CHANNEL, out.toByteArray());
  }

  public void notifyForForward(final Player player) {

    if(!plugin.getJavaPlugin().isEnabled()) {
      return;
    }
    final ByteArrayDataOutput out = ByteStreams.newDataOutput();
    out.writeUTF(CHAT_FORWARD_SUB_CHANNEL_COMMAND);
    out.writeUTF(CHAT_COMMAND_REQUEST);
    out.writeUTF(player.getUniqueId().toString());
    player.sendPluginMessage(plugin.getJavaPlugin(), CHAT_FORWARD_CHANNEL, out.toByteArray());
  }

  @Override
  public void onPluginMessageReceived(@NotNull final String channel, @NotNull final Player player, @NotNull final byte[] bytes) {

    if(!CHAT_FORWARD_CHANNEL.equalsIgnoreCase(channel)) {
      return;
    }

    try {
      final ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
      final String subChannel = in.readUTF();
      if(!CHAT_FORWARD_SUB_CHANNEL_FORWARD.equalsIgnoreCase(subChannel)) {
        return;
      }
      final String chatMessage = in.readUTF();
      Log.debug("Handling the plugin channel " + channel + " with sub channel " + subChannel + " on player " + player.getName() + " for message: " + chatMessage);
      plugin.getShopManager().handleChat(player, chatMessage);
    } catch(Exception ignore) {

    }
  }
}
