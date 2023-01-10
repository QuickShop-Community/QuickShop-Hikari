package com.ghostchu.quickshop;

import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.platform.Platform;
import com.ghostchu.quickshop.platform.paper.PaperPlatform;
import com.ghostchu.quickshop.platform.spigot.AbstractSpigotPlatform;
import com.ghostchu.quickshop.platform.spigot.v1_18_1.Spigot1181Platform;
import com.ghostchu.quickshop.platform.spigot.v1_18_2.Spigot1182Platform;
import com.ghostchu.quickshop.platform.spigot.v1_19_1.Spigot1191Platform;
import com.ghostchu.quickshop.platform.spigot.v1_19_2.Spigot1193Platform;
import com.ghostchu.quickshop.util.PackageUtil;
import io.papermc.lib.PaperLib;
import kong.unirest.Unirest;
import net.kyori.adventure.Adventure;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class QuickShopBukkit extends JavaPlugin {
    private Platform platform;
    private Logger logger;
    private QuickShop quickShop;

    @Override
    public void onLoad() {
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
    }

    @Override
    public void onDisable() {
        logger.info("Forwarding onDisable() to QuickShop instance...");
        this.quickShop.onDisable();
    }

    @Override
    public void onEnable() {
        logger.info("Forwarding onEnable() to QuickShop instance...");
        this.quickShop.onEnable();
    }

    @Override
    public void reloadConfig() {
        logger.info("Forwarding reloadConfig() to QuickShop instance...");
        super.reloadConfig();
        this.quickShop.reloadConfig();
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
        new UnirestLibLoader(this);
        new AdventureLibLoader(this);
    }

    private void loadPlatform() {
        if (PaperLib.isPaper()) {
            getLogger().info("Platform detected: Paper");
            this.platform = new PaperPlatform();
        } else if (PaperLib.isSpigot()) {
            getLogger().info("Platform detected: Spigot");
            getLogger().warning("Use Paper to get best performance and enhanced features!");
            getLogger().warning("");
            getLogger().warning("QuickShop-Hikari cannot handle translatable components");
            getLogger().warning("on Spigot platform! Make sure you're using Paper or Paper's fork");
            getLogger().warning("to unlock full functions!");
            getLogger().warning("Due the limitation of Spigot, QuickShop-Hikari running under compatibility mode.");

            this.platform = switch (AbstractSpigotPlatform.getNMSVersion()) {
                case "v1_18_R1" -> new Spigot1181Platform(this);
                case "v1_18_R2" -> new Spigot1182Platform(this);
                case "v1_19_R1" -> new Spigot1191Platform(this);
                case "v1_19_R2" -> new Spigot1193Platform(this);
                default -> {
                    getLogger().warning("This server running " + AbstractSpigotPlatform.getNMSVersion() + " not supported by Hikari. (Try update? or Use Paper's fork to get cross-platform compatibility.)");
                    Bukkit.getPluginManager().disablePlugin(this);
                    throw new IllegalStateException("This server running " + AbstractSpigotPlatform.getNMSVersion() + " not supported by Hikari. (Try update? or Use Paper's fork to get cross-platform compatibility.)");
                }
            };
        } else {
            throw new UnsupportedOperationException("Unsupported platform");
        }
        this.logger = this.platform.getSlf4jLogger(this);
        logger.info("Platform initialized: {}", this.platform.getClass().getName());
    }

    private void initQuickShop() {
        logger.info("Creating QuickShop instance...");
        this.quickShop = new QuickShop(this, logger, platform);
        logger.info("Forwarding onLoad() to QuickShop instance...");
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
     * Returns QS version, this method only exist on QuickShop forks If running other QuickShop forks, result
     * may not is "Reremake x.x.x" If running QS official, Will throw exception.
     *
     * @return Plugin Version
     */
    @NotNull
    public String getVersion() {
        return getDescription().getVersion();
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
            plugin.getLogger().info("Adventure MiniMessage Lib loaded from: " + CommonUtil.getClassPath(LegacyComponentSerializer.class));
        }
    }
}