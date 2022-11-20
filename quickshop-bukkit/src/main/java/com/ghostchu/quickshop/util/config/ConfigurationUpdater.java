package com.ghostchu.quickshop.util.config;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.converter.HikariConverter;
import com.ghostchu.quickshop.util.logger.Log;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;

public class ConfigurationUpdater {
    private static final String CONFIG_VERSION_KEY = "config-version";
    private final QuickShop plugin;
    @Getter
    private final ConfigurationSection configuration;
    private int selectedVersion = -1;

    public ConfigurationUpdater(QuickShop plugin) {
        this.plugin = plugin;
        this.configuration = plugin.getConfig();
    }

    private void brokenConfigurationFix() {
        try (InputStreamReader buildInConfigReader = new InputStreamReader(new BufferedInputStream(Objects.requireNonNull(plugin.getResource("config.yml"))), StandardCharsets.UTF_8)) {
            if (new ConfigurationFixer(plugin, new File(plugin.getDataFolder(), "config.yml"), plugin.getConfig(), YamlConfiguration.loadConfiguration(buildInConfigReader)).fix()) {
                plugin.reloadConfig();
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to fix config.yml, plugin may not working properly.", e);
        }
    }

    public void update(@NotNull Object configUpdateScript) {
        Log.debug("Starting configuration update...");
        writeServerUniqueId();
        selectedVersion = configuration.getInt(CONFIG_VERSION_KEY, -1);
        legacyUpdate();
        for (Method method : getUpdateScripts(configUpdateScript)) {
            try {
                UpdateScript updateScript = method.getAnnotation(UpdateScript.class);
                int current = getConfiguration().getInt(CONFIG_VERSION_KEY);
                if (current >= updateScript.version()) {
                    continue;
                }
                plugin.getLogger().info("[ConfigUpdater] Updating configuration from " + current + " to " + updateScript.version());
                String scriptName = updateScript.description();
                if (StringUtils.isEmpty(scriptName)) {
                    scriptName = method.getName();
                }
                plugin.getLogger().info("[ConfigUpdater] Executing update script " + scriptName);
                try {
                    if (method.getParameterCount() == 0) {
                        method.invoke(configUpdateScript);
                    } else {
                        if (method.getParameterCount() == 1 && (method.getParameterTypes()[0] == int.class || method.getParameterTypes()[0] == Integer.class)) {
                            method.invoke(configUpdateScript, current);
                        }
                    }
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Failed to execute update script " + method.getName() + " for version " + updateScript.version() + ": " + e.getMessage() + ", plugin may not working properly!", e);
                }
                getConfiguration().set(CONFIG_VERSION_KEY, updateScript.version());
                plugin.getLogger().info("[ConfigUpdater] Configuration updated to version " + updateScript.version());
            } catch (Throwable throwable) {
                plugin.getLogger().log(Level.WARNING, "Failed execute update script " + method.getName() + " for updating to version " + method.getAnnotation(UpdateScript.class).version() + ", some configuration options may missing or outdated", throwable);
            }
        }
        plugin.getLogger().info("[ConfigUpdater] Saving configuration changes...");
        plugin.saveConfig();
        plugin.reloadConfig();
        //Delete old example configuration files
        try {
            cleanupOldConfigs();
        } catch (IOException e) {
            Log.debug("Failed to cleanup old configuration files: " + e.getMessage());
        }

    }

    private void writeServerUniqueId() {
        String serverUUID = plugin.getConfig().getString("server-uuid");
        if (serverUUID == null || serverUUID.isEmpty() || !CommonUtil.isUUID(serverUUID)) {
            plugin.getConfig().set("server-uuid", UUID.randomUUID().toString());
        }
    }

    private void legacyUpdate() {
        if (selectedVersion < 1000) {
            new HikariConverter(plugin).upgrade();
            plugin.getLogger().info("Save changes & Reloading configurations...");
            plugin.saveConfig();
            plugin.reloadConfig();
            if (plugin.getReloadManager() != null) {
                plugin.getReloadManager().reload();
            }
        }
    }

    @NotNull
    public List<Method> getUpdateScripts(@NotNull Object configUpdateScript) {
        List<Method> methods = new ArrayList<>();
        for (Method declaredMethod : configUpdateScript.getClass().getDeclaredMethods()) {
            if (declaredMethod.getAnnotation(UpdateScript.class) == null) {
                continue;
            }
            methods.add(declaredMethod);
        }
        methods.sort(Comparator.comparingInt(o -> o.getAnnotation(UpdateScript.class).version()));
        return methods;
    }

    private void cleanupOldConfigs() throws IOException {
        Files.deleteIfExists(new File(plugin.getDataFolder(), "example.config.yml").toPath());
        Files.deleteIfExists(new File(plugin.getDataFolder(), "example-configuration.txt").toPath());
        Files.deleteIfExists(new File(plugin.getDataFolder(), "example-configuration.yml").toPath());
        try {
            if (new File(plugin.getDataFolder(), "messages.yml").exists()) {
                Files.move(new File(plugin.getDataFolder(), "messages.yml").toPath(), new File(plugin.getDataFolder(), "messages.yml.outdated").toPath());
            }
        } catch (Exception ignore) {
        }
    }
}
