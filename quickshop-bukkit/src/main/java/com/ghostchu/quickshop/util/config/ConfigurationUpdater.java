package com.ghostchu.quickshop.util.config;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.common.util.CommonUtil;
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
        try (InputStreamReader buildInConfigReader = new InputStreamReader(new BufferedInputStream(Objects.requireNonNull(plugin.getJavaPlugin().getResource("config.yml"))), StandardCharsets.UTF_8)) {
            if (new ConfigurationFixer(plugin, new File(plugin.getDataFolder(), "config.yml"), plugin.getConfig(), YamlConfiguration.loadConfiguration(buildInConfigReader)).fix()) {
                plugin.getJavaPlugin().reloadConfig();
            }
        } catch (Exception e) {
            plugin.logger().warn("Failed to fix config.yml, plugin may not working properly.", e);
        }
    }

    public void update(@NotNull Object configUpdateScript) {
        Log.debug("Starting configuration update...");
        writeServerUniqueId();
        selectedVersion = configuration.getInt(CONFIG_VERSION_KEY, -1);
        for (Method method : getUpdateScripts(configUpdateScript)) {
            try {
                UpdateScript updateScript = method.getAnnotation(UpdateScript.class);
                int current = getConfiguration().getInt(CONFIG_VERSION_KEY);
                if (current >= updateScript.version()) {
                    continue;
                }
                plugin.logger().info("[ConfigUpdater] Updating configuration from " + current + " to " + updateScript.version());
                String scriptName = updateScript.description();
                if (StringUtils.isEmpty(scriptName)) {
                    scriptName = method.getName();
                }
                plugin.logger().info("[ConfigUpdater] Executing update script " + scriptName);
                try {
                    if (method.getParameterCount() == 0) {
                        method.invoke(configUpdateScript);
                    } else {
                        if (method.getParameterCount() == 1 && (method.getParameterTypes()[0] == int.class || method.getParameterTypes()[0] == Integer.class)) {
                            method.invoke(configUpdateScript, current);
                        }
                    }
                } catch (Exception e) {
                    plugin.logger().warn("Failed to execute update script {} for version {}: {}, plugin may not working properly!", method.getName(), updateScript.version(), e.getMessage(), e);
                }
                getConfiguration().set(CONFIG_VERSION_KEY, updateScript.version());
                plugin.logger().info("[ConfigUpdater] Configuration updated to version " + updateScript.version());
            } catch (Throwable throwable) {
                plugin.logger().warn("Failed execute update script {} for updating to version {}, some configuration options may missing or outdated", method.getName(), method.getAnnotation(UpdateScript.class).version(), throwable);
            }
        }
        plugin.logger().info("[ConfigUpdater] Saving configuration changes...");
        plugin.getJavaPlugin().saveConfig();
        plugin.getJavaPlugin().reloadConfig();
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
