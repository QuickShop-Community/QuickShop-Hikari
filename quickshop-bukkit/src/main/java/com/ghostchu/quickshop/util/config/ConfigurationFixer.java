package com.ghostchu.quickshop.util.config;

import com.ghostchu.quickshop.QuickShop;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

/**
 * ConfigurationFixer is a utilities to help user automatically fix broken configuration.
 *
 * @author sandtechnology
 */
public class ConfigurationFixer {

  private final QuickShop plugin;
  private final File externalConfigFile;
  private final FileConfiguration externalConfig;
  private final FileConfiguration builtInConfig;

  public ConfigurationFixer(final QuickShop plugin, final File externalConfigFile, final FileConfiguration externalConfig, final FileConfiguration builtInConfig) {

    this.plugin = plugin;
    this.externalConfigFile = externalConfigFile;
    this.externalConfig = externalConfig;
    this.builtInConfig = builtInConfig;
  }


  public boolean fix() {
    // There read the default value as true but we should set default value as false in config.yml
    // So that we can check the configuration may broken or other else.
    if(!externalConfig.getBoolean("config-damaged", true)) {
      return false;
    }

    plugin.logger().warn("Warning! QuickShop detected the configuration has been corrupted.");
    plugin.logger().warn("Backup - Creating backup for configuration...");
    try {
      Files.copy(externalConfigFile.toPath(), new File(externalConfigFile.getParent(), externalConfigFile.getName() + "." + System.currentTimeMillis()).toPath());
    } catch(final IOException ioException) {
      plugin.logger().warn("Failed to create file backup.", ioException);
    }
    plugin.logger().warn("Fix - Fixing the configuration, this may take a while...");
    for(final String key : builtInConfig.getKeys(true)) {
      final Object value = externalConfig.get(key);
      final Object buildInValue = builtInConfig.get(key);
      if(!(value instanceof ConfigurationSection) || !value.getClass().getTypeName().equals(Objects.requireNonNull(buildInValue).getClass().getTypeName())) {
        plugin.logger().warn("Fixing configuration use default value: {}", key);
        plugin.getConfig().set(key, buildInValue);
      }
    }
    plugin.logger().info("QuickShop fixed the corrupted parts in configuration that we can found. We recommend you restart the server and make fix apply.");
    externalConfig.set("config-damaged", false);
    try {
      externalConfig.save(externalConfigFile);
    } catch(final IOException e) {
      plugin.logger().warn("Couldn't save fixed configuration!", e);
    }
    return true;
  }
}

