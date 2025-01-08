package com.ghostchu.quickshop.compatibility.velocity;

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
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.velocitypowered.api.network.ProtocolVersion.LEGACY;
import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_1_10;
import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_1_11;
import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_1_11_1;
import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_1_12;
import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_1_12_1;
import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_1_12_2;
import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_1_13;
import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_1_13_1;
import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_1_13_2;
import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_1_14;
import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_1_14_1;
import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_1_14_2;
import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_1_14_3;
import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_1_14_4;
import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_1_15;
import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_1_15_1;
import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_1_15_2;
import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_1_16;
import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_1_16_1;
import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_1_16_2;
import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_1_16_3;
import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_1_16_4;
import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_1_17;
import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_1_17_1;
import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_1_18;
import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_1_18_2;
import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_1_19;
import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_1_7_2;
import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_1_7_6;
import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_1_8;
import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_1_9;
import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_1_9_1;
import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_1_9_2;
import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_1_9_4;

public final class Main {

  private static final MinecraftChannelIdentifier QUICKSHOP_BUNGEE_CHANNEL = MinecraftChannelIdentifier.create("quickshop", "bungee");
  private static final String SUB_CHANNEL_FORWARD = "forward";
  private static final String SUB_CHANNEL_COMMAND = "command";
  private static final String CHAT_COMMAND_REQUEST = "request";
  private static final String CHAT_COMMAND_CANCEL = "cancel";
  private static final List<ProtocolVersion> NO_SIGN_VERSIONS = List.of(
          LEGACY,
          MINECRAFT_1_7_2,
          MINECRAFT_1_7_6,
          MINECRAFT_1_8,
          MINECRAFT_1_9,
          MINECRAFT_1_9_1,
          MINECRAFT_1_9_2,
          MINECRAFT_1_9_4,
          MINECRAFT_1_10,
          MINECRAFT_1_11,
          MINECRAFT_1_11_1,
          MINECRAFT_1_12,
          MINECRAFT_1_12_1,
          MINECRAFT_1_12_2,
          MINECRAFT_1_13,
          MINECRAFT_1_13_1,
          MINECRAFT_1_13_2,
          MINECRAFT_1_14,
          MINECRAFT_1_14_1,
          MINECRAFT_1_14_2,
          MINECRAFT_1_14_3,
          MINECRAFT_1_14_4,
          MINECRAFT_1_15,
          MINECRAFT_1_15_1,
          MINECRAFT_1_15_2,
          MINECRAFT_1_16,
          MINECRAFT_1_16_1,
          MINECRAFT_1_16_2,
          MINECRAFT_1_16_3,
          MINECRAFT_1_16_4,
          MINECRAFT_1_17,
          MINECRAFT_1_17_1,
          MINECRAFT_1_18,
          MINECRAFT_1_18_2,
          MINECRAFT_1_19
                                                                       );
  private final Set<UUID> pendingForward = Collections.synchronizedSet(new HashSet<>());
  @Inject
  private ProxyServer proxy;

  @Subscribe
  public void onProxyInitialization(final ProxyInitializeEvent event) {

    proxy.getChannelRegistrar().register(QUICKSHOP_BUNGEE_CHANNEL);
  }

  @Subscribe
  public void onProxyShutdown(final ProxyShutdownEvent event) {

    this.pendingForward.clear();
    proxy.getChannelRegistrar().unregister(QUICKSHOP_BUNGEE_CHANNEL);
  }

  @Subscribe
  public void on(final PluginMessageEvent event) {
    // Is this our business?
    if (!QUICKSHOP_BUNGEE_CHANNEL.equals(event.getIdentifier())) {
      return;
    }
    // Let's not be a snitch
    event.setResult(PluginMessageEvent.ForwardResult.handled());
    // Is the source correct?
    if (!(event.getSource() instanceof ServerConnection)) return;
    // We can trust the source
    final ByteArrayDataInput in = event.dataAsDataStream();
    final String subChannel = in.readUTF();
    if (SUB_CHANNEL_COMMAND.equalsIgnoreCase(subChannel)) {
      final String command = in.readUTF();
      processCommand(command, in);
    }
  }

  private void processCommand(final String command, final ByteArrayDataInput in) {

    final UUID uuid = UUID.fromString(in.readUTF());
    switch(command) {
      case CHAT_COMMAND_REQUEST -> this.pendingForward.add(uuid);
      case CHAT_COMMAND_CANCEL -> this.pendingForward.remove(uuid);
    }
  }

  @Subscribe(order = PostOrder.FIRST)
  public void onChat(final PlayerChatEvent event) {

    final Player player = event.getPlayer();
    final UUID uuid = player.getUniqueId();
    if(pendingForward.contains(uuid)) {
      forwardMessage(player, event.getMessage());

      // Workaround against kicking of players with valid signed key and client 1.19.1 or higher by velocity
      final ProtocolVersion protocol = player.getProtocolVersion();
      if(player.getIdentifiedKey() == null || NO_SIGN_VERSIONS.contains(protocol)) {
        event.setResult(PlayerChatEvent.ChatResult.denied());
      }
    }
  }

  private void forwardMessage(final Player player, final String message) {

    final ByteArrayDataOutput out = ByteStreams.newDataOutput();
    out.writeUTF(SUB_CHANNEL_FORWARD);
    out.writeUTF(message);
    player.getCurrentServer().ifPresent(server->
                                                server.sendPluginMessage(QUICKSHOP_BUNGEE_CHANNEL, out.toByteArray())
                                       );
  }

  @Subscribe(order = PostOrder.FIRST)
  public void onDisconnect(final DisconnectEvent event) {

    pendingForward.remove(event.getPlayer().getUniqueId());
  }

  @Subscribe(order = PostOrder.FIRST)
  public void onServerSwitch(final ServerPreConnectEvent event) {

    pendingForward.remove(event.getPlayer().getUniqueId());
  }

  @Subscribe(order = PostOrder.FIRST)
  public void onServerKick(final ServerPostConnectEvent event) {

    pendingForward.remove(event.getPlayer().getUniqueId());
  }
}