/*
 *  This file is a part of project QuickShop, the name is ConfigurationUpdater.java
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

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.util.Util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class ConfigurationUpdater {
    private final QuickShop plugin;
    @Getter
    private final ConfigurationSection configuration;

    public ConfigurationUpdater(QuickShop plugin) {
        this.plugin = plugin;
        this.configuration = plugin.getConfig();
    }

    private void writeServerUniqueId() {
        String serverUUID = getConfiguration().getString("server-uuid");
        if (serverUUID == null || serverUUID.isEmpty()) {
            UUID uuid = UUID.randomUUID();
            serverUUID = uuid.toString();
            getConfiguration().set("server-uuid", serverUUID);
        }
        plugin.saveConfig();
    }

    public void update() {
        Util.debugLog("Starting configuration update...");
        writeServerUniqueId();

        for (Method updateScript : getUpdateScripts()) {
            try {
                ConfigUpdater configUpdater = updateScript.getAnnotation(ConfigUpdater.class);
                int current = getConfiguration().getInt("config-version");
                if (current >= configUpdater.version()) {
                    continue;
                }
                Util.debugLog("Executing " + updateScript.getName() + " for version " + configUpdater.version());
                if (updateScript.getParameterCount() == 0) {
                    updateScript.invoke(this);
                }
                if (updateScript.getParameterCount() == 1 && (updateScript.getParameterTypes()[0] == int.class || updateScript.getParameterTypes()[0] == Integer.class)) {
                    updateScript.invoke(this, current);
                }
                getConfiguration().set("config-version", configUpdater.version() + 1);
            } catch (Throwable throwable) {
                plugin.getLogger().log(Level.WARNING, "Failed execute update script " + updateScript.getName() + " for updating to version " + updateScript.getAnnotation(ConfigUpdater.class).version() + ", some configuration options may missing or outdated", throwable);
            }
        }
        saveConfig();
    }

    public List<Method> getUpdateScripts() {
        List<Method> methods = new ArrayList<>();
        for (Method declaredMethod : this.getClass().getDeclaredMethods()) {
            if (declaredMethod.getAnnotation(ConfigUpdater.class) == null) {
                continue;
            }
            methods.add(declaredMethod);
        }
        methods.sort(Comparator.comparingInt(o -> o.getAnnotation(ConfigUpdater.class).version()));
        return methods;
    }

    private void saveConfig() {
        plugin.saveConfig();
    }


}
