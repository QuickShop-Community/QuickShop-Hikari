package com.ghostchu.quickshop.util;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.simplereloadlib.ReloadResult;
import com.ghostchu.simplereloadlib.Reloadable;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class ItemMarker implements Reloadable {
    private final QuickShop plugin;
    private final Map<String, ItemStack> stacks = new HashMap<>();
    private final File file;
    @Getter
    @SuppressWarnings("RegExpSimplifiable")
    private final String nameRegExp = "[a-zA-Z0-9_]*";
    private final Pattern namePattern = Pattern.compile(nameRegExp);
    private YamlConfiguration configuration;

    public ItemMarker(@NotNull QuickShop plugin) {
        this.plugin = plugin;
        file = new File(plugin.getDataFolder(), "items-lookup.yml");
        init();
        plugin.getReloadManager().register(this);
    }

    public void init() {
        stacks.clear();
        if (!file.exists()) {
            initDefaultConfiguration(file);
        }
        configuration = YamlConfiguration.loadConfiguration(file);
        for (String key : configuration.getKeys(false)) {
            if (configuration.isItemStack(key)) {
                stacks.put(key, configuration.getItemStack(key));
            }
        }
    }

    private void initDefaultConfiguration(@NotNull File file) {
        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        yamlConfiguration.set("version", 1);
        try {
            //noinspection ResultOfMethodCallIgnored
            file.createNewFile();
            yamlConfiguration.save(file);
        } catch (Exception e) {
            Log.permission(Level.SEVERE, "Failed to create default items configuration file");
            plugin.getLogger().log(Level.SEVERE, "Failed to create default items configuration", e);
        }
    }

    @Nullable
    public ItemStack get(@NotNull String itemName) {
        return stacks.get(itemName);
    }

    @Nullable
    public String get(@NotNull ItemStack item) {
        for (Map.Entry<String, ItemStack> entry : stacks.entrySet()) {
            if (plugin.getItemMatcher().matches(entry.getValue(), item)) {
                return entry.getKey();
            }
        }
        return null;
    }

    @NotNull
    public List<String> getRegisteredItems() {
        return new ArrayList<>(stacks.keySet());
    }

    @Override
    public ReloadResult reloadModule() throws Exception {
        init();
        return Reloadable.super.reloadModule();
    }

    @NotNull
    public OperationResult remove(@NotNull String itemName) {
        if (!stacks.containsKey(itemName)) {
            return OperationResult.NOT_EXISTS;
        }
        stacks.remove(itemName);
        configuration.set(itemName, null);
        if (saveConfig()) {
            Log.debug("Removed item " + itemName + " !");
            return OperationResult.SUCCESS;
        } else {
            return OperationResult.UNKNOWN;
        }
    }

    private boolean saveConfig() {
        try {
            configuration.save(file);
            return true;
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save items.yml", e);
            return false;
        }
    }

    @NotNull
    public OperationResult save(@NotNull String itemName, @NotNull ItemStack itemStack) {
        if (stacks.containsKey(itemName)) {
            return OperationResult.NAME_CONFLICT;
        }
        if (!namePattern.matcher(itemName).matches()) {
            return OperationResult.REGEXP_FAILURE;
        }
        stacks.put(itemName, itemStack);
        configuration.set(itemName, itemStack);
        if (saveConfig()) {
            Log.debug("Saved item " + itemName + " !");
            return OperationResult.SUCCESS;
        } else {
            return OperationResult.UNKNOWN;
        }
    }

    public enum OperationResult {
        SUCCESS,
        NAME_CONFLICT,
        REGEXP_FAILURE,
        UNKNOWN,
        NOT_EXISTS
    }
}
