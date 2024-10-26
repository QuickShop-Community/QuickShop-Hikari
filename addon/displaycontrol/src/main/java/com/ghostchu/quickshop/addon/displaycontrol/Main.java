package com.ghostchu.quickshop.addon.displaycontrol;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.addon.displaycontrol.bean.ClientType;
import com.ghostchu.quickshop.addon.displaycontrol.bean.DisplayOption;
import com.ghostchu.quickshop.addon.displaycontrol.command.SubCommand_DisplayControl;
import com.ghostchu.quickshop.addon.displaycontrol.database.DisplayControlDatabaseHelper;
import com.ghostchu.quickshop.api.command.CommandContainer;
import com.ghostchu.quickshop.api.event.display.DisplayApplicableCheckEvent;
import com.ghostchu.quickshop.util.PackageUtil;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public final class Main extends JavaPlugin implements Listener, PluginMessageListener {

  private static final String BUNGEE_CHANNEL = "quickshopcompat:bcgeyser";
  private static final String RESPONSE_PREFIX = "CLIENTTYPE";
  static Main instance;
  private final Map<UUID, ClientType> playerClientMapping = new ConcurrentHashMap<>();
  private final Map<UUID, DisplayOption> playerDisplayStatus = new ConcurrentHashMap<>();
  private QuickShop plugin;
  private DisplayControlDatabaseHelper databaseHelper;

  @Override
  public void onLoad() {

    instance = this;
  }

  @Override
  public void onDisable() {

    HandlerList.unregisterAll((Plugin)this);
  }

  @Override
  public void onEnable() {

    saveDefaultConfig();
    plugin = QuickShop.getInstance();
    try {
      databaseHelper = new DisplayControlDatabaseHelper(instance, plugin.getSqlManager(), plugin.getDbPrefix());
    } catch(SQLException e) {
      getLogger().log(Level.WARNING, "Failed to init database helper", e);
      Bukkit.getPluginManager().disablePlugin(this);
      return;
    }
    Bukkit.getPluginManager().registerEvents(this, this);
    QuickShop.folia().getImpl().runTimerAsync(()->this.playerClientMapping.entrySet().removeIf(e->Bukkit.getPlayer(e.getKey()) == null), 60 * 20 * 60, 60 * 20 * 60);
    plugin.getCommandManager().registerCmd(CommandContainer.builder()
                                                   .prefix("displaycontrol")
                                                   .permission("quickshopaddon.displaycontrol.use")
                                                   .description((locale)->plugin.text().of("addon.displaycontrol.command.displaycontrol").forLocale(locale))
                                                   .executor(new SubCommand_DisplayControl(plugin, this))
                                                   .build());
    getLogger().info("BungeeCord: " + Util.checkIfBungee());
    if(Util.checkIfBungee()) {
      getLogger().info("Detected BungeeCord, register the BungeeCord client information forward module, you will need install Compat-BungeeCord-Geyser module to make this feature work.");
      Bukkit.getMessenger().registerIncomingPluginChannel
              (this, BUNGEE_CHANNEL, this); // we register the incoming channel
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void playerLogin(final AsyncPlayerPreLoginEvent event) {

    final UUID uuid = event.getUniqueId();
    try {
      final DisplayOption displayOption = databaseHelper.getDisplayOption(uuid);
      this.playerDisplayStatus.put(uuid, displayOption);
    } catch(SQLException e) {
      getLogger().log(Level.WARNING, "Failed to getting the player display status from database", e);
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void playerQuit(final PlayerQuitEvent event) {

    cleanup(event.getPlayer().getUniqueId());
  }

  private void cleanup(final UUID uuid) {

    this.playerDisplayStatus.remove(uuid);
    this.playerClientMapping.remove(uuid);
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void displaySending(final DisplayApplicableCheckEvent event) {

    final UUID uuid = event.getPlayer();
    final DisplayOption option = this.playerDisplayStatus.getOrDefault(uuid, DisplayOption.AUTO);
    final boolean bedrockClient = this.playerClientMapping.getOrDefault(uuid, ClientType.UNDISCOVERED).isBedrockType();
    if(!option.isDisplayAvailable(bedrockClient)) {
      event.setApplicable(false);
    }
  }

  public DisplayControlDatabaseHelper getDatabaseHelper() {

    return databaseHelper;
  }

  @Override
  public void onPluginMessageReceived(@NotNull final String channel, @NotNull final Player player, final byte @NotNull [] message) {

    if(!channel.equalsIgnoreCase(BUNGEE_CHANNEL)) {
      return;
    }
    final ByteArrayDataInput in = ByteStreams.newDataInput(message);
    final String prefix = in.readUTF();
    //noinspection SwitchStatementWithTooFewBranches
    switch(prefix) {
      case RESPONSE_PREFIX -> handleBungeeBedrockPlayerCallback(in);
      default -> getLogger().log(Level.WARNING, "Unrecognized type: " + prefix);
    }
  }

  private void handleBungeeBedrockPlayerCallback(final ByteArrayDataInput in) {

    if(!PackageUtil.parsePackageProperly("acceptBungeeCordBedrockPlayerDiscoveryCallback").asBoolean(true)) {
      return;
    }
    final UUID uuid = UUID.fromString(in.readUTF());
    final int responseType = in.readShort();
    ClientType clientType = ClientType.UNDISCOVERED;
    if(responseType == 0) {
      clientType = ClientType.JAVA_EDITION_PLAYER;
    }
    if(responseType == 1) {
      clientType = ClientType.BEDROCK_EDITION_PLAYER_GEYSER;
    }
    if(responseType == 2) {
      clientType = ClientType.BEDROCK_EDITION_PLAYER_FLOODGATE;
    }
    Log.debug("Player " + uuid + " client type check callback: " + clientType.name() + ", isBedrockClient: " + clientType.isBedrockType());
    if(this.playerClientMapping.getOrDefault(uuid, ClientType.UNDISCOVERED).isWaitingDiscover()) {
      Log.debug("[BUNGEE-CALLBACK] Discovered player " + uuid + " client type is: " + clientType.name());
      this.playerClientMapping.put(uuid, clientType);
    }
  }
}
