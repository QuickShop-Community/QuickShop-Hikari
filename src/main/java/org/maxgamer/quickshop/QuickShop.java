/*
 * This file is a part of project QuickShop, the name is QuickShop.java
 *  Copyright (C) PotatoCraft Studio and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.maxgamer.quickshop;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import de.leonhard.storage.LightningBuilder;
import de.leonhard.storage.Yaml;
import de.leonhard.storage.internal.settings.ConfigSettings;
import de.leonhard.storage.internal.settings.ReloadSettings;
import de.tr7zw.nbtapi.plugin.NBTAPI;
import lombok.Getter;
import lombok.Setter;
import me.minebuilders.clearlag.Clearlag;
import me.minebuilders.clearlag.listeners.ItemMergeListener;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.plugin.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.api.QuickShopAPI;
import org.maxgamer.quickshop.api.chat.QuickChat;
import org.maxgamer.quickshop.api.command.CommandManager;
import org.maxgamer.quickshop.api.compatibility.CompatibilityManager;
import org.maxgamer.quickshop.api.database.DatabaseHelper;
import org.maxgamer.quickshop.api.economy.AbstractEconomy;
import org.maxgamer.quickshop.api.economy.EconomyType;
import org.maxgamer.quickshop.api.event.QSConfigurationReloadEvent;
import org.maxgamer.quickshop.api.integration.IntegrateStage;
import org.maxgamer.quickshop.api.integration.IntegrationManager;
import org.maxgamer.quickshop.api.localization.text.TextManager;
import org.maxgamer.quickshop.api.shop.*;
import org.maxgamer.quickshop.api.shop.inventory.InventoryWrapperManager;
import org.maxgamer.quickshop.chat.platform.minedown.BungeeQuickChat;
import org.maxgamer.quickshop.command.SimpleCommandManager;
import org.maxgamer.quickshop.database.*;
import org.maxgamer.quickshop.economy.Economy_GemsEconomy;
import org.maxgamer.quickshop.economy.Economy_TNE;
import org.maxgamer.quickshop.economy.Economy_Vault;
import org.maxgamer.quickshop.integration.SimpleIntegrationManager;
import org.maxgamer.quickshop.integration.worldguard.WorldGuardIntegration;
import org.maxgamer.quickshop.listener.*;
import org.maxgamer.quickshop.listener.worldedit.WorldEditAdapter;
import org.maxgamer.quickshop.localization.text.SimpleTextManager;
import org.maxgamer.quickshop.nonquickshopstuff.com.rylinaux.plugman.util.PluginUtil;
import org.maxgamer.quickshop.permission.PermissionManager;
import org.maxgamer.quickshop.shop.ShopLoader;
import org.maxgamer.quickshop.shop.ShopPurger;
import org.maxgamer.quickshop.shop.SimpleShopManager;
import org.maxgamer.quickshop.shop.VirtualDisplayItem;
import org.maxgamer.quickshop.shop.inventory.BukkitInventoryWrapperManager;
import org.maxgamer.quickshop.shop.inventory.InventoryWrapperRegistry;
import org.maxgamer.quickshop.util.Timer;
import org.maxgamer.quickshop.util.*;
import org.maxgamer.quickshop.util.compatibility.SimpleCompatibilityManager;
import org.maxgamer.quickshop.util.config.ConfigCommentUpdater;
import org.maxgamer.quickshop.util.config.ConfigProvider;
import org.maxgamer.quickshop.util.config.ConfigurationFixer;
import org.maxgamer.quickshop.util.envcheck.*;
import org.maxgamer.quickshop.util.matcher.item.BukkitItemMatcherImpl;
import org.maxgamer.quickshop.util.matcher.item.QuickShopItemMatcherImpl;
import org.maxgamer.quickshop.util.reload.ReloadManager;
import org.maxgamer.quickshop.util.reporter.error.RollbarErrorReporter;
import org.maxgamer.quickshop.watcher.*;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;

public class QuickShop extends JavaPlugin implements QuickShopAPI {
    /**
     * The active instance of QuickShop
     * You shouldn't use this if you really need it.
     */
    @Deprecated
    private static QuickShop instance;
    /**
     * The manager to check permissions.
     */
    private static PermissionManager permissionManager;
    private static boolean loaded = false;
    /**
     * If running environment test
     */
    @Getter
    private static volatile boolean testing = false;
    /* Public QuickShop API */
    private final SimpleCompatibilityManager compatibilityTool = new SimpleCompatibilityManager(this);
    private final Map<String, Integer> limits = new HashMap<>(15);
    private GameVersion gameVersion;
    /**
     * The shop limites.
     */
    private final ConfigProvider configProvider = new ConfigProvider(this, new File(getDataFolder(), "config.yml"));
    private final List<BukkitTask> timerTaskList = new ArrayList<>(3);
    @Getter
    private final ReloadManager reloadManager = new ReloadManager();
    /* Public QuickShop API End */
    @Getter
    private final QuickChat quickChat = new BungeeQuickChat(this);
    @Getter
    private final TpsWatcher tpsWatcher = new TpsWatcher();
    boolean onLoadCalled = false;
    private SimpleIntegrationManager integrationHelper;
    private SimpleDatabaseHelper databaseHelper;
    private SimpleCommandManager commandManager;
    private ItemMatcher itemMatcher;
    private SimpleShopManager shopManager;
    private SimpleTextManager textManager;
    private boolean priceChangeRequiresFee = false;
    private final InventoryWrapperRegistry inventoryWrapperRegistry = new InventoryWrapperRegistry(this);
    @Getter
    private final InventoryWrapperManager inventoryWrapperManager = new BukkitInventoryWrapperManager();

    /**
     * The BootError, if it not NULL, plugin will stop loading and show setted errors when use /qs
     */
    @Nullable
    @Getter
    @Setter
    private BootError bootError;
    /**
     * Queued database manager
     */
    @Getter
    private DatabaseManager databaseManager;
    /**
     * Default database prefix, can overwrite by config
     */
    @Getter
    private String dbPrefix = "";
    /**
     * Whether we should use display items or not
     */
    private boolean display = true;
    @Getter
    private int displayItemCheckTicks;
    @Getter
    @Deprecated
    private DisplayWatcher displayWatcher;
    /**
     * The economy we hook into for transactions
     */
    @Getter
    private AbstractEconomy economy;
    /**
     * Whether or not to limit players shop amounts
     */
    private boolean limit = false;
    @Nullable
    @Getter
    private LogWatcher logWatcher;
    /**
     * bStats, good helper for metrics.
     */
    private Metrics metrics;
    /**
     * The plugin OpenInv (null if not present)
     */
    @Getter
    private Plugin openInvPlugin;
    /**
     * The plugin PlaceHolderAPI(null if not present)
     */
    @Getter
    private Plugin placeHolderAPI;
    /**
     * A util to call to check some actions permission
     */
    @Getter
    private PermissionChecker permissionChecker;
    /**
     * The error reporter to help devs report errors to Sentry.io
     */
    @Getter
    private RollbarErrorReporter sentryErrorReporter;
    /**
     * The server UniqueID, use to the ErrorReporter
     */
    @Getter
    private UUID serverUniqueID;
    private boolean setupDBonEnableding = false;
    /**
     * Rewrited shoploader, more faster.
     */
    @Getter
    private ShopLoader shopLoader;
    @Getter
    private DisplayAutoDespawnWatcher displayAutoDespawnWatcher;
    @Getter
    private OngoingFeeWatcher ongoingFeeWatcher;
    @Getter
    private SignUpdateWatcher signUpdateWatcher;
    @Getter
    private ShopContainerWatcher shopContainerWatcher;
    @Getter
    private @Deprecated
    DisplayDupeRemoverWatcher displayDupeRemoverWatcher;
    @Getter
    @Deprecated
    private boolean enabledAsyncDisplayDespawn;
    @Getter
    private Plugin blockHubPlugin;
    @Getter
    private Plugin lwcPlugin;
    @Getter
    private Cache shopCache;
    @Getter
    private boolean allowStack;
    @Getter
    private EnvironmentChecker environmentChecker;
    @Getter
    @Nullable
    private UpdateWatcher updateWatcher;
    @Getter
    private BuildInfo buildInfo;
    @Getter
    @Nullable
    private String currency = null;
    @Getter
    private CalendarWatcher calendarWatcher;
    @Getter
    private Plugin worldEditPlugin;
    @Getter
    private WorldEditAdapter worldEditAdapter;
    @Getter
    private ShopPurger shopPurger;
    @Getter
    @Nullable
    private NBTAPI nbtapi = null;

    private int loggingLocation = 0;

    public void disableNBTAPI() {
        nbtapi = null;
    }

    /**
     * Use for mock bukkit
     */
    public QuickShop() {
        super();
    }

    /**
     * Use for mock bukkit
     */
    protected QuickShop(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
        super(loader, description, dataFolder, file);
    }

    @NotNull
    public static QuickShop getInstance() {
        return instance;
    }

    @NotNull
    @Override
    public InventoryWrapperRegistry getInventoryWrapperRegistry() {
        return inventoryWrapperRegistry;
    }

    /**
     * Returns QS version, this method only exist on QuickShop forks If running other QuickShop forks,, result
     * may not is "Reremake x.x.x" If running QS official, Will throw exception.
     *
     * @return Plugin Version
     */
    public static String getVersion() {
        return QuickShop.instance.getDescription().getVersion();
    }

    /**
     * Get the permissionManager as static
     *
     * @return the permission Manager.
     */
    public static PermissionManager getPermissionManager() {
        return permissionManager;
    }

    /**
     * Return the QSRR's fork edition name, you can modify this if you want create yourself fork.
     *
     * @return The fork name.
     */
    public static String getFork() {
        return "Reremake";
    }

    public IntegrationManager getIntegrationHelper() {
        return integrationHelper;
    }

    @Deprecated
    private Yaml configurationForCompatibility = null;

    /**
     * Get the Player's Shop limit.
     *
     * @param p The player you want get limit.
     * @return int Player's shop limit
     */
    public int getShopLimit(@NotNull Player p) {
        int max = getConfig().getInt("limits.default");
        for (Entry<String, Integer> entry : limits.entrySet()) {
            if (entry.getValue() > max && getPermissionManager().hasPermission(p, entry.getKey())) {
                max = entry.getValue();
            }
        }
        return max;
    }

    /**
     * Load 3rdParty plugin support module.
     */
    private void load3rdParty() {
        // added for compatibility reasons with OpenInv - see
        // https://github.com/KaiKikuchi/QuickShop/issues/139
        if (getConfig().getBoolean("plugin.OpenInv")) {
            this.openInvPlugin = Bukkit.getPluginManager().getPlugin("OpenInv");
            if (this.openInvPlugin != null) {
                getLogger().info("Successfully loaded OpenInv support!");
            }
        }
        if (getConfig().getBoolean("plugin.PlaceHolderAPI")) {
            this.placeHolderAPI = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
            if (this.placeHolderAPI != null) {
                getLogger().info("Successfully loaded PlaceHolderAPI support!");
            }
        }
        if (getConfig().getBoolean("plugin.BlockHub")) {
            this.blockHubPlugin = Bukkit.getPluginManager().getPlugin("BlockHub");
            if (this.blockHubPlugin != null) {
                getLogger().info("Successfully loaded BlockHub support!");
            }
        }
        if (getConfig().getBoolean("plugin.WorldEdit")) {
            //  GameVersion gameVersion = GameVersion.get(nmsVersion);
            this.worldEditPlugin = Bukkit.getPluginManager().getPlugin("WorldEdit");
            if (this.worldEditPlugin != null) {
                this.worldEditAdapter = new WorldEditAdapter(this, (WorldEditPlugin) this.worldEditPlugin);
                this.worldEditAdapter.register();
                getLogger().info("Successfully loaded WorldEdit support!");
            }
        }

        if (getConfig().getBoolean("plugin.LWC")) {
            this.lwcPlugin = Bukkit.getPluginManager().getPlugin("LWC");
            if (this.lwcPlugin != null) {
                if (Util.isMethodAvailable("com.griefcraft.lwc.LWC", "findProtection", org.bukkit.Location.class)) {
                    getLogger().info("Successfully loaded LWC support!");
                } else {
                    getLogger().warning("Unsupported LWC version, please make sure you are using the modern version of LWC!");
                    this.lwcPlugin = null;
                }
            }
        }
        if (getConfig().getBoolean("plugin.NBTAPI")) {
            this.nbtapi = (NBTAPI) Bukkit.getPluginManager().getPlugin("NBTAPI");
            if (this.nbtapi != null) {
                if (!this.nbtapi.isCompatible()) {
                    getLogger().warning("NBTAPI plugin failed to loading, QuickShop NBTAPI support module has been disabled. Try update NBTAPI version to resolve the issue. (" + nbtapi.getDescription().getVersion() + ")");
                    this.nbtapi = null;
                } else {
                    getLogger().info("Successfully loaded NBTAPI support!");
                }
            }
        }
        Bukkit.getPluginManager().registerEvents(this.compatibilityTool, this);
        compatibilityTool.searchAndRegisterPlugins();
        if (this.display) {
            //VirtualItem support
            if (AbstractDisplayItem.getNowUsing() == DisplayType.VIRTUALITEM) {
                getLogger().info("Using Virtual Item display, loading ProtocolLib support...");
                Plugin protocolLibPlugin = Bukkit.getPluginManager().getPlugin("ProtocolLib");
                if (protocolLibPlugin != null && protocolLibPlugin.isEnabled()) {
                    getLogger().info("Successfully loaded ProtocolLib support!");
                } else {
                    getLogger().warning("Failed to load ProtocolLib support, fallback to real item display");
                    getConfig().set("shop.display-type", 0);
                    saveConfiguration();
                }
            }
            if (AbstractDisplayItem.getNowUsing() == DisplayType.REALITEM) {
                getLogger().warning("You're using Real Display system and that may cause your server lagg, switch to Virtual Display system if you can!");
                if (Bukkit.getPluginManager().getPlugin("ClearLag") != null) {
                    try {
                        Clearlag clearlag = (Clearlag) Bukkit.getPluginManager().getPlugin("ClearLag");
                        for (RegisteredListener clearLagListener : ItemSpawnEvent.getHandlerList().getRegisteredListeners()) {
                            if (!clearLagListener.getPlugin().equals(clearlag)) {
                                continue;
                            }
                            if (clearLagListener.getListener().getClass().equals(ItemMergeListener.class)) {
                                ItemSpawnEvent.getHandlerList().unregister(clearLagListener.getListener());
                                getLogger().warning("+++++++++++++++++++++++++++++++++++++++++++");
                                getLogger().severe("Detected incompatible module of ClearLag-ItemMerge module, it will broken the QuickShop display, we already unregister this module listener!");
                                getLogger().severe("Please turn off it in the ClearLag config.yml or turn off the QuickShop display feature!");
                                getLogger().severe("If you didn't do that, this message will keep spam in your console every times you server boot up!");
                                getLogger().warning("+++++++++++++++++++++++++++++++++++++++++++");
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }

    // /**
//     * Logs the given string to qs.log, if QuickShop is configured to do so.
//     *
//     * @param s The string to log. It will be prefixed with the date and time.
//     */
//    public void log(@NotNull String s) {
//        Util.debugLog("[SHOP LOG] " + s);
//        if (this.getLogWatcher() == null) {
//            return;
//        }
//        this.getLogWatcher().log(s);
//    }

    public void logEvent(@NotNull Object eventObject) {
        if (this.getLogWatcher() == null) {
            return;
        }
        if (loggingLocation == 0) {
            this.getLogWatcher().log(JsonUtil.getGson().toJson(eventObject));
        } else {
            getDatabaseHelper().insertHistoryRecord(eventObject);
        }

    }

    /**
     * Tries to load the economy and its core. If this fails, it will try to use vault. If that fails,
     * it will return false.
     *
     * @return true if successful, false if the core is invalid or is not found, and vault cannot be
     * used.
     */

    public boolean loadEcon() {
        try {
            // EconomyCore core = new Economy_Vault();
            switch (EconomyType.fromID(getConfig().getInt("economy-type"))) {
                case UNKNOWN:
                    setupBootError(new BootError(this.getLogger(), "Can't load the Economy provider, invaild value in config.yml."), true);
                    return false;
                case VAULT:
                    economy = new Economy_Vault(this);
                    Util.debugLog("Now using the Vault economy system.");
                    if (getConfig().getDouble("tax", 0.0d) > 0) {
                        try {
                            String taxAccount = getConfig().getString("tax-account", "tax");
                            if (!taxAccount.isEmpty()) {
                                OfflinePlayer tax;
                                if (Util.isUUID(taxAccount)) {
                                    tax = PlayerFinder.findOfflinePlayerByUUID(UUID.fromString(taxAccount));
                                } else {
                                    tax = PlayerFinder.findOfflinePlayerByName((Objects.requireNonNull(taxAccount)));
                                }
                                Economy_Vault vault = (Economy_Vault) economy;
                                if (vault.isValid()) {
                                    if (!Objects.requireNonNull(vault.getVault()).hasAccount(tax)) {
                                        try {
                                            Util.debugLog("Tax account not exists! Creating...");
                                            getLogger().warning("QuickShop detected tax account not exists, we're trying to create one. If you see any errors, please change tax-account in config.yml to server owner in-game username");
                                            if (vault.getVault().createPlayerAccount(tax)) {
                                                getLogger().info("Tax account created.");
                                            } else {
                                                getLogger().warning("Cannot to create tax-account,  please change tax-account in config.yml to server owner in-game username");
                                            }
                                        } catch (Exception ignored) {
                                        }
                                        if (!vault.getVault().hasAccount(tax)) {
                                            getLogger().warning("Tax account's player never played this server before and failed to create one, that may cause server lagg or economy system error, you should change that name. But if this warning not cause any issues, you can safety ignore this.");
                                        }
                                    }

                                }
                            }
                        } catch (Exception ignored) {
                            Util.debugLog("Failed to fix account issue.");
                        }
                    }
                    break;
                case GEMS_ECONOMY:
                    economy = new Economy_GemsEconomy(this);
                    Util.debugLog("Now using the GemsEconomy economy system.");
                    break;
                case TNE:
                    economy = new Economy_TNE(this);
                    Util.debugLog("Now using the TNE economy system.");
                    break;
                default:
                    Util.debugLog("No any economy provider selected.");
                    break;
            }
            if (economy == null) {
                return false;
            }
            if (!economy.isValid()) {
                setupBootError(BuiltInSolution.econError(), false);
                return false;
            }
            economy = ServiceInjector.getEconomy(economy);
        } catch (Throwable e) {
            this.getSentryErrorReporter().ignoreThrow();
            getLogger().log(Level.WARNING, "Something going wrong when loading up economy system", e);
            getLogger().severe("QuickShop could not hook into a economy/Not found Vault or Reserve!");
            getLogger().severe("QuickShop CANNOT start!");
            setupBootError(BuiltInSolution.econError(), false);
            getLogger().severe("Plugin listeners was disabled, please fix the economy issue.");
            return false;
        }
        return true;
    }

    @Override
    public @NotNull FileConfiguration getConfig() throws UnsupportedOperationException {
        return configProvider.get();
    }

    @Deprecated
    public @NotNull Yaml getConfiguration() {
        return configurationForCompatibility == null ? configurationForCompatibility = LightningBuilder
                .fromFile(new File(getDataFolder(), "config.yml"))
                .addInputStreamFromResource("config.yml")
                .setReloadSettings(ReloadSettings.MANUALLY)
                .setConfigSettings(ConfigSettings.PRESERVE_COMMENTS)
                .createYaml() : configurationForCompatibility;
    }

    @ApiStatus.ScheduledForRemoval
    @Override
    @Deprecated
    public void saveConfig() {
        this.saveConfiguration();
        //configProvider.save();
    }

    public void saveConfiguration() {
        this.configProvider.save();
    }


    /**
     * Reloads QuickShops config
     */
    @Override
    @Deprecated
    public void reloadConfig() {
        this.reloadConfiguration();
    }

    public void reloadConfiguration() {
        configProvider.reload();
        // Load quick variables
        this.display = this.getConfig().getBoolean("shop.display-items");
        this.priceChangeRequiresFee = this.getConfig().getBoolean("shop.price-change-requires-fee");
        this.displayItemCheckTicks = this.getConfig().getInt("shop.display-items-check-ticks");
        this.allowStack = this.getConfig().getBoolean("shop.allow-stacks");
        this.currency = this.getConfig().getString("currency");
        this.loggingLocation = this.getConfig().getInt("logging.location");
        if (StringUtils.isEmpty(this.currency)) {
            this.currency = null;
        }
        if (this.getConfig().getBoolean("logging.enable")) {
            logWatcher = new LogWatcher(this, new File(getDataFolder(), "qs.log"));
        } else {
            logWatcher = null;
        }
        Bukkit.getPluginManager().callEvent(new QSConfigurationReloadEvent(this));
    }

    /**
     * Early than onEnable, make sure instance was loaded in first time.
     */
    @Override
    public final void onLoad() {
        instance = this;
        Util.setPlugin(this);
        this.onLoadCalled = true;
        getLogger().info("QuickShop " + getFork() + " - Early boot step - Booting up");
        //BEWARE THESE ONLY RUN ONCE
        this.buildInfo = new BuildInfo(getResource("BUILDINFO"));
        runtimeCheck(EnvCheckEntry.Stage.ON_LOAD);
        getLogger().info("Reading the configuration...");
        this.initConfiguration();
        this.bootError = null;
        getLogger().info("Loading messages translation over-the-air (this may need take a while).");
        this.textManager = new SimpleTextManager(this);
        textManager.load();
        getLogger().info("Registering InventoryWrapper...");
        this.inventoryWrapperRegistry.register(this,this.inventoryWrapperManager);
        getLogger().info("Loading up integration modules.");
        this.integrationHelper = new SimpleIntegrationManager(this);
        this.integrationHelper.callIntegrationsLoad(IntegrateStage.onLoadBegin);
        if (getConfig().getBoolean("integration.worldguard.enable")) {
            Plugin wg = Bukkit.getPluginManager().getPlugin("WorldGuard");
            // WG require register flags when onLoad called.
            if (wg != null) {
                this.integrationHelper.register(new WorldGuardIntegration(this));
            }
        }
        this.integrationHelper.callIntegrationsLoad(IntegrateStage.onLoadAfter);
        getLogger().info("QuickShop " + getFork() + " - Early boot step - Complete");
    }

    @Override
    public final void onDisable() {
        getLogger().info("QuickShop is finishing remaining work, this may need a while...");
        if (sentryErrorReporter != null) {
            sentryErrorReporter.unregister();
        }

        if (this.integrationHelper != null) {
            this.integrationHelper.callIntegrationsUnload(IntegrateStage.onUnloadBegin);
        }
        Util.debugLog("Unloading all shops...");
        try {
            if (getShopManager() != null) {
                getShopManager().getLoadedShops().forEach(Shop::onUnload);
            }
        } catch (Exception ignored) {
        }
        Util.debugLog("Unregister hooks...");
        if (worldEditAdapter != null) {
            worldEditAdapter.unregister();
        }

        Util.debugLog("Calling integrations...");
        if (integrationHelper != null) {
            integrationHelper.callIntegrationsUnload(IntegrateStage.onUnloadAfter);
            integrationHelper.unregisterAll();
        }
        compatibilityTool.unregisterAll();

        Util.debugLog("Cleaning up resources and unloading all shops...");
        /* Remove all display items, and any dupes we can find */
        if (shopManager != null) {
            shopManager.clear();
        }
        if (AbstractDisplayItem.getNowUsing() == DisplayType.VIRTUALITEM) {
            VirtualDisplayItem.VirtualDisplayItemManager.unload();
        }

        Util.debugLog("Cleaning up database queues...");
        if (this.getDatabaseManager() != null) {
            this.getDatabaseManager().unInit();
        }

        Util.debugLog("Unregistering tasks...");
        if (logWatcher != null) {
            logWatcher.close();
        }
        Iterator<BukkitTask> taskIterator = timerTaskList.iterator();
        while (taskIterator.hasNext()) {
            BukkitTask task = taskIterator.next();
            if (!task.isCancelled()) {
                task.cancel();
            }
            taskIterator.remove();
        }
        if (calendarWatcher != null) {
            calendarWatcher.stop();
        }

        try {
            tpsWatcher.cancel();
        } catch (IllegalStateException ignored) {
        }

        /* Unload UpdateWatcher */
        if (this.updateWatcher != null) {
            this.updateWatcher.uninit();
        }

        Util.debugLog("Cleanup tasks...");

        try {
            Bukkit.getScheduler().cancelTasks(this);
        } catch (Throwable ignored) {
        }

        Util.debugLog("Cleanup listeners...");

        HandlerList.unregisterAll(this);
        Util.debugLog("Unregistering plugin services...");
        getServer().getServicesManager().unregisterAll(this);
        Util.debugLog("Cleanup...");
        Util.debugLog("All shutdown work is finished.");

    }

    public void reload() {
        PluginManager pluginManager = getServer().getPluginManager();
        try {
            File file = Paths.get(getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).toFile();
            //When quickshop was updated, we need to stop reloading
            if (getUpdateWatcher() != null) {
                File updatedJar = getUpdateWatcher().getUpdater().getUpdatedJar();
                if (updatedJar != null) {
                    throw new IllegalStateException("Failed to reload QuickShop! Please consider restarting the server. (Plugin was updated)");
                }
            }
            if (!file.exists()) {
                throw new IllegalStateException("Failed to reload QuickShop! Please consider restarting the server. (Failed to find plugin jar)");
            }
            Throwable throwable = PluginUtil.unload(this);
            if (throwable != null) {
                throw new IllegalStateException("Failed to reload QuickShop! Please consider restarting the server. (Plugin unloading has failed)", throwable);
            }
            Plugin plugin = pluginManager.loadPlugin(file);
            if (plugin != null) {
                plugin.onLoad();
                pluginManager.enablePlugin(plugin);
            } else {
                throw new IllegalStateException("Failed to reload QuickShop! Please consider restarting the server. (Plugin loading has failed)");
            }
        } catch (URISyntaxException | InvalidDescriptionException | InvalidPluginException e) {
            throw new RuntimeException("Failed to reload QuickShop! Please consider restarting the server.", e);
        }
    }

    private void initConfiguration() {
        /* Process the config */
        //noinspection ResultOfMethodCallIgnored
        getDataFolder().mkdirs();
        try {
            saveDefaultConfig();
        } catch (IllegalArgumentException resourceNotFoundException) {
            getLogger().severe("Failed to save config.yml from jar, The binary file of QuickShop may corrupted. Please re-download from our website.");
        }
        reloadConfiguration();
        if (getConfig().getInt("config-version", 0) == 0) {
            getConfig().set("config-version", 1);
        }
        /* It will generate a new UUID above updateConfig */
        this.serverUniqueID = UUID.fromString(Objects.requireNonNull(getConfig().getString("server-uuid", String.valueOf(UUID.randomUUID()))));
        try {
            updateConfig(getConfig().getInt("config-version"));
        } catch (IOException exception) {
            getLogger().log(Level.WARNING, "Failed to update configuration", exception);
        }
    }

    private void runtimeCheck(@NotNull EnvCheckEntry.Stage stage) {
        testing = true;
        environmentChecker = new org.maxgamer.quickshop.util.envcheck.EnvironmentChecker(this);
        ResultReport resultReport = environmentChecker.run(stage);
        StringJoiner joiner = new StringJoiner("\n", "", "");
        if (resultReport.getFinalResult().ordinal() > CheckResult.WARNING.ordinal()) {
            for (Entry<EnvCheckEntry, ResultContainer> result : resultReport.getResults().entrySet()) {
                if (result.getValue().getResult().ordinal() > CheckResult.WARNING.ordinal()) {
                    joiner.add(String.format("- [%s/%s] %s", result.getValue().getResult().getDisplay(), result.getKey().name(), result.getValue().getResultMessage()));
                }
            }
        }
        // Check If we need kill the server or disable plugin

        switch (resultReport.getFinalResult()) {
            case DISABLE_PLUGIN:
                Bukkit.getPluginManager().disablePlugin(this);
                break;
            case STOP_WORKING:
                setupBootError(new BootError(this.getLogger(), joiner.toString()), true);
                PluginCommand command = getCommand("qs");
                if (command != null) {
                    Util.mainThreadRun(() -> command.setTabCompleter(this)); //Disable tab completer
                }
                break;
            case KILL_SERVER:
                getLogger().severe("[Security Risk Detected] QuickShop forcing crash the server for security, contact the developer for details.");
                String result = environmentChecker.getReportMaker().bake();
                File reportFile = writeSecurityReportToFile();
                URI uri = null;
                if (reportFile != null) {
                    uri = reportFile.toURI();
                }
                if (uri != null) {
                    getLogger().warning("[Security Risk Detected] To get more details, please check: " + uri);
                    try {
                        if (java.awt.Desktop.isDesktopSupported()) {
                            java.awt.Desktop dp = java.awt.Desktop.getDesktop();
                            if (dp.isSupported(java.awt.Desktop.Action.BROWSE)) {
                                dp.browse(uri);
                                getLogger().warning("[Security Risk Detected] A browser already open for you. ");
                            }
                        }
                    } catch (Throwable ignored) {
                        //If failed, write directly to console
                        getLogger().severe(result);
                    }
                } else {
                    //If write failed, write directly to console
                    getLogger().severe(result);
                }
                //Wait for a while for user and logger outputting
                try {
                    //10 seconds
                    Thread.yield();
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // Halt the process, kill the server
                Runtime.getRuntime().halt(-1);
            default:
                break;
        }
        testing = false;
    }

    private File writeSecurityReportToFile() {
        File file = new File(getDataFolder(), UUID.randomUUID() + ".security.letter.txt");
        try {
            Files.write(new File(getDataFolder(), UUID.randomUUID() + ".security.letter.txt").toPath(), environmentChecker.getReportMaker().bake().getBytes(StandardCharsets.UTF_8));
            file = file.getCanonicalFile();
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Failed to write security report!", e);
            return null;
        }
        return file;
    }

    @Override
    public final void onEnable() {
        if (!this.onLoadCalled) {
            getLogger().severe("FATAL: onLoad not called and QuickShop trying patching them... Some Integrations will won't work or work incorrectly!");
            try {
                onLoad();
            } catch (Throwable ignored) {
            }
        }
        Timer enableTimer = new Timer(true);
        this.integrationHelper.callIntegrationsLoad(IntegrateStage.onEnableBegin);

        getLogger().info("QuickShop " + getFork());

        /* Check the running envs is support or not. */
        getLogger().info("Starting plugin self-test, please wait...");
        runtimeCheck(EnvCheckEntry.Stage.ON_ENABLE);

        getLogger().info("Reading the configuration...");
        this.initConfiguration();

        getLogger().info("Developers: " + Util.list2String(this.getDescription().getAuthors()));
        getLogger().info("Original author: Netherfoam, Timtower, KaiNoMood");
        getLogger().info("Let's start loading the plugin");
        getLogger().info("Chat processor selected: Hardcoded BungeeChat Lib");
        /* Process Metrics and Sentry error reporter. */
        metrics = new Metrics(this, 3320);

        try {
            if (!getConfig().getBoolean("auto-report-errors")) {
                Util.debugLog("Error reporter was disabled!");
            } else {
                sentryErrorReporter = new RollbarErrorReporter(this);
            }
        } catch (Throwable th) {
            getLogger().warning("Cannot load the Sentry Error Reporter: " + th.getMessage());
            getLogger().warning("Because our error reporter doesn't work, please report this error to developer, thank you!");
        }

        /* Initalize the Utils */
        this.loadItemMatcher();
        Util.initialize();
        try {
            MsgUtil.loadI18nFile();
        } catch (Exception e) {
            getLogger().log(Level.WARNING, "Error when loading translation", e);
        }
        MsgUtil.loadItemi18n();
        MsgUtil.loadEnchi18n();
        MsgUtil.loadPotioni18n();

        /* Load 3rd party supports */
        load3rdParty();

        //Load the database
        setupDBonEnableding = true;
        setupDatabase();

        setupDBonEnableding = false;

        /* Initalize the tools */
        // Create the shop manager.
        permissionManager = new PermissionManager(this);
        // This should be inited before shop manager
        if (this.display && getConfig().getBoolean("shop.display-auto-despawn")) {
            this.displayAutoDespawnWatcher = new DisplayAutoDespawnWatcher(this);
            //BUKKIT METHOD SHOULD ALWAYS EXECUTE ON THE SERVER MAIN THEAD
            timerTaskList.add(this.displayAutoDespawnWatcher.runTaskTimer(this, 20, getConfig().getInt("shop.display-check-time"))); // not worth async
        }

        getLogger().info("Registering commands...");
        /* PreInit for BootError feature */
        commandManager = new SimpleCommandManager(this);
        //noinspection ConstantConditions
        getCommand("qs").setExecutor(commandManager);
        //noinspection ConstantConditions
        getCommand("qs").setTabCompleter(commandManager);

        this.registerCustomCommands();

        this.shopManager = new SimpleShopManager(this);

        this.permissionChecker = new PermissionChecker(this);
        // Limit
        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "config.yml"));
        ConfigurationSection limitCfg = yamlConfiguration.getConfigurationSection("limits");
        if (limitCfg != null) {
            this.limit = limitCfg.getBoolean("use", false);
            limitCfg = limitCfg.getConfigurationSection("ranks");
            for (String key : Objects.requireNonNull(limitCfg).getKeys(true)) {
                limits.put(key, limitCfg.getInt(key));
            }
        }
        // Limit end
        if (getConfig().getInt("shop.finding.distance") > 100 && (getConfig().getBoolean("shop.finding.exclude-out-of-stock"))) {
            getLogger().severe("Shop find distance is too high with chunk loading feature turned on! It may cause lag! Pick a number under 100!");
        }

        if (getConfig().getBoolean("use-caching")) {
            this.shopCache = new Cache(this);
        } else {
            this.shopCache = null;
        }

        signUpdateWatcher = new SignUpdateWatcher();
        shopContainerWatcher = new ShopContainerWatcher();
        if (display && AbstractDisplayItem.getNowUsing() != DisplayType.VIRTUALITEM) {
            displayDupeRemoverWatcher = new DisplayDupeRemoverWatcher();
            timerTaskList.add(displayDupeRemoverWatcher.runTaskTimerAsynchronously(this, 0, 1));
        }
        /* Load all shops. */
        shopLoader = new ShopLoader(this);
        shopLoader.loadShops();

        getLogger().info("Registering listeners...");
        // Register events
        // Listeners (These don't)
        new BlockListener(this, this.shopCache).register();
        new PlayerListener(this).register();
        new WorldListener(this).register();
        // Listeners - We decide which one to use at runtime
        new ChatListener(this).register();
        new ChunkListener(this).register();
        new CustomInventoryListener(this).register();
        new ShopProtectionListener(this, this.shopCache).register();
        new PluginListener(this).register();
        new EconomySetupListener(this).register();
        InternalListener internalListener = new InternalListener(this);
        internalListener.register();

        if (this.display && AbstractDisplayItem.getNowUsing() != DisplayType.VIRTUALITEM) {
            if (getDisplayItemCheckTicks() > 0) {
                if (getConfig().getInt("shop.display-items-check-ticks") < 3000) {
                    getLogger().severe("Shop.display-items-check-ticks is too low! It may cause HUGE lag! Pick a number > 3000");
                }
                getLogger().info("Registering DisplayCheck task....");
                timerTaskList.add(getServer().getScheduler().runTaskTimer(this, () -> {
                    for (Shop shop : getShopManager().getLoadedShops()) {
                        //Shop may be deleted or unloaded when iterating
                        if (shop.isDeleted() || !shop.isLoaded()) {
                            continue;
                        }
                        shop.checkDisplay();
                    }
                }, 1L, getDisplayItemCheckTicks()));
            } else {
                if (getDisplayItemCheckTicks() != 0) {
                    getLogger().severe("Shop.display-items-check-ticks is invalid! Pick a number > 3000");
                } else {
                    getLogger().severe("Shop.display-items-check-ticks is zero, display check is disabled");
                }
            }
            new DisplayProtectionListener(this, this.shopCache).register();
            if (Bukkit.getPluginManager().getPlugin("ClearLag") != null) {
                new ClearLaggListener(this).register();
            }
        }
        if (getConfig().getBoolean("shop.lock")) {
            new LockListener(this, this.shopCache).register();
        }
        getLogger().info("Cleaning MsgUtils...");
        MsgUtil.loadTransactionMessages();
        MsgUtil.clean();
        if (this.getConfig().getBoolean("updater", true)) {
            updateWatcher = new UpdateWatcher();
            updateWatcher.init();
        }


        /* Delay the Ecoonomy system load, give a chance to let economy system regiser. */
        /* And we have a listener to listen the ServiceRegisterEvent :) */
        Util.debugLog("Loading economy system...");
        new BukkitRunnable() {
            @Override
            public void run() {
                loadEcon();
            }
        }.runTaskLater(this, 1);
        Util.debugLog("Registering watchers...");
        calendarWatcher = new CalendarWatcher(this);
        // shopVaildWatcher.runTaskTimer(this, 0, 20 * 60); // Nobody use it
        timerTaskList.add(signUpdateWatcher.runTaskTimer(this, 0, 10));
        timerTaskList.add(shopContainerWatcher.runTaskTimer(this, 0, 5)); // Nobody use it

        if (logWatcher != null) {
            timerTaskList.add(logWatcher.runTaskTimerAsynchronously(this, 10, 10));
            getLogger().info("Log actions is enabled, actions will log in the qs.log file!");
        }
        if (getConfig().getBoolean("shop.ongoing-fee.enable")) {
            ongoingFeeWatcher = new OngoingFeeWatcher(this);
            timerTaskList.add(ongoingFeeWatcher.runTaskTimerAsynchronously(this, 0, getConfig().getInt("shop.ongoing-fee.ticks")));
            getLogger().info("Ongoing fee feature is enabled.");
        }
        integrationHelper.searchAndRegisterPlugins();
        this.integrationHelper.callIntegrationsLoad(IntegrateStage.onEnableAfter);
        new BukkitRunnable() {
            @Override
            public void run() {
                getLogger().info("Registering bStats metrics...");
                submitMeritcs();
            }
        }.runTask(this);
        if (loaded) {
            getServer().getPluginManager().callEvent(new QSConfigurationReloadEvent(this));
        } else {
            loaded = true;
        }
        calendarWatcher = new CalendarWatcher(this);
        calendarWatcher.start();
        tpsWatcher.runTaskTimer(this, 1000, 50);
        this.shopPurger = new ShopPurger(this);
        if (getConfig().getBoolean("purge.at-server-startup")) {
            shopPurger.purge();
        }
        Util.debugLog("Now using display-type: " + AbstractDisplayItem.getNowUsing().name());
        getLogger().info("QuickShop Loaded! " + enableTimer.stopAndGetTimePassed() + " ms.");
    }

    private void loadItemMatcher() {
        ItemMatcher defItemMatcher;
        switch (getConfig().getInt("matcher.work-type")) {
            case 1:
                defItemMatcher = new BukkitItemMatcherImpl(this);
                break;
            case 0:
            default:
                defItemMatcher = new QuickShopItemMatcherImpl(this);
                break;
        }
        this.itemMatcher = ServiceInjector.getItemMatcher(defItemMatcher);
    }

    /**
     * Setup the database
     *
     * @return The setup result
     */
    private boolean setupDatabase() {
        getLogger().info("Setting up database...");
        try {
            ConfigurationSection dbCfg = getConfig().getConfigurationSection("database");
            AbstractDatabaseCore dbCore;
            if (Objects.requireNonNull(dbCfg).getBoolean("mysql")) {
                // MySQL database - Required database be created first.
                dbPrefix = dbCfg.getString("prefix");
                if (dbPrefix == null || "none".equals(dbPrefix)) {
                    dbPrefix = "";
                }
                String user = dbCfg.getString("user");
                String pass = dbCfg.getString("password");
                String host = dbCfg.getString("host");
                String port = dbCfg.getString("port");
                String database = dbCfg.getString("database");
                boolean useSSL = dbCfg.getBoolean("usessl");
                dbCore = new MySQLCore(this, Objects.requireNonNull(host, "MySQL host can't be null"), Objects.requireNonNull(user, "MySQL username can't be null"), Objects.requireNonNull(pass, "MySQL password can't be null"), Objects.requireNonNull(database, "MySQL database name can't be null"), Objects.requireNonNull(port, "MySQL port can't be null"), useSSL);
            } else {
                // SQLite database - Doing this handles file creation
                dbCore = new SQLiteCore(this, new File(this.getDataFolder(), "shops.db"));
            }
            this.databaseManager = new DatabaseManager(this, ServiceInjector.getDatabaseCore(dbCore));
            // Make the database up to date
            this.databaseHelper = new SimpleDatabaseHelper(this, this.databaseManager);
        } catch (DatabaseManager.ConnectionException e) {
            getLogger().log(Level.SEVERE, "Error when connecting to the database", e);
            if (setupDBonEnableding) {
                bootError = BuiltInSolution.databaseError();
            }
            return false;
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error when setup database", e);
            getServer().getPluginManager().disablePlugin(this);
            if (setupDBonEnableding) {
                bootError = BuiltInSolution.databaseError();
            }
            return false;
        }
        return true;
    }

    private void submitMeritcs() {
        if (!getConfig().getBoolean("disabled-metrics")) {
            String vaultVer;
            Plugin vault = Bukkit.getPluginManager().getPlugin("Vault");
            if (vault != null) {
                vaultVer = vault.getDescription().getVersion();
            } else {
                vaultVer = "Vault not found";
            }
            // Use internal Metric class not Maven for solve plugin name issues
            String economyType = AbstractEconomy.getNowUsing().name();
            if (getEconomy() != null) {
                economyType = this.getEconomy().getName();
            }
            String eventAdapter;
            if (getConfig().getInt("shop.protection-checking-handler") == 1) {
                eventAdapter = "QUICKSHOP";
            } else {
                eventAdapter = "BUKKIT";
            }
            // Version
            metrics.addCustomChart(new Metrics.SimplePie("server_version", Bukkit::getVersion));
            metrics.addCustomChart(new Metrics.SimplePie("bukkit_version", Bukkit::getBukkitVersion));
            metrics.addCustomChart(new Metrics.SimplePie("vault_version", () -> vaultVer));
            metrics.addCustomChart(new Metrics.SimplePie("use_display_items", () -> Util.boolean2Status(getConfig().getBoolean("shop.display-items"))));
            metrics.addCustomChart(new Metrics.SimplePie("use_locks", () -> Util.boolean2Status(getConfig().getBoolean("shop.lock"))));
            metrics.addCustomChart(new Metrics.SimplePie("use_sneak_action", () -> Util.boolean2Status(getConfig().getBoolean("shop.interact.sneak-to-create") || getConfig().getBoolean("shop.interact.sneak-to-trade") || getConfig().getBoolean("shop.interact.sneak-to-control"))));
            String finalEconomyType = economyType;
            metrics.addCustomChart(new Metrics.SimplePie("economy_type", () -> finalEconomyType));
            metrics.addCustomChart(new Metrics.SimplePie("use_display_auto_despawn", () -> String.valueOf(getConfig().getBoolean("shop.display-auto-despawn"))));
            metrics.addCustomChart(new Metrics.SimplePie("use_enhance_display_protect", () -> String.valueOf(getConfig().getBoolean("shop.enchance-display-protect"))));
            metrics.addCustomChart(new Metrics.SimplePie("use_enhance_shop_protect", () -> String.valueOf(getConfig().getBoolean("shop.enchance-shop-protect"))));
            metrics.addCustomChart(new Metrics.SimplePie("use_ongoing_fee", () -> String.valueOf(getConfig().getBoolean("shop.ongoing-fee.enable"))));
            metrics.addCustomChart(new Metrics.SimplePie("database_type", () -> this.getDatabaseManager().getDatabase().getName()));
            metrics.addCustomChart(new Metrics.SimplePie("display_type", () -> AbstractDisplayItem.getNowUsing().name()));
            metrics.addCustomChart(new Metrics.SimplePie("itemmatcher_type", () -> this.getItemMatcher().getName()));
            metrics.addCustomChart(new Metrics.SimplePie("use_stack_item", () -> String.valueOf(this.isAllowStack())));
            metrics.addCustomChart(new Metrics.SimplePie("chat_adapter", () -> "Hardcoded Adventure"));
            metrics.addCustomChart(new Metrics.SimplePie("event_adapter", () -> eventAdapter));
            metrics.addCustomChart(new Metrics.SingleLineChart("shops_created_on_all_servers", () -> this.getShopManager().getAllShops().size()));
        } else {
            getLogger().info("You have disabled mertics, Skipping...");
        }

    }

    //TODO: Refactor it
    private void updateConfig(int selectedVersion) throws IOException {
        String serverUUID = getConfig().getString("server-uuid");
        if (serverUUID == null || serverUUID.isEmpty()) {
            UUID uuid = UUID.randomUUID();
            serverUUID = uuid.toString();
            getConfig().set("server-uuid", serverUUID);
        }
        if (selectedVersion == 1) {
            getConfig().set("disabled-metrics", false);
            getConfig().set("config-version", 2);
            selectedVersion = 2;
        }
        if (selectedVersion == 2) {
            getConfig().set("protect.minecart", true);
            getConfig().set("protect.entity", true);
            getConfig().set("protect.redstone", true);
            getConfig().set("protect.structuregrow", true);
            getConfig().set("protect.explode", true);
            getConfig().set("protect.hopper", true);
            getConfig().set("config-version", 3);
            selectedVersion = 3;
        }
        if (selectedVersion == 3) {
            getConfig().set("shop.alternate-currency-symbol", '$');
            getConfig().set("config-version", 4);
            selectedVersion = 4;
        }
        if (selectedVersion == 4) {
            getConfig().set("updater", true);
            getConfig().set("config-version", 5);
            selectedVersion = 5;
        }
        if (selectedVersion == 5) {
            getConfig().set("config-version", 6);
            selectedVersion = 6;
        }
        if (selectedVersion == 6) {
            getConfig().set("shop.sneak-to-control", false);
            getConfig().set("config-version", 7);
            selectedVersion = 7;
        }
        if (selectedVersion == 7) {
            getConfig().set("database.prefix", "none");
            getConfig().set("config-version", 8);
            selectedVersion = 8;
        }
        if (selectedVersion == 8) {
            getConfig().set("limits.old-algorithm", false);
            getConfig().set("plugin.ProtocolLib", false);
            getConfig().set("shop.ignore-unlimited", false);
            getConfig().set("config-version", 9);
            selectedVersion = 9;
        }
        if (selectedVersion == 9) {
            getConfig().set("shop.enable-enderchest", true);
            getConfig().set("config-version", 10);
            selectedVersion = 10;
        }
        if (selectedVersion == 10) {
            getConfig().set("shop.pay-player-from-unlimited-shop-owner", null); // Removed
            getConfig().set("config-version", 11);
            selectedVersion = 11;
        }
        if (selectedVersion == 11) {
            getConfig().set("shop.enable-enderchest", null); // Removed
            getConfig().set("plugin.OpenInv", true);
            List<String> shoppable = getConfig().getStringList("shop-blocks");
            shoppable.add("ENDER_CHEST");
            getConfig().set("shop-blocks", shoppable);
            getConfig().set("config-version", 12);
            selectedVersion = 12;
        }
        if (selectedVersion == 12) {
            getConfig().set("plugin.ProtocolLib", null); // Removed
            getConfig().set("plugin.BKCommonLib", null); // Removed
            getConfig().set("database.use-varchar", null); // Removed
            getConfig().set("database.reconnect", null); // Removed
            getConfig().set("display-items-check-ticks", 1200);
            getConfig().set("shop.bypass-owner-check", null); // Removed
            getConfig().set("config-version", 13);
            selectedVersion = 13;
        }
        if (selectedVersion == 13) {
            getConfig().set("config-version", 14);
            selectedVersion = 14;
        }
        if (selectedVersion == 14) {
            getConfig().set("plugin.AreaShop", null);
            getConfig().set("shop.special-region-only", null);
            getConfig().set("config-version", 15);
            selectedVersion = 15;
        }
        if (selectedVersion == 15) {
            getConfig().set("ongoingfee", null);
            getConfig().set("shop.display-item-show-name", false);
            getConfig().set("shop.auto-fetch-shop-messages", true);
            getConfig().set("config-version", 16);
            selectedVersion = 16;
        }
        if (selectedVersion == 16) {
            getConfig().set("config-version", 17);
            selectedVersion = 17;
        }
        if (selectedVersion == 17) {
            getConfig().set("ignore-cancel-chat-event", false);
            getConfig().set("float", null);
            getConfig().set("config-version", 18);
            selectedVersion = 18;
        }
        if (selectedVersion == 18) {
            getConfig().set("shop.disable-vault-format", false);
            getConfig().set("config-version", 19);
            selectedVersion = 19;
        }
        if (selectedVersion == 19) {
            getConfig().set("shop.allow-shop-without-space-for-sign", true);
            getConfig().set("config-version", 20);
            selectedVersion = 20;
        }
        if (selectedVersion == 20) {
            getConfig().set("shop.maximum-price", -1);
            getConfig().set("config-version", 21);
            selectedVersion = 21;
        }
        if (selectedVersion == 21) {
            getConfig().set("shop.sign-material", "OAK_WALL_SIGN");
            getConfig().set("config-version", 22);
            selectedVersion = 22;
        }
        if (selectedVersion == 22) {
            getConfig().set("include-offlineplayer-list", "false");
            getConfig().set("config-version", 23);
            selectedVersion = 23;
        }
        if (selectedVersion == 23) {
            getConfig().set("lockette.enable", null);
            getConfig().set("lockette.item", null);
            getConfig().set("lockette.lore", null);
            getConfig().set("lockette.displayname", null);
            getConfig().set("float", null);
            getConfig().set("lockette.enable", true);
            getConfig().set("shop.blacklist-world", Lists.newArrayList("disabled_world_name"));
            getConfig().set("config-version", 24);
            selectedVersion = 24;
        }
        if (selectedVersion == 24) {
            getConfig().set("config-version", 25);
            selectedVersion = 25;
        }
        if (selectedVersion == 25) {
            String language = getConfig().getString("language");
            if (language == null || language.isEmpty() || "default".equals(language)) {
                getConfig().set("language", "en");
            }
            getConfig().set("config-version", 26);
            selectedVersion = 26;
        }
        if (selectedVersion == 26) {
            getConfig().set("database.usessl", false);
            getConfig().set("config-version", 27);
            selectedVersion = 27;
        }
        if (selectedVersion == 27) {
            getConfig().set("queue.enable", true);
            getConfig().set("queue.shops-per-tick", 20);
            getConfig().set("config-version", 28);
            selectedVersion = 28;
        }
        if (selectedVersion == 28) {
            getConfig().set("database.queue", true);
            getConfig().set("config-version", 29);
            selectedVersion = 29;
        }
        if (selectedVersion == 29) {
            getConfig().set("plugin.Multiverse-Core", null);
            getConfig().set("shop.protection-checking", true);
            getConfig().set("config-version", 30);
            selectedVersion = 30;
        }
        if (selectedVersion == 30) {
            getConfig().set("auto-report-errors", true);
            getConfig().set("config-version", 31);
            selectedVersion = 31;
        }
        if (selectedVersion == 31) {
            getConfig().set("shop.display-type", 0);
            getConfig().set("config-version", 32);
            selectedVersion = 32;
        }
        if (selectedVersion == 32) {
            getConfig().set("effect.sound.ontabcomplete", true);
            getConfig().set("effect.sound.oncommand", true);
            getConfig().set("effect.sound.ononclick", true);
            getConfig().set("config-version", 33);
            selectedVersion = 33;
        }
        if (selectedVersion == 33) {
            getConfig().set("matcher.item.damage", true);
            getConfig().set("matcher.item.displayname", true);
            getConfig().set("matcher.item.lores", true);
            getConfig().set("matcher.item.enchs", true);
            getConfig().set("matcher.item.potions", true);
            getConfig().set("matcher.item.attributes", true);
            getConfig().set("matcher.item.itemflags", true);
            getConfig().set("matcher.item.custommodeldata", true);
            getConfig().set("config-version", 34);
            selectedVersion = 34;
        }
        if (selectedVersion == 34) {
            if (getConfig().getInt("shop.display-items-check-ticks") == 1200) {
                getConfig().set("shop.display-items-check-ticks", 6000);
            }
            getConfig().set("config-version", 35);
            selectedVersion = 35;
        }
        if (selectedVersion == 35) {
            getConfig().set("queue", null); // Close it for everyone
            getConfig().set("config-version", 36);
            selectedVersion = 36;
        }
        if (selectedVersion == 36) {
            getConfig().set("economy-type", 0); // Close it for everyone
            getConfig().set("config-version", 37);
            selectedVersion = 37;
        }
        if (selectedVersion == 37) {
            getConfig().set("shop.ignore-cancel-chat-event", true);
            getConfig().set("config-version", 38);
            selectedVersion = 38;
        }
        if (selectedVersion == 38) {
            getConfig().set("protect.inventorymove", true);
            getConfig().set("protect.spread", true);
            getConfig().set("protect.fromto", true);
            getConfig().set("protect.minecart", null);
            getConfig().set("protect.hopper", null);
            getConfig().set("config-version", 39);
            selectedVersion = 39;
        }
        if (selectedVersion == 39) {
            getConfig().set("update-sign-when-inventory-moving", true);
            getConfig().set("config-version", 40);
            selectedVersion = 40;
        }
        if (selectedVersion == 40) {
            getConfig().set("allow-economy-loan", false);
            getConfig().set("config-version", 41);
            selectedVersion = 41;
        }
        if (selectedVersion == 41) {
            getConfig().set("send-display-item-protection-alert", true);
            getConfig().set("config-version", 42);
            selectedVersion = 42;
        }
        if (selectedVersion == 42) {
            getConfig().set("config-version", 43);
            selectedVersion = 43;
        }
        if (selectedVersion == 43) {
            getConfig().set("config-version", 44);
            selectedVersion = 44;
        }
        if (selectedVersion == 44) {
            getConfig().set("matcher.item.repaircost", false);
            getConfig().set("config-version", 45);
            selectedVersion = 45;
        }
        if (selectedVersion == 45) {
            getConfig().set("shop.display-item-use-name", true);
            getConfig().set("config-version", 46);
            selectedVersion = 46;
        }
        if (selectedVersion == 46) {
            getConfig().set("shop.max-shops-checks-in-once", 100);
            getConfig().set("config-version", 47);
            selectedVersion = 47;
        }
        if (selectedVersion == 47) {
            getConfig().set("config-version", 48);
            selectedVersion = 48;
        }
        if (selectedVersion == 48) {
            getConfig().set("permission-type", null);
            getConfig().set("shop.use-protection-checking-filter", null);
            getConfig().set("shop.protection-checking-filter", null);
            getConfig().set("config-version", 49);
            selectedVersion = 49;
        }
        if (selectedVersion == 49 || selectedVersion == 50) {
            getConfig().set("shop.enchance-display-protect", false);
            getConfig().set("shop.enchance-shop-protect", false);
            getConfig().set("protect", null);
            getConfig().set("config-version", 51);
            selectedVersion = 51;
        }
        if (selectedVersion < 60) { // Ahhh fuck versions
            getConfig().set("config-version", 60);
            selectedVersion = 60;
        }
        if (selectedVersion == 60) { // Ahhh fuck versions
            getConfig().set("shop.strict-matches-check", null);
            getConfig().set("shop.display-auto-despawn", true);
            getConfig().set("shop.display-despawn-range", 10);
            getConfig().set("shop.display-check-time", 10);
            getConfig().set("config-version", 61);
            selectedVersion = 61;
        }
        if (selectedVersion == 61) { // Ahhh fuck versions
            getConfig().set("shop.word-for-sell-all-items", "all");
            getConfig().set("plugin.PlaceHolderAPI", true);
            getConfig().set("config-version", 62);
            selectedVersion = 62;
        }
        if (selectedVersion == 62) { // Ahhh fuck versions
            getConfig().set("shop.display-auto-despawn", false);
            getConfig().set("shop.word-for-trade-all-items", getConfig().getString("shop.word-for-sell-all-items"));

            getConfig().set("config-version", 63);
            selectedVersion = 63;
        }
        if (selectedVersion == 63) { // Ahhh fuck versions
            getConfig().set("shop.ongoing-fee.enable", false);
            getConfig().set("shop.ongoing-fee.ticks", 42000);
            getConfig().set("shop.ongoing-fee.cost-per-shop", 2);
            getConfig().set("shop.ongoing-fee.ignore-unlimited", true);
            getConfig().set("config-version", 64);
            selectedVersion = 64;
        }
        if (selectedVersion == 64) {
            getConfig().set("shop.allow-free-shop", false);
            getConfig().set("config-version", 65);
            selectedVersion = 65;
        }
        if (selectedVersion == 65) {
            getConfig().set("shop.minimum-price", 0.01);
            getConfig().set("config-version", 66);
            selectedVersion = 66;
        }
        if (selectedVersion == 66) {
            getConfig().set("use-decimal-format", false);
            getConfig().set("decimal-format", "#,###.##");
            getConfig().set("shop.show-owner-uuid-in-controlpanel-if-op", false);
            getConfig().set("config-version", 67);
            selectedVersion = 67;
        }
        if (selectedVersion == 67) {
            getConfig().set("disable-debuglogger", false);
            getConfig().set("matcher.use-bukkit-matcher", null);
            getConfig().set("config-version", 68);
            selectedVersion = 68;
        }
        if (selectedVersion == 68) {
            getConfig().set("shop.blacklist-lores", Lists.newArrayList("SoulBound"));
            getConfig().set("config-version", 69);
            selectedVersion = 69;
        }
        if (selectedVersion == 69) {
            getConfig().set("shop.display-item-use-name", false);
            getConfig().set("config-version", 70);
            selectedVersion = 70;
        }
        if (selectedVersion == 70) {
            getConfig().set("cachingpool.enable", false);
            getConfig().set("cachingpool.maxsize", 100000000);
            getConfig().set("config-version", 71);
            selectedVersion = 71;
        }
        if (selectedVersion == 71) {
            if (Objects.equals(getConfig().getString("language"), "en")) {
                getConfig().set("language", "en-US");
            }
            getConfig().set("server-platform", 0);
            getConfig().set("config-version", 72);
            selectedVersion = 72;
        }
        if (selectedVersion == 72) {
            if (getConfig().getBoolean("use-deciaml-format")) {
                getConfig().set("use-decimal-format", getConfig().getBoolean("use-deciaml-format"));
            } else {
                getConfig().set("use-decimal-format", false);
            }
            getConfig().set("use-deciaml-format", null);

            getConfig().set("shop.force-load-downgrade-items.enable", false);
            getConfig().set("shop.force-load-downgrade-items.method", 0);
            getConfig().set("config-version", 73);
            selectedVersion = 73;
        }
        if (selectedVersion == 73) {
            getConfig().set("mixedeconomy.deposit", "eco give {0} {1}");
            getConfig().set("mixedeconomy.withdraw", "eco take {0} {1}");
            getConfig().set("config-version", 74);
            selectedVersion = 74;
        }
        if (selectedVersion == 74) {
            String langUtilsLanguage = getConfig().getString("langutils-language", "en_us");
            getConfig().set("langutils-language", null);
            if ("en_us".equals(langUtilsLanguage)) {
                langUtilsLanguage = "default";
            }
            getConfig().set("game-language", langUtilsLanguage);
            getConfig().set("maximum-digits-in-price", -1);
            getConfig().set("config-version", 75);
            selectedVersion = 75;
        }
        if (selectedVersion == 75) {
            getConfig().set("langutils-language", null);
            if (getConfig().get("game-language") == null) {
                getConfig().set("game-language", "default");
            }
            getConfig().set("config-version", 76);
            selectedVersion = 76;
        }
        if (selectedVersion == 76) {
            getConfig().set("database.auto-fix-encoding-issue-in-database", false);
            getConfig().set("send-shop-protection-alert", false);
            getConfig().set("send-display-item-protection-alert", false);
            getConfig().set("shop.use-fast-shop-search-algorithm", false);
            getConfig().set("config-version", 77);
            selectedVersion = 77;
        }
        if (selectedVersion == 77) {
            getConfig().set("integration.towny.enable", false);
            getConfig().set("integration.towny.create", new String[]{"SHOPTYPE", "MODIFY"});
            getConfig().set("integration.towny.trade", new String[]{});
            getConfig().set("integration.worldguard.enable", false);
            getConfig().set("integration.worldguard.create", new String[]{"FLAG", "CHEST_ACCESS"});
            getConfig().set("integration.worldguard.trade", new String[]{});
            getConfig().set("integration.plotsquared.enable", false);
            getConfig().set("integration.plotsquared.enable", false);
            getConfig().set("integration.plotsquared.enable", false);
            getConfig().set("integration.residence.enable", false);
            getConfig().set("integration.residence.create", new String[]{"FLAG", "interact", "use"});
            getConfig().set("integration.residence.trade", new String[]{});

            getConfig().set("integration.factions.enable", false);
            getConfig().set("integration.factions.create.flag", new String[]{});
            getConfig().set("integration.factions.trade.flag", new String[]{});
            getConfig().set("integration.factions.create.require.open", false);
            getConfig().set("integration.factions.create.require.normal", true);
            getConfig().set("integration.factions.create.require.wilderness", false);
            getConfig().set("integration.factions.create.require.peaceful", true);
            getConfig().set("integration.factions.create.require.permanent", false);
            getConfig().set("integration.factions.create.require.safezone", false);
            getConfig().set("integration.factions.create.require.own", false);
            getConfig().set("integration.factions.create.require.warzone", false);
            getConfig().set("integration.factions.trade.require.open", false);
            getConfig().set("integration.factions.trade.require.normal", true);
            getConfig().set("integration.factions.trade.require.wilderness", false);
            getConfig().set("integration.factions.trade.require.peaceful", false);
            getConfig().set("integration.factions.trade.require.permanent", false);
            getConfig().set("integration.factions.trade.require.safezone", false);
            getConfig().set("integration.factions.trade.require.own", false);
            getConfig().set("integration.factions.trade.require.warzone", false);
            getConfig().set("anonymous-metrics", null);
            getConfig().set("shop.ongoing-fee.async", true);
            getConfig().set("config-version", 78);
            selectedVersion = 78;
        }
        if (selectedVersion == 78) {
            getConfig().set("shop.display-type-specifics", null);
            getConfig().set("config-version", 79);
            selectedVersion = 79;
        }
        if (selectedVersion == 79) {
            getConfig().set("matcher.item.books", true);
            getConfig().set("config-version", 80);
            selectedVersion = 80;
        }
        if (selectedVersion == 80) {
            getConfig().set("shop.use-fast-shop-search-algorithm", true);
            getConfig().set("config-version", 81);
            selectedVersion = 81;
        }
        if (selectedVersion == 81) {
            getConfig().set("config-version", 82);
            selectedVersion = 82;
        }
        if (selectedVersion == 82) {
            getConfig().set("matcher.item.banner", true);
            getConfig().set("config-version", 83);
            selectedVersion = 83;
        }
        if (selectedVersion == 83) {
            getConfig().set("matcher.item.banner", true);
            getConfig().set("protect.explode", true);
            getConfig().set("config-version", 84);
            selectedVersion = 84;
        }
        if (selectedVersion == 84) {
            getConfig().set("disable-debuglogger", null);
            getConfig().set("config-version", 85);
            selectedVersion = 85;
        }
        if (selectedVersion == 85) {
            getConfig().set("config-version", 86);
            selectedVersion = 86;
        }
        if (selectedVersion == 86) {
            getConfig().set("shop.use-fast-shop-search-algorithm", true);
            getConfig().set("config-version", 87);
            selectedVersion = 87;
        }
        if (selectedVersion == 87) {
            getConfig().set("plugin.BlockHub.enable", true);
            getConfig().set("plugin.BlockHub.only", false);
            if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
                getConfig().set("shop.display-type", 2);
            }
            getConfig().set("config-version", 88);
            selectedVersion = 88;
        }
        if (selectedVersion == 88) {
            getConfig().set("respect-item-flag", true);
            getConfig().set("config-version", 89);
            selectedVersion = 89;
        }
        if (selectedVersion == 89) {
            getConfig().set("use-caching", true);
            getConfig().set("config-version", 90);
            selectedVersion = 90;
        }
        if (selectedVersion == 90) {
            getConfig().set("protect.hopper", true);
            getConfig().set("config-version", 91);
            selectedVersion = 91;
        }
        if (selectedVersion == 91) {
            getConfig().set("database.queue-commit-interval", 2);
            getConfig().set("config-version", 92);
            selectedVersion = 92;
        }
        if (selectedVersion == 92) {
            getConfig().set("send-display-item-protection-alert", false);
            getConfig().set("send-shop-protection-alert", false);
            getConfig().set("disable-creative-mode-trading", false);
            getConfig().set("disable-super-tool", false);
            getConfig().set("allow-owner-break-shop-sign", false);
            getConfig().set("matcher.item.skull", true);
            getConfig().set("matcher.item.firework", true);
            getConfig().set("matcher.item.map", true);
            getConfig().set("matcher.item.leatherArmor", true);
            getConfig().set("matcher.item.fishBucket", true);
            getConfig().set("matcher.item.suspiciousStew", true);
            getConfig().set("matcher.item.shulkerBox", true);
            getConfig().set("config-version", 93);
            selectedVersion = 93;
        }
        if (selectedVersion == 93) {
            getConfig().set("disable-creative-mode-trading", null);
            getConfig().set("disable-super-tool", null);
            getConfig().set("allow-owner-break-shop-sign", null);
            getConfig().set("shop.disable-creative-mode-trading", true);
            getConfig().set("shop.disable-super-tool", true);
            getConfig().set("shop.allow-owner-break-shop-sign", false);
            getConfig().set("config-version", 94);
            selectedVersion = 94;
        }
        if (selectedVersion == 94) {
            if (getConfig().get("price-restriction") != null) {
                getConfig().set("shop.price-restriction", getConfig().getStringList("price-restriction"));
                getConfig().set("price-restriction", null);
            } else {
                getConfig().set("shop.price-restriction", new ArrayList<>(0));
            }
            getConfig().set("enable-log4j", null);
            getConfig().set("config-version", 95);
            selectedVersion = 95;
        }
        if (selectedVersion == 95) {
            getConfig().set("shop.allow-stacks", false);
            getConfig().set("shop.display-allow-stacks", false);
            getConfig().set("custom-item-stacksize", new ArrayList<>(0));
            getConfig().set("config-version", 96);
            selectedVersion = 96;
        }
        if (selectedVersion == 96) {
            getConfig().set("shop.deny-non-shop-items-to-shop-container", false);
            getConfig().set("config-version", 97);
            selectedVersion = 97;
        }
        if (selectedVersion == 97) {
            getConfig().set("shop.disable-quick-create", false);
            getConfig().set("config-version", 98);
            selectedVersion = 98;
        }
        if (selectedVersion == 98) {
            getConfig().set("config-version", 99);
            selectedVersion = 99;
        }
        if (selectedVersion == 99) {
            getConfig().set("shop.currency-symbol-on-right", false);
            getConfig().set("config-version", 100);
            selectedVersion = 100;
        }
        if (selectedVersion == 100) {
            getConfig().set("integration.towny.ignore-disabled-worlds", false);
            getConfig().set("config-version", 101);
            selectedVersion = 101;
        }
        if (selectedVersion == 101) {
            getConfig().set("matcher.work-type", 1);
            getConfig().set("work-type", null);
            getConfig().set("plugin.LWC", true);
            getConfig().set("config-version", 102);
            selectedVersion = 102;
        }
        if (selectedVersion == 102) {
            getConfig().set("protect.entity", true);
            getConfig().set("config-version", 103);
            selectedVersion = 103;
        }
        if (selectedVersion == 103) {
            getConfig().set("integration.worldguard.whitelist-mode", false);
            getConfig().set("integration.factions.whitelist-mode", true);
            getConfig().set("integration.plotsquared.whitelist-mode", true);
            getConfig().set("integration.residence.whitelist-mode", true);
            getConfig().set("config-version", 104);
            selectedVersion = 104;
        }
        if (selectedVersion == 104) {
            getConfig().set("cachingpool", null);
            getConfig().set("config-version", 105);
            selectedVersion = 105;
        }
        if (selectedVersion == 105) {
            getConfig().set("shop.interact.sneak-to-create", getConfig().getBoolean("shop.sneak-to-create"));
            getConfig().set("shop.sneak-to-create", null);
            getConfig().set("shop.interact.sneak-to-trade", getConfig().getBoolean("shop.sneak-to-trade"));
            getConfig().set("shop.sneak-to-trade", null);
            getConfig().set("shop.interact.sneak-to-control", getConfig().getBoolean("shop.sneak-to-control"));
            getConfig().set("shop.sneak-to-control", null);
            getConfig().set("config-version", 106);
            selectedVersion = 106;
        }
        if (selectedVersion == 106) {
            getConfig().set("shop.use-enchantment-for-enchanted-book", false);
            getConfig().set("config-version", 107);
            selectedVersion = 107;
        }
        if (selectedVersion == 107) {
            getConfig().set("integration.lands.enable", false);
            getConfig().set("integration.lands.whitelist-mode", false);
            getConfig().set("integration.lands.ignore-disabled-worlds", true);
            getConfig().set("config-version", 108);
            selectedVersion = 108;
        }
        if (selectedVersion == 108) {
            getConfig().set("debug.shop-deletion", false);
            getConfig().set("config-version", 109);
            selectedVersion = 109;
        }
        if (selectedVersion == 109) {
            getConfig().set("shop.protection-checking-blacklist", Collections.singletonList("disabled_world"));
            getConfig().set("config-version", 110);
            selectedVersion = 110;
        }
        if (selectedVersion == 110) {
            getConfig().set("integration.worldguard.any-owner", true);
            getConfig().set("config-version", 111);
            selectedVersion = 111;
        }
        if (selectedVersion == 111) {
            getConfig().set("logging.enable", getConfig().getBoolean("log-actions"));
            getConfig().set("logging.log-actions", getConfig().getBoolean("log-actions"));
            getConfig().set("logging.log-balance", true);
            getConfig().set("logging.file-size", 10);
            getConfig().set("debug.disable-debuglogger", false);
            getConfig().set("trying-fix-banlance-insuffient", false);
            getConfig().set("log-actions", null);
            getConfig().set("config-version", 112);
            selectedVersion = 112;
        }
        if (selectedVersion == 112) {
            getConfig().set("integration.lands.delete-on-lose-permission", false);
            getConfig().set("config-version", 113);
            selectedVersion = 113;
        }
        if (selectedVersion == 113) {
            getConfig().set("config-damaged", false);
            getConfig().set("config-version", 114);
            selectedVersion = 114;
        }
        if (selectedVersion == 114) {
            getConfig().set("shop.interact.interact-mode", getConfig().getBoolean("shop.interact.switch-mode") ? 0 : 1);
            getConfig().set("shop.interact.switch-mode", null);
            getConfig().set("config-version", 115);
            selectedVersion = 115;
        }
        if (selectedVersion == 115) {
            getConfig().set("integration.griefprevention.enable", false);
            getConfig().set("integration.griefprevention.whitelist-mode", false);
            getConfig().set("integration.griefprevention.create", Collections.emptyList());
            getConfig().set("integration.griefprevention.trade", Collections.emptyList());
            getConfig().set("config-version", 116);
            selectedVersion = 116;
        }
        if (selectedVersion == 116) {
            getConfig().set("shop.sending-stock-message-to-staffs", false);
            getConfig().set("integration.towny.delete-shop-on-resident-leave", false);
            getConfig().set("config-version", 117);
            selectedVersion = 117;
        }
        if (selectedVersion == 117) {
            getConfig().set("shop.finding.distance", getConfig().getInt("shop.find-distance"));
            getConfig().set("shop.finding.limit", 10);
            getConfig().set("shop.find-distance", null);
            getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 118) {
            getConfig().set("shop.finding.oldLogic", false);
            getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 119) {
            getConfig().set("debug.adventure", false);
            getConfig().set("shop.finding.all", false);
            getConfig().set("chat-type", 0);
            getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 120) {
            getConfig().set("shop.finding.exclude-out-of-stock", false);
            getConfig().set("chat-type", 0);
            getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 121) {
            getConfig().set("shop.protection-checking-handler", 0);
            getConfig().set("shop.protection-checking-listener-blacklist", Collections.singletonList("ignored_listener"));
            getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 122) {
            getConfig().set("currency", "");
            getConfig().set("shop.alternate-currency-symbol-list", Arrays.asList("CNY;¥", "USD;$"));
            getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 123) {
            getConfig().set("integration.fabledskyblock.enable", false);
            getConfig().set("integration.fabledskyblock.whitelist-mode", false);
            getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 124) {
            getConfig().set("plugin.BKCommonLib", true);
            getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 125) {
            getConfig().set("integration.superiorskyblock.enable", false);
            getConfig().set("integration.superiorskyblock.owner-create-only", false);
            getConfig().set("integration.superiorskyblock.delete-shop-on-member-leave", true);
            getConfig().set("shop.interact.swap-click-behavior", false);
            getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 126) {
            getConfig().set("debug.delete-corrupt-shops", false);
            getConfig().set("config-version", ++selectedVersion);
        }

        if (selectedVersion == 127) {
            getConfig().set("integration.plotsquared.delete-when-user-untrusted", true);
            getConfig().set("integration.towny.delete-shop-on-plot-clear", true);
            getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 128) {
            getConfig().set("shop.force-use-item-original-name", false);
            getConfig().set("integration.griefprevention.delete-on-untrusted", false);
            getConfig().set("config-version", ++selectedVersion);
        }

        if (selectedVersion == 129) {
            getConfig().set("shop.use-global-virtual-item-queue", false);
            getConfig().set("config-version", ++selectedVersion);
        }

        if (selectedVersion == 130) {
            getConfig().set("plugin.WorldEdit", true);
            getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 131) {
            getConfig().set("custom-commands", ImmutableList.of("shop", "chestshop", "cshop"));
            getConfig().set("unlimited-shop-owner-change", false);
            getConfig().set("unlimited-shop-owner-change-account", "quickshop");
            getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 132) {
            getConfig().set("shop.sign-glowing", false);
            getConfig().set("shop.sign-dye-color", "null");
            getConfig().set("unlimited-shop-owner-change-account", "quickshop");
            getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 133) {
            getConfig().set("integration.griefprevention.delete-on-unclaim", false);
            getConfig().set("integration.griefprevention.delete-on-claim-expired", false);
            getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 134) {
            getConfig().set("integration.griefprevention.delete-on-claim-resized", false);
            getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 135) {
            getConfig().set("integration.advancedregionmarket.enable", true);
            getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 136) {
            getConfig().set("shop.use-global-virtual-item-queue", null);
            getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 137) {
            getConfig().set("integration.griefprevention.create", null);
            getConfig().set("integration.griefprevention.create", "INVENTORY");

            getConfig().set("integration.griefprevention.trade", null);
            getConfig().set("integration.griefprevention.trade", Collections.emptyList());

            boolean oldValueUntrusted = getConfig().getBoolean("integration.griefprevention.delete-on-untrusted", false);
            getConfig().set("integration.griefprevention.delete-on-untrusted", null);
            getConfig().set("integration.griefprevention.delete-on-claim-trust-changed", oldValueUntrusted);

            boolean oldValueUnclaim = getConfig().getBoolean("integration.griefprevention.delete-on-unclaim", false);
            getConfig().set("integration.griefprevention.delete-on-unclaim", null);
            getConfig().set("integration.griefprevention.delete-on-claim-unclaimed", oldValueUnclaim);

            getConfig().set("integration.griefprevention.delete-on-subclaim-created", false);
            getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 138) {
            getConfig().set("integration.towny.whitelist-mode", true);
            getConfig().set("config-version", ++selectedVersion);
        }

        if (selectedVersion == 139) {
            getConfig().set("integration.iridiumskyblock.enable", false);
            getConfig().set("integration.iridiumskyblock.owner-create-only", false);
            getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 140) {
            getConfig().set("integration.towny.delete-shop-on-plot-destroy", true);
            getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 141) {
            getConfig().set("disabled-languages", Collections.singletonList("disable_here"));
            getConfig().set("mojangapi-mirror", 0);
            getConfig().set("purge.enabled", false);
            getConfig().set("purge.days", 60);
            getConfig().set("purge.banned", true);
            getConfig().set("purge.skip-op", true);
            getConfig().set("purge.return-create-fee", true);
            getConfig().set("shop.use-fast-shop-search-algorithm", null);
            getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 142) {
            getConfig().set("disabled-languages", null);
            getConfig().set("enabled-languages", Collections.singletonList("*"));
            getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 143) {
//            if (getConfig().get("language") == null) {
//                getConfig().set("language", "en-US");
//            }
            getConfig().set("config-version", ++selectedVersion);
        }

        if (selectedVersion == 144) {
            // Updater set it to true because plugin upgrading
            // Default configuration disable it cause probably fresh install
            getConfig().set("legacy-updater.shop-sign", true);
            getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 145) {
            // Updater set it to true because plugin upgrading
            // Default configuration disable it cause probably fresh install
            getConfig().set("logger.location", 0);
            getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 146) {
            // Updater set it to true because plugin upgrading
            // Default configuration disable it cause probably fresh install
            getConfig().set("language", null);
            getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 147) {
            // Updater set it to true because plugin upgrading
            // Default configuration disable it cause probably fresh install
            getConfig().set("plugin.BKCommonLib", null);
            getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 148) {
            getConfig().set("integration.worldguard.respect-global-region", false);
            getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 149) {
            //Fix sign-dye-color setting is missing
            if (!getConfig().isSet("shop.sign-dye-color")) {
                getConfig().set("shop.sign-dye-color", "");
            }
            getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 150) {
            getConfig().set("purge.at-server-startup", true);
            getConfig().set("purge.backup", true);
            getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 151){
            getConfig().set("shop.protection-checking-handler",null);
            getConfig().set("config-version", ++selectedVersion);

        }
        if (getConfig().getInt("matcher.work-type") != 0 && GameVersion.get(ReflectFactory.getServerVersion()).name().contains("1_16")) {
            getLogger().warning("You are not using QS Matcher, it may meeting item comparing issue mentioned there: https://hub.spigotmc.org/jira/browse/SPIGOT-5063");
        }

        try (InputStreamReader buildInConfigReader = new InputStreamReader(new BufferedInputStream(Objects.requireNonNull(getResource("config.yml"))), StandardCharsets.UTF_8)) {
            if (new ConfigurationFixer(this, new File(getDataFolder(), "config.yml"), getConfig(), YamlConfiguration.loadConfiguration(buildInConfigReader)).fix()) {
                reloadConfiguration();
            }
        }

        //Check if server software support comment saving
        if (Util.isMethodAvailable("org.bukkit.configuration.MemoryConfiguration", "getInlineComments", String.class)) {
            //If so, clean the comment
            ConfigurationSection configurationSection = getConfig();
            List<String> emptyList = Collections.emptyList();
            for (String key : configurationSection.getKeys(true)) {
                configurationSection.setComments(key, emptyList);
                configurationSection.setInlineComments(key, emptyList);
            }
        }

        saveConfiguration();
        reloadConfiguration();

        //Re-add comment for config.yml
        try (InputStream inputStream = Objects.requireNonNull(getResource("config.yml"))) {
            new ConfigCommentUpdater(this, inputStream, new File(getDataFolder(), "config.yml")).updateComment();
        }

        //Delete old example configuration files
        Files.deleteIfExists(new File(getDataFolder(), "example.config.yml").toPath());
        Files.deleteIfExists(new File(getDataFolder(), "example-configuration.txt").toPath());
        Files.deleteIfExists(new File(getDataFolder(), "example-configuration.yml").toPath());

        try {
            if (new File(getDataFolder(), "messages.json").exists())
                Files.move(new File(getDataFolder(), "messages.json").toPath(), new File(getDataFolder(), "messages.json.outdated").toPath());
            if (new File(getDataFolder(), "messages.json.outdated").exists()) {
                Files.write(new File(getDataFolder(), "about-messages.json.outdated.txt").toPath(), Collections.singletonList("Please read v4 migrate guide there, after migration you can delete messages.json.outdated and this file: https://github.com/PotatoCraft-Studio/QuickShop-Reremake/wiki/Migrate:-v4-to-v5"));
            }
        } catch (Exception ignore) {
        }

//        // Path exampleConfigFile = new File(getDataFolder(), "example-configuration.yml").toPath();
//        try {
//            Files.copy(Objects.requireNonNull(getResource("config.yml")), exampleConfigFile, REPLACE_EXISTING);
//        } catch (IOException ioe) {
//            getLogger().warning("Error when creating the example config file: " + ioe.getMessage());
//        }
    }

    /**
     * Mark plugins stop working
     *
     * @param bootError           reason
     * @param unregisterListeners should we disable all listeners?
     */
    public void setupBootError(BootError bootError, boolean unregisterListeners) {
        this.bootError = bootError;
        if (unregisterListeners) {
            HandlerList.unregisterAll(this);
        }
        Bukkit.getScheduler().cancelTasks(this);
    }

    public void registerCustomCommands() {
        List<String> customCommands = getConfig().getStringList("custom-commands");
        PluginCommand quickShopCommand = getCommand("qs");
        if (quickShopCommand == null) {
            getLogger().warning("Failed to get QuickShop PluginCommand instance.");
            return;
        }
        List<String> aliases = quickShopCommand.getAliases();
        aliases.addAll(customCommands);
        quickShopCommand.setAliases(aliases);
        try {
            ReflectFactory.getCommandMap().register("qs", quickShopCommand);
            ReflectFactory.syncCommands();
        } catch (Exception e) {
            getLogger().log(Level.WARNING, "Failed to register command aliases", e);
            return;
        }
        Util.debugLog("Command alias successfully registered.");
    }

    public @NotNull TextManager text() {
        return this.textManager;
    }

    @Override
    public CompatibilityManager getCompatibilityManager() {
        return this.compatibilityTool;
    }

    @Override
    public ShopManager getShopManager() {
        return this.shopManager;
    }

    @Override
    public boolean isDisplayEnabled() {
        return this.display;
    }

    @Override
    public boolean isLimit() {
        return this.limit;
    }

    @Override
    public DatabaseHelper getDatabaseHelper() {
        return this.databaseHelper;
    }

    @Override
    public TextManager getTextManager() {
        return this.textManager;
    }

    @Override
    public ItemMatcher getItemMatcher() {
        return this.itemMatcher;
    }

    @Override
    public boolean isPriceChangeRequiresFee() {
        return this.priceChangeRequiresFee;
    }

    @Override
    public CommandManager getCommandManager() {
        return this.commandManager;
    }

    @Override
    public Map<String, Integer> getLimits() {
        return this.limits;
    }

    @Override
    public GameVersion getGameVersion() {
        if (gameVersion == null) {
            gameVersion = GameVersion.get(ReflectFactory.getNMSVersion());
        }
        return this.gameVersion;
    }
}
