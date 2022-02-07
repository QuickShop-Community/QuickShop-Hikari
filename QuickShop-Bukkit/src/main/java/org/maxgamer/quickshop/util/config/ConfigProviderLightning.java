/*
 * This file is a part of project QuickShop, the name is ConfigProviderLightning.java
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

package org.maxgamer.quickshop.util.config;

import de.leonhard.storage.LightningBuilder;
import de.leonhard.storage.Yaml;
import de.leonhard.storage.internal.settings.ConfigSettings;
import de.leonhard.storage.internal.settings.ReloadSettings;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.util.holder.QuickShopInstanceHolder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class ConfigProviderLightning extends QuickShopInstanceHolder {
    private final File configFile;
    private final Logger logger = plugin.getLogger();
    private Yaml config = null;

    public ConfigProviderLightning(QuickShop plugin, File configFile) {
        super(plugin);
        this.configFile = configFile;
    }

    public @NotNull Yaml get() {
        if (config == null) {
            reload();
        }
        return config;
    }

    public void save() {
        get().write();
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

        try (InputStream defaultConfigStream = plugin.getResource(configFile.getName())) {
            config = LightningBuilder
                    .fromFile(configFile)
                    .addInputStreamFromResource(configFile.getName())
                    .setReloadSettings(ReloadSettings.MANUALLY)
                    .setConfigSettings(ConfigSettings.PRESERVE_COMMENTS)
                    .createYaml();

            if (defaultConfigStream != null) {
                //try (InputStreamReader reader = new InputStreamReader(defaultConfigStream, StandardCharsets.UTF_8)) {
                config.addDefaultsFromInputStream(defaultConfigStream);
                //}
            }
        } catch (IOException exception) {
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
