package com.ghostchu.quickshop.util.config;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.QuickShopInstanceHolder;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class ConfigProvider extends QuickShopInstanceHolder {
    private final File configFile;
    private final Logger logger = plugin.getLogger();
    private FileConfiguration config = null;

    public ConfigProvider(QuickShop plugin, File configFile) {
        super(plugin);
        this.configFile = configFile;
    }

    public @NotNull FileConfiguration get() {
        if (config == null) {
            reload();
        }
        return config;
    }

    public void save() {
        try {
            get().save(configFile);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to save configuration " + configFile.getName(), e);
        }
    }

    public void reload() {
        reload(false);
    }

    /**
     * Reloads QuickShops config
     *
     * @param defaults is using default configuration
     */
    public void reload(boolean defaults) {
        if (!configFile.exists()) {
            plugin.saveDefaultConfig();
        }
        if (config == null) {
            config = new YamlConfiguration();
        }
        try (InputStream defaultConfigStream = plugin.getResource(configFile.getName())) {
            config.load(configFile);
            if (defaultConfigStream != null) {
                try (InputStreamReader reader = new InputStreamReader(defaultConfigStream, StandardCharsets.UTF_8)) {
                    config.setDefaults(YamlConfiguration.loadConfiguration(reader));
                }
            }
        } catch (IOException | InvalidConfigurationException exception) {
            if (!defaults) {
                logger.log(Level.SEVERE, "Cannot reading the configuration " + configFile.getName() + ", doing backup configuration and use default", exception);
                try {
                    Files.copy(configFile.toPath(), plugin.getDataFolder().toPath().resolve(configFile.getName() + "-broken-" + UUID.randomUUID() + ".yml"), REPLACE_EXISTING);
                } catch (IOException fatalException) {
                    throw new IllegalStateException("Failed to backup configuration " + configFile.getName(), fatalException);
                }
                plugin.saveResource(configFile.getName(), true);
                reload(true);
            } else {
                throw new IllegalStateException("Failed to load default configuration " + configFile.getName(), exception);
            }
        }

    }
}
