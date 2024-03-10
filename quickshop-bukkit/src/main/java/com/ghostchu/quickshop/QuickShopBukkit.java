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
import com.ghostchu.quickshop.platform.spigot.v1_18_1.Spigot1181Platform;
import com.ghostchu.quickshop.platform.spigot.v1_18_2.Spigot1182Platform;
import com.ghostchu.quickshop.platform.spigot.v1_19_1.Spigot1191Platform;
import com.ghostchu.quickshop.platform.spigot.v1_19_2.Spigot1193Platform;
import com.ghostchu.quickshop.platform.spigot.v1_19_3.Spigot1194Platform;
import com.ghostchu.quickshop.platform.spigot.v1_20_1.Spigot1201Platform;
import com.ghostchu.quickshop.platform.spigot.v1_20_2.Spigot1202Platform;
import com.ghostchu.quickshop.platform.spigot.v1_20_2.Spigot1203Platform;
import com.ghostchu.quickshop.util.PackageUtil;
import com.vdurmont.semver4j.Semver;
import io.papermc.lib.PaperLib;
import kong.unirest.Unirest;
import net.kyori.adventure.Adventure;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.platform.viaversion.ViaFacet;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
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
        try {
            getLogger().info("QuickShop-" + getFork() + " - Bootloader");
            getLogger().info("Bootloader preparing for startup, please stand by...");
            getLogger().info("Initializing libraries...");
            loadLibraries();
            getLogger().info("Initializing platform...");
            loadPlatform();
            getLogger().info("Boot QuickShop instance...");
            initQuickShop();
            // SLF4J now should available
            logger.info("QuickShop-" + getFork() + " - Booting...");
        } catch (Throwable e) {
            getLogger().log(Level.SEVERE, "Failed to startup the QuickShop-Hikari due unexpected exception!", e);
            Bukkit.getPluginManager().disablePlugin(this);
            abortLoading = e;
            throw new IllegalStateException("Boot failure", e);
        }
    }

    @Override
    public void onDisable() {
        logger.info("Forwarding onDisable() to QuickShop instance...");
        this.quickShop.onDisable();
        logger.info("Finishing up onDisable() in Bootloader...");
        logger.info("Cleaning up resources...");
        HandlerList.unregisterAll(this);
        Bukkit.getScheduler().cancelTasks(this);
        Bukkit.getServicesManager().unregisterAll(this);
        Unirest.shutDown(true);
        Bukkit.getMessenger().unregisterIncomingPluginChannel(this);
        this.platform.shutdown();
    }

    @Override
    public void onEnable() {
        if (abortLoading != null) {
            throw new IllegalStateException("Plugin is disabled due an loading error", abortLoading);
        }
        logger.info("Forwarding onEnable() to QuickShop instance...");
        this.quickShop.onEnable();
        logger.info("Finishing up onEnable() in Bootloader...");
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
        new AdventureLibLoader(this);
    }

    private void resolveLibraries(QuickShopBukkit quickShopBukkit) {
        if (Boolean.parseBoolean(System.getProperty("com.ghostchu.quickshop.QuickShopBukkit.doNotResolveLibraries"))) {
            getLogger().warning("Warning! You have disabled libraries resolver! Make sure you added libraries in plugin.yml!");
            return;
        }
//        LogAdapter adapter = new LogAdapter() {
//            @Override
//            public void log(@NotNull LogLevel logLevel, @Nullable String s) {
//                // silent
//            }
//
//            @Override
//            public void log(@NotNull LogLevel logLevel, @Nullable String s, @Nullable Throwable throwable) {
//                // silent
//            }
//        };
         LogAdapter adapter = new JDKLogAdapter(getLogger());
//        if (Boolean.parseBoolean(System.getProperty("com.ghostchu.quickshop.QuickShopBukkit.verboseLibraryManager"))) {
//            adapter = new JDKLogAdapter(getLogger());
//        }
        this.bukkitLibraryManager = new BukkitLibraryManager(quickShopBukkit, "lib", adapter);
        if (!Boolean.parseBoolean(System.getProperty("com.ghostchu.quickshop.QuickShopBukkit.disableMavenLocal"))) {
            this.bukkitLibraryManager.addMavenLocal();
        }
        if (!Boolean.parseBoolean(System.getProperty("com.ghostchu.quickshop.QuickShopBukkit.disableSpigotLocal"))) {
            File relative = new File(getDataFolder().getParentFile().getParentFile(), "libraries");
            if(relative.exists()) {
                this.bukkitLibraryManager.addRepository(relative.toPath().toUri().toString());
            }
        }
        if (!Boolean.parseBoolean(System.getProperty("com.ghostchu.quickshop.QuickShopBukkit.disableMirrorTesting"))) {
            GeoUtil.determineBestMirrorServer(getLogger()).forEach(m -> this.bukkitLibraryManager.addRepository(m.getRepoUrl()));
        }
        //this.bukkitLibraryManager.addMavenCentral();
        this.bukkitLibraryManager.getRepositories().forEach(r -> {
            if (Boolean.parseBoolean(System.getProperty("com.ghostchu.quickshop.QuickShopBukkit.verboseLibraryManager"))) {
                getLogger().info("Registered repository: " + r);
            }
        });
        try {
            loadLibraries(this.bukkitLibraryManager);
        } catch (IllegalStateException e) {
            getLogger().log(Level.SEVERE, e.getMessage() + " The startup cannot continue.", e);
        }
    }

    private void loadLibraries(LibraryManager manager) {
        try (InputStream stream = getResource("libraries.maven")) {
            if (stream == null) {
                throw new IllegalStateException("Jar file doesn't include a valid libraries.maven file");
            }
            String dat = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            String[] libraries = dat.split("\n");
            List<Library> libraryList = new ArrayList<>();
            for (String library : libraries) {
                if (library.isBlank() || library.startsWith("#") || library.startsWith("//")) continue;
                library = library.trim();
                String[] libExplode = library.split(":");
                if (libExplode.length < 3) {
                    throw new IllegalArgumentException("[" + library + "] not a valid maven dependency syntax");
                }
                String groupId = libExplode[0];
                String artifactId = libExplode[1];
                String version = libExplode[2];
                String classifier = null;
                if (libExplode.length >= 4) {
                    classifier = libExplode[3];
                }
                Library.Builder libBuilder = Library.builder()
                        .groupId(groupId)
                        .artifactId(artifactId)
                        .version(version)
                        .resolveTransitiveDependencies(true)
                        .isolatedLoad(false);
                if (classifier != null) {
                    libBuilder = libBuilder.classifier(classifier);
                }
                Library lib = libBuilder.build();
                libraryList.add(lib);
            }
            getLogger().info("Loading " + libraryList.size() + " libraries...");
            for (int i = 0; i < libraryList.size(); i++) {
                Library load = libraryList.get(i);
                getLogger().info("Loading library " + load.toString() + " [" + (i + 1) + "/" + libraryList.size() + "]");
                if (Boolean.parseBoolean(System.getProperty("com.ghostchu.quickshop.QuickShopBukkit.verboseLibraryManager"))) {
                    for (String url : load.getUrls()) {
                        getLogger().info(load + " url selected: " + url);
                    }
                }
                manager.loadLibrary(load);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot download the libraries, the first time install/upgrade need the Internet connection.", e);
        }
    }


    private void loadPlatform() throws Exception {
        int platformId = 0;
        if (PaperLib.isSpigot()) {
            platformId = 1;
        }
        if (PaperLib.isPaper()) {
            platformId = 2;
        }

        platformId = PackageUtil.parsePackageProperly("forcePlatform").asInteger(platformId);
        try {
            switch (platformId) {
                case 1 -> {
                    getLogger().info("Platform detected: Spigot");
                    getLogger().warning("Use Paper or Paper's fork to get best performance and enhanced features!");

                    initNbtApi();

                    this.platform = switch (AbstractSpigotPlatform.getNMSVersion()) {
                        case "v1_18_R1" -> new Spigot1181Platform(this);
                        case "v1_18_R2" -> new Spigot1182Platform(this);
                        case "v1_19_R1" -> new Spigot1191Platform(this);
                        case "v1_19_R2" -> new Spigot1193Platform(this);
                        case "v1_19_R3" -> new Spigot1194Platform(this);
                        case "v1_20_R1" -> new Spigot1201Platform(this);
                        case "v1_20_R2" -> new Spigot1202Platform(this);
                        case "v1_20_R3" -> new Spigot1203Platform(this);
                        default -> {
                            getLogger().warning("This server running " + AbstractSpigotPlatform.getNMSVersion() + " not supported by Hikari. (Try update? or Use Paper's fork to get cross-platform compatibility.)");
                            Bukkit.getPluginManager().disablePlugin(this);
                            throw new IllegalStateException("This server running " + AbstractSpigotPlatform.getNMSVersion() + " not supported by Hikari. (Try update? or Use Paper's fork to get cross-platform compatibility.)");
                        }
                    };
                }
                case 2 -> {
                    getLogger().info("Platform detected: Paper");
                    this.platform = new PaperPlatform();
                }
                default -> throw new UnsupportedOperationException("Unsupported platform");
            }
            try {
                this.logger = this.platform.getSlf4jLogger(this);
            } catch (Throwable th) {
                this.logger = LoggerFactory.getLogger(getDescription().getName());
            }
            logger.info("Platform initialized: {}", this.platform.getClass().getName());
        } catch (Throwable e) {
            throw new Exception("Failed to initialize the platform", e);
        }
    }

    private void initNbtApi() {
        new NbtApiInitializer(getLogger());
    }

    private void initQuickShop() {
        logger.info("Creating QuickShop instance...");
        this.quickShop = new QuickShop(this, logger, platform);
        logger.info("Forwarding onLoad() to QuickShop instance...");
        this.quickShop.onLoad();
        logger.info("Finishing up onLoad() in Bootloader...");
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
     * Returns QS version, this method only exist on QuickShop forks If running other QuickShop forks, result
     * may not is "Reremake x.x.x" If running QS official, Will throw exception.
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
        } catch (Exception e) {
            return new Semver("0.0.0.0");
        }
    }

    public QuickShop getQuickShop() {
        return quickShop;
    }

    static class UnirestLibLoader {
        public UnirestLibLoader(QuickShopBukkit plugin) {
            plugin.getLogger().info("Initialing Unirest...");
            Unirest.config()
                    .concurrency(10, 5)
                    .setDefaultHeader("User-Agent", "QuickShop/" + plugin.getFork() + "-" + plugin.getDescription().getVersion() + " Java/" + System.getProperty("java.version"));
            Unirest.config().verifySsl(PackageUtil.parsePackageProperly("verifySSL").asBoolean());
            if (PackageUtil.parsePackageProperly("proxyHost").isPresent()) {
                plugin.getLogger().info("Unirest proxy feature has been enabled.");
                Unirest.config().proxy(PackageUtil.parsePackageProperly("proxyHost").asString("127.0.0.1"), PackageUtil.parsePackageProperly("proxyPort").asInteger(1080));
            }
            if (PackageUtil.parsePackageProperly("proxyUsername").isPresent()) {
                plugin.getLogger().info("Unirest proxy authentication activated.");
                Unirest.config().proxy(PackageUtil.parsePackageProperly("proxyHost").asString("127.0.0.1"), PackageUtil.parsePackageProperly("proxyPort").asInteger(1080), PackageUtil.parsePackageProperly("proxyUsername").asString(""), PackageUtil.parsePackageProperly("proxyPassword").asString(""));
            }
        }
    }

    static class AdventureLibLoader {
        public AdventureLibLoader(QuickShopBukkit plugin) {
            plugin.getLogger().info("Loading the Adventure Chat Processor...");
            plugin.getLogger().info("Adventure API loaded from: " + CommonUtil.getClassPath(Adventure.class));
            plugin.getLogger().info("Adventure Bukkit Platform loaded from: " + CommonUtil.getClassPath(BukkitAudiences.class));
            plugin.getLogger().info("Adventure Text Serializer (Legacy) loaded from: " + CommonUtil.getClassPath(LegacyComponentSerializer.class));
            plugin.getLogger().info("Adventure Text Serializer (Gson) loaded from: " + CommonUtil.getClassPath(GsonComponentSerializer.class));
            plugin.getLogger().info("Adventure Text Serializer (Json) loaded from: " + CommonUtil.getClassPath(JSONComponentSerializer.class));
            plugin.getLogger().info("Adventure Text Serializer (BungeeChat) loaded from: " + CommonUtil.getClassPath(BungeeComponentSerializer.class));
            plugin.getLogger().info("Adventure Text Serializer (ViaVersion Facet) loaded from: " + CommonUtil.getClassPath(ViaFacet.class));
            plugin.getLogger().info("Adventure Text Serializer (ANSI) loaded from: " + CommonUtil.getClassPath(ANSIComponentSerializer.class));
            plugin.getLogger().info("Adventure Text Serializer (Plain) loaded from: " + CommonUtil.getClassPath(PlainTextComponentSerializer.class));
            plugin.getLogger().info("Adventure MiniMessage Lib loaded from: " + CommonUtil.getClassPath(MiniMessage.class));

        }
    }
}