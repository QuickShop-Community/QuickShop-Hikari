package com.ghostchu.quickshop.util.config;

import com.dumptruckman.bukkit.configuration.json.JsonConfiguration;
import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.util.Util;
import de.themoep.minedown.adventure.MineDown;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.UUID;

@SuppressWarnings("unused")
public class ConfigUpdateScript {
    private final QuickShop plugin;
    @Getter
    private final FileConfiguration config;

    public ConfigUpdateScript(@NotNull FileConfiguration config, @NotNull QuickShop plugin) {
        this.config = config;
        this.plugin = plugin;
    }

    @UpdateScript(version = 1002)
    public void adventureMiniMessage() {
        getConfig().set("syntax-parser", 0);
    }

    @UpdateScript(version = 1010)
    public void allowDisableQsSizeCommandMaxStackSizeCheck() {
        getConfig().set("shop.disable-max-size-check-for-size-command", false);
    }

    @UpdateScript(version = 1011)
    public void removeTryingFixBanlanceInsuffientFeature() {
        getConfig().set("trying-fix-banlance-insuffient", null);
    }

    @UpdateScript(version = 1012)
    public void displayCenterControl() {
        getConfig().set("shop.display-center", false);
    }

    @UpdateScript(version = 1013)
    public void asyncOwnerNameFetch() {
        getConfig().set("shop.async-owner-name-fetch", false);
    }

    @UpdateScript(version = 1014)
    public void removeDisplayCenterConfig() {
        getConfig().set("shop.display-center", null);
    }

    @UpdateScript(version = 1004)
    public void configurableDatabaseProperties() {
        getConfig().set("database.queue", null);
        getConfig().set("database.queue-commit-interval", null);
        getConfig().set("database.auto-fix-encoding-issue-in-database", null);
        getConfig().set("database.properties.connection-timeout", 60000);
        getConfig().set("database.properties.validation-timeout", 3000);
        getConfig().set("database.properties.idle-timeout", 60000);
        getConfig().set("database.properties.login-timeout", 10);
        getConfig().set("database.properties.maxLifeTime", 60000);
        getConfig().set("database.properties.maximum-pool-size", 8);
        getConfig().set("database.properties.minimum-idle", 2);
        getConfig().set("database.properties.cachePrepStmts", true);
        getConfig().set("database.properties.prepStmtCacheSize", 250);
        getConfig().set("database.properties.prepStmtCacheSqlLimit", 2048);
        getConfig().set("database.properties.useUnicode", true);
        getConfig().set("database.properties.characterEncoding", "utf8");
        getConfig().set("database.properties.connection-timeout", 60000);
    }

    @UpdateScript(version = 1009)
    public void deleteSqlitePlayerMapping() {
        File f = new File(Util.getCacheFolder(), "player_mapping.db");
        if (f.exists()) {
            f.delete();
        }
    }

    @UpdateScript(version = 1008)
    public void disableTaxForUnlimitedShop() {
        getConfig().set("tax-free-for-unlimited-shop", false);
    }

    @UpdateScript(version = 1003)
    public void metricAndPapiController() {
        getConfig().set("transaction-metric.enable", true);
        boolean papiEnabled = getConfig().getBoolean("plugin.PlaceHolderAPI", true);
        getConfig().set("plugin.PlaceHolderAPI", null);
        getConfig().set("plugin.PlaceHolderAPI.enable", papiEnabled);
        getConfig().set("plugin.PlaceHolderAPI.cache", 15 * 60 * 1000);
    }

    @UpdateScript(version = 1006)
    public void migrateToMiniMessage() {
        File locales = new File(plugin.getDataFolder(), "overrides");
        if (!locales.exists()) {
            return;
        }
        for (File localeDirectory : locales.listFiles()) {
            if (!localeDirectory.isDirectory()) {
                continue;
            }
            File jsonFile = new File(localeDirectory, localeDirectory.getName() + ".json");
            if (!jsonFile.exists()) {
                continue;
            }
            try {
                File yamlFile = new File(jsonFile.getParent(), jsonFile.getName().replace(".json", ".yml"));
                yamlFile.createNewFile();
                YamlConfiguration yamlConfiguration = new YamlConfiguration();
                JsonConfiguration jsonConfiguration = JsonConfiguration.loadConfiguration(jsonFile);
                if(jsonConfiguration.equals(new JsonConfiguration())){
                    continue;
                }
                if(jsonConfiguration.getKeys(true).isEmpty()){
                    continue;
                }
                jsonConfiguration.getKeys(true).forEach(key -> yamlConfiguration.set(key, translate(jsonConfiguration.get(key))));
                try {
                    Files.copy(jsonFile.toPath(), new File(jsonFile.getParent(), jsonFile.getName() + ".bak").toPath());
                } catch (IOException e) {
                    Files.copy(jsonFile.toPath(), new File(jsonFile.getParent(), jsonFile.getName() + ".bak." + UUID.randomUUID().toString().replace("-", "")).toPath());
                }
                jsonFile.deleteOnExit();
                yamlConfiguration.save(yamlFile);
            } catch (Exception e) {
                plugin.logger().warn("Failed to upgrade override translation file {}.", jsonFile.getName(), e);
            }
        }
        getConfig().set("syntax-parser", null);
    }

    private Object translate(Object o) {
        if (o instanceof String str) {
            Component component = MineDown.parse(str);
            return MiniMessage.miniMessage().serialize(component);
        }
        return o;
    }

    @UpdateScript(version = 1008)
    public void perPlayerShopSign() {
        getConfig().set("shop.per-player-shop-sign", false);
    }

    @UpdateScript(version = 1007)
    public void refundFromTaxAccountOption() {
        getConfig().set("shop.refund-from-tax-account", false);
    }

    @UpdateScript(version = 1001)
    public void shopName() {
        getConfig().set("shop.name-fee", 0);
        getConfig().set("shop.name-max-length", 32);
        getConfig().set("matcher.item.bundle", true);
    }

    @UpdateScript(version = 1000)
    public void updateCustomTranslationKey() {
        getConfig().set("custom-translation-key", new ArrayList<>());
    }
}
