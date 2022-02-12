/*
 * This file is a part of project QuickShop, the name is HumanReadableJsonConfiguration.java
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

package org.maxgamer.quickshop.file;

import com.dumptruckman.bukkit.configuration.json.JsonConfiguration;
import com.dumptruckman.bukkit.configuration.util.SerializationHelper;
import org.bukkit.configuration.InvalidConfigurationException;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.util.JsonUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HumanReadableJsonConfiguration extends JsonConfiguration {

    private static final Logger logger = Logger.getLogger(HumanReadableJsonConfiguration.class.getName());

    public static HumanReadableJsonConfiguration loadConfiguration(@NotNull File file) {
        HumanReadableJsonConfiguration configuration = new HumanReadableJsonConfiguration();
        try {
            if (!file.exists()) {
                //noinspection ResultOfMethodCallIgnored
                file.createNewFile();
            }
            configuration.load(file);
        } catch (IOException | InvalidConfigurationException var4) {
            logger.log(Level.SEVERE, "Cannot load json file " + file, var4);
        }
        return configuration;
    }

    public static HumanReadableJsonConfiguration loadConfiguration(@NotNull InputStreamReader inputStreamReader) {
        HumanReadableJsonConfiguration configuration = new HumanReadableJsonConfiguration();
        try {
            configuration.load(inputStreamReader);
        } catch (IOException | InvalidConfigurationException exception) {
            logger.log(Level.SEVERE, "Cannot load original language file", exception);
        }
        return configuration;
    }

    @Override
    public @NotNull String saveToString() {
        return JsonUtil.prettyPrinting().toJson(SerializationHelper.serialize(this.getValues(false)));
    }
}
