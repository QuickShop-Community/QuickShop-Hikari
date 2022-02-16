/*
 *  This file is a part of project QuickShop, the name is ConfigurationFixer.java
 *  Copyright (C) Ghost_chu and contributors
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

