package com.ghostchu.quickshop.util.config;

import com.ghostchu.quickshop.QuickShop;
import lombok.AllArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.logging.Level;

/**
 * ConfigurationFixer is a utilities to help user automatically fix broken configuration.
 *
 * @author sandtechnology
 */
@AllArgsConstructor
public class ConfigurationFixer {
    private final QuickShop plugin;
    private final File externalConfigFile;
    private final FileConfiguration externalConfig;
    private final FileConfiguration builtInConfig;


    public boolean fix() {
        // There read the default value as true but we should set default value as false in config.yml
        // So that we can check the configuration may broken or other else.
        if (!externalConfig.getBoolean("config-damaged", true)) {
            return false;
        }

        plugin.getLogger().warning("Warning! QuickShop detected the configuration has been corrupted.");
        plugin.getLogger().warning("Backup - Creating backup for configuration...");
        try {
            Files.copy(externalConfigFile.toPath(), new File(externalConfigFile.getParent(), externalConfigFile.getName() + "." + System.currentTimeMillis()).toPath());
        } catch (IOException ioException) {
            plugin.getLogger().log(Level.WARNING, "Failed to create file backup.", ioException);
        }
        plugin.getLogger().warning("Fix - Fixing the configuration, this may take a while...");
        for (String key : builtInConfig.getKeys(true)) {
            Object value = externalConfig.get(key);
            Object buildInValue = builtInConfig.get(key);
            if (!(value instanceof ConfigurationSection) || !value.getClass().getTypeName().equals(Objects.requireNonNull(buildInValue).getClass().getTypeName())) {
                plugin.getLogger().warning("Fixing configuration use default value: " + key);
                plugin.getConfig().set(key, buildInValue);
            }
        }
        plugin.getLogger().info("QuickShop fixed the corrupted parts in configuration that we can found. We recommend you restart the server and make fix apply.");
        externalConfig.set("config-damaged", false);
        try {
            externalConfig.save(externalConfigFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Couldn't save fixed configuration!", e);
        }
        return true;
    }
}

