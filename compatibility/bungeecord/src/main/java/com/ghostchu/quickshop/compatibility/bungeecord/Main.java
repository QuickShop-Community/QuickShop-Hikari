package com.ghostchu.quickshop.compatibility.bungeecord;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
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
  public void on(final PluginMessageEvent event) {
    // Is this our business?
    if (!QUICKSHOP_BUNGEE_CHANNEL.equalsIgnoreCase(event.getTag())) {
      return;
    }
    // Let's not be a snitch
    event.setCancelled(true);
    // Is the source correct?
    if (!(event.getSender() instanceof Server)) return; // Somebody is being nasty
    // We can trust the source
    final ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
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

  @EventHandler(priority = EventPriority.LOWEST)
  public void onChat(final ChatEvent event) {

    if(event.getSender() instanceof ProxiedPlayer player) {
      final UUID uuid = player.getUniqueId();
      if(pendingForward.contains(uuid)) {
        forwardMessage(player, event.getMessage());
        event.setCancelled(true);
      }
    }
  }

  private void forwardMessage(final ProxiedPlayer player, final String message) {

    final ByteArrayDataOutput out = ByteStreams.newDataOutput();
    out.writeUTF(SUB_CHANNEL_FORWARD);
    out.writeUTF(message);
    player.sendData(QUICKSHOP_BUNGEE_CHANNEL, out.toByteArray());
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onDisconnect(final PlayerDisconnectEvent event) {

    pendingForward.remove(event.getPlayer().getUniqueId());
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onServerSwitch(final ServerSwitchEvent event) {

    pendingForward.remove(event.getPlayer().getUniqueId());
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onServerKick(final ServerKickEvent event) {

    pendingForward.remove(event.getPlayer().getUniqueId());
  }
}
