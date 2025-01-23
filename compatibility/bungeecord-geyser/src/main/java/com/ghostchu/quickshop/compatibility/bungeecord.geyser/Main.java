package com.ghostchu.quickshop.compatibility.bungeecord.geyser;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.event.PluginMessageEvent;
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
  public void on(final PluginMessageEvent event) {
    // Is this our business?
    if(!BUNGEE_CHANNEL.equalsIgnoreCase(event.getTag())) {
      return;
    }
    // Let's not be a snitch
    // we don't want the client to send any message to the server
    // nor do we want the proxy to send any message to the player
    event.setCancelled(true);
  }

  @EventHandler
  public void switchServer(final ServerConnectedEvent event) {

    final UUID uuid = event.getPlayer().getUniqueId();
    boolean isGeyserPlayer = false;
    boolean isFloodgatePlayer = false;
    if(isGeyserInstalled) {

      isGeyserPlayer = GeyserApi.api().isBedrockPlayer(uuid);
    }
    if(isFloodgateInstalled) {
      isFloodgatePlayer = FloodgateApi.getInstance().isFloodgatePlayer(uuid);
      if(!isFloodgatePlayer) {
        isFloodgatePlayer = FloodgateApi.getInstance().isFloodgateId(uuid);
      }
    }
    final ByteArrayDataOutput out = ByteStreams.newDataOutput();
    out.writeUTF(RESPONSE_PREFIX);
    out.writeUTF(uuid.toString());
    int playerType = 0; // 0=Java
    String playerTypeDbgString = "Java Edition";
    if(isGeyserPlayer) {
      playerType++; // 1=Geyser
      playerTypeDbgString = "Geyser (Bedrock)";
    }
    if(isFloodgatePlayer) {
      playerType++; // 2=Floodgate, FG based on Geyser so increase the number
      playerTypeDbgString = "Floodgate (Bedrock)";
    }
    out.writeShort(playerType);
    getLogger().info("Player " + event.getPlayer().getName() + " client: " + playerTypeDbgString + ", forwarding to backend Spigot server.");
    event.getServer().getInfo().sendData(BUNGEE_CHANNEL, out.toByteArray());
  }
}
