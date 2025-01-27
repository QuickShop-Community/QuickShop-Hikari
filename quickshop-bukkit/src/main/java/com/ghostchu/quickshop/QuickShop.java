package com.ghostchu.quickshop;

import cc.carm.lib.easysql.EasySQL;
import cc.carm.lib.easysql.api.SQLManager;
import cc.carm.lib.easysql.hikari.HikariConfig;
import cc.carm.lib.easysql.hikari.HikariDataSource;
import cc.carm.lib.easysql.manager.SQLManagerImpl;
import com.ghostchu.quickshop.api.GameVersion;
import com.ghostchu.quickshop.api.QuickShopAPI;
import com.ghostchu.quickshop.api.QuickShopProvider;
import com.ghostchu.quickshop.api.RankLimiter;
import com.ghostchu.quickshop.api.command.CommandManager;
import com.ghostchu.quickshop.api.database.DatabaseHelper;
import com.ghostchu.quickshop.api.economy.AbstractEconomy;
import com.ghostchu.quickshop.api.economy.EconomyType;
import com.ghostchu.quickshop.api.event.QSConfigurationReloadEvent;
import com.ghostchu.quickshop.api.inventory.InventoryWrapperManager;
import com.ghostchu.quickshop.api.inventory.InventoryWrapperRegistry;
import com.ghostchu.quickshop.api.localization.text.TextManager;
import com.ghostchu.quickshop.api.registry.BuiltInRegistry;
import com.ghostchu.quickshop.api.registry.RegistryManager;
import com.ghostchu.quickshop.api.registry.builtin.itemexpression.ItemExpressionRegistry;
import com.ghostchu.quickshop.api.shop.ItemMatcher;
import com.ghostchu.quickshop.api.shop.PlayerFinder;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopControlPanelManager;
import com.ghostchu.quickshop.api.shop.ShopItemBlackList;
import com.ghostchu.quickshop.api.shop.ShopManager;
import com.ghostchu.quickshop.api.shop.display.DisplayType;
import com.ghostchu.quickshop.command.QuickShopCommand;
import com.ghostchu.quickshop.command.SimpleCommandManager;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.common.util.JsonUtil;
import com.ghostchu.quickshop.common.util.QuickExecutor;
import com.ghostchu.quickshop.database.DatabaseIOUtil;
import com.ghostchu.quickshop.database.HikariUtil;
import com.ghostchu.quickshop.database.SimpleDatabaseHelperV2;
import com.ghostchu.quickshop.economy.impl.Economy_Vault;
import com.ghostchu.quickshop.economy.impl.Economy_VaultUnlocked;
import com.ghostchu.quickshop.listener.BlockListener;
import com.ghostchu.quickshop.listener.BungeeListener;
import com.ghostchu.quickshop.listener.ChatListener;
import com.ghostchu.quickshop.listener.ChunkListener;
import com.ghostchu.quickshop.listener.CustomInventoryListener;
import com.ghostchu.quickshop.listener.DisplayProtectionListener;
import com.ghostchu.quickshop.listener.InternalListener;
import com.ghostchu.quickshop.listener.LockListener;
import com.ghostchu.quickshop.listener.PlayerListener;
import com.ghostchu.quickshop.listener.ShopProtectionListener;
import com.ghostchu.quickshop.listener.WorldListener;
import com.ghostchu.quickshop.localization.text.SimpleTextManager;
import com.ghostchu.quickshop.menu.ShopBrowseMenu;
import com.ghostchu.quickshop.menu.ShopHistoryMenu;
import com.ghostchu.quickshop.menu.ShopKeeperMenu;
import com.ghostchu.quickshop.menu.ShopStaffMenu;
import com.ghostchu.quickshop.menu.ShopTradeMenu;
import com.ghostchu.quickshop.metric.MetricListener;
import com.ghostchu.quickshop.papi.QuickShopPAPI;
import com.ghostchu.quickshop.permission.PermissionManager;
import com.ghostchu.quickshop.platform.Platform;
import com.ghostchu.quickshop.registry.SimpleRegistryManager;
import com.ghostchu.quickshop.registry.builtin.itemexpression.SimpleItemExpressionRegistry;
import com.ghostchu.quickshop.registry.builtin.itemexpression.handlers.SimpleEnchantmentExpressionHandler;
import com.ghostchu.quickshop.registry.builtin.itemexpression.handlers.SimpleItemReferenceExpressionHandler;
import com.ghostchu.quickshop.registry.builtin.itemexpression.handlers.SimpleMaterialExpressionHandler;
import com.ghostchu.quickshop.shop.InteractionController;
import com.ghostchu.quickshop.shop.ShopLoader;
import com.ghostchu.quickshop.shop.ShopPurger;
import com.ghostchu.quickshop.shop.SimpleShopItemBlackList;
import com.ghostchu.quickshop.shop.SimpleShopManager;
import com.ghostchu.quickshop.shop.SimpleShopPermissionManager;
import com.ghostchu.quickshop.shop.controlpanel.SimpleShopControlPanel;
import com.ghostchu.quickshop.shop.controlpanel.SimpleShopControlPanelManager;
import com.ghostchu.quickshop.shop.display.AbstractDisplayItem;
import com.ghostchu.quickshop.shop.display.virtual.VirtualDisplayItemManager;
import com.ghostchu.quickshop.shop.inventory.BukkitInventoryWrapperManager;
import com.ghostchu.quickshop.shop.sign.SignHooker;
import com.ghostchu.quickshop.util.FastPlayerFinder;
import com.ghostchu.quickshop.util.ItemMarker;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.PackageUtil;
import com.ghostchu.quickshop.util.PermissionChecker;
import com.ghostchu.quickshop.util.ReflectFactory;
import com.ghostchu.quickshop.util.ShopUtil;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.config.ConfigUpdateScript;
import com.ghostchu.quickshop.util.config.ConfigurationUpdater;
import com.ghostchu.quickshop.util.envcheck.CheckResult;
import com.ghostchu.quickshop.util.envcheck.EnvCheckEntry;
import com.ghostchu.quickshop.util.envcheck.EnvironmentChecker;
import com.ghostchu.quickshop.util.envcheck.ResultContainer;
import com.ghostchu.quickshop.util.envcheck.ResultReport;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.quickshop.util.matcher.item.BukkitItemMatcherImpl;
import com.ghostchu.quickshop.util.matcher.item.QuickShopItemMatcherImpl;
import com.ghostchu.quickshop.util.matcher.item.TNEItemMatcherImpl;
import com.ghostchu.quickshop.util.metric.MetricManager;
import com.ghostchu.quickshop.util.paste.PasteManager;
import com.ghostchu.quickshop.util.performance.PerfMonitor;
import com.ghostchu.quickshop.util.privacy.PrivacyController;
import com.ghostchu.quickshop.util.reporter.error.RollbarErrorReporter;
import com.ghostchu.quickshop.util.updater.NexusManager;
import com.ghostchu.quickshop.watcher.CalendarWatcher;
import com.ghostchu.quickshop.watcher.DisplayAutoDespawnWatcher;
import com.ghostchu.quickshop.watcher.LogWatcher;
import com.ghostchu.quickshop.watcher.OngoingFeeWatcher;
import com.ghostchu.quickshop.watcher.ShopDataSaveWatcher;
import com.ghostchu.quickshop.watcher.SignUpdateWatcher;
import com.ghostchu.quickshop.watcher.UpdateWatcher;
import com.ghostchu.simplereloadlib.ReloadManager;
import com.ghostchu.simplereloadlib.ReloadResult;
import com.ghostchu.simplereloadlib.Reloadable;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.tcoded.folialib.FoliaLib;
import com.vdurmont.semver4j.Semver;
import io.papermc.lib.PaperLib;
import lombok.Getter;
import lombok.Setter;
import net.tnemc.item.AbstractItemStack;
import net.tnemc.item.bukkit.BukkitHelper;
import net.tnemc.item.bukkit.BukkitItemStack;
import net.tnemc.item.paper.PaperItemStack;
import net.tnemc.item.providers.HelperMethods;
import net.tnemc.menu.bukkit.BukkitMenuHandler;
import net.tnemc.menu.bukkit.BukkitPlayer;
import net.tnemc.menu.core.MenuHandler;
import net.tnemc.menu.core.compatibility.MenuPlayer;
import net.tnemc.menu.core.manager.MenuManager;
import net.tnemc.menu.folia.FoliaMenuHandler;
import net.tnemc.menu.folia.FoliaPlayer;
import net.tnemc.menu.paper.PaperMenuHandler;
import net.tnemc.menu.paper.PaperPlayer;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.h2.Driver;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class QuickShop implements QuickShopAPI, Reloadable {

  public static final Queue<UUID> inShop = new ConcurrentLinkedQueue<>();

  public static final Cache<UUID, ShopUtil.PendingTransferTask> taskCache = CacheBuilder
          .newBuilder()
          .expireAfterWrite(60, TimeUnit.SECONDS)
          .build();

  /**
   * If running environment test
   */
  @Getter
  private static final boolean TESTING = false;
  /**
   * The active instance of QuickShop You shouldn't use this if you really need it.
   */
  @ApiStatus.Internal
  private static QuickShop instance;
  /**
   * The manager to check permissions.
   */
  private static PermissionManager permissionManager;
  @Getter
  private final ReloadManager reloadManager = new ReloadManager();
  private final InventoryWrapperRegistry inventoryWrapperRegistry = new InventoryWrapperRegistry();
  @Getter
  private final InventoryWrapperManager inventoryWrapperManager = new BukkitInventoryWrapperManager();
  @Getter
  private final ShopControlPanelManager shopControlPanelManager = new SimpleShopControlPanelManager(this);
  private final Map<String, String> addonRegisteredMapping = new HashMap<>();
  @Getter
  private final QuickShopBukkit javaPlugin;
  private final Logger logger;
  @Getter
  private final Platform platform;
  @Getter
  private final EconomyLoader economyLoader = new EconomyLoader(this);
  @Getter
  private final PasteManager pasteManager = new PasteManager();

  private FoliaLib folia;
  protected MenuHandler menuHandler;
  protected HelperMethods helperMethods;

  /* Public QuickShop API End */
  private GameVersion gameVersion;
  private volatile SimpleDatabaseHelperV2 databaseHelper;
  private SimpleCommandManager commandManager;
  private ItemMatcher itemMatcher;
  private SimpleShopManager shopManager;
  private SimpleTextManager textManager;
  @Getter
  private SimpleShopPermissionManager shopPermissionManager;
  private boolean priceChangeRequiresFee = false;
  @Getter
  private DatabaseDriverType databaseDriverType = null;
  /**
   * The BootError, if it not NULL, plugin will stop loading and show setted errors when use /qs
   */
  @Nullable
  @Getter
  @Setter
  private BootError bootError;
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
  /**
   * The economy we hook into for transactions
   */
  @Getter
  private AbstractEconomy economy;
  @Nullable
  @Getter
  private LogWatcher logWatcher;

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
  @Nullable
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
  private ShopPurger shopPurger;
  private int loggingLocation = 0;
  @Getter
  private InteractionController interactionController;
  @Getter
  private volatile SQLManager sqlManager;
  @Getter
  @Nullable
  private QuickShopPAPI quickShopPAPI;
  @Getter
  private ItemMarker itemMarker;
  private Map<String, String> translationMapping;
  @Getter
  private PlayerFinder playerFinder;
  @Getter
  private ShopItemBlackList shopItemBlackList;
  @Getter
  private NexusManager nexusManager;
  @Getter
  private ShopDataSaveWatcher shopSaveWatcher;
  @Getter
  private SignHooker signHooker;
  @Getter
  private BungeeListener bungeeListener;
  private RankLimiter rankLimiter;
  @Nullable
  @Getter
  private VirtualDisplayItemManager virtualDisplayItemManager;
  @Getter
  private PrivacyController privacyController;
  @Getter
  private MetricManager metricManager;
  @Getter
  private RegistryManager registry;
//    @Getter
//    private InventoryWrapperUpdateManager invWrapperUpdateManager;

  public QuickShop(final QuickShopBukkit javaPlugin, final Logger logger, final Platform platform) {

    this.javaPlugin = javaPlugin;
    this.logger = logger;
    this.platform = platform;
    this.helperMethods = new BukkitHelper();
  }

  /**
   * Get the QuickShop instance You should use QuickShopAPI if possible, we don't promise the
   * internal access will be stable
   *
   * @return QuickShop instance
   */
  @NotNull
  public static QuickShop getInstance() {

    return instance;
  }

  /**
   * Get the permissionManager as static
   *
   * @return the permission Manager.
   */
  @NotNull
  public static PermissionManager getPermissionManager() {

    return permissionManager;
  }


  /**
   * Early than onEnable, make sure instance was loaded in first time.
   */
  public final void onLoad() {

    instance = this;

    registerService();
    // Reset the BootError status to normal.
    this.bootError = null;
    Util.setPlugin(this);
    logger.info("QuickShop {} - Early boot step - Booting up", javaPlugin.getFork());
    getReloadManager().register(this);
    //BEWARE THESE ONLY RUN ONCE
    this.buildInfo = new BuildInfo(javaPlugin.getResource("BUILDINFO"));
    logger.info("Self testing...");
    if(!runtimeCheck(EnvCheckEntry.Stage.ON_LOAD)) {
      return;
    }
    logger.info("Reading the configuration...");
    initConfiguration();
    logger.info("Setting up privacy controller...");
    this.privacyController = new PrivacyController(this);
    logger.info("Setting up QuickShop registry....");
    this.registry = new SimpleRegistryManager();
    this.registry.registerRegistry(BuiltInRegistry.ITEM_EXPRESSION.getName(), new SimpleItemExpressionRegistry(this));
    logger.info("Setting up metrics manager...");
    this.metricManager = new MetricManager(this);
    logger.info("Loading player name and unique id mapping...");
    this.playerFinder = new FastPlayerFinder(this);
    loadTextManager();
    logger.info("Register InventoryWrapper...");
    this.inventoryWrapperRegistry.register(javaPlugin, this.inventoryWrapperManager);
    logger.info("Initializing NexusManager...");
    this.nexusManager = new NexusManager(this);
    logger.info("QuickShop " + javaPlugin.getFork() + " - Early boot step - Complete");
  }

  private void registerService() {

    logger.info("Registering Bukkit Service: {}", QuickShopProvider.class.getName());
    Bukkit.getServicesManager().register(QuickShopProvider.class, new QuickShopProvider() {
      @Override
      public @NotNull QuickShopAPI getApiInstance() {

        return instance;
      }

      @Override
      public @NotNull Plugin getInstance() {

        return javaPlugin;
      }
    }, javaPlugin, ServicePriority.High);
  }

  private boolean runtimeCheck(@NotNull final EnvCheckEntry.Stage stage) {

    environmentChecker = new EnvironmentChecker(this);
    final ResultReport resultReport = environmentChecker.run(stage);
    final StringJoiner joiner = new StringJoiner("\n", "", "");
    if(resultReport.getFinalResult().ordinal() > CheckResult.WARNING.ordinal()) {
      for(final Entry<EnvCheckEntry, ResultContainer> result : resultReport.getResults().entrySet()) {
        if(result.getValue().getResult().ordinal() > CheckResult.WARNING.ordinal()) {
          joiner.add(String.format("- [%s/%s] %s", result.getValue().getResult().getDisplay(), result.getKey().name(), result.getValue().getResultMessage()));
        }
      }
    }
    // Check If we need kill the server or disable plugin

    switch(resultReport.getFinalResult()) {
      case DISABLE_PLUGIN -> {
        Bukkit.getPluginManager().disablePlugin(javaPlugin);
        return false;
      }
      case STOP_WORKING -> {
        setupBootError(new BootError(logger, joiner.toString()), true);
        final PluginCommand command = javaPlugin.getCommand("quickshop");
        if(command != null) {
          Util.mainThreadRun(()->command.setTabCompleter(javaPlugin)); //Disable tab completer
        }
      }
      default -> {
      }
    }
    return true;
  }

  private void initConfiguration() {
    /* Process the config */
    //noinspection ResultOfMethodCallIgnored
    javaPlugin.getDataFolder().mkdirs();
    try {
      javaPlugin.saveDefaultConfig();
    } catch(final IllegalArgumentException resourceNotFoundException) {
      logger.error("Failed to save config.yml from jar, The binary file of QuickShop may be corrupted. Please re-download from our website.");
    }
    javaPlugin.reloadConfig();
    if(getConfig().getInt("config-version", 0) == 0) {
      getConfig().set("config-version", 1);
    }
    /* It will generate a new UUID above updateConfig */
    this.serverUniqueID = UUID.fromString(Objects.requireNonNull(getConfig().getString("server-uuid", String.valueOf(UUID.randomUUID()))));
    updateConfig();
  }

  private void loadTextManager() {

    logger.info("Loading translations (This may take a while)...");
    try {
      this.textManager = new SimpleTextManager(this);
    } catch(final NoSuchMethodError | NoClassDefFoundError e) {
      logger.error("Failed to initialize text manager, the QuickShop doesn't compatible with your Server version. Did you up-to-date?", e);
      Bukkit.getPluginManager().disablePlugin(javaPlugin);
      throw new IllegalStateException("Cannot initialize text manager");
    }
    textManager.load();
  }

  /**
   * Mark plugins stop working
   *
   * @param bootError           reason
   * @param unregisterListeners should we disable all listeners?
   */
  public void setupBootError(final BootError bootError, final boolean unregisterListeners) {

    this.bootError = bootError;
    if(unregisterListeners) {
      HandlerList.unregisterAll(javaPlugin);
    }
    folia.getImpl().cancelAllTasks();
  }

  /**
   * Reloads QuickShops config
   */
  public void reloadConfigSubModule() {
    // Load quick variables
    this.display = this.getConfig().getBoolean("shop.display-items");
    final int type = getConfig().getInt("shop.display-type");
    if(type != 2 && type != 900) {
      this.display = false;
    }

    this.priceChangeRequiresFee = this.getConfig().getBoolean("shop.price-change-requires-fee");
    this.displayItemCheckTicks = this.getConfig().getInt("shop.display-items-check-ticks");
    this.allowStack = this.getConfig().getBoolean("shop.allow-stacks");
    this.currency = this.getConfig().getString("currency");
    this.loggingLocation = this.getConfig().getInt("logging.location");
    this.translationMapping = new HashMap<>();
    getConfig().getStringList("custom-translation-key").forEach(str->{
      final String[] split = str.split("=", 0);
      this.translationMapping.put(split[0], split[1]);
    });
    this.translationMapping.putAll(this.addonRegisteredMapping);
    if(this.platform != null) {
      this.platform.updateTranslationMappingSection(this.translationMapping);
    }

    if(StringUtils.isEmpty(this.currency)) {
      this.currency = null;
    }
    if(this.getConfig().getBoolean("logging.enable")) {
      logWatcher = new LogWatcher(this, new File(javaPlugin.getDataFolder(), "qs.log"));
    } else {
      logWatcher = null;
    }
    // Schedule this event can be run in next tick.
    //Util.mainThreadRun(() -> new QSConfigurationReloadEvent(javaPlugin).callEvent());
  }

  @NotNull
  public FileConfiguration getConfig() {

    return javaPlugin.getConfig();
  }

  private void updateConfig() {

    new ConfigurationUpdater(this).update(new ConfigUpdateScript(getConfig(), this));
  }

  @NotNull
  public Logger logger() {

    return logger;
  }

  @Override
  public CommandManager getCommandManager() {

    return this.commandManager;
  }

  @Override
  public DatabaseHelper getDatabaseHelper() {

    return this.databaseHelper;
  }

  @Override
  public GameVersion getGameVersion() {

    if(gameVersion == null) {
      gameVersion = GameVersion.get(ReflectFactory.getNMSVersion());
      if(gameVersion == GameVersion.UNKNOWN) {
        gameVersion = GameVersion.get(platform.getMinecraftVersion());
      }
    }
    return this.gameVersion;
  }

  @Override
  public @NotNull InventoryWrapperRegistry getInventoryWrapperRegistry() {

    return inventoryWrapperRegistry;
  }

  @Override
  public ItemMatcher getItemMatcher() {

    return this.itemMatcher;
  }

  @SuppressWarnings("removal")
  @Override
  @ApiStatus.Obsolete
  @Deprecated(forRemoval = true)
  public Map<String, Integer> getLimits() {

    return this.rankLimiter.getLimits();
  }

  @Override
  public ShopManager getShopManager() {

    return this.shopManager;
  }

  @Override
  public TextManager getTextManager() {

    return this.textManager;
  }

  @Override
  public boolean isDisplayEnabled() {

    return this.display;
  }

  @SuppressWarnings("removal")
  @Override
  @Deprecated(forRemoval = true)
  @ApiStatus.Obsolete
  public boolean isLimit() {

    return this.rankLimiter.isLimit();
  }

  @Override
  public RankLimiter getRankLimiter() {

    return rankLimiter;
  }

  @Override
  public boolean isPriceChangeRequiresFee() {

    return this.priceChangeRequiresFee;
  }

  @Override
  public void logEvent(@NotNull final Object eventObject) {

    if(this.getLogWatcher() == null) {
      return;
    }
    if(loggingLocation == 0) {
      this.getLogWatcher().log(JsonUtil.getGson().toJson(eventObject));
    } else {
      getDatabaseHelper().insertHistoryRecord(eventObject)
              .thenAccept(result->{
              })
              .exceptionally(throwable->{
                Log.debug("Failed to log event: " + throwable.getMessage());
                return null;
              });
    }

  }

  @Override
  public void registerLocalizedTranslationKeyMapping(@NotNull final String translationKey, @NotNull final String key) {

    addonRegisteredMapping.put(translationKey, key);
    translationMapping.putAll(addonRegisteredMapping);
    if(this.platform != null) {
      this.platform.updateTranslationMappingSection(translationMapping);
    }
  }

  @Override
  public Semver getSemVersion() {

    return javaPlugin.getSemVersion();
  }

  /**
   * Get the permissionManager as static
   *
   * @return the permission Manager.
   */
  @NotNull
  public PermissionManager perm() {

    return permissionManager;
  }

  public final void onEnable() {

    logger.info("QuickShop " + javaPlugin.getFork());

    this.folia = new FoliaLib(javaPlugin);

    if(this.folia.isFolia()) {
      this.menuHandler = new FoliaMenuHandler(javaPlugin, true);
    } else if(this.folia.isPaper()) {
      this.menuHandler = new PaperMenuHandler(javaPlugin, true);
    } else {
      this.menuHandler = new BukkitMenuHandler(javaPlugin, true);
    }

    MenuManager.instance().addMenu(new ShopHistoryMenu());
    MenuManager.instance().addMenu(new ShopKeeperMenu());
    MenuManager.instance().addMenu(new ShopBrowseMenu());
    MenuManager.instance().addMenu(new ShopTradeMenu());
    MenuManager.instance().addMenu(new ShopStaffMenu());

    registerService();
    /* Check the running envs is support or not. */
    logger.info("Starting plugin self-test, please wait...");
    try(final PerfMonitor ignored = new PerfMonitor("Self Test")) {
      runtimeCheck(EnvCheckEntry.Stage.ON_ENABLE);
    }
    logger.info("Reading the configuration...");
    initConfiguration();
    logger.info("Contributors: {}", CommonUtil.list2String(javaPlugin.getDescription().getAuthors()));
    logger.info("Original author: Netherfoam, Timtower, KaiNoMood, sandtechnology");
    logger.info("Let's start loading the plugin");
    loadErrorReporter();
    loadItemMatcher();
    this.itemMarker = new ItemMarker(this);
    loadRegistry();
    this.shopItemBlackList = new SimpleShopItemBlackList(this);
    Util.initialize();
    try {
      loadVirtualDisplayItem();
    } catch(final Exception e) {
      logger.warn("Failed to process virtual display item system", e);
    }
    //Load the database
    try(final PerfMonitor ignored = new PerfMonitor("Initialize database")) {
      initDatabase();
    }
    Util.asyncThreadRun(()->{
      logger.info("Start to caching usernames (async)...");
      ((FastPlayerFinder)getPlayerFinder()).bakeCaches();
    });
    /* Initalize the tools */
    // Create the shop manager.
    permissionManager = new PermissionManager(this);
    shopPermissionManager = new SimpleShopPermissionManager(this);
    // This should be inited before shop manager
    this.registerDisplayAutoDespawn();
    logger.info("Registering commands...");
    this.permissionChecker = new PermissionChecker(this);
    loadCommandHandler();
//        this.invWrapperUpdateManager = new InventoryWrapperUpdateManager(this);
//        this.invWrapperUpdateManager.register();
    this.shopManager = new SimpleShopManager(this);
    // Limit
    //this.registerLimitRanks();
    this.rankLimiter = new SimpleRankLimiter(this);
    // Limit end
    if(getConfig().getInt("shop.finding.distance") > 100 && getConfig().getBoolean("shop.finding.exclude-out-of-stock")) {
      logger.error("Shop find distance is too high with chunk loading feature turned on! It may cause lag! Pick a number below 100!");
    }
    signUpdateWatcher = new SignUpdateWatcher();
    //shopContainerWatcher = new ShopContainerWatcher();
    shopSaveWatcher = new ShopDataSaveWatcher(this);
    shopSaveWatcher.start(0, 20L * 60L * 5L);
    /* Load all shops. */
    shopLoader = new ShopLoader(this);
    shopLoader.loadShops();
    QuickExecutor.getCommonExecutor().submit(this::bakeShopsOwnerCache);
    logger.info("Registering listeners...");
    this.interactionController = new InteractionController(this);
    // Register events
    // Listeners (These don't)
    registerListeners();
    this.shopControlPanelManager.register(new SimpleShopControlPanel());
    this.registerDisplayItem();
    this.registerShopLock();
    logger.info("Cleaning MsgUtils...");
    MsgUtil.clean();
    this.registerUpdater();
    /* Delay the Economy system load, give a chance to let economy system register. */
    /* And we have a listener to listen the ServiceRegisterEvent :) */
    Log.debug("Scheduled economy system loading.");
    folia.getImpl().runLater(economyLoader::load, 1);
    registerTasks();
    Log.debug("DisplayItem selected: " + AbstractDisplayItem.getNowUsing().name());
    registerCommunicationChannels();
    new QSConfigurationReloadEvent(javaPlugin).callEvent();
    load3rdParty();
    try(final PerfMonitor ignored = new PerfMonitor("Self Test")) {
      runtimeCheck(EnvCheckEntry.Stage.AFTER_ON_ENABLE);
    }

  }

  private void loadRegistry() {

    logger.info("Setting up ItemExpressionRegistry...");
    final ItemExpressionRegistry itemExpressionRegistry = (ItemExpressionRegistry)this.registry.getRegistry(BuiltInRegistry.ITEM_EXPRESSION);
    itemExpressionRegistry.registerHandlerSafely(new SimpleMaterialExpressionHandler(this));
    itemExpressionRegistry.registerHandlerSafely(new SimpleEnchantmentExpressionHandler(this));
    itemExpressionRegistry.registerHandlerSafely(new SimpleItemReferenceExpressionHandler(this));
  }


  private void loadErrorReporter() {

    try {
      if(!getConfig().getBoolean("auto-report-errors")) {
        Log.debug("Error Reporter has been disabled by the configuration.");
      } else {
        sentryErrorReporter = new RollbarErrorReporter(this);
        Log.debug("Error Reporter has been initialized.");
      }
    } catch(final Exception th) {
      logger.warn("Cannot load the Rollbar Error Reporter: {}", th.getMessage());
      logger.warn("Because the error reporter doesn't work, report this error to the developer. Thank you!");
    }
  }

  private void loadItemMatcher() {

    final ItemMatcher defItemMatcher = switch(getConfig().getInt("matcher.work-type")) {
      case 3 -> new TNEItemMatcherImpl(this);
      case 1 -> new BukkitItemMatcherImpl(this);
      case 0 -> new QuickShopItemMatcherImpl(this);
      default ->
              throw new IllegalStateException("Unexpected value: " + getConfig().getInt("matcher.work-type"));
    };
    this.itemMatcher = ServiceInjector.getInjectedService(ItemMatcher.class, defItemMatcher);
  }

  private void loadVirtualDisplayItem() {

    if(this.display) {
      //VirtualItem support
      if(AbstractDisplayItem.getNowUsing() == DisplayType.VIRTUALITEM) {
        logger.info("Using Virtual Displays. Attempting to initialize packet factory...");
        try {

          virtualDisplayItemManager = new VirtualDisplayItemManager(this);
          if(getConfig().getBoolean("shop.per-player-shop-sign")) {

            //TODO: Revamp sign system.
            signHooker = new SignHooker(this);
            logger.info("Successfully registered per-player shop sign!");
          } else {

            signHooker = null;
          }
        } catch(final Exception e) {

          //disable displays since we don't have packet support
          signHooker = null;
          this.display = false;
          getConfig().set("shop.display-items", false);
          javaPlugin.saveConfig();

          logger.warn("Failed to initialize Virtual Display packet factory. Please validate that you have an up-to-date ProtocolLib or PacketEvents installation.", e);
          throw e;
        }
      }
    }
  }

  private void initDatabase() {

    setupDBonEnableding = true;
    if(!setupDatabase()) {
      logger.error("Failed to setup database, please check the logs for more information!");
      Bukkit.getPluginManager().disablePlugin(javaPlugin);
      throw new IllegalStateException("Failed to setup database");
    }
    setupDBonEnableding = false;
  }

  private void registerDisplayAutoDespawn() {

    if(this.display && getConfig().getBoolean("shop.display-auto-despawn")) {
      this.displayAutoDespawnWatcher = new DisplayAutoDespawnWatcher(this);
      //BUKKIT METHOD SHOULD ALWAYS EXECUTE ON THE SERVER MAIN THEAD
      this.displayAutoDespawnWatcher.runTaskTimer(javaPlugin, 20, getConfig().getInt("shop.display-check-time")); // not worth async
      logger.warn("Unrecommended use of display-auto-despawn. This feature may have a heavy impact on the server's performance!");
    } else {
      if(this.displayAutoDespawnWatcher != null) {
        this.displayAutoDespawnWatcher.cancel();
        this.displayAutoDespawnWatcher = null;
      }
    }
  }

  private void loadCommandHandler() {
    /* PreInit for BootError feature */
    this.registerQuickShopCommands();

  }

  private void bakeShopsOwnerCache() {

    if(PackageUtil.parsePackageProperly("bakeuuids").asBoolean(false)) {
      logger.info("Baking shops owner and moderators caches (This may take a while if you upgrade from old versions)...");
      final Set<UUID> waitingForBake = new HashSet<>();
      this.shopManager.getAllShops().forEach(shop->{
        final UUID uuid = shop.getOwner().getUniqueIdIfRealPlayer().orElse(null);
        if(uuid != null && !this.playerFinder.isCached(uuid)) {
          waitingForBake.add(uuid);
        }
        shop.getPermissionAudiences().keySet().forEach(audience->{
          if(!this.playerFinder.isCached(audience)) {
            waitingForBake.add(audience);
          }
        });
      });
      for(final UUID uuid : waitingForBake) {
        QuickExecutor.getSecondaryProfileIoExecutor().submit(()->{
          final String name = playerFinder.uuid2Name(uuid);
          if(name != null) {
            playerFinder.cache(uuid, name);
          }
        });
      }
      if(!waitingForBake.isEmpty()) {
        javaPlugin.logger().info("Performing {} players username caching.", waitingForBake.size());
      }
    }
  }

  private void registerListeners() {

    new BlockListener(this).register();
    new PlayerListener(this).register();
    new WorldListener(this).register();
    // Listeners - We decide which one to use at runtime
    new ChatListener(this).register();
    new ChunkListener(this).register();
    new CustomInventoryListener(this).register();
    new ShopProtectionListener(this).register();
    new MetricListener(this).register();
    new InternalListener(this).register();
    if(Util.checkIfBungee()) {
      this.bungeeListener = new BungeeListener(this);
      this.bungeeListener.register();
    }
  }

  private void registerDisplayItem() {

    if(this.display && AbstractDisplayItem.getNowUsing() != DisplayType.VIRTUALITEM) {
      if(getDisplayItemCheckTicks() > 0) {
        if(getConfig().getInt("shop.display-items-check-ticks") < 3000) {
          logger.error("Shop.display-items-check-ticks is too low! It may cause HUGE lag! Pick a number > 3000");
        }
        logger.info("Registering DisplayCheck task....");
        folia.getImpl().runTimerAsync(()->{
          for(final Shop shop : getShopManager().getLoadedShops()) {
            //Shop may be deleted or unloaded when iterating
            if(!shop.isLoaded()) {
              continue;
            }
            shop.checkDisplay();
          }
        }, 1L, getDisplayItemCheckTicks());
      } else if(getDisplayItemCheckTicks() == 0) {

        logger.info("shop.display-items-check-ticks was set to 0. Display Check has been disabled");
      } else {
        logger.error("shop.display-items-check-ticks has been set to an invalid value. Please use a value above 3000.");
      }
      new DisplayProtectionListener(this).register();
    } else {
      Util.unregisterListenerClazz(javaPlugin, DisplayProtectionListener.class);
    }
  }

  private void registerShopLock() {

    Util.unregisterListenerClazz(javaPlugin, LockListener.class);
    final boolean useShopLock = getConfig().getBoolean("shop.lock");
    if(useShopLock) {

      new LockListener(this).register();
    }
  }

  private void registerUpdater() {

    final boolean updaterEnabled = this.getConfig().getBoolean("updater", true);
    if(updaterEnabled) {
      updateWatcher = new UpdateWatcher();
      updateWatcher.init();
    } else {
      if(updateWatcher != null) {
        updateWatcher.uninit();
        updateWatcher = null;
      }
    }
  }

  private void registerTasks() {

    calendarWatcher = new CalendarWatcher(this);
    // shopVaildWatcher.runTaskTimer(this, 0, 20 * 60); // Nobody use it
    signUpdateWatcher.start(1, 10);
    //shopContainerWatcher.runTaskTimer(this, 0, 5); // Nobody use it
    if(logWatcher != null) {
      logWatcher.start(10, 10);
      logger.info("Log actions is enabled. Actions will be logged in the qs.log file!");
    }
    this.registerOngoingFee();
    calendarWatcher = new CalendarWatcher(this);
    calendarWatcher.start();
    this.shopPurger = new ShopPurger(this);
    if(getConfig().getBoolean("purge.at-server-startup")) {
      shopPurger.purge();
    }
  }

  private void registerCommunicationChannels() {

    Bukkit.getMessenger().registerOutgoingPluginChannel(javaPlugin, "BungeeCord");
  }

  /**
   * Load 3rdParty plugin support module.
   */
  private void load3rdParty() {

    final boolean usePAPI = getConfig().getBoolean("plugin.PlaceHolderAPI.enable");
    if(usePAPI) {
      this.placeHolderAPI = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
      if(this.placeHolderAPI != null && placeHolderAPI.isEnabled()) {
        this.quickShopPAPI = new QuickShopPAPI(this);
        this.quickShopPAPI.register();
        logger.info("Successfully loaded PlaceHolderAPI support!");
      }
    }
  }

  /**
   * Setup the database
   *
   * @return The setup result
   */
  public boolean setupDatabase() {

    logger.info("Setting up database...");
    final HikariConfig config = HikariUtil.createHikariConfig();
    try {
      final ConfigurationSection dbCfg = getConfig().getConfigurationSection("database");
      if(Objects.requireNonNull(dbCfg).getBoolean("mysql")) {
        databaseDriverType = DatabaseDriverType.MYSQL;
        // MySQL database - Required database be created first.
        dbPrefix = dbCfg.getString("prefix");
        if(dbPrefix == null || "none".equals(dbPrefix)) {
          dbPrefix = "";
        }
        final String user = dbCfg.getString("user");
        final String pass = dbCfg.getString("password");
        final String host = dbCfg.getString("host");
        final String port = dbCfg.getString("port");
        final String database = dbCfg.getString("database");
        final boolean useSSL = dbCfg.getBoolean("usessl");
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=" + useSSL);
        config.setUsername(user);
        config.setPassword(pass);
        this.sqlManager = new SQLManagerImpl(new HikariDataSource(config), "QuickShop-Hikari-SQLManager");
      } else {
        // H2 database - Doing this handles file creation
        databaseDriverType = DatabaseDriverType.H2;
        Log.debug("Registering JDBC H2 driver...");
        Driver.load();
        logger.info("Create database backup...");
        final String driverClassName = Driver.class.getName();
        Log.debug("Setting up H2 driver class name to: " + driverClassName);
        config.setDriverClassName(driverClassName);
        config.setJdbcUrl("jdbc:h2:" + new File(javaPlugin.getDataFolder(), "shops").getCanonicalFile().getAbsolutePath() + ";MODE=MYSQL");
        this.sqlManager = new SQLManagerImpl(new HikariDataSource(config), "QuickShop-Hikari-SQLManager");
        this.sqlManager.executeSQL("SET MODE=MYSQL"); // Switch to MySQL mode
      }
      //this.sqlManager.setDebugMode(Util.isDevMode());
      this.sqlManager.setExecutorPool(QuickExecutor.getHikaricpExecutor());
      // Make the database up to date
      this.databaseHelper = new SimpleDatabaseHelperV2(this, this.sqlManager, this.getDbPrefix());
      final DatabaseIOUtil ioUtil = new DatabaseIOUtil(databaseHelper);
      ioUtil.performBackup("startup");
      return true;
    } catch(final Exception e) {
      logger.error("Error when connecting to the database", e);
      if(setupDBonEnableding) {
        bootError = BuiltInSolution.databaseError();
      }
      return false;
    }
  }

  public void registerQuickShopCommands() {

    commandManager = new SimpleCommandManager(this);
    final List<String> customCommands = getConfig().getStringList("custom-commands");
    final Command quickShopCommand = new QuickShopCommand("quickshop", commandManager, new ArrayList<>(new HashSet<>(customCommands)));
    try {
      platform.registerCommand("quickshop-hikari", quickShopCommand);
    } catch(final Exception e) {
      logger.warn("Failed to register command aliases", e);
    }
    Log.debug("QuickShop command registered with those aliases: " + CommonUtil.list2String(quickShopCommand.getAliases()));
  }

  private void registerOngoingFee() {

    if(getConfig().getBoolean("shop.ongoing-fee.enable")) {
      ongoingFeeWatcher = new OngoingFeeWatcher(this);
      ongoingFeeWatcher.start(1, getConfig().getInt("shop.ongoing-fee.ticks"));
      logger.info("Ongoing fee feature is enabled.");
    } else {
      if(ongoingFeeWatcher != null) {
        ongoingFeeWatcher.stop();
        ongoingFeeWatcher = null;
      }
    }
  }

  public final void onDisable() {

    logger.info("QuickShop is finishing remaining work, this may need a while...");
    if(sentryErrorReporter != null) {
      logger.info("Shutting down error reporter...");
      sentryErrorReporter.unregister();
    }
    if(this.quickShopPAPI != null) {
      logger.info("Unregistering PlaceHolderAPI hooks...");
      if(this.quickShopPAPI.unregister()) {
        logger.info("Successfully unregistered PlaceholderAPI hook!");
      } else {
        logger.info("Unregistering not successful. Was it already unloaded?");
      }
    }
    if(getShopManager() != null) {
      logger.info("Unloading all loaded shops...");
      getShopManager().getLoadedShops().forEach(shop->getShopManager().unloadShop(shop));
    }
    if(this.bungeeListener != null) {
      logger.info("Disabling the BungeeChat messenger listener.");
      Bukkit.getOnlinePlayers().forEach(player->this.bungeeListener.notifyForCancel(player));
      this.bungeeListener.unregister();
    }
    if(getShopSaveWatcher() != null) {
      logger.info("Stopping shop auto save...");
      getShopSaveWatcher().stop();
    }
    if(getShopManager() != null) {
      logger.info("Saving all in-memory changed shops...");
      final List<CompletableFuture<Void>> futures = getShopManager().getAllShops().stream().filter(Shop::isDirty).map(Shop::update).toList();
      final CompletableFuture<?>[] completableFutures = futures.toArray(new CompletableFuture<?>[0]);
      CompletableFuture.allOf(completableFutures)
              .join();
    }
    /* Remove all display items, and any dupes we can find */
    if(shopManager != null) {
      logger.info("Cleaning up shop manager...");
      shopManager.clear();
    }
    if(AbstractDisplayItem.getNowUsing() == DisplayType.VIRTUALITEM && virtualDisplayItemManager != null) {
      logger.info("Cleaning up display manager...");
      virtualDisplayItemManager.unload();
    }
    if(logWatcher != null) {
      logger.info("Stopping log watcher...");
      logWatcher.close();
    }
    logger.info("Shutting down scheduled timers...");
    folia.getImpl().cancelAllTasks();
    if(calendarWatcher != null) {
      logger.info("Shutting down event calendar watcher...");
      calendarWatcher.stop();
    }
    /* Unload UpdateWatcher */
    if(this.updateWatcher != null) {
      logger.info("Shutting down update watcher...");
      this.updateWatcher.uninit();
    }
    logger.info("Shutting down 3rd-party integrations...");
    unload3rdParty();
    if(this.getSqlManager() != null) {
      logger.info("Shutting down database connections...");
      EasySQL.shutdownManager(this.getSqlManager());
    }
  }

  private void unload3rdParty() {

    if(this.placeHolderAPI != null && placeHolderAPI.isEnabled() && this.quickShopPAPI != null) {

      this.quickShopPAPI.unregister();
      logger.info("Unload PlaceHolderAPI module successfully!");
    }
    if(this.signHooker != null) {

      this.signHooker.unload();
      logger.info("Unload SignHooker module successfully!");
    }

    if(this.virtualDisplayItemManager != null) {

      this.virtualDisplayItemManager.unload();
    }
  }

  public static FoliaLib folia() {

    return instance.folia;
  }

  @NotNull
  public File getDataFolder() {

    return javaPlugin.getDataFolder();
  }

  @Override
  public ReloadResult reloadModule() throws Exception {

    registerDisplayAutoDespawn();
    //registerOngoingFee();
    registerUpdater();
    registerShopLock();
    registerDisplayItem();
    return Reloadable.super.reloadModule();
  }

  public AbstractItemStack<?> stack() {

    if(PaperLib.isPaper()) {
      return new PaperItemStack();
    }
    return new BukkitItemStack();
  }

  public @NotNull TextManager text() {

    return this.textManager;
  }

  public MenuPlayer createMenuPlayer(final OfflinePlayer player) {
    if(this.folia.isFolia()) {
      return new FoliaPlayer(player, this.javaPlugin);
    } else if(this.folia.isPaper()) {
      return new PaperPlayer(player, this.javaPlugin);
    } else {
      return new BukkitPlayer(player, this.javaPlugin);
    }
  }

  /**
   * Return the QuickShop fork name.
   *
   * @return The fork name.
   */
  @NotNull
  public String getFork() {

    return javaPlugin.getFork();
  }

  public static MenuHandler menu() {

    return instance.menuHandler;
  }

  /**
   * Return the QuickShop fork name.
   *
   * @return The fork name.
   */
  @NotNull
  public String getVersion() {

    return javaPlugin.getVersion();
  }

  public enum DatabaseDriverType {
    MYSQL,
    H2
  }

  public String getMainCommand() {

    final List<String> customCommands = getConfig().getStringList("custom-commands");
    return customCommands.isEmpty() ? "quickshop" : customCommands.getFirst();
  }

  public String getCommandPrefix(final String commandLabel) {

    final ConfigurationSection section = getConfig().getConfigurationSection("custom-subcommands");

    if (section == null) return commandLabel;
    final String prefix = section.getString(commandLabel);

    if (prefix == null || prefix.isEmpty()) return commandLabel;
    return prefix;
  }

  public static class EconomyLoader {

    private final QuickShop parent;

    public EconomyLoader(final QuickShop parent) {

      this.parent = parent;
    }

    /**
     * Tries to load the economy and its core. If this fails, it will try to use vault. If that
     * fails, it will return false.
     *
     * @return true if successful, false if the core is invalid or is not found, and vault cannot be
     * used.
     */

    public boolean load() {

      try(final PerfMonitor ignored = new PerfMonitor("Loading Economy Bridge")) {
        return setupEconomy();
      } catch(final Exception e) {
        if(parent.sentryErrorReporter != null) {
          parent.sentryErrorReporter.ignoreThrow();
        }
        parent.logger().error("Something went wrong while trying to load the economy system!");
        parent.logger().error("QuickShop was unable to hook into an economy system (Couldn't find Vault or Reserve)!");
        parent.logger().error("QuickShop can NOT enable properly!");
        parent.setupBootError(BuiltInSolution.econError(), false);
        parent.logger().error("Plugin Listeners have been disabled. Please fix this economy issue.", e);
        return false;
      }
    }

    private boolean setupEconomy() throws Exception {

      AbstractEconomy abstractEconomy = switch(EconomyType.fromID(parent.getConfig().getInt("economy-type"))) {
        case VAULT -> loadVaultAbstract();
        default -> null;
      };
      abstractEconomy = ServiceInjector.getInjectedService(AbstractEconomy.class, abstractEconomy);
      if(abstractEconomy == null) {
        Log.debug("No economy bridge found.");
        return false;
      }
      if(!abstractEconomy.isValid()) {
        parent.setupBootError(BuiltInSolution.econError(), false);
        return false;
      }
      parent.logger().info("Selected economy bridge: {}", abstractEconomy.getName());
      parent.economy = abstractEconomy;
      return true;
    }

    /**
     * Used to load Vault or VaultUnlocked depending on which is loaded.
     */
    @Nullable
    private AbstractEconomy loadVaultAbstract() throws Exception {

      if(vaultUnlockedPresent()) {

        final RegisteredServiceProvider<net.milkbowl.vault2.economy.Economy> economyProvider;
        try {

          economyProvider = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault2.economy.Economy.class);

          if(economyProvider == null) {

            return loadVault();
          }

        } catch(final Exception ignore) {

          return loadVault();
        }

        return loadVaultUnlocked();

      } else {

        return loadVault();
      }
    }

    @Nullable
    private AbstractEconomy loadVaultUnlocked() {

      return new Economy_VaultUnlocked(parent);
    }

    // Vault may create exception, we need catch it.
    @SuppressWarnings("RedundantThrows")
    @Nullable
    private AbstractEconomy loadVault() throws Exception {

      final Economy_Vault vault = new Economy_Vault(parent);
      final boolean taxEnabled = parent.getConfig().getDouble("tax", 0.0d) > 0;
      final String taxAccount = parent.getConfig().getString("tax-account", "tax");
      if(!vault.isValid()) {
        return null;
      }
      if(!taxEnabled) {
        return vault;
      }
      if(StringUtils.isEmpty(taxAccount)) {
        return vault;
      }
      final OfflinePlayer tax;
      if(CommonUtil.isUUID(taxAccount)) {
        tax = Bukkit.getOfflinePlayer(UUID.fromString(taxAccount));
      } else {
        tax = Bukkit.getOfflinePlayer(taxAccount);
      }
      if(!Objects.requireNonNull(vault.getVault()).hasAccount(tax)) {
        Log.debug("Tax account doesn't exists: " + tax);
        parent.logger().warn("QuickShop detected that no tax account exists and will try to create one. If you see any errors, please change the tax-account name in the config.yml to that of the Server owner.");
        if(vault.getVault().createPlayerAccount(tax)) {
          parent.logger().info("Tax account created.");
        } else {
          parent.logger().warn("Cannot create tax-account, please change the tax-account name in the config.yml to that of the server owner");
        }
        if(!vault.getVault().hasAccount(tax)) {
          parent.logger().warn("Player for the Tax-account has never played on this server before and we couldn't create an account. This may cause server lag or economy errors, therefore changing the name is recommended. You may ignore this warning if it doesn't cause any issues.");
        }
      }
      return vault;
    }

    private boolean vaultUnlockedPresent() {
      final Plugin vault = parent.javaPlugin.getServer().getPluginManager().getPlugin("Vault");
      return vault != null && vault.getDescription().getVersion().startsWith("2");
    }

    private boolean vaultPresent() {
      final Plugin vault = parent.javaPlugin.getServer().getPluginManager().getPlugin("Vault");
      return vault != null && vault.getDescription().getVersion().startsWith("1");
    }
  }
}
