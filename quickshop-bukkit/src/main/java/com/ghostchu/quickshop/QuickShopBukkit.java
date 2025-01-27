package com.ghostchu.quickshop;

import com.alessiodp.libby.BukkitLibraryManager;
import com.alessiodp.libby.Library;
import com.alessiodp.libby.LibraryManager;
import com.alessiodp.libby.logging.adapters.JDKLogAdapter;
import com.alessiodp.libby.logging.adapters.LogAdapter;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.common.util.GeoUtil;
import com.ghostchu.quickshop.platform.Platform;
import com.ghostchu.quickshop.platform.paper.PaperPlatform;
import com.ghostchu.quickshop.platform.spigot.AbstractSpigotPlatform;
import com.ghostchu.quickshop.platform.spigot.v1_20_1.Spigot1201Platform;
import com.ghostchu.quickshop.platform.spigot.v1_20_2.Spigot1202Platform;
import com.ghostchu.quickshop.platform.spigot.v1_20_3.Spigot1203Platform;
import com.ghostchu.quickshop.platform.spigot.v1_20_4.Spigot1205Platform;
import com.ghostchu.quickshop.platform.spigot.v1_21_1.Spigot1210Platform;
import com.ghostchu.quickshop.platform.spigot.v1_21_3.Spigot1231Platform;
import com.ghostchu.quickshop.platform.spigot.v1_21_4.Spigot1214Platform;
import com.ghostchu.quickshop.util.PackageUtil;
import com.vdurmont.semver4j.Semver;
import io.papermc.lib.PaperLib;
import kong.unirest.Unirest;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class QuickShopBukkit extends JavaPlugin {

  @Getter
  private final java.util.logging.Logger bootstrapLogger = java.util.logging.Logger.getLogger("QuickShop-Hikari/Bootstrap");
  private Platform platform;
  private Logger logger;
  private QuickShop quickShop;
  private Throwable abortLoading;
  private BukkitLibraryManager bukkitLibraryManager;

  @Override
  public void reloadConfig() {

    super.reloadConfig();
    this.quickShop.reloadConfigSubModule();
  }

  @Override
  public void onLoad() {

    final long startLoadAt = System.currentTimeMillis();
    try {
      bootstrapLogger.info("QuickShop-" + getFork() + " - Bootstrap -> Execute the initialization sequence");
      bootstrapLogger.info("Bootloader preparing for startup, please wait...");
      bootstrapLogger.info("Initializing libraries...");
      loadLibraries();
      bootstrapLogger.info("Initializing platform...");
      loadPlatform();
      bootstrapLogger.info("Boot QuickShop instance...");
      initQuickShop();
      // SLF4J now should available
      bootstrapLogger.info("QuickShop-" + getFork() + " - Bootstrap -> Complete (" + (System.currentTimeMillis() - startLoadAt) + "ms). Waiting for enable...");
    } catch(final Throwable e) {
      bootstrapLogger.log(Level.SEVERE, "Failed to startup the QuickShop-Hikari due unexpected exception!", e);
      Bukkit.getPluginManager().disablePlugin(this);
      abortLoading = e;
      throw new IllegalStateException("Boot failure", e);
    }
  }

  @Override
  public void onDisable() {

    final long shutdownAtTime = System.currentTimeMillis();
    bootstrapLogger.info("QuickShop-" + getFork() + " - Bootstrap -> Execute the shutdown sequence");
    this.quickShop.onDisable();
    bootstrapLogger.info("Cleaning up resources...");
    HandlerList.unregisterAll(this);
    QuickShop.folia().getImpl().cancelAllTasks();
    Bukkit.getServicesManager().unregisterAll(this);
    Unirest.shutDown(true);
    Bukkit.getMessenger().unregisterIncomingPluginChannel(this);
    this.platform.shutdown();
    bootstrapLogger.info("QuickShop-" + getFork() + " - Bootstrap -> All Complete (" + (System.currentTimeMillis() - shutdownAtTime) + "ms)");
  }

  @Override
  public void onEnable() {

    if(abortLoading != null) {
      throw new IllegalStateException("Plugin is disabled due an loading error", abortLoading);
    }
    final long enableAtTime = System.currentTimeMillis();
    bootstrapLogger.info("QuickShop-" + getFork() + " - Bootstrap -> Execute the enable sequence");
    this.quickShop.onEnable();
    bootstrapLogger.info("QuickShop-" + getFork() + " - Bootstrap -> All Complete. (" + (System.currentTimeMillis() - enableAtTime) + "ms)");
  }

  /**
   * Return the QuickShop fork name.
   *
   * @return The fork name.
   */
  @NotNull
  public String getFork() {

    return "Hikari";
  }

  private void loadLibraries() {

    resolveLibraries(this);
    new UnirestLibLoader(this);
  }

  private void resolveLibraries(final QuickShopBukkit quickShopBukkit) {

    if(Boolean.parseBoolean(System.getProperty("com.ghostchu.quickshop.QuickShopBukkit.doNotResolveLibraries"))) {
      bootstrapLogger.warning("Warning! You have disabled libraries resolver! Make sure you added libraries in plugin.yml!");
      return;
    }
    final LogAdapter adapter = new JDKLogAdapter(getLogger());

    this.bukkitLibraryManager = new BukkitLibraryManager(quickShopBukkit, "lib", adapter);
    if(!Boolean.parseBoolean(System.getProperty("com.ghostchu.quickshop.QuickShopBukkit.disableMavenLocal"))) {
      this.bukkitLibraryManager.addMavenLocal();
    }
    if(!Boolean.parseBoolean(System.getProperty("com.ghostchu.quickshop.QuickShopBukkit.disableSpigotLocal"))) {
      final File relative = new File(getDataFolder().getParentFile().getParentFile(), "libraries");
      if(relative.exists()) {
        this.bukkitLibraryManager.addRepository(relative.toPath().toUri().toString());
      }
    }
    if(!Boolean.parseBoolean(System.getProperty("com.ghostchu.quickshop.QuickShopBukkit.disableMirrorTesting"))) {
      GeoUtil.determineBestMirrorServer(getLogger()).forEach(m->this.bukkitLibraryManager.addRepository(m.getRepoUrl()));
    }
    //this.bukkitLibraryManager.addMavenCentral();
    this.bukkitLibraryManager.getRepositories().forEach(r->{
      if(Boolean.parseBoolean(System.getProperty("com.ghostchu.quickshop.QuickShopBukkit.verboseLibraryManager"))) {
        bootstrapLogger.info("Registered repository: " + r);
      }
    });
    try {
      loadLibraries(this.bukkitLibraryManager);
    } catch(final IllegalStateException e) {
      bootstrapLogger.log(Level.SEVERE, e.getMessage() + " The startup cannot continue.", e);
    }
  }

  private void loadLibraries(final LibraryManager manager) {

    try(final InputStream stream = getResource("libraries.maven")) {
      if(stream == null) {
        throw new IllegalStateException("Jar file doesn't include a valid libraries.maven file");
      }
      final String dat = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
      final String[] libraries = dat.split("\n");
      final List<Library> libraryList = new ArrayList<>();
      int skipped = 0;
      for(String library : libraries) {
        if(library.isBlank() || library.startsWith("#") || library.startsWith("//")) continue;
        library = library.trim();
        final String[] cases = library.split("@");
        String testClass = null;
        if(cases.length == 2) {
          testClass = cases[1];
        }
        if(testClass != null) {
          if(Boolean.parseBoolean(System.getProperty("com.ghostchu.quickshop.QuickShopBukkit.reuseDependencies", "false")) && CommonUtil.isClassAvailable(testClass)) {
            skipped++;
            continue;
          }
        }

        final String[] libExplode = cases[0].split(":");
        if(libExplode.length < 3) {
          throw new IllegalArgumentException("[" + library + "] not a valid maven dependency syntax");
        }
        final String groupId = libExplode[0];
        final String artifactId = libExplode[1];
        final String version = libExplode[2];
        String classifier = null;
        if(libExplode.length >= 4) {
          classifier = libExplode[3];
        }
        Library.Builder libBuilder = Library.builder()
                .groupId(groupId)
                .artifactId(artifactId)
                .version(version)
                .resolveTransitiveDependencies(true)
                .isolatedLoad(false);
        if(classifier != null) {
          libBuilder = libBuilder.classifier(classifier);
        }
        final Library lib = libBuilder.build();
        libraryList.add(lib);
      }
      bootstrapLogger.info("Loading " + libraryList.size() + " libraries (" + skipped + " skipped libraries)...");
      for(int i = 0; i < libraryList.size(); i++) {
        final Library load = libraryList.get(i);
        bootstrapLogger.info("Loading library " + load.toString() + " [" + (i + 1) + "/" + libraryList.size() + "]");
        if(Boolean.parseBoolean(System.getProperty("com.ghostchu.quickshop.QuickShopBukkit.verboseLibraryManager"))) {
          for(final String url : load.getUrls()) {
            bootstrapLogger.info(load + " url selected: " + url);
          }
        }
        manager.loadLibrary(load);
      }
    } catch(final IOException e) {
      throw new IllegalStateException("Cannot download the libraries, the first time install/upgrade need the Internet connection.", e);
    }
  }


  private void loadPlatform() throws Exception {

    int platformId = 0;
    if(PaperLib.isSpigot()) {
      platformId = 1;
    }
    if(PaperLib.isPaper()) {
      platformId = 2;
    }

    platformId = PackageUtil.parsePackageProperly("forcePlatform").asInteger(platformId);
    try {
      switch(platformId) {
        case 1 -> {
          bootstrapLogger.info("Platform detected: Spigot");
          bootstrapLogger.warning("=================================================================");
          bootstrapLogger.warning("=========================   ATTENTION   =========================");
          bootstrapLogger.warning("=================================================================");
          bootstrapLogger.warning("Use Paper or Paper's fork to get best performance and enhanced features!");
          bootstrapLogger.warning("Spigot lacks modern functionality and overall performance fixes.");
          bootstrapLogger.warning("=================================================================");

          initNbtApi();

          //noinspection deprecation
          final String internalNMSVersion = AbstractSpigotPlatform.getNMSVersion();
          this.platform = switch(internalNMSVersion) {
            case "v1_20_R1" -> new Spigot1201Platform(this);
            case "v1_20_R2" -> new Spigot1202Platform(this);
            case "v1_20_R3" -> new Spigot1203Platform(this);
            case "v1_20_R4" -> new Spigot1205Platform(this);
            case "v1_21_R1" -> new Spigot1210Platform(this);
            //case "v1_21_R2" -> new Spigot1211Platform(this);
            case "v1_21_R2" -> new Spigot1231Platform(this);
            case "v1_21_R3" -> new Spigot1214Platform(this);
            default -> {
              bootstrapLogger.warning("This server running " + internalNMSVersion + " not supported by Hikari. (Try update? or Use Paper's fork to get cross-platform compatibility.)");
              Bukkit.getPluginManager().disablePlugin(this);
              throw new IllegalStateException("This server running " + internalNMSVersion + " not supported by Hikari. (Try update? or Use Paper's fork to get cross-platform compatibility.)");
            }
          };
        }
        case 2 -> {
          bootstrapLogger.info("Platform detected: Paper");
          this.platform = new PaperPlatform();
        }
        default -> throw new UnsupportedOperationException("Unsupported platform");
      }
      try {
        this.logger = this.platform.getSlf4jLogger(this);
      } catch(final Throwable th) {
        this.logger = LoggerFactory.getLogger(getDescription().getName());
      }
      logger.info("Slf4jLogger initialized");
      bootstrapLogger.info("Platform initialized: " + this.platform.getClass().getName());
    } catch(final Throwable e) {
      throw new Exception("Failed to initialize the platform", e);
    }
  }

  private void initNbtApi() {

    new NbtApiInitializer(bootstrapLogger);
  }

  private void initQuickShop() {

    bootstrapLogger.info("Creating QuickShop instance...");
    this.quickShop = new QuickShop(this, logger, platform);
    this.quickShop.onLoad();
  }

  @NotNull
  public Logger logger() {

    return this.logger;
  }

  @NotNull
  public Platform platform() {

    return this.platform;
  }

  /**
   * Returns QS version, this method only exist on QuickShop forks If running other QuickShop forks,
   * result may not is "Reremake x.x.x" If running QS official, Will throw exception.
   *
   * @return Plugin Version
   */
  @NotNull
  public String getVersion() {

    return getDescription().getVersion();
  }

  @NotNull
  public Semver getSemVersion() {

    try {
      return new Semver(getDescription().getVersion());
    } catch(final Exception e) {
      return new Semver("0.0.0.0");
    }
  }

  public QuickShop getQuickShop() {

    return quickShop;
  }

  static class UnirestLibLoader {

    public UnirestLibLoader(final QuickShopBukkit plugin) {

      plugin.getBootstrapLogger().info("Initialing Unirest...");
      Unirest.config()
              .concurrency(10, 5)
              .setDefaultHeader("User-Agent", "QuickShop/" + plugin.getFork() + "-" + plugin.getDescription().getVersion() + " Java/" + System.getProperty("java.version"));
      Unirest.config().verifySsl(PackageUtil.parsePackageProperly("verifySSL").asBoolean());
      if(PackageUtil.parsePackageProperly("proxyHost").isPresent()) {
        plugin.getBootstrapLogger().info("Unirest proxy feature has been enabled.");
        Unirest.config().proxy(PackageUtil.parsePackageProperly("proxyHost").asString("127.0.0.1"), PackageUtil.parsePackageProperly("proxyPort").asInteger(1080));
      }
      if(PackageUtil.parsePackageProperly("proxyUsername").isPresent()) {
        plugin.getBootstrapLogger().info("Unirest proxy authentication activated.");
        Unirest.config().proxy(PackageUtil.parsePackageProperly("proxyHost").asString("127.0.0.1"), PackageUtil.parsePackageProperly("proxyPort").asInteger(1080), PackageUtil.parsePackageProperly("proxyUsername").asString(""), PackageUtil.parsePackageProperly("proxyPassword").asString(""));
      }
    }
  }
}